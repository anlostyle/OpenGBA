package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import android.view.KeyEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

val PixelInk = Color(0xFF0B1114)
val PixelPanel = Color(0xFF162229)
val PixelPanelLight = Color(0xFF22323B)
val PixelPaper = Color(0xFFF1E7C9)
val PixelGreen = Color(0xFFA8D944)
val PixelAmber = Color(0xFFF2A93B)
val PixelRed = Color(0xFFD86A55)
val PixelMuted = Color(0xFF91A1A8)
val PixelOutline = Color(0xFF344751)
val PixelShadow = Color(0xFF030709)

val PixelShape =
    object : Shape {
        override fun createOutline(
            size: Size,
            layoutDirection: LayoutDirection,
            density: Density,
        ): Outline {
            val step = minOf(size.width, size.height).div(16f).coerceAtMost(with(density) { 8.dp.toPx() })
            val path =
                Path().apply {
                    moveTo(step * 2f, 0f)
                    lineTo(size.width - step * 2f, 0f)
                    lineTo(size.width - step * 2f, step)
                    lineTo(size.width - step, step)
                    lineTo(size.width - step, step * 2f)
                    lineTo(size.width, step * 2f)
                    lineTo(size.width, size.height - step * 2f)
                    lineTo(size.width - step, size.height - step * 2f)
                    lineTo(size.width - step, size.height - step)
                    lineTo(size.width - step * 2f, size.height - step)
                    lineTo(size.width - step * 2f, size.height)
                    lineTo(step * 2f, size.height)
                    lineTo(step * 2f, size.height - step)
                    lineTo(step, size.height - step)
                    lineTo(step, size.height - step * 2f)
                    lineTo(0f, size.height - step * 2f)
                    lineTo(0f, step * 2f)
                    lineTo(step, step * 2f)
                    lineTo(step, step)
                    lineTo(step * 2f, step)
                    close()
                }
            return Outline.Generic(path)
        }
    }

fun Modifier.pixelFocusBrackets(
    visible: Boolean,
    color: Color = PixelGreen,
    length: Dp = 14.dp,
    thickness: Dp = 3.dp,
): Modifier {
    if (!visible) return this

    return drawWithContent {
        drawContent()
        val l = length.toPx()
        val t = thickness.toPx()
        drawRect(color, size = Size(l, t))
        drawRect(color, size = Size(t, l))
        drawRect(color, topLeft = Offset(size.width - l, 0f), size = Size(l, t))
        drawRect(color, topLeft = Offset(size.width - t, 0f), size = Size(t, l))
        drawRect(color, topLeft = Offset(0f, size.height - t), size = Size(l, t))
        drawRect(color, topLeft = Offset(0f, size.height - l), size = Size(t, l))
        drawRect(color, topLeft = Offset(size.width - l, size.height - t), size = Size(l, t))
        drawRect(color, topLeft = Offset(size.width - t, size.height - l), size = Size(t, l))
    }
}

fun Modifier.pixelGrid(color: Color = PixelOutline.copy(alpha = 0.16f)): Modifier =
    drawWithContent {
        drawContent()
        val gap = 8.dp.toPx()
        var y = gap
        while (y < size.height) {
            var x = gap
            while (x < size.width) {
                drawRect(color, Offset(x, y), Size(1.dp.toPx(), 1.dp.toPx()))
                x += gap
            }
            y += gap
        }
    }

fun Modifier.onLauncherMenu(onMenu: () -> Unit): Modifier =
    onPreviewKeyEvent { event ->
        val keyCode = event.nativeKeyEvent.keyCode
        val menuKey =
            keyCode == KeyEvent.KEYCODE_MENU ||
                keyCode == KeyEvent.KEYCODE_BUTTON_START ||
                keyCode == KeyEvent.KEYCODE_BUTTON_MODE
        if (event.type == KeyEventType.KeyUp && menuKey) {
            onMenu()
            true
        } else {
            false
        }
    }

@Composable
fun PixelKeyHint(
    key: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .background(color, PixelShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = key,
                color = PixelInk,
                style = MaterialTheme.typography.labelLarge,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
            )
        }
        Text(
            text = label,
            color = PixelPaper,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun PixelMatrixIcon(
    pixels: List<String>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(28.dp)) {
        val rows = pixels.size
        val columns = pixels.maxOfOrNull { it.length } ?: return@Canvas
        val pixel = minOf(size.width / columns, size.height / rows)
        val left = (size.width - columns * pixel) / 2f
        val top = (size.height - rows * pixel) / 2f
        pixels.forEachIndexed { row, line ->
            line.forEachIndexed { column, value ->
                if (value == '1') {
                    drawRect(
                        color = color,
                        topLeft = Offset(left + column * pixel, top + row * pixel),
                        size = Size(pixel, pixel),
                    )
                }
            }
        }
    }
}
