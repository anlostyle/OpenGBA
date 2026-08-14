package com.swordfish.lemuroid.app.mobile.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelGreen
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelMatrixIcon
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelPanel
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelPaper
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.pixelFocusBrackets

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainNavigationRail(
    currentRoute: MainRoute?,
    navController: NavHostController,
    selectedItemFocusRequester: FocusRequester,
) {
    Column(
        modifier =
            Modifier
                .fillMaxHeight()
                .width(46.dp)
                .background(PixelPanel)
                .focusRestorer()
                .focusGroup(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        MainNavigationRoutes.values().forEach { destination ->
            val selected = currentRoute?.root == destination.route
            PixelNavigationItem(
                modifier = Modifier.weight(1f),
                route = destination.route,
                label = stringResource(destination.titleId),
                selected = selected,
                focusRequester = if (selected) selectedItemFocusRequester else null,
                onClick = {
                    navController.navigate(destination.route.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = false
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                },
            )
        }
    }
}

@Composable
private fun PixelNavigationItem(
    modifier: Modifier,
    route: MainRoute,
    label: String,
    selected: Boolean,
    focusRequester: FocusRequester?,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val active = selected || focused
    val color = if (active) PixelGreen else PixelPaper

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                .onFocusChanged {
                    focused = it.isFocused
                    if (it.isFocused && !selected) onClick()
                }
                .clickable(onClick = onClick)
                .semantics {
                    contentDescription = label
                    this.selected = selected
                },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(36.dp).pixelFocusBrackets(active, color),
            contentAlignment = Alignment.Center,
        ) {
            PixelMatrixIcon(
                pixels = route.pixelIcon(),
                color = color,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private fun MainRoute.pixelIcon(): List<String> =
    when (this) {
        MainRoute.HOME ->
            listOf(
                "0111110",
                "0100010",
                "0111110",
                "0100010",
                "0101010",
                "0111110",
            )
        MainRoute.SYSTEMS ->
            listOf(
                "001111100",
                "011111110",
                "111111111",
                "110101011",
                "111000111",
                "010000010",
            )
        MainRoute.SEARCH ->
            listOf(
                "001111000",
                "011001100",
                "110000110",
                "110000110",
                "011001100",
                "001111000",
                "000011000",
                "000001100",
            )
        MainRoute.FAVORITES ->
            listOf(
                "010000010",
                "111000111",
                "111101111",
                "011111110",
                "001111100",
                "000111000",
                "000010000",
            )
        else ->
            listOf(
                "001010100",
                "011111110",
                "110111011",
                "111000111",
                "110000011",
                "111000111",
                "110111011",
                "011111110",
                "001010100",
            )
    }
