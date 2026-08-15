package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.view.KeyEvent as AndroidKeyEvent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.nativeKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alorma.compose.settings.storage.memory.rememberMemoryBooleanSettingState
import com.alorma.compose.settings.storage.memory.rememberMemoryIntSettingState
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.shared.game.BaseGameScreenViewModel
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsList
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsMenuLink
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsSwitch
import com.swordfish.lemuroid.app.utils.android.stringListResource
import kotlin.reflect.KFunction1

@Composable
fun GameMenuHomeScreen(
    navController: NavController,
    gameMenuRequest: GameMenuActivity.GameMenuRequest,
    onResult: KFunction1<Intent.() -> Unit, Unit>,
) {
    var showRestartConfirmation by remember { mutableStateOf(false) }
    val compactRow = Modifier.height(48.dp)

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        if (gameMenuRequest.coreConfig.statesSupported) {
            LemuroidSettingsMenuLink(
                modifier = compactRow,
                title = {
                    Text(
                        text = stringResource(R.string.game_menu_quick_save),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                action = {
                    Text(
                        stringResource(R.string.game_menu_slot_value, gameMenuRequest.currentSaveSlot + 1),
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_menu_save),
                        contentDescription = stringResource(id = R.string.game_menu_quick_save),
                    )
                },
                onClick = {
                    onResult { putExtra(GameMenuContract.RESULT_QUICK_SAVE, true) }
                },
            )

            LemuroidSettingsMenuLink(
                modifier = compactRow,
                title = {
                    Text(
                        text = stringResource(R.string.game_menu_quick_load),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                action = {
                    Text(
                        stringResource(R.string.game_menu_slot_value, gameMenuRequest.currentSaveSlot + 1),
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_menu_load),
                        contentDescription = stringResource(id = R.string.game_menu_quick_load),
                    )
                },
                onClick = {
                    onResult { putExtra(GameMenuContract.RESULT_QUICK_LOAD, true) }
                },
            )

            LemuroidSettingsMenuLink(
                modifier = compactRow,
                title = {
                    Text(
                        text = stringResource(id = R.string.game_menu_states),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_menu_save),
                        contentDescription = stringResource(id = R.string.game_menu_states),
                    )
                },
                onClick = { navController.navigateToRoute(GameMenuRoute.STATES) },
            )
        }

        LemuroidSettingsMenuLink(
            modifier = compactRow,
            title = {
                Text(
                    text = stringResource(id = R.string.game_menu_cheats),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            icon = {
                Icon(
                    Icons.Default.Code,
                    contentDescription = stringResource(id = R.string.game_menu_cheats),
                )
            },
            onClick = { navController.navigateToRoute(GameMenuRoute.CHEATS) },
        )

        val screenFilters = stringListResource(R.array.pref_key_shader_filter_values)
        LemuroidSettingsList(
            modifier = compactRow,
            title = {
                Text(
                    text = stringResource(id = R.string.display_filter),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            items = stringListResource(R.array.pref_key_shader_filter_display_names),
            useSelectedValueAsSubtitle = false,
            action = {
                Text(
                    stringListResource(R.array.pref_key_shader_filter_display_names)[
                        screenFilters.indexOf(gameMenuRequest.screenFilter).coerceAtLeast(0)
                    ],
                    style = MaterialTheme.typography.labelMedium,
                )
            },
            icon = {
                Icon(
                    Icons.Default.FilterAlt,
                    contentDescription = stringResource(id = R.string.display_filter),
                )
            },
            state =
                rememberMemoryIntSettingState(
                    screenFilters.indexOf(gameMenuRequest.screenFilter).coerceAtLeast(0),
                ),
            onItemSelected = { index, _ ->
                onResult { putExtra(GameMenuContract.RESULT_SCREEN_FILTER, screenFilters[index]) }
            },
        )

        if (gameMenuRequest.fastForwardSupported) {
            val fastForwardSpeeds = BaseGameScreenViewModel.FAST_FORWARD_SPEEDS
            LemuroidSettingsList(
                modifier = compactRow,
                title = {
                    Text(
                        text = stringResource(id = R.string.game_menu_fast_forward_speed),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                items = fastForwardSpeeds.map { "$it×" },
                useSelectedValueAsSubtitle = false,
                action = {
                    Text(
                        "${gameMenuRequest.fastForwardSpeed}×",
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                icon = {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = stringResource(id = R.string.game_menu_fast_forward_speed),
                    )
                },
                state =
                    rememberMemoryIntSettingState(
                        fastForwardSpeeds.indexOf(gameMenuRequest.fastForwardSpeed).coerceAtLeast(0),
                    ),
                onItemSelected = { index, _ ->
                    onResult {
                        putExtra(GameMenuContract.RESULT_FAST_FORWARD_SPEED, fastForwardSpeeds[index])
                    }
                },
            )
            LemuroidSettingsSwitch(
                modifier = compactRow,
                title = {
                    Text(
                        text = stringResource(id = R.string.game_menu_fast_forward),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_menu_fast_forward),
                        contentDescription = stringResource(id = R.string.game_menu_fast_forward),
                    )
                },
                state = rememberMemoryBooleanSettingState(gameMenuRequest.fastForwardEnabled),
                onCheckedChange = {
                    onResult { putExtra(GameMenuContract.RESULT_ENABLE_FAST_FORWARD, it) }
                },
            )
        }

        LemuroidSettingsMenuLink(
            modifier = compactRow,
            title = {
                Text(
                    text = stringResource(id = R.string.game_menu_restart),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_menu_restart),
                    contentDescription = stringResource(id = R.string.game_menu_restart),
                )
            },
            onClick = { showRestartConfirmation = true },
        )

        LemuroidSettingsMenuLink(
            modifier = compactRow,
            title = {
                Text(
                    text = stringResource(id = R.string.game_menu_quit),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_menu_quit),
                    contentDescription = stringResource(id = R.string.game_menu_quit),
                )
            },
            onClick = {
                onResult { putExtra(GameMenuContract.RESULT_QUIT, true) }
            },
        )
    }

    if (showRestartConfirmation) {
        AlertDialog(
            onDismissRequest = { showRestartConfirmation = false },
            title = { Text(stringResource(R.string.game_menu_restart)) },
            text = { Text(stringResource(R.string.game_menu_restart_confirmation)) },
            dismissButton = {
                TextButton(onClick = { showRestartConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestartConfirmation = false
                        onResult { putExtra(GameMenuContract.RESULT_RESET, true) }
                    },
                ) {
                    Text(stringResource(R.string.game_menu_restart))
                }
            },
        )
    }
}

@Composable
fun GameMenuCheatsScreen(
    gameMenuRequest: GameMenuActivity.GameMenuRequest,
    onResult: KFunction1<Intent.() -> Unit, Unit>,
) {
    val context = LocalContext.current
    val clipboardManager =
        remember(context) {
            context.getSystemService(ClipboardManager::class.java)
        }
    val currentCheatCodes =
        remember(gameMenuRequest.cheats) {
            gameMenuRequest.cheats
                .lineSequence()
                .map(String::trim)
                .filter(String::isNotEmpty)
                .toList()
        }
    val builtInCheats =
        remember(gameMenuRequest.game) {
            runCatching { GbaCheatDatabase.load(context.assets, gameMenuRequest.game) }.getOrNull()
        }
    val builtInCodes =
        remember(builtInCheats) {
            builtInCheats?.cheats?.map(GbaCheat::code)?.toSet().orEmpty()
        }
    var enabledBuiltInCodes by
        remember(gameMenuRequest.cheats, builtInCheats) {
            mutableStateOf(currentCheatCodes.filter { it in builtInCodes }.toSet())
        }
    var cheatText by
        remember(gameMenuRequest.cheats, builtInCheats) {
            mutableStateOf(currentCheatCodes.filterNot { it in builtInCodes }.joinToString("\n"))
        }
    var searchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var enabledOnly by remember { mutableStateOf(false) }
    val visibleCheats =
        remember(builtInCheats, enabledBuiltInCodes, searchQuery, enabledOnly) {
            builtInCheats?.cheats.orEmpty().filter { cheat ->
                (!enabledOnly || cheat.code in enabledBuiltInCodes) &&
                    cheat.displayDescription().contains(searchQuery, ignoreCase = true)
            }
        }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(start = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(
                    R.string.game_menu_cheats_summary,
                    builtInCheats?.cheats?.size ?: 0,
                    enabledBuiltInCodes.size,
                ),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
            TextButton(onClick = { enabledOnly = !enabledOnly }) {
                Text(
                    stringResource(
                        if (enabledOnly) {
                            R.string.game_menu_cheats_filter_enabled
                        } else {
                            R.string.game_menu_cheats_filter_all
                        },
                    ),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            IconButton(onClick = { searchVisible = !searchVisible }) {
                Icon(Icons.Default.Search, stringResource(R.string.game_menu_cheats_search))
            }
        }

        if (searchVisible) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                singleLine = true,
                placeholder = {
                    Text(
                        stringResource(R.string.game_menu_cheats_search),
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
            )
        }

        if (builtInCheats == null) {
            Text(
                stringResource(
                    if (gameMenuRequest.game.metadataCrcMatched) {
                        R.string.game_menu_cheats_builtin_empty
                    } else {
                        R.string.game_menu_cheats_crc_empty
                    },
                ),
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(visibleCheats, key = GbaCheat::code) { cheat ->
                CheatMenuRow(
                    text = cheat.displayDescription(),
                    checked = cheat.code in enabledBuiltInCodes,
                    onToggle = {
                        enabledBuiltInCodes =
                            if (cheat.code in enabledBuiltInCodes) {
                                enabledBuiltInCodes - cheat.code
                            } else {
                                enabledBuiltInCodes + cheat.code
                            }
                    },
                    onCopy = {
                        clipboardManager.setPrimaryClip(
                            ClipData.newPlainText(cheat.displayDescription(), cheat.code),
                        )
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.game_menu_cheats_copy_success,
                                cheat.displayDescription(),
                            ),
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                )
            }
            if (!enabledOnly && searchQuery.isBlank()) {
                item(key = "manual-cheats") {
                    OutlinedTextField(
                        value = cheatText,
                        onValueChange = { cheatText = it },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                        minLines = 3,
                        maxLines = 6,
                        label = {
                            Text(
                                stringResource(R.string.game_menu_cheats_hint),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                        supportingText = {
                            Text(
                                stringResource(R.string.game_menu_cheats_manual_warning),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    val pastedCode =
                                        clipboardManager.primaryClip
                                            ?.takeIf { it.itemCount > 0 }
                                            ?.getItemAt(0)
                                            ?.coerceToText(context)
                                            ?.toString()
                                            .orEmpty()
                                    if (pastedCode.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            R.string.game_menu_cheats_clipboard_empty,
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    } else {
                                        cheatText = appendCheatCodes(cheatText, pastedCode)
                                        Toast.makeText(
                                            context,
                                            R.string.game_menu_cheats_paste_warning,
                                            Toast.LENGTH_LONG,
                                        ).show()
                                    }
                                },
                            ) {
                                Icon(
                                    Icons.Default.ContentPaste,
                                    stringResource(R.string.game_menu_cheats_paste),
                                )
                            }
                        },
                    )
                }
            }
        }

        Button(
            onClick = {
                val selectedCodes =
                    builtInCheats?.cheats.orEmpty()
                        .map(GbaCheat::code)
                        .filter { it in enabledBuiltInCodes }
                val manualCodes = cheatText.lineSequence().map(String::trim).filter(String::isNotEmpty)
                val codes =
                    (selectedCodes.asSequence() + manualCodes)
                        .distinct()
                        .joinToString("\n")
                onResult { putExtra(GameMenuContract.RESULT_CHEATS, codes) }
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .height(40.dp),
        ) {
            Text(stringResource(R.string.game_menu_cheats_apply))
        }
    }
}

@Composable
private fun CheatMenuRow(
    text: String,
    checked: Boolean,
    onToggle: () -> Unit,
    onCopy: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    var gamepadLongPressHandled by remember { mutableStateOf(false) }
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .onFocusChanged { focused = it.isFocused }
                .background(if (focused) accent.copy(alpha = 0.16f) else Color.Transparent)
                .onPreviewKeyEvent { event ->
                    val nativeEvent = event.nativeKeyEvent
                    if (!nativeEvent.isGameMenuConfirmKey()) return@onPreviewKeyEvent false
                    when (nativeEvent.action) {
                        AndroidKeyEvent.ACTION_DOWN -> {
                            if (nativeEvent.repeatCount > 0 || nativeEvent.isLongPress) {
                                if (!gamepadLongPressHandled) {
                                    gamepadLongPressHandled = true
                                    onCopy()
                                }
                                true
                            } else {
                                false
                            }
                        }
                        AndroidKeyEvent.ACTION_UP -> {
                            gamepadLongPressHandled.also { gamepadLongPressHandled = false }
                        }
                        else -> false
                    }
                }
                .combinedClickable(onClick = onToggle, onLongClick = onCopy)
                .focusable()
                .padding(end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        androidx.compose.foundation.layout.Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .background(if (focused) accent else Color.Transparent),
        )
        Text(
            text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Switch(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.scale(0.75f),
        )
    }
}

internal fun appendCheatCodes(
    current: String,
    pasted: CharSequence,
): String =
    (current.lineSequence() + pasted.toString().lineSequence())
        .map(String::trim)
        .filter(String::isNotEmpty)
        .distinct()
        .joinToString("\n")

private fun AndroidKeyEvent.isGameMenuConfirmKey(): Boolean =
    when (keyCode) {
        AndroidKeyEvent.KEYCODE_BUTTON_A,
        AndroidKeyEvent.KEYCODE_DPAD_CENTER,
        AndroidKeyEvent.KEYCODE_ENTER,
        AndroidKeyEvent.KEYCODE_NUMPAD_ENTER,
        AndroidKeyEvent.KEYCODE_SPACE,
        -> true
        else -> false
    }
