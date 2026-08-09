package com.swordfish.lemuroid.app.mobile.feature.main

import android.content.Context
import android.os.BatteryManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelGreen
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelInk
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelMatrixIcon
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelMuted
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelOutline
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelPanel
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelPaper
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelShape
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainTopBar(
    currentRoute: MainRoute,
    navController: NavHostController,
    onUpdateQueryString: (String) -> Unit,
    mainUIState: MainViewModel.UiState,
) {
    Column {
        PixelTopBar(
            route = currentRoute,
            navController = navController,
            mainUIState = mainUIState,
            onUpdateQueryString = onUpdateQueryString,
        )

        AnimatedVisibility(mainUIState.operationInProgress) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = PixelGreen,
                trackColor = PixelPanel,
            )
        }
    }
}

@Composable
private fun PixelTopBar(
    route: MainRoute,
    navController: NavController,
    mainUIState: MainViewModel.UiState,
    onUpdateQueryString: (String) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(PixelInk)
                .border(1.dp, PixelOutline)
                .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (route.parent != null) {
            Box(
                modifier =
                    Modifier
                        .size(28.dp)
                        .background(PixelPanel, PixelShape)
                        .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "<",
                    color = PixelGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                )
            }
        }

        Text(
            text = "OPEN GBA",
            color = PixelGreen,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = "//",
            color = PixelMuted,
            style = MaterialTheme.typography.labelLarge,
            fontFamily = FontFamily.Monospace,
        )

        if (route == MainRoute.SEARCH) {
            PixelSearchView(
                modifier = Modifier.weight(1f),
                mainUIState = mainUIState,
                onUpdateQueryString = onUpdateQueryString,
            )
        } else {
            Text(
                text = stringResource(if (route == MainRoute.HOME) R.string.launcher_library else route.titleId),
                modifier = Modifier.weight(1f),
                color = PixelPaper,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }

        PixelSystemStatus()
    }
}

@Composable
private fun PixelSystemStatus() {
    val time = rememberClock()
    val context = LocalContext.current
    val battery =
        remember(time) {
            val manager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceIn(0, 100)
        }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = time,
            color = PixelPaper,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
        )
        PixelMatrixIcon(
            pixels = WIFI_PIXELS,
            color = PixelGreen,
            modifier = Modifier.size(20.dp),
        )
        PixelBattery(level = battery)
    }
}

@Composable
private fun PixelBattery(level: Int) {
    val fillColor = if (level <= 20) MaterialTheme.colorScheme.error else PixelGreen
    Canvas(modifier = Modifier.width(30.dp).height(16.dp)) {
        val stroke = 2.dp.toPx()
        val terminal = 3.dp.toPx()
        drawRect(
            color = PixelPaper,
            topLeft = Offset(0f, stroke),
            size = Size(size.width - terminal, size.height - stroke * 2f),
            style = Stroke(width = stroke),
        )
        drawRect(
            color = PixelPaper,
            topLeft = Offset(size.width - terminal, size.height * 0.32f),
            size = Size(terminal, size.height * 0.36f),
        )
        val segments = ((level + 24) / 25).coerceIn(0, 4)
        val gap = 2.dp.toPx()
        val innerWidth = size.width - terminal - stroke * 2f
        val segmentWidth = (innerWidth - gap * 5f) / 4f
        repeat(segments) { index ->
            drawRect(
                color = fillColor,
                topLeft = Offset(stroke + gap + index * (segmentWidth + gap), stroke * 2f),
                size = Size(segmentWidth, size.height - stroke * 4f),
            )
        }
    }
}

@Composable
private fun PixelSearchView(
    modifier: Modifier = Modifier,
    mainUIState: MainViewModel.UiState,
    onUpdateQueryString: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    TextField(
        value = mainUIState.searchQuery,
        modifier =
            modifier
                .height(36.dp)
                .focusRequester(focusRequester)
                .background(PixelPanel, PixelShape),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = PixelPaper),
        placeholder = { Text(stringResource(R.string.game_page_search_suggestion)) },
        onValueChange = onUpdateQueryString,
        singleLine = true,
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(true) }),
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = PixelGreen,
            ),
    )
}

@Composable
private fun rememberClock(): String {
    var currentTime by remember { mutableStateOf(formatTime()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            currentTime = formatTime()
        }
    }
    return currentTime
}

private fun formatTime(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

private val WIFI_PIXELS =
    listOf(
        "1111111",
        "0000000",
        "0111110",
        "0000000",
        "0011100",
        "0001000",
    )
