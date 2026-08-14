package com.swordfish.lemuroid.lib.storage.local

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PegasusMetadataParserTest {
    @Test
    fun keepsSingleRomTitlesAndLeavesMultiRomEntriesIndependent() {
        val titles =
            PegasusMetadataParser.parse(
                """
                game: 塞尔达传说 缩小帽
                file: 塞尔达传说 缩小帽.zip

                game: 恶魔城2 白夜协奏曲改版
                files:
                  白夜终极版1.zip
                  白夜终极版2.zip
                launch: ignored
                  -e ROM {file.path}
                """.trimIndent().lineSequence(),
            )

        assertEquals("塞尔达传说 缩小帽", titles["塞尔达传说 缩小帽.zip"])
        assertFalse("白夜终极版1.zip" in titles)
        assertFalse("白夜终极版2.zip" in titles)
    }

    @Test
    fun prefersChineseLocalTitlesThenMetadata() {
        assertEquals(
            "塞尔达传说 缩小帽",
            PegasusMetadataParser.preferredTitle("塞尔达传说 缩小帽", "Minish Cap.zip", "The Minish Cap"),
        )
        assertEquals(
            "白夜终极版1",
            PegasusMetadataParser.preferredTitle(null, "白夜终极版1.zip", "Castlevania"),
        )
        assertEquals(
            "The Minish Cap",
            PegasusMetadataParser.preferredTitle("Minish Cap", "Minish Cap.zip", "The Minish Cap"),
        )
    }
}
