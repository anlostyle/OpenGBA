package com.swordfish.lemuroid.lib.storage.local

internal object PegasusMetadataParser {
    fun parse(lines: Sequence<String>): Map<String, String> {
        val titles = linkedMapOf<String, String>()
        var title: String? = null
        var files = mutableListOf<String>()
        var readingFiles = false

        fun flush() {
            val currentTitle = title?.trim().orEmpty()
            if (currentTitle.isNotEmpty() && files.size == 1) {
                titles.putIfAbsent(normalizeFileName(files.single()), currentTitle)
            }
            title = null
            files = mutableListOf()
            readingFiles = false
        }

        lines.forEach { rawLine ->
            val line = rawLine.trimEnd()
            val trimmed = line.trim()
            when {
                line.startsWith("game:") -> {
                    flush()
                    title = line.substringAfter(':').trim()
                }
                title != null && line.startsWith("file:") -> {
                    files += line.substringAfter(':').trim()
                    readingFiles = false
                }
                title != null && line.startsWith("files:") -> readingFiles = true
                readingFiles && line.firstOrNull()?.isWhitespace() == true && isRomFile(trimmed) -> {
                    files += trimmed
                }
                line.isBlank() || line.firstOrNull()?.isWhitespace() != true -> readingFiles = false
            }
        }
        flush()
        return titles
    }

    fun normalizeFileName(value: String): String =
        value
            .trim()
            .removeSurrounding("\"")
            .removePrefix("./")
            .substringAfterLast('/')
            .substringAfterLast('\\')
            .lowercase()

    fun preferredTitle(
        pegasusTitle: String?,
        fileName: String,
        metadataTitle: String?,
    ): String {
        val localFileTitle = fileName.substringBeforeLast('.', fileName)
        return pegasusTitle?.takeIf(::containsChinese)
            ?: localFileTitle.takeIf(::containsChinese)
            ?: metadataTitle?.takeIf(String::isNotBlank)
            ?: localFileTitle
    }

    private fun containsChinese(value: String): Boolean =
        value.any { it in '\u3400'..'\u9FFF' || it in '\uF900'..'\uFAFF' }

    private fun isRomFile(value: String): Boolean =
        value.endsWith(".gba", ignoreCase = true) ||
            value.endsWith(".zip", ignoreCase = true) ||
            value.endsWith(".7z", ignoreCase = true)
}
