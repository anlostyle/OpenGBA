package com.swordfish.lemuroid.app.shared.settings

import android.view.InputDevice
import android.view.KeyEvent
import com.swordfish.lemuroid.app.shared.input.InputKey
import com.swordfish.lemuroid.app.shared.input.normalizeInputKeyCode

data class GameShortcut(
    val type: GameShortcutType,
    val keys: Set<Int>,
) {
    val name: String = keys.joinToString(" + ") { InputKey(it).displayName() }

    companion object {
        fun getDefault(
            inputDevice: InputDevice,
            type: GameShortcutType,
        ): GameShortcut? {
            fun hasKey(keyCode: Int) = inputDevice.hasKeys(inputDevice.normalizeInputKeyCode(keyCode)).first()

            if (type == GameShortcutType.MENU) {
                val menuKey =
                    listOf(KeyEvent.KEYCODE_BUTTON_L2, KeyEvent.KEYCODE_BUTTON_MODE)
                        .firstOrNull(::hasKey)
                if (menuKey != null) return GameShortcut(type = type, keys = setOf(menuKey))

                return if (hasKey(KeyEvent.KEYCODE_BUTTON_THUMBL) && hasKey(KeyEvent.KEYCODE_BUTTON_THUMBR)) {
                    GameShortcut(keys = setOf(KeyEvent.KEYCODE_BUTTON_THUMBL, KeyEvent.KEYCODE_BUTTON_THUMBR), type = type)
                } else if (hasKey(KeyEvent.KEYCODE_BUTTON_SELECT) && hasKey(KeyEvent.KEYCODE_BUTTON_START)) {
                    GameShortcut(keys = setOf(KeyEvent.KEYCODE_BUTTON_SELECT, KeyEvent.KEYCODE_BUTTON_START), type = type)
                } else {
                    null
                }
            }

            val keyCode =
                when (type) {
                    GameShortcutType.REWIND -> KeyEvent.KEYCODE_BUTTON_X
                    GameShortcutType.FORWARD -> KeyEvent.KEYCODE_BUTTON_Y
                    GameShortcutType.TOGGLE_FAST_FORWARD -> KeyEvent.KEYCODE_BUTTON_R2
                    else -> return null
                }

            return if (hasKey(keyCode)) GameShortcut(type = type, keys = setOf(keyCode)) else null
        }
    }
}

enum class GameShortcutType {
    MENU,
    REWIND,
    FORWARD,
    QUICK_LOAD,
    QUICK_SAVE,
    TOGGLE_FAST_FORWARD,
    ;

    fun displayName() =
        when (this) {
            FORWARD -> "Undo Rewind"
            TOGGLE_FAST_FORWARD -> "Fast Forward"
            else -> name.split('_').joinToString(" ") { it.lowercase().replaceFirstChar { it.uppercase() } }
        }
}
