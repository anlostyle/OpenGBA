package com.swordfish.lemuroid.lib.storage.local

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import androidx.leanback.preference.LeanbackPreferenceFragment
import com.swordfish.lemuroid.common.kotlin.extractEntryToFile
import com.swordfish.lemuroid.common.kotlin.isZipped
import com.swordfish.lemuroid.common.kotlin.writeToFile
import com.swordfish.lemuroid.lib.R
import com.swordfish.lemuroid.lib.library.db.entity.DataFile
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.storage.BaseStorageFile
import com.swordfish.lemuroid.lib.storage.RomFiles
import com.swordfish.lemuroid.lib.storage.StorageFile
import com.swordfish.lemuroid.lib.storage.StorageProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset
import java.util.Locale
import java.util.zip.ZipInputStream

class StorageAccessFrameworkProvider(private val context: Context) : StorageProvider {
    private val metadataCache = mutableMapOf<String, Map<String, String>>()

    override val id: String = "access_framework"

    override val name: String = context.getString(R.string.local_storage)

    override val uriSchemes = listOf("content")

    override val prefsFragmentClass: Class<LeanbackPreferenceFragment>? = null

    override val enabledByDefault = true

    override fun listBaseStorageFiles(): Flow<List<BaseStorageFile>> {
        synchronized(metadataCache) { metadataCache.clear() }
        return getExternalFolder()?.let { folder ->
            traverseDirectoryEntries(Uri.parse(folder))
        } ?: emptyFlow()
    }

    override fun getStorageFile(baseStorageFile: BaseStorageFile): StorageFile? {
        return DocumentFileParser.parseDocumentFile(context, baseStorageFile)
    }

    override fun findArtworkUri(baseStorageFile: BaseStorageFile): Uri? {
        val baseName = baseStorageFile.name.substringBeforeLast('.', baseStorageFile.name)
        val parentDocumentId = runCatching {
            DocumentsContract.getDocumentId(baseStorageFile.uri).substringBeforeLast('/')
        }.getOrNull() ?: return null
        if (parentDocumentId.isEmpty()) return null

        val artworkNames = COVER_NAMES.flatMap { name -> COVER_EXTENSIONS.map { "$name.$it" } }
            .map { it.lowercase(Locale.ROOT) }
            .toSet()
        val parentChildrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(baseStorageFile.uri, parentDocumentId)
        findArtworkInDirectory(baseStorageFile.uri, parentChildrenUri, artworkNames)?.let { return it }

        val mediaId = findDirectory(parentChildrenUri, listOf("media")) ?: return null
        val mediaChildrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(baseStorageFile.uri, mediaId)
        val gameMediaId = findDirectory(mediaChildrenUri, artworkDirectoryNames(baseName)) ?: return null
        val gameMediaChildrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(baseStorageFile.uri, gameMediaId)
        return findArtworkInDirectory(baseStorageFile.uri, gameMediaChildrenUri, artworkNames)
    }

    override fun findGameTitle(baseStorageFile: BaseStorageFile): String? {
        val parentDocumentId =
            runCatching {
                DocumentsContract.getDocumentId(baseStorageFile.uri).substringBeforeLast('/')
            }.getOrNull() ?: return null
        val titles =
            synchronized(metadataCache) {
                metadataCache.getOrPut(parentDocumentId) {
                    loadPegasusTitles(baseStorageFile.uri, parentDocumentId)
                }
            }
        return titles[PegasusMetadataParser.normalizeFileName(baseStorageFile.name)]
    }

