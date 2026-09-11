package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GbaCheatDatabaseTest {
    @Test
    fun appendsClipboardCodesWithoutChangingMultipartCode() {
        assertEquals(
            "02023FE0 0064\n830030EC+03E7",
            appendCheatCodes("02023FE0 0064", "830030EC+03E7\n02023FE0 0064"),
        )
    }

    @Test
    fun hidesTechnicalPrefixWithoutChangingStoredCheat() {
        val cheat = GbaCheat("direct_v1 获得999经验", "12345678 0001")

        assertEquals("获得999经验", cheat.displayDescription())
        assertEquals("direct_v1 获得999经验", cheat.description)
    }

    @Test
    fun matchesChineseDisplayNameWithoutGuessingAnotherEdition() {
        val fireRed = GbaCheatDatabase.nameAssetFile("口袋妖怪 火红")!!
        val leafGreen = GbaCheatDatabase.nameAssetFile("口袋妖怪 叶绿")!!
        val finalFight = GbaCheatDatabase.nameAssetFile("快打旋风ONE")!!
        val files = setOf(fireRed, leafGreen, finalFight)

        assertEquals(
            "ee7753c0c7f9719d66d7e6e656a51705c035700a36454f10c791d770413a661a.cht",
            fireRed,
        )
        assertEquals(
            fireRed to "口袋妖怪 火红",
            GbaCheatDatabase.findNameMatch(files, listOf("口袋妖怪 火红", "Pokemon FireRed")),
        )
        assertEquals(
            finalFight to "快打旋风One.zip",
            GbaCheatDatabase.findNameMatch(files, listOf("快打旋风One.zip")),
        )
        assertNull(GbaCheatDatabase.findNameMatch(files, listOf("口袋妖怪 火红 改版")))
    }
}
