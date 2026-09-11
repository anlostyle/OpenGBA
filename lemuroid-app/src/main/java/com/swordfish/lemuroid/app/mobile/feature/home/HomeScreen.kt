package com.swordfish.lemuroid.app.mobile.feature.home

import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelCoverShape
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelGreen
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelInk
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelMuted
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelOutline
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelPanel
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelPaper
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelShape
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.onLauncherMenu
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.pixelFocusBrackets
import com.swordfish.lemuroid.app.utils.games.GameUtils
import com.swordfish.lemuroid.lib.library.db.entity.Game
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    leftNavigationRequester: FocusRequester,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
) {
    val context = LocalContext.current
    val state = viewModel.getViewStates().collectAsState(HomeViewModel.UIState()).value

    HomeContent(
        modifier = modifier,
        state = state,
        leftNavigationRequester = leftNavigationRequester,
        onGameClicked = onGameClick,
        onGameLongClick = onGameLongClick,
        onSetDirectoryClicked = { viewModel.changeLocalStorageFolder(context) },
    )
}

@Composable
private fun HomeContent(
    modifier: Modifier,
    state: HomeViewModel.UIState,
    leftNavigationRequester: FocusRequester,
    onGameClicked: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onSetDirectoryClicked: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(PixelInk)
                .padding(horizontal = 32.dp, vertical = 14.dp),
    ) {
        if (state.gamesCount == 0) {
            HomeEmptyState(
                modifier = Modifier.fillMaxSize(),
                scanning = state.indexInProgress,
                onSetDirectoryClicked = onSetDirectoryClicked,
            )
            return@Box
        }

        val featuredGame =
            state.recentGames.firstOrNull()
                ?: state.favoritesGames.firstOrNull()
                ?: state.discoveryGames.firstOrNull()
        val recentGames =
            (state.recentGames + state.favoritesGames + state.discoveryGames)
                .distinctBy { it.id }
                .filterNot { it.id == featuredGame?.id }
                .take(4)

        featuredGame?.let { game ->
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                FeaturedGame(
                    modifier =
                        Modifier
                            .width(214.dp)
                            .focusProperties { left = leftNavigationRequester },
                    game = game,
                    onClick = { onGameClicked(game) },
                    onLongClick = { onGameLongClick(game) },
                )
                RecentGamesGrid(
                    modifier = Modifier.weight(1f),
                    games = recentGames,
                    onGameClicked = onGameClicked,
                    onGameLongClick = onGameLongClick,
                )
            }
        }
    }
}

@Composable
private fun HomeEmptyState(
    modifier: Modifier = Modifier,
    scanning: Boolean,
    onSetDirectoryClicked: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = PixelShape,
        color = PixelPanel,
        border = BorderStroke(2.dp, PixelOutline),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "GBA // LIBRARY",
                color = PixelGreen,
                style = MaterialTheme.typography.labelLarge,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = stringResource(R.string.home_empty_title),
                modifier = Modifier.padding(top = 8.dp),
                color = PixelPaper,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = stringResource(R.string.home_empty_message),
                modifier = Modifier.padding(top = 8.dp),
                color = PixelMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (scanning) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(0.5f).padding(top = 18.dp),
                    color = PixelGreen,
                    trackColor = PixelInk,
                )
            } else {
                Surface(
                    modifier = Modifier.padding(top = 18.dp).clickable(onClick = onSetDirectoryClicked),
                    shape = PixelShape,
                    color = PixelGreen,
                    border = BorderStroke(2.dp, PixelInk),
                ) {
                    Text(
                        text = "A  ${stringResource(R.string.home_empty_action)}",
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        color = PixelInk,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeaturedGame(
    game: Game,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val context = LocalContext.current
    val subtitle =
        remember(game.id, game.lastPlayedAt) {
            game.lastPlayedAt?.let { playedAt ->
                val formatted = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(playedAt))
                val time =
                    if (DateUtils.isToday(playedAt)) {
                        context.getString(R.string.home_today_time, formatted)
                    } else {
                        DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(playedAt))
                    }
                context.getString(R.string.home_last_played, time)
            } ?: GameUtils.getGameSubtitle(context, game)
        }
    var focused by remember { mutableStateOf(false) }

    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .focusable()
                .onFocusChanged { focused = it.isFocused }
                .pixelFocusBrackets(focused)
                .onLauncherMenu(onLongClick)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        horizontalAlignment = Alignment.CenterHorizontally,
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
            fontSize = 16.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = subtitle,
            modifier = Modifier.fillMaxWidth(),
            color = PixelMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(12.dp))
        StartGameButton()
    }
}

@Composable
private fun StartGameButton() {
    Row(
        modifier =
            Modifier
                .width(143.dp)
                .height(28.dp)
                .background(PixelGreen, PixelShape)
                .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(22.dp).background(PixelInk, PixelShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "A",
                color = PixelGreen,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
            )
        }
        Text(
            text = stringResource(R.string.home_start_game),
            modifier = Modifier.padding(start = 8.dp),
            color = PixelInk,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun RecentGamesGrid(
    games: List<Game>,
    modifier: Modifier = Modifier,
    onGameClicked: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(2) { row ->
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                repeat(2) { column ->
                    val game = games.getOrNull(row * 2 + column)
                    if (game == null) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        RecentGameCard(
                            modifier = Modifier.weight(1f),
                            game = game,
                            onClick = { onGameClicked(game) },
                            onLongClick = { onGameLongClick(game) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecentGameCard(
    game: Game,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }

    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .focusable()
                .onFocusChanged { focused = it.isFocused }
                .pixelFocusBrackets(focused)
                .onLauncherMenu(onLongClick)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LemuroidGameImage(
            modifier = Modifier.fillMaxWidth().clip(PixelCoverShape),
            game = game,
            aspectRatio = 1f,
        )
        Text(
            text = game.displayName,
            modifier = Modifier.fillMaxWidth().padding(top = 3.dp),
            color = PixelPaper,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
