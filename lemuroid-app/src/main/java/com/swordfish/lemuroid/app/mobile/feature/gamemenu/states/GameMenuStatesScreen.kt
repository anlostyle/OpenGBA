package com.swordfish.lemuroid.app.mobile.feature.gamemenu.states

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsMenuLink

@Composable
fun GameMenuStatesScreen(
    viewModel: GameMenuStatesViewModel,
    currentSlot: Int,
    onSave: (Int) -> Unit,
    onLoad: (Int) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val state = viewModel.uiStates.collectAsState(initial = GameMenuStatesViewModel.State())
    var selectedSlot by remember { mutableStateOf<Int?>(null) }
    var pendingAction by remember { mutableStateOf<SlotAction?>(null) }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        state.value.entries.forEachIndexed { index, entry ->
            LemuroidSettingsMenuLink(
                title = {
                    Text(
                        text =
                            if (index == currentSlot) {
                                stringResource(R.string.game_menu_state_current, index + 1)
                            } else {
                                entry.title
                            },
                    )
                },
                subtitle = { Text(text = entry.description) },
                icon = {
                    if (entry.preview != null) {
                        Image(
                            modifier = Modifier.size(48.dp),
                            bitmap = entry.preview.asImageBitmap(),
                            contentScale = ContentScale.Crop,
                            contentDescription = null,
                        )
                    }
                },
                onClick = { selectedSlot = index },
            )
        }
    }

    val slot = selectedSlot
    val entry = slot?.let { state.value.entries.getOrNull(it) }
    if (slot != null && entry != null && pendingAction == null) {
        AlertDialog(
            onDismissRequest = { selectedSlot = null },
            title = { Text(stringResource(R.string.game_menu_state, slot + 1)) },
            text = {
                Column {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (entry.exists) {
                                pendingAction = SlotAction.SAVE
                            } else {
                                onSave(slot)
                            }
                        },
                    ) {
                        Text(stringResource(R.string.game_menu_state_action_save))
                    }
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = entry.exists,
                        onClick = { onLoad(slot) },
                    ) {
                        Text(stringResource(R.string.game_menu_state_action_load))
                    }
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = entry.exists,
                        onClick = { pendingAction = SlotAction.DELETE },
                    ) {
                        Text(stringResource(R.string.game_menu_state_action_delete))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedSlot = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    val action = pendingAction
    if (slot != null && action != null) {
        val isDelete = action == SlotAction.DELETE
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = {
                Text(
                    stringResource(
                        if (isDelete) {
                            R.string.game_menu_state_delete_title
                        } else {
                            R.string.game_menu_state_overwrite_title
                        },
                    ),
                )
            },
            text = {
                Text(
                    stringResource(
                        if (isDelete) {
                            R.string.game_menu_state_delete_confirmation
                        } else {
                            R.string.game_menu_state_overwrite_confirmation
                        },
                        slot + 1,
                    ),
                )
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(onClick = { if (isDelete) onDelete(slot) else onSave(slot) }) {
                    Text(
                        stringResource(
                            if (isDelete) {
                                R.string.game_menu_state_action_delete
                            } else {
                                R.string.game_menu_state_action_save
                            },
                        ),
                    )
                }
            },
        )
    }
}

private enum class SlotAction { SAVE, DELETE }
