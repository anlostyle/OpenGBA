package com.swordfish.lemuroid.app.mobile.feature.main

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidSmallGameImage
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelGreen
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelInk
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelKeyHint
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelOutline
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelPanel
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelPanelLight
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelPaper
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelRed
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelShape
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.pixelFocusBrackets
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun MainGameContextActions(
    selectedGameState: MutableState<Game?>,
    shortcutSupported: Boolean,
    onGamePlay: (Game) -> Unit,
    onGameRestart: (Game) -> Unit,
    onFavoriteToggle: (Game, Boolean) -> Unit,
    onCreateShortcut: (Game) -> Unit,
) {
    val selectedGame = selectedGameState.value ?: return
    val firstAction = remember { FocusRequester() }

    LaunchedEffect(selectedGame.id) {
        firstAction.requestFocus()
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.76f))
                .onPreviewKeyEvent { event ->
                    val keyCode = event.nativeKeyEvent.keyCode
                    if (
                        event.type == KeyEventType.KeyUp &&
                        (keyCode == KeyEvent.KEYCODE_BUTTON_B || keyCode == KeyEvent.KEYCODE_BACK)
                    ) {
                        selectedGameState.value = null
                        true
                    } else {
                        false
                    }
                }
                .clickable { selectedGameState.value = null },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth(0.58f)
                    .widthIn(max = 620.dp)
                    .background(PixelPanel, PixelShape)
                    .border(2.dp, PixelGreen, PixelShape)
                    .clickable { },
        ) {
            ContextActionHeader(selectedGame)
            ContextActionEntry(
                modifier = Modifier.focusRequester(firstAction),
                label = stringResource(R.string.game_context_menu_resume),
                icon = Icons.Default.PlayArrow,
                onClick = {
                    onGamePlay(selectedGame)
                    selectedGameState.value = null
                },
            )
            ContextActionEntry(
                label = stringResource(R.string.game_context_menu_restart),
                icon = Icons.Default.RestartAlt,
                onClick = {
                    onGameRestart(selectedGame)
                    selectedGameState.value = null
                },
            )
            ContextActionEntry(
                label =
                    stringResource(
                        if (selectedGame.isFavorite) {
                            R.string.game_context_menu_remove_from_favorites
                        } else {
                            R.string.game_context_menu_add_to_favorites
                        },
                    ),
                icon = if (selectedGame.isFavorite) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                onClick = {
                    onFavoriteToggle(selectedGame, !selectedGame.isFavorite)
                    selectedGameState.value = null
                },
            )
            if (shortcutSupported) {
                ContextActionEntry(
                    label = stringResource(R.string.game_context_menu_create_shortcut),
                    icon = Icons.Default.AppShortcut,
                    onClick = {
                        onCreateShortcut(selectedGame)
                        selectedGameState.value = null
                    },
                )
            }
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .border(1.dp, PixelOutline)
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                PixelKeyHint("A", stringResource(R.string.launcher_confirm), PixelGreen)
                PixelKeyHint("B", stringResource(R.string.cancel), PixelRed)
            }
        }
    }
}

@Composable
private fun ContextActionHeader(game: Game) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(PixelPaper, PixelShape)
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LemuroidSmallGameImage(
            modifier = Modifier.size(54.dp).border(2.dp, PixelInk, PixelShape),
            game = game,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = game.title,
                color = PixelInk,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = game.systemId.uppercase(),
                color = PixelInk.copy(alpha = 0.68f),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun ContextActionEntry(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(52.dp)
                .onFocusChanged { focused = it.isFocused }
                .pixelFocusBrackets(focused)
                .background(if (focused) PixelPanelLight else PixelPanel)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (focused) PixelGreen else PixelPaper,
        )
        Text(
            text = label,
            color = if (focused) PixelGreen else PixelPaper,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}
