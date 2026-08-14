package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun LemuroidGameCard(
    modifier: Modifier = Modifier,
    game: Game,
    onClick: () -> Unit = { },
    onLongClick: () -> Unit = { },
    onFocused: () -> Unit = { },
    imageAspectRatio: Float = 0.72f,
    showSubtitle: Boolean = true,
) {
    var focused by remember { mutableStateOf(false) }

    Box(
        modifier =
            modifier
                .background(PixelShadow, PixelShape)
                .padding(end = 3.dp, bottom = 3.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        focused = it.isFocused
                        if (it.isFocused) onFocused()
                    }
                    .pixelFocusBrackets(focused)
                    .clip(PixelShape)
                    .border(if (focused) 2.dp else 1.dp, if (focused) PixelGreen else PixelOutline, PixelShape)
                    .background(PixelPanel, PixelShape)
                    .onLauncherMenu(onLongClick)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick,
                    ),
        ) {
            LemuroidGameImage(game = game, aspectRatio = imageAspectRatio)
            LemuroidGameTexts(game = game, showSubtitle = showSubtitle)
        }
    }
}
