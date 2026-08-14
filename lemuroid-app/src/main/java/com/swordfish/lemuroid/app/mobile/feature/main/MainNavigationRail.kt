package com.swordfish.lemuroid.app.mobile.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelGreen
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelMatrixIcon
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelOutline
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelPanel
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelPaper
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelShape
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.pixelFocusBrackets

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainNavigationRail(
    currentRoute: MainRoute?,
    navController: NavHostController,
) {
    Column(
        modifier =
            Modifier
                .fillMaxHeight()
                .width(112.dp)
                .background(PixelPanel, PixelShape)
                .border(2.dp, PixelOutline, PixelShape)
                .focusRestorer()
                .focusGroup()
                .padding(horizontal = 9.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        MainNavigationRoutes.values().forEach { destination ->
            val selected = currentRoute?.root == destination.route
            PixelNavigationItem(
                route = destination.route,
                label = stringResource(destination.titleId),
                selected = selected,
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
    route: MainRoute,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val active = selected || focused
    val color = if (active) PixelGreen else PixelPaper

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 58.dp)
                .onFocusChanged {
                    focused = it.isFocused
                    if (it.isFocused && !selected) onClick()
                }
                .pixelFocusBrackets(active, color)
                .clickable(onClick = onClick)
                .padding(horizontal = 5.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
    ) {
        PixelMatrixIcon(
            pixels = route.pixelIcon(),
            color = color,
        )
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
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
