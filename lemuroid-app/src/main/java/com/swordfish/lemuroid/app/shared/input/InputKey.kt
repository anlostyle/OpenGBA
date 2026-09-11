package com.swordfish.lemuroid.app.shared.input

import android.view.InputDevice
import android.view.KeyEvent
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
@JvmInline
value class InputKey(val keyCode: Int) {
    fun displayName(): String {
        return when (keyCode) {
            KeyEvent.KEYCODE_BUTTON_THUMBL -> "L3"
            KeyEvent.KEYCODE_BUTTON_THUMBR -> "R3"
            KeyEvent.KEYCODE_BUTTON_MODE -> "Options"
            KeyEvent.KEYCODE_UNKNOWN -> " - "
            else ->
                KeyEvent.keyCodeToString(keyCode)
                    .split("_")
                    .last()
                    .lowercase()
                    .replaceFirstChar { it.titlecase(Locale.ENGLISH) }
        }
    }
}

fun InputDevice.normalizeInputKeyCode(keyCode: Int): Int {
    if (!isAyaneoController()) return keyCode

    return when (keyCode) {
        KeyEvent.KEYCODE_BUTTON_X -> KeyEvent.KEYCODE_BUTTON_Y
        KeyEvent.KEYCODE_BUTTON_Y -> KeyEvent.KEYCODE_BUTTON_X
        else -> keyCode
    }
}

fun InputDevice.isAyaneoController(): Boolean = vendorId == AYANEO_VENDOR_ID && productId == AYANEO_PRODUCT_ID

private const val AYANEO_VENDOR_ID = 0x4001
private const val AYANEO_PRODUCT_ID = 0x0428
