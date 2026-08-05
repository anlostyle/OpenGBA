package com.swordfish.lemuroid.app.shared.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

class AppUpdateManager(private val context: Context) {
    data class UpdateManifest(
        val packageName: String,
        val versionCode: Long,
        val versionName: String,
        val apkUrl: String,
        val sha256: String,
        val notes: String,
    )

    suspend fun checkForUpdate(): Result<UpdateManifest?> = withContext(Dispatchers.IO) {
        runCatching {
            val manifest = fetchManifest()
            if (manifest.packageName != context.packageName) {
                return@runCatching null
            }
            if (manifest.versionCode <= installedVersionCode()) null else manifest
        }
    }

    suspend fun downloadAndInstall(manifest: UpdateManifest): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            require(manifest.packageName == context.packageName) { "Update package does not match this build" }
            require(manifest.apkUrl.startsWith("https://")) { "Update URL must use HTTPS" }
            require(manifest.sha256.matches(SHA256_PATTERN)) { "Update manifest has no valid SHA-256" }
            require(canRequestPackageInstalls()) { "Install permission is required" }

            val updateDir = File(context.cacheDir, UPDATE_DIRECTORY).apply { mkdirs() }
            val apkFile = File(updateDir, "opengba-${manifest.versionCode}.apk")
            download(manifest.apkUrl, apkFile)
            check(sha256(apkFile).equals(manifest.sha256, ignoreCase = true)) {
                "Downloaded APK checksum does not match the manifest"
            }

            val apkUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
            context.applicationContext.startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, APK_MIME_TYPE)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
            )
        }
    }

    fun installPermissionIntent(): Intent =
        Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}"),
        )

    fun canRequestPackageInstalls(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.packageManager.canRequestPackageInstalls()

    private fun fetchManifest(): UpdateManifest {
        val connection = openConnection(MANIFEST_URL)
        return connection.inputStream.use { input ->
            val json = JSONObject(input.bufferedReader().use { it.readText() })
            UpdateManifest(
                packageName = json.optString("packageName", RELEASE_PACKAGE_NAME),
                versionCode = json.getLong("versionCode"),
                versionName = json.getString("versionName"),
                apkUrl = json.getString("apkUrl"),
                sha256 = json.getString("sha256"),
                notes = json.optString("notes"),
            )
        }.also { connection.disconnect() }
    }

    private fun download(
        url: String,
        destination: File,
    ) {
        val connection = openConnection(url)
        connection.inputStream.use { input ->
            destination.outputStream().use { output -> input.copyTo(output) }
        }
        connection.disconnect()
    }

    private fun openConnection(url: String): HttpURLConnection {
        require(url.startsWith("https://")) { "Only HTTPS update endpoints are supported" }
        return (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = NETWORK_TIMEOUT_MS
            readTimeout = NETWORK_TIMEOUT_MS
            instanceFollowRedirects = true
            connect()
            check(responseCode in 200..299) { "Update server returned HTTP $responseCode" }
        }
    }

    private fun installedVersionCode(): Long {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val MANIFEST_URL = "https://github.com/anlostyle/OpenGBA/releases/latest/download/update.json"
        private const val RELEASE_PACKAGE_NAME = "com.opengba.launcher"
        private const val UPDATE_DIRECTORY = "updates"
        private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
        private const val NETWORK_TIMEOUT_MS = 15_000
        private val SHA256_PATTERN = Regex("[0-9a-fA-F]{64}")
    }
}