    private fun loadPegasusTitles(
        treeUri: Uri,
        parentDocumentId: String,
    ): Map<String, String> {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocumentId)
        val projection =
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
            )
        val metadataUri =
            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    if (
                        cursor.getString(2) != DocumentsContract.Document.MIME_TYPE_DIR &&
                        cursor.getString(1).equals(PEGASUS_METADATA_FILE, ignoreCase = true)
                    ) {
                        return@use DocumentsContract.buildDocumentUriUsingTree(treeUri, cursor.getString(0))
                    }
                }
                null
            } ?: return emptyMap()

        return runCatching {
            context.contentResolver.openInputStream(metadataUri)?.bufferedReader(Charsets.UTF_8)?.use { reader ->
                PegasusMetadataParser.parse(reader.lineSequence())
            }.orEmpty()
        }.onFailure { Timber.w(it, "Unable to read Pegasus metadata") }
            .getOrDefault(emptyMap())
    }

    private fun findArtworkInDirectory(
        treeUri: Uri,
        childrenUri: Uri,
        candidates: Set<String>,
    ): Uri? {
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
        )

        return context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val documentId = cursor.getString(0)
                val name = cursor.getString(1)
                val mimeType = cursor.getString(2)
                if (mimeType != DocumentsContract.Document.MIME_TYPE_DIR && name.lowercase(Locale.ROOT) in candidates) {
                    return@use DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
                }
            }
            null
        }
    }

    private fun findDirectory(childrenUri: Uri, names: List<String>): String? {
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
        )
        return context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                if (cursor.getString(2) == DocumentsContract.Document.MIME_TYPE_DIR &&
                    names.any { cursor.getString(1).equals(it, ignoreCase = true) }
                ) {
                    return@use cursor.getString(0)
                }
            }
            null
        }
    }

    private fun getExternalFolder(): String? {
        val prefString = context.getString(R.string.pref_key_extenral_folder)
        val preferenceManager = SharedPreferencesHelper.getLegacySharedPreferences(context)
        return preferenceManager.getString(prefString, null)
    }

    private fun traverseDirectoryEntries(rootUri: Uri): Flow<List<BaseStorageFile>> =
        flow {
            val directoryDocumentIds = mutableListOf<String>()
            DocumentsContract.getTreeDocumentId(rootUri)?.let { directoryDocumentIds.add(it) }

            while (directoryDocumentIds.isNotEmpty()) {
                val currentDirectoryDocumentId = directoryDocumentIds.removeAt(0)

                val result =
                    runCatching {
                        listBaseStorageFiles(rootUri, currentDirectoryDocumentId)
                    }
                if (result.isFailure) {
                    Timber.e(result.exceptionOrNull(), "Error while listing files")
                }

                val (files, directories) =
                    result.getOrDefault(
                        listOf<BaseStorageFile>() to listOf<String>(),
                    )

                emit(files)
                directoryDocumentIds.addAll(directories)
            }
        }

    private fun listBaseStorageFiles(
        treeUri: Uri,
        rootDocumentId: String,
    ): Pair<List<BaseStorageFile>, List<String>> {
        val resultFiles = mutableListOf<BaseStorageFile>()
        val resultDirectories = mutableListOf<String>()

        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, rootDocumentId)

        Timber.d("Querying files in directory: $childrenUri")

        val projection =
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
            )
        context.contentResolver.query(childrenUri, projection, null, null, null)?.use {
            while (it.moveToNext()) {
                val documentId = it.getString(0)
                val documentName = it.getString(1)
                val documentSize = it.getLong(2)
                val mimeType = it.getString(3)

                if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                    resultDirectories.add(documentId)
                } else {
                    val documentUri =
                        DocumentsContract.buildDocumentUriUsingTree(
                            treeUri,
                            documentId,
                        )
                    resultFiles.add(
                        BaseStorageFile(
                            name = documentName,
                            size = documentSize,
                            uri = documentUri,
                            path = documentUri.path,
                        ),
                    )
                }
            }
        }

        return resultFiles to resultDirectories
    }

    override fun getGameRomFiles(
        game: Game,
        dataFiles: List<DataFile>,
        allowVirtualFiles: Boolean,
    ): RomFiles {
        val originalDocumentUri = Uri.parse(game.fileUri)
        val originalDocument = DocumentFile.fromSingleUri(context, originalDocumentUri)!!

        val isZipped = originalDocument.isZipped() && originalDocument.name != game.fileName

        return when {
            isZipped && dataFiles.isEmpty() -> getGameRomFilesZipped(game, originalDocument)
            allowVirtualFiles -> getGameRomFilesVirtual(game, dataFiles)
            else -> getGameRomFilesStandard(game, dataFiles, originalDocument)
        }
    }

    private fun getGameRomFilesStandard(
        game: Game,
        dataFiles: List<DataFile>,
        originalDocument: DocumentFile,
    ): RomFiles {
        val gameEntry = getGameRomStandard(game, originalDocument)
        val dataEntries = dataFiles.map { getDataFileStandard(game, it) }
        return RomFiles.Standard(listOf(gameEntry) + dataEntries)
    }

    private fun getGameRomFilesZipped(
        game: Game,
        originalDocument: DocumentFile,
    ): RomFiles {
        val cacheFile = GameCacheUtils.getCacheFileForGame(SAF_CACHE_SUBFOLDER, context, game)
        if (cacheFile.exists()) {
            return RomFiles.Standard(listOf(cacheFile))
        }

        runCatching {
            ZipInputStream(
                context.contentResolver.openInputStream(originalDocument.uri),
            ).extractEntryToFile(game.fileName, cacheFile)
        }.getOrElse { error ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || error !is IllegalArgumentException) {
                throw error
            }
            cacheFile.delete()
            ZipInputStream(
                context.contentResolver.openInputStream(originalDocument.uri),
                Charset.forName("GBK"),
            ).extractEntryToFile(game.fileName, cacheFile)
        }
        return RomFiles.Standard(listOf(cacheFile))
    }

    private fun getGameRomFilesVirtual(
        game: Game,
        dataFiles: List<DataFile>,
    ): RomFiles {
        val gameEntry = getGameRomVirtual(game)
        val dataEntries = dataFiles.map { getDataFileVirtual(it) }
        return RomFiles.Virtual(listOf(gameEntry) + dataEntries)
    }

    private fun getDataFileVirtual(dataFile: DataFile): RomFiles.Virtual.Entry {
        return RomFiles.Virtual.Entry(
            "$VIRTUAL_FILE_PATH/${dataFile.fileName}",
            context.contentResolver.openFileDescriptor(Uri.parse(dataFile.fileUri), "r")!!,
        )
    }

    private fun getDataFileStandard(
        game: Game,
        dataFile: DataFile,
    ): File {
        val cacheFile =
            GameCacheUtils.getDataFileForGame(
                SAF_CACHE_SUBFOLDER,
                context,
                game,
                dataFile,
            )

        if (cacheFile.exists()) {
            return cacheFile
        }

        val stream = context.contentResolver.openInputStream(Uri.parse(dataFile.fileUri))!!
        stream.writeToFile(cacheFile)
        return cacheFile
    }

    private fun getGameRomVirtual(game: Game): RomFiles.Virtual.Entry {
        return RomFiles.Virtual.Entry(
            "$VIRTUAL_FILE_PATH/${game.fileName}",
            context.contentResolver.openFileDescriptor(Uri.parse(game.fileUri), "r")!!,
        )
    }

    private fun getGameRomStandard(
        game: Game,
        originalDocument: DocumentFile,
    ): File {
        val cacheFile = GameCacheUtils.getCacheFileForGame(SAF_CACHE_SUBFOLDER, context, game)

        if (cacheFile.exists()) {
            return cacheFile
        }

        val stream = context.contentResolver.openInputStream(originalDocument.uri)!!
        stream.writeToFile(cacheFile)
        return cacheFile
    }

    override fun getInputStream(uri: Uri): InputStream? {
        return context.contentResolver.openInputStream(uri)
    }

    companion object {
        const val SAF_CACHE_SUBFOLDER = "storage-framework-games"
        const val VIRTUAL_FILE_PATH = "/virtual/file/path"
        const val PEGASUS_METADATA_FILE = "metadata.pegasus.txt"
        private val COVER_NAMES = listOf("boxfront", "coverfront", "cover")
        private val COVER_EXTENSIONS = listOf("png", "jpg", "jpeg", "webp")
    }
}
