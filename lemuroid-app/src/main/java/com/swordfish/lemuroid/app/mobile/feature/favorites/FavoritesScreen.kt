package com.swordfish.lemuroid.app.mobile.feature.favorites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.paging.compose.collectAsLazyPagingItems
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LauncherGameGrid
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidEmptyView
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel,
    leftNavigationRequester: FocusRequester,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onPageChanged: (Int, Int) -> Unit,
) {
    val games = viewModel.favorites.collectAsLazyPagingItems()

    if (games.itemCount == 0) {
        LaunchedEffect(Unit) { onPageChanged(0, 0) }
        LemuroidEmptyView(modifier)
        return
    }

    LauncherGameGrid(
        games = games,
        modifier = modifier,
        leftNavigationRequester = leftNavigationRequester,
        onGameClick = onGameClick,
        onGameLongClick = onGameLongClick,
        onPageChanged = onPageChanged,
    )
}
