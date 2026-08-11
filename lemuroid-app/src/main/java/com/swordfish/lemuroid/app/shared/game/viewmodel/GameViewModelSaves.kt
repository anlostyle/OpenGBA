package com.swordfish.lemuroid.app.shared.game.viewmodel

import android.content.Context
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.lemuroid.common.graphics.GraphicsUtils
import com.swordfish.lemuroid.common.graphics.takeScreenshot
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.IncompatibleStateException
import com.swordfish.lemuroid.lib.saves.SaveState
import com.swordfish.lemuroid.lib.saves.SavesManager
import com.swordfish.lemuroid.lib.saves.StatesManager
import com.swordfish.lemuroid.lib.saves.StatesPreviewManager
import com.swordfish.libretrodroid.GLRetroView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.roundToInt

class GameViewModelSaves(
    private val appContext: Context,
    private val system: GameSystem,
    private val game: Game,
    private val systemCoreConfig: SystemCoreConfig,
    private val retroGameView: GameViewModelRetroGameView,
    private val settingsManager: SettingsManager,
    private val savesManager: SavesManager,
    private val statesManager: StatesManager,
    private val statesPreviewManager: StatesPreviewManager,
    private val sideEffects: GameViewModelSideEffects,
) {
    data class SaveSnapshot(
        val sram: ByteArray,
        val autoSave: SaveState?,
    )

    suspend fun saveSlot(index: Int): Boolean {
        val saveState = getCurrentSaveState() ?: return false
        statesManager.setSlotSave(game, saveState, systemCoreConfig.coreID, index)
        runCatching {
            takeScreenshotPreview(index)
        }
        return true
    }

    suspend fun loadSlot(index: Int): Boolean =
        try {
            val saveState = statesManager.getSlotSave(game, systemCoreConfig.coreID, index) ?: return false
            loadSlot(saveState)
        } catch (e: Throwable) {
            showLoadError(e)
            false
        }

    suspend fun deleteSlot(index: Int): Boolean =
        runCatching {
            val deleted = statesManager.deleteSlotSave(game, systemCoreConfig.coreID, index)
            statesPreviewManager.deletePreviewForSlot(game, systemCoreConfig.coreID, index)
            deleted
        }.getOrDefault(false)

    suspend fun captureSaveSnapshot(useEmulationThread: Boolean): SaveSnapshot? {
        val retroGameView = retroGameView.retroGameView ?: return null
        val sramState = retroGameView.serializeSRAM(useEmulationThread)
        val autoSaveState = if (isAutoSaveEnabled()) getCurrentSaveState(useEmulationThread) else null
        return SaveSnapshot(sramState, autoSaveState)
    }

    suspend fun writeSaveSnapshot(snapshot: SaveSnapshot?) {
        if (snapshot == null) return
        Timber.i(
            "GameViewModelSaves.write game=%s core=%s writingSram=%s writingAutoSave=%s",
            game.id,
            systemCoreConfig.coreID,
            true,
            snapshot.autoSave != null,
        )
        savesManager.setSaveRAM(game, snapshot.sram)
        snapshot.autoSave?.let { statesManager.setAutoSave(game, systemCoreConfig.coreID, it) }
    }

    // On some cores unserialize fails with no reason. So we need to try multiple times.
    suspend fun restoreAutoSaveAsync(saveState: SaveState) {
        // PPSSPP and Mupen64 initialize some state while rendering the first frame, so we have to wait before restoring
        // the autosave. Do not change thread here. Stick to the GL one to avoid issues with PPSSPP.
        if (!isAutoSaveEnabled()) return

        try {
            retroGameView.waitGLEvent<GLRetroView.GLRetroEvents.FrameRendered>()
            restoreQuickSave(saveState)
        } catch (e: Throwable) {
            Timber.e(e, "Error while loading auto-save")
        }
    }

    private fun getCurrentSaveState(useEmulationThread: Boolean = true): SaveState? {
        val retroGameView = retroGameView.retroGameView ?: return null
        val currentDisk =
            if (system.hasMultiDiskSupport) {
                retroGameView.getCurrentDisk(useEmulationThread)
            } else {
                0
            }
        return SaveState(
            retroGameView.serializeState(useEmulationThread),
            SaveState.Metadata(currentDisk, systemCoreConfig.statesVersion),
        )
    }

    private suspend fun isAutoSaveEnabled(): Boolean {
        return systemCoreConfig.statesSupported && settingsManager.autoSave()
    }

    private suspend fun takeScreenshotPreview(index: Int) {
        val sizeInDp = StatesPreviewManager.PREVIEW_SIZE_DP
        val previewSize = GraphicsUtils.convertDpToPixel(sizeInDp, appContext).roundToInt()
        val preview = retroGameView.retroGameView?.takeScreenshot(previewSize, 3)
        if (preview != null) {
            statesPreviewManager.setPreviewForSlot(game, preview, systemCoreConfig.coreID, index)
        }
    }

    // Now that we wait for the first rendered frame this is probably no longer needed, but we'll keep it just to be sure
    private suspend fun restoreQuickSave(saveState: SaveState) {
        var times = 10

        while (!loadSaveState(saveState) && times > 0) {
            delay(200)
            times--
        }
    }

    private fun loadSaveState(saveState: SaveState): Boolean {
        val retroGameView = retroGameView.retroGameView ?: return false

        if (systemCoreConfig.statesVersion != saveState.metadata.version) {
            throw IncompatibleStateException()
        }

        if (system.hasMultiDiskSupport &&
            retroGameView.getAvailableDisks() > 1 &&
            retroGameView.getCurrentDisk() != saveState.metadata.diskIndex
        ) {
            retroGameView.changeDisk(saveState.metadata.diskIndex)
        }

        return retroGameView.unserializeState(saveState.state)
    }

    suspend fun saveQuickSave(index: Int) {
        if (saveSlot(index)) {
            sideEffects.showToast(appContext.getString(R.string.game_toast_quick_save_slot_saved, index + 1))
        }
    }

    suspend fun loadQuickSave(index: Int) {
        val saveState = statesManager.getSlotSave(game, systemCoreConfig.coreID, index)
        if (saveState == null) {
            sideEffects.showToast(appContext.getString(R.string.game_toast_quick_load_empty, index + 1))
            return
        }

        try {
            if (loadSlot(saveState)) {
                sideEffects.showToast(appContext.getString(R.string.game_toast_quick_save_slot_loaded, index + 1))
            }
        } catch (e: Throwable) {
            showLoadError(e)
        }
    }

    private suspend fun loadSlot(saveState: SaveState): Boolean {
        val loaded =
            withContext(Dispatchers.IO) {
                loadSaveState(saveState)
            }
        if (!loaded) {
            sideEffects.showToast(appContext.getString(R.string.game_toast_load_state_failed))
        }
        return loaded
    }

    private fun showLoadError(error: Throwable) {
        val errorMessageId =
            when (error) {
                is IncompatibleStateException -> R.string.error_message_incompatible_state
                else -> R.string.game_toast_load_state_failed
            }
        sideEffects.showToast(appContext.getString(errorMessageId))
    }
}
