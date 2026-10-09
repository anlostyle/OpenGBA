package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import android.view.KeyEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.swordfish.lemuroid.app.mobile.feature.apps.LauncherApp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherAppGrid(
    apps: List<LauncherApp>,
    modifier: Modifier = Modifier,
    resetKey: Any? = Unit,
    leftNavigationRequester: FocusRequester? = null,
    onAppClick: (LauncherApp) -> Unit,
    onPageChanged: (Int, Int) -> Unit,
) {
    var page by rememberSaveable(resetKey) { mutableStateOf(0) }
    var focusedSlot by rememberSaveable(resetKey) { mutableStateOf(0) }
    var refocusAfterPaging by remember { mutableStateOf(false) }
    val focusRequesters = remember { List(LAUNCHER_PAGE_SIZE) { FocusRequester() } }
    val pageCount = launcherPageCount(apps.size)
    val safePage = page.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
    val pageStart = launcherPageStart(safePage, apps.size)
    val pageItemCount = (apps.size - pageStart).coerceIn(0, LAUNCHER_PAGE_SIZE)
    val slots = remember(pageStart, pageItemCount) { List(pageItemCount) { it } }

    val gridState = rememberLazyGridState()
    val swipeThreshold = with(LocalDensity.current) { 64.dp.toPx() }

    fun changePage(
        direction: Int,
        slot: Int,
    ): Boolean {
        val nextPage = (safePage + direction).coerceIn(0, (pageCount - 1).coerceAtLeast(0))
        if (nextPage == safePage) return false
        page = nextPage
        focusedSlot = slot
        refocusAfterPaging = true
        return true
    }

    LaunchedEffect(pageCount, safePage) {
        if (page != safePage) page = safePage
        onPageChanged(if (pageCount == 0) 0 else safePage + 1, pageCount)
    }

    LazyVerticalGrid(
        state = gridState,
        modifier =
            modifier
                .fillMaxSize()
                .focusGroup()
                .launcherPagingInput(
                    gridState = gridState,
                    pageKey = safePage,
                    pageCount = pageCount,
                    pageItemCount = pageItemCount,
                    focusedSlot = focusedSlot,
                    swipeThreshold = swipeThreshold,
                    changePage = ::changePage,
                ),
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(start = 23.dp, top = 13.dp, end = 23.dp, bottom = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(23.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = true,
    ) {
        items(slots, key = { pageStart + it }) { slot ->
            val app = apps[pageStart + slot]
            val targetSlot =
                launcherPageItemIndex(safePage, focusedSlot, apps.size)
                    ?.minus(pageStart)
                    ?: 0
            LaunchedEffect(refocusAfterPaging, app.packageName, safePage) {
                if (refocusAfterPaging && slot == targetSlot) {
                    focusRequesters[slot].requestFocus()
                    refocusAfterPaging = false
                }
            }
            LauncherAppCard(
                app = app,
                iconPainter = rememberDrawablePainter(drawable = app.icon),
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
                onClick = { onAppClick(app) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LauncherAppCard(
    app: LauncherApp,
    iconPainter: Painter,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onFocused: () -> Unit,
    onClick: () -> Unit,
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
                .combinedClickable(onClick = onClick)
                .semantics { contentDescription = app.label },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(PixelCoverShape)
                    .background(PixelInk),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().padding(12.dp),
                contentScale = ContentScale.Fit,
            )
        }
        Text(
            text = app.label,
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
