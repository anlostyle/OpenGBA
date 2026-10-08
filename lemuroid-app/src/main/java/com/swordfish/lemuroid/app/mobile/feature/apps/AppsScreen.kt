package com.swordfish.lemuroid.app.mobile.feature.apps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LauncherAppGrid
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidEmptyView

@Composable
fun AppsScreen(
    modifier: Modifier = Modifier,
    viewModel: AppsViewModel,
    leftNavigationRequester: FocusRequester,
    onAppClick: (LauncherApp) -> Unit,
    onPageChanged: (Int, Int) -> Unit,
) {
    val apps by viewModel.apps.collectAsState()
    val appList = apps

    if (appList == null) {
        LaunchedEffect(Unit) { onPageChanged(0, 0) }
        Box(modifier = modifier.fillMaxSize())
        return
    }

    if (appList.isEmpty()) {
        LaunchedEffect(Unit) { onPageChanged(0, 0) }
        LemuroidEmptyView(modifier)
        return
    }

    LauncherAppGrid(
        apps = appList,
        modifier = modifier,
        leftNavigationRequester = leftNavigationRequester,
        onAppClick = onAppClick,
        onPageChanged = onPageChanged,
    )
}
