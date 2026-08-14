package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    val context = LocalContext.current
    var showCheatsDialog by remember { mutableStateOf(false) }
    var showRestartConfirmation by remember { mutableStateOf(false) }
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

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        if (gameMenuRequest.coreConfig.statesSupported) {
            LemuroidSettingsMenuLink(
                title = {
                    Text(
                        text =
                            stringResource(
                                R.string.game_menu_quick_save_slot,
                                gameMenuRequest.currentSaveSlot + 1,
                            ),
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
                title = {
                    Text(
                        text =
                            stringResource(
                                R.string.game_menu_quick_load_slot,
                                gameMenuRequest.currentSaveSlot + 1,
                            ),
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
                title = { Text(text = stringResource(id = R.string.game_menu_states)) },
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
            title = { Text(text = stringResource(id = R.string.game_menu_cheats)) },
            icon = {
                Icon(
                    Icons.Default.Code,
                    contentDescription = stringResource(id = R.string.game_menu_cheats),
                )
            },
            onClick = { showCheatsDialog = true },
        )

        val screenFilters = stringListResource(R.array.pref_key_shader_filter_values)
        LemuroidSettingsList(
            title = { Text(text = stringResource(id = R.string.display_filter)) },
            items = stringListResource(R.array.pref_key_shader_filter_display_names),
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
                title = { Text(text = stringResource(id = R.string.game_menu_fast_forward_speed)) },
                items = fastForwardSpeeds.map { "$it×" },
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
                title = { Text(text = stringResource(id = R.string.game_menu_fast_forward)) },
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
            title = { Text(text = stringResource(id = R.string.game_menu_restart)) },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_menu_restart),
                    contentDescription = stringResource(id = R.string.game_menu_restart),
                )
            },
            onClick = { showRestartConfirmation = true },
        )

        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.game_menu_quit)) },
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

    if (showCheatsDialog) {
        AlertDialog(
            onDismissRequest = { showCheatsDialog = false },
            title = { Text(stringResource(R.string.game_menu_cheats)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (builtInCheats == null) {
                        Text(
                            stringResource(
                                if (gameMenuRequest.game.metadataCrcMatched) {
                                    R.string.game_menu_cheats_builtin_empty
                                } else {
                                    R.string.game_menu_cheats_crc_empty
                                },
                            ),
                        )
                    } else {
                        Text(stringResource(R.string.game_menu_cheats_builtin_source, builtInCheats.source))
                        Text(stringResource(R.string.game_menu_cheats_builtin_hint))
                        builtInCheats.cheats.forEachIndexed { index, cheat ->
                            key(index, cheat.code) {
                                LemuroidSettingsSwitch(
                                    title = { Text(cheat.description) },
                                    state = rememberMemoryBooleanSettingState(cheat.code in enabledBuiltInCodes),
                                    onCheckedChange = { enabled ->
                                        enabledBuiltInCodes =
                                            if (enabled) {
                                                enabledBuiltInCodes + cheat.code
                                            } else {
                                                enabledBuiltInCodes - cheat.code
                                            }
                                    },
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = cheatText,
                        onValueChange = { cheatText = it },
                        minLines = 3,
                        maxLines = 6,
                        label = { Text(stringResource(R.string.game_menu_cheats_hint)) },
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheatsDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCheatsDialog = false
                        val selectedCodes =
                            builtInCheats?.cheats.orEmpty()
                                .map(GbaCheat::code)
                                .filter { it in enabledBuiltInCodes }
                        val manualCodes =
                            cheatText.lineSequence().map(String::trim).filter(String::isNotEmpty)
                        val codes =
                            (selectedCodes.asSequence() + manualCodes)
                                .distinct()
                                .joinToString("\n")
                        onResult { putExtra(GameMenuContract.RESULT_CHEATS, codes) }
                    },
                ) {
                    Text(stringResource(R.string.ok))
                }
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
