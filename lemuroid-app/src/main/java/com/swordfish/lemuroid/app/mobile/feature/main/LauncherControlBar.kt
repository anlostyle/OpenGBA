package com.swordfish.lemuroid.app.mobile.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelGreen
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelKeyHint
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelOutline
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelPanel
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelPaper
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelRed

@Composable
fun LauncherControlBar(currentRoute: MainRoute) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(PixelPanel)
                .border(1.dp, PixelOutline)
                .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        PixelKeyHint(
            key = "A",
            label = stringResource(R.string.launcher_confirm),
            color = PixelGreen,
        )
        if (currentRoute != MainRoute.HOME) {
            PixelKeyHint(
                key = "B",
                label = stringResource(R.string.back),
                color = PixelRed,
            )
        }
        PixelKeyHint(
            key = "MENU",
            label = stringResource(R.string.launcher_menu),
            color = PixelPaper,
        )
    }
}
