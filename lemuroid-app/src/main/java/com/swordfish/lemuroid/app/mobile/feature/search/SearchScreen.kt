package com.swordfish.lemuroid.app.mobile.feature.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.paging.compose.collectAsLazyPagingItems
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LauncherGameGrid
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidEmptyView
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel,
    searchQuery: String,
    leftNavigationRequester: FocusRequester,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onResetSearchQuery: () -> Unit,
    onPageChanged: (Int, Int) -> Unit,
) {
    val searchState = viewModel.searchState.collectAsState(SearchViewModel.UIState.Idle)
    val searchGames = viewModel.searchResults.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        onResetSearchQuery()
    }

    LaunchedEffect(key1 = searchQuery) {
        viewModel.queryString.value = searchQuery
    }

    LaunchedEffect(searchState.value, searchGames.itemCount) {
        if (searchState.value != SearchViewModel.UIState.Ready || searchGames.itemCount == 0) {
            onPageChanged(0, 0)
        }
    }

    AnimatedContent(
        targetState = searchState.value,
        label = "SearchContent",
        transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { state ->
        when {
            state == SearchViewModel.UIState.Idle -> {
                SearchEmptyView(modifier, stringResource(R.string.game_page_search_suggestion))
            }

            state == SearchViewModel.UIState.Loading -> {
                SearchLoadingView(modifier)
            }

            state == SearchViewModel.UIState.Ready && searchGames.itemCount == 0 -> {
                SearchEmptyView(modifier, stringResource(id = R.string.empty_view_default))
            }

            else -> {
                SearchResultsView(
                    modifier,
                    searchGames,
                    searchQuery,
                    leftNavigationRequester,
                    onGameClick,
                    onGameLongClick,
                    onPageChanged,
                )
            }
        }
    }
}

@Composable
private fun SearchResultsView(
    modifier: Modifier,
    games: androidx.paging.compose.LazyPagingItems<Game>,
    searchQuery: String,
    leftNavigationRequester: FocusRequester,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onPageChanged: (Int, Int) -> Unit,
) {
    LauncherGameGrid(
        games = games,
        modifier = modifier,
        resetKey = searchQuery,
        leftNavigationRequester = leftNavigationRequester,
        onGameClick = onGameClick,
        onGameLongClick = onGameLongClick,
        onPageChanged = onPageChanged,
    )
}

@Composable
private fun SearchEmptyView(
    modifier: Modifier,
    text: String,
) {
    LemuroidEmptyView(
        modifier = modifier,
        text = text,
    )
}

@Composable
private fun SearchLoadingView(modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
