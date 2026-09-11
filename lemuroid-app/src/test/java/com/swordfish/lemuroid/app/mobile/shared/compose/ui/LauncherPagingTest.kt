package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LauncherPagingTest {
    @Test
    fun pagesAndFocusStayInBounds() {
        assertEquals(0, launcherPageCount(0))
        assertEquals(1, launcherPageCount(8))
        assertEquals(2, launcherPageCount(9))
        assertEquals(60, launcherPageCount(474))
        assertEquals(8, launcherPageStart(1, 9))
        assertEquals(8, launcherPageStart(99, 9))
        assertEquals(8, launcherPageItemIndex(1, 7, 9))
        assertNull(launcherPageItemIndex(0, 0, 0))
    }
}
