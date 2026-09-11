package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import android.view.KeyEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import com.swordfish.lemuroid.lib.library.db.entity.Game

internal const val LAUNCHER_PAGE_SIZE = 8

internal fun launcherPageCount(itemCount: Int): Int =
    if (itemCount == 0) 0 else (itemCount - 1) / LAUNCHER_PAGE_SIZE + 1

internal fun launcherPageStart(
    page: Int,
    itemCount: Int,
): Int = page.coerceIn(0, (launcherPageCount(itemCount) - 1).coerceAtLeast(0)) * LAUNCHER_PAGE_SIZE

internal fun launcherPageItemIndex(
    page: Int,
    slot: Int,
    itemCount: Int,
): Int? {
    if (itemCount == 0) return null
    return (launcherPageStart(page, itemCount) + slot.coerceIn(0, LAUNCHER_PAGE_SIZE - 1))
        .coerceAtMost(itemCount - 1)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherGameGrid(
    games: LazyPagingItems<Game>,
    modifier: Modifier = Modifier,
    resetKey: Any? = Unit,
    leftNavigationRequester: FocusRequester? = null,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onPageChanged: (Int, Int) -> Unit,
) {
    var page by rememberSaveable(resetKey) { mutableStateOf(0) }
    var focusedSlot by rememberSaveable(resetKey) { mutableStateOf(0) }
    var refocusAfterPaging by remember { mutableStateOf(false) }
    val focusRequesters = remember { List(LAUNCHER_PAGE_SIZE) { FocusRequester() } }
    val pageCount = launcherPageCount(games.itemCount)
    val safePage = page.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
    val pageStart = launcherPageStart(safePage, games.itemCount)
    val pageItemCount = (games.itemCount - pageStart).coerceIn(0, LAUNCHER_PAGE_SIZE)
    val slots = remember(pageStart, pageItemCount) { List(pageItemCount) { it } }

    LaunchedEffect(pageCount, safePage) {
        if (page != safePage) page = safePage
        onPageChanged(if (pageCount == 0) 0 else safePage + 1, pageCount)
    }

    LazyVerticalGrid(
        modifier =
            modifier
                .fillMaxSize()
                .focusGroup()
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyUp) return@onPreviewKeyEvent false
                    val direction =
                        when (event.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_BUTTON_L1 -> -1
                            KeyEvent.KEYCODE_BUTTON_R1 -> 1
                            else -> return@onPreviewKeyEvent false
                        }
                    val nextPage = (safePage + direction).coerceIn(0, (pageCount - 1).coerceAtLeast(0))
                    if (nextPage != safePage) {
                        page = nextPage
                        refocusAfterPaging = true
                    }
                    true
                },
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(start = 23.dp, top = 13.dp, end = 23.dp, bottom = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(23.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        // Keep the grid usable on touch-only devices. L/R still changes pages;
        // touch scrolling is a harmless fallback when a page does not fit.
        userScrollEnabled = true,
    ) {
        items(slots, key = { pageStart + it }) { slot ->
            val game = games[pageStart + slot]
            if (game == null) {
                Box(modifier = Modifier.fillMaxWidth().height(152.dp))
            } else {
                val targetSlot =
                    launcherPageItemIndex(safePage, focusedSlot, games.itemCount)
                        ?.minus(pageStart)
                        ?: 0
                LaunchedEffect(refocusAfterPaging, game.id, safePage) {
                    if (refocusAfterPaging && slot == targetSlot) {
                        focusRequesters[slot].requestFocus()
                        refocusAfterPaging = false
                    }
                }
                LauncherGameCard(
                    game = game,
                    focusRequester = focusRequesters[slot],
                    modifier =
                        Modifier.then(
                            if (slot % 4 == 0 && leftNavigationRequester != null) {
                                Modifier.focusProperties { left = leftNavigationRequester }
                            } else {
                                Modifier
                            },
                        ),
                    onFocused = { focusedSlot = slot },
                    onClick = { onGameClick(game) },
                    onLongClick = { onGameLongClick(game) },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LauncherGameCard(
    game: Game,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onFocused: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }

    Column(
        modifier =
            modifier
                .height(152.dp)
                .focusRequester(focusRequester)
                .onFocusChanged {
                    focused = it.isFocused
                    if (it.isFocused) onFocused()
                }
                .pixelFocusBrackets(focused)
                .onLauncherMenu(onLongClick)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .semantics { contentDescription = game.displayName },
    ) {
        LemuroidGameImage(
            modifier = Modifier.fillMaxWidth().clip(PixelCoverShape),
            game = game,
            aspectRatio = 1f,
        )
        Text(
            text = game.displayName,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            color = PixelPaper,
            style = MaterialTheme.typography.titleSmall,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
