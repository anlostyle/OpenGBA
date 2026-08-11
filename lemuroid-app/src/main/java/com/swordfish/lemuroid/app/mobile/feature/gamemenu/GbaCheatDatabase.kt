package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import android.content.res.AssetManager
import com.swordfish.lemuroid.lib.library.db.entity.Game
import java.util.Locale
import java.util.Properties

internal data class GbaCheat(
    val description: String,
    val code: String,
)

internal data class GbaCheatSet(
    val source: String,
    val cheats: List<GbaCheat>,
)

internal object GbaCheatDatabase {
    private const val ASSET_DIRECTORY = "gba-cheats"
    private val extension = Regex("\\.(cht|gba|zip|7z)$", RegexOption.IGNORE_CASE)
    private val codeType =
        Regex("\\b(code\\s*breaker|game\\s*shark|gameshark|diff)\\b", RegexOption.IGNORE_CASE)
    private val nonAlphaNumeric = Regex("[^\\p{L}\\p{N}]+")

    fun load(
        assets: AssetManager,
        game: Game,
    ): GbaCheatSet? {
        if (game.systemId != "gba") return null

        val files = assets.list(ASSET_DIRECTORY).orEmpty().filter { it.endsWith(".cht") }
        val names = listOf(game.title, game.fileName)
        val exactNames = names.map(::normalize).filter(String::isNotEmpty).toSet()
        val exactMatch = files.firstOrNull { normalize(it) in exactNames }
        val match = exactMatch ?: findUniqueBaseMatch(files, names) ?: return null

        val properties = Properties()
        assets.open("$ASSET_DIRECTORY/$match").bufferedReader(Charsets.UTF_8).use {
            properties.load(it)
        }
        val cheatCount = properties.getProperty("cheats")?.trim()?.toIntOrNull() ?: return null
        val cheats =
            (0 until cheatCount)
                .mapNotNull { index ->
                    val description =
                        properties.getProperty("cheat${index}_desc")?.unquote() ?: return@mapNotNull null
                    val code = properties.getProperty("cheat${index}_code")?.unquote() ?: return@mapNotNull null
                    GbaCheat(description, code)
                }
                .distinctBy(GbaCheat::code)

        return cheats
            .takeIf(List<GbaCheat>::isNotEmpty)
            ?.let { GbaCheatSet(match.removeSuffix(".cht"), it) }
    }

    private fun findUniqueBaseMatch(
        files: List<String>,
        names: List<String>,
    ): String? {
        val baseNames = names.map(::baseName).filter(String::isNotEmpty).toSet()
        return files.filter { baseName(it) in baseNames }.singleOrNull()
    }

    private fun normalize(value: String): String =
        value
            .replace(extension, "")
            .replace(codeType, "")
            .lowercase(Locale.ROOT)
            .replace(nonAlphaNumeric, " ")
            .trim()

    private fun baseName(value: String): String = normalize(value.substringBefore('('))

    private fun String.unquote(): String = trim().removeSurrounding("\"")
}
