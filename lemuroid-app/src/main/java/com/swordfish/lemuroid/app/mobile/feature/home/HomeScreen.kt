package com.swordfish.lemuroid.app.mobile.feature.home

import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameCard
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelGreen
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelInk
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelMuted
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelOutline
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelPanel
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelPaper
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelShadow
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
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
) {
    val context = LocalContext.current

    val state = viewModel.getViewStates().collectAsState(HomeViewModel.UIState()).value
    HomeContent(
        modifier = modifier,
        state = state,
        onGameClicked = onGameClick,
        onGameLongClick = onGameLongClick,
        onSetDirectoryClicked = { viewModel.changeLocalStorageFolder(context) },
    )
}

@Composable
private fun HomeContent(
    modifier: Modifier,
    state: HomeViewModel.UIState,
    onGameClicked: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onSetDirectoryClicked: () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize().padding(10.dp)) {
        if (state.gamesCount == 0) {
            HomeEmptyState(
                modifier = Modifier.fillMaxSize(),
                scanning = state.indexInProgress,
                onSetDirectoryClicked = onSetDirectoryClicked,
            )
        } else {
            val featuredGame =
                state.recentGames.firstOrNull()
                    ?: state.favoritesGames.firstOrNull()
                    ?: state.discoveryGames.firstOrNull()
            val shelfGames =
                (state.recentGames + state.favoritesGames + state.discoveryGames)
                    .distinctBy { it.id }
                    .take(HomeViewModel.CAROUSEL_MAX_ITEMS)

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                featuredGame?.let { game ->
                    HomeFeaturedGame(
                        modifier = Modifier.weight(1.1f),
                        game = game,
                        onClick = { onGameClicked(game) },
                        onLongClick = { onGameLongClick(game) },
                    )
                }
                if (shelfGames.isNotEmpty()) {
                    HomeShelf(
                        modifier = Modifier.weight(0.9f),
                        games = shelfGames,
                        onGameClicked = onGameClicked,
                        onGameLongClick = onGameLongClick,
                    )
                }
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
private fun HomeFeaturedGame(
    modifier: Modifier = Modifier,
    game: Game,
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

    Box(modifier = modifier.padding(end = 4.dp, bottom = 4.dp)) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .offset(4.dp, 4.dp)
                    .background(PixelShadow, PixelShape),
        )
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .onFocusChanged { focused = it.isFocused }
                    .pixelFocusBrackets(true)
                    .clip(PixelShape)
                    .border(if (focused) 3.dp else 2.dp, if (focused) PixelGreen else PixelOutline, PixelShape)
                    .background(PixelPaper, PixelShape)
                    .onLauncherMenu(onLongClick)
                    .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                    .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .aspectRatio(0.72f)
                        .clip(PixelShape)
                        .border(2.dp, PixelInk, PixelShape),
            ) {
                LemuroidGameImage(modifier = Modifier.fillMaxSize(), game = game)
            }

            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().padding(vertical = 3.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_continue_game),
                    color = PixelInk,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = game.title,
                    color = PixelInk,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(PixelInk.copy(alpha = 0.28f)),
                )
                Text(
                    text = subtitle,
                    color = PixelInk.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.weight(1f))
                PixelStartButton()
            }
        }
    }
}

@Composable
private fun PixelStartButton() {
    Box(modifier = Modifier.wrapContentWidth().padding(end = 3.dp, bottom = 3.dp)) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .offset(3.dp, 3.dp)
                    .background(PixelInk, PixelShape),
        )
        Row(
            modifier =
                Modifier
                    .background(PixelGreen, PixelShape)
                    .border(2.dp, PixelInk, PixelShape)
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Text(
                text = "A",
                modifier = Modifier.background(PixelInk, PixelShape).padding(horizontal = 7.dp, vertical = 2.dp),
                color = PixelGreen,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = stringResource(R.string.home_start_game),
                color = PixelInk,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun HomeShelf(
    modifier: Modifier = Modifier,
    games: List<Game>,
    onGameClicked: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val cardWidth = (maxHeight - 62.dp).coerceIn(64.dp, 110.dp)
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Box(modifier = Modifier.size(6.dp).background(PixelGreen))
                Text(
                    text = stringResource(R.string.home_recent_games),
                    color = PixelPaper,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                )
            }
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 7.dp, end = 3.dp),
            ) {
                items(games.size, key = { games[it].id }) { index ->
                    val game = games[index]
                    LemuroidGameCard(
                        modifier = Modifier.width(cardWidth),
                        game = game,
                        onClick = { onGameClicked(game) },
                        onLongClick = { onGameLongClick(game) },
                        imageAspectRatio = 1f,
                        showSubtitle = false,
                    )
                }
            }
        }
    }
}
