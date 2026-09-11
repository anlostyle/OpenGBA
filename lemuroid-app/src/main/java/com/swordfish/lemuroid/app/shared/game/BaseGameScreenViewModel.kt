package com.swordfish.lemuroid.app.shared.game

import android.content.Context
import android.content.SharedPreferences
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.Density
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.game.GameService
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.lemuroid.app.shared.game.viewmodel.GameViewModelInput
import com.swordfish.lemuroid.app.shared.game.viewmodel.GameViewModelRetroGameView
import com.swordfish.lemuroid.app.shared.game.viewmodel.GameViewModelSaves
import com.swordfish.lemuroid.app.shared.game.viewmodel.GameViewModelSideEffects
import com.swordfish.lemuroid.app.shared.game.viewmodel.GameViewModelTilt
import com.swordfish.lemuroid.app.shared.game.viewmodel.GameViewModelTouchControls
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.rumble.RumbleManager
import com.swordfish.lemuroid.app.shared.settings.ControllerConfigsManager
import com.swordfish.lemuroid.app.shared.settings.HDModeQuality
import com.swordfish.lemuroid.app.shared.settings.HapticFeedbackMode
import com.swordfish.lemuroid.common.longAnimationDuration
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.core.CoreVariablesManager
import com.swordfish.lemuroid.lib.game.GameLoader
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.SavesManager
import com.swordfish.lemuroid.lib.saves.StatesManager
import com.swordfish.lemuroid.lib.saves.StatesPreviewManager
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.touchinput.radial.sensors.TiltConfiguration
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import gg.padkit.inputevents.InputEvent
import gg.padkit.inputstate.InputState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.ArrayDeque

class BaseGameScreenViewModel(
    private val appContext: Context,
    private val game: Game,
    settingsManager: SettingsManager,
    inputDeviceManager: InputDeviceManager,
    controllerConfigsManager: ControllerConfigsManager,
    private val system: GameSystem,
    systemCoreConfig: SystemCoreConfig,
    private val sharedPreferences: SharedPreferences,
    savesManager: SavesManager,
    statesManager: StatesManager,
    statesPreviewManager: StatesPreviewManager,
    coreVariablesManager: CoreVariablesManager,
    rumbleManager: RumbleManager,
) : ViewModel(), DefaultLifecycleObserver {
    companion object {
        val FAST_FORWARD_SPEEDS = listOf(2, 3, 5)
        const val DEFAULT_FAST_FORWARD_SPEED = 2
        private const val FAST_FORWARD_SPEED_PREFERENCE = "opengba_fast_forward_speed"
        private const val CURRENT_SAVE_SLOT_PREFERENCE = "opengba_current_save_slot_"
    }

    class Factory(
        private val appContext: Context,
        private val game: Game,
        private val settingsManager: SettingsManager,
        private val inputDeviceManager: InputDeviceManager,
        private val controllerConfigsManager: ControllerConfigsManager,
        private val system: GameSystem,
        private val systemCoreConfig: SystemCoreConfig,
        private val sharedPreferences: SharedPreferences,
        private val savesManager: SavesManager,
        private val statesManager: StatesManager,
        private val statesPreviewManager: StatesPreviewManager,
        private val coreVariablesManager: CoreVariablesManager,
        private val rumbleManager: RumbleManager,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BaseGameScreenViewModel(
                appContext,
                game,
                settingsManager,
                inputDeviceManager,
                controllerConfigsManager,
                system,
                systemCoreConfig,
                sharedPreferences,
                savesManager,
                statesManager,
                statesPreviewManager,
                coreVariablesManager,
                rumbleManager,
            ) as T
        }
    }

    private val sideEffects = GameViewModelSideEffects(viewModelScope)
    val retroGameView =
        GameViewModelRetroGameView(
            appContext,
            system,
            systemCoreConfig,
            settingsManager,
            coreVariablesManager,
            sideEffects,
            rumbleManager,
            viewModelScope,
        )
    private val tilt = GameViewModelTilt(appContext, settingsManager)
    private val inputs =
        GameViewModelInput(
            appContext,
            system,
            systemCoreConfig,
            inputDeviceManager,
            controllerConfigsManager,
            retroGameView,
            tilt,
            sideEffects,
            viewModelScope,
            ::commitTimeline,
        )
    private val touchControls =
        GameViewModelTouchControls(
            settingsManager,
            TouchControllerSettingsManager(sharedPreferences),
            retroGameView,
            inputs,
            tilt,
            sideEffects,
            viewModelScope,
        )
    private val saves =
        GameViewModelSaves(
            appContext,
            system,
            game,
            systemCoreConfig,
            retroGameView,
            settingsManager,
            savesManager,
            statesManager,
            statesPreviewManager,
            sideEffects,
        )

    private val rewindStates = ArrayDeque<ByteArray>()
    private var rewindCaptureJob: kotlinx.coroutines.Job? = null
    private var rewindHoldJob: kotlinx.coroutines.Job? = null
    private var rewinding = false
    private var rewindOrigin: ByteArray? = null
    private var fastForwardSpeed =
        sharedPreferences.getInt(FAST_FORWARD_SPEED_PREFERENCE, DEFAULT_FAST_FORWARD_SPEED)
            .takeIf { it in FAST_FORWARD_SPEEDS }
            ?: DEFAULT_FAST_FORWARD_SPEED
    private var persistentFastForward = false
    private var heldFastForward = false
    private var currentSaveSlot =
        sharedPreferences.getInt(CURRENT_SAVE_SLOT_PREFERENCE + game.fileUri, 0)
            .coerceIn(0, StatesManager.MAX_STATES - 1)
    private var cheatCodes = emptyList<String>()

    val loadingState = MutableStateFlow(false)
    val fastForwardMultiplier = MutableStateFlow(1)

    private inline fun withLoading(block: () -> Unit) {
        loadingState.value = true
        block()
        loadingState.value = false
    }

    fun getGameState(): Flow<GameViewModelRetroGameView.GameState> {
        return retroGameView.getGameState()
    }

    fun getSideEffects(): Flow<GameViewModelSideEffects.UiEffect> {
        return sideEffects.getUiEffects()
    }

    fun getTiltConfiguration(): Flow<TiltConfiguration> {
        return tilt.getTiltConfiguration()
    }

    fun getSimulatedTiltEvents(): Flow<InputState> {
        return tilt.getSimulatedTiltEvents()
    }

    fun getTouchControlsSettings(
        density: Density,
        insets: WindowInsets,
    ): Flow<TouchControllerSettingsManager.Settings?> {
        return touchControls.getTouchControlsSettings(density, insets)
    }

    fun getTouchHapticFeedbackMode(): Flow<HapticFeedbackMode> {
        return touchControls.getTouchHapticFeedbackMode()
    }

    fun createRetroView(
        context: Context,
        lifecycle: LifecycleOwner,
    ): GLRetroView {
        val (gameData, result) = retroGameView.createRetroView(context, lifecycle)
        viewModelScope.launch {
            gameData.quickSaveData?.let {
                saves.restoreAutoSaveAsync(it)
            }
        }
        updateFastForward()
        startRewindCapture()
        return result
    }

    suspend fun loadGame(
        applicationContext: Context,
        game: Game,
        systemCoreConfig: SystemCoreConfig,
        gameLoader: GameLoader,
        requestLoadSave: Boolean,
    ) {
        Timber.i("Calling load game: $game")
        retroGameView.initialize(applicationContext, game, systemCoreConfig, gameLoader, requestLoadSave)
    }

    fun showEditControls(show: Boolean) {
        touchControls.showEditControls(show)
    }

    fun isEditControlShown(): Flow<Boolean> {
        return touchControls.isEditControlsShown()
    }

    fun updateTouchControllerSettings(touchControllerSettings: TouchControllerSettingsManager.Settings) {
        touchControls.updateTouchControllerSettings(touchControllerSettings)
    }

    fun resetTouchControls() {
        touchControls.resetTouchControls()
    }

    fun onScreenOrientationChanged(orientation: TouchControllerSettingsManager.Orientation) {
        touchControls.updateScreenOrientation(orientation)
    }

    fun isTouchControllerVisible(): Flow<Boolean> {
        return touchControls.isTouchControllerVisible()
    }

    fun getTouchControllerConfig(): Flow<ControllerConfig> {
        return touchControls.getTouchControllerConfig()
    }

    fun changeTiltConfiguration(tiltConfig: TiltConfiguration) {
        tilt.changeTiltConfiguration(tiltConfig)
    }

    fun isMenuPressed(): Flow<Boolean> {
        return touchControls.isMenuPressed()
    }

    suspend fun saveSlot(index: Int) {
        if (loadingState.value) return
        var saved = false
        withLoading {
            saved = saves.saveSlot(index)
        }
        if (saved) setCurrentSaveSlot(index)
    }

    suspend fun loadSlot(index: Int) {
        if (loadingState.value) return
        commitTimeline()
        var loaded = false
        withLoading {
            loaded = saves.loadSlot(index)
        }
        if (loaded) setCurrentSaveSlot(index)
    }

    fun deleteSlot(index: Int) {
        if (loadingState.value) return
        viewModelScope.launch {
            var deleted = false
            withLoading {
                deleted = saves.deleteSlot(index)
            }
            val message =
                if (deleted) R.string.game_toast_state_deleted else R.string.game_toast_state_delete_failed
            sideEffects.showToast(appContext.getString(message, index + 1))
        }
    }

    fun saveQuickSave() {
        Timber.d("Saving quick save")
        if (loadingState.value) return
        viewModelScope.launch {
            withLoading {
                saves.saveQuickSave(currentSaveSlot)
            }
        }
    }

    fun loadQuickSave() {
        Timber.d("Loading quick save")
        if (loadingState.value) return
        commitTimeline()
        viewModelScope.launch {
            withLoading {
                saves.loadQuickSave(currentSaveSlot)
            }
        }
    }

    fun getCurrentSaveSlot(): Int = currentSaveSlot

    private fun setCurrentSaveSlot(index: Int) {
        if (index !in 0 until StatesManager.MAX_STATES) return
        currentSaveSlot = index
        sharedPreferences.edit()
            .putInt(CURRENT_SAVE_SLOT_PREFERENCE + game.fileUri, index)
            .apply()
    }

    fun toggleFastForward() {
        Timber.d("Toggling fast forward")
        persistentFastForward = !persistentFastForward
        updateFastForward()
    }

    fun setFastForward(enabled: Boolean) {
        persistentFastForward = enabled
        updateFastForward()
    }

    fun setHeldFastForward(enabled: Boolean) {
        heldFastForward = enabled
        updateFastForward()
    }

    fun isPersistentFastForwardEnabled(): Boolean = persistentFastForward

    fun getFastForwardSpeed(): Int = fastForwardSpeed

    fun setFastForwardSpeed(speed: Int) {
        if (loadingState.value || speed !in FAST_FORWARD_SPEEDS) return
        fastForwardSpeed = speed
        sharedPreferences.edit().putInt(FAST_FORWARD_SPEED_PREFERENCE, speed).apply()
        updateFastForward()
    }

    private fun updateFastForward() {
        val multiplier = if (persistentFastForward || heldFastForward) fastForwardSpeed else 1
        retroGameView.retroGameView?.frameSpeed = multiplier
        fastForwardMultiplier.value = multiplier
    }

    fun getScreenFilter(): String {
        val filters = appContext.resources.getStringArray(R.array.pref_key_shader_filter_values)
        return sharedPreferences
            .getString(appContext.getString(R.string.pref_key_shader_filter), filters.first())
            ?.takeIf { it in filters }
            ?: filters.first()
    }

    fun setScreenFilter(filter: String) {
        val filters = appContext.resources.getStringArray(R.array.pref_key_shader_filter_values)
        if (loadingState.value || filter !in filters) return
        val view = retroGameView.retroGameView ?: return
        view.shader =
            ShaderChooser.getShaderForSystem(
                appContext,
                false,
                HDModeQuality.LOW,
                filter,
                system,
            )
        sharedPreferences.edit()
            .putString(appContext.getString(R.string.pref_key_shader_filter), filter)
            .putBoolean(appContext.getString(R.string.pref_key_hd_mode), false)
            .apply()
    }

    fun getCheatCodes(): String = cheatCodes.joinToString("\n")

    fun applyCheats(rawCodes: String) {
        if (loadingState.value) return
        commitTimeline()
        val nextCodes =
            rawCodes
                .lineSequence()
                .map(String::trim)
                .filter(String::isNotEmpty)
                .map { it.take(1024) }
                .take(512)
                .toList()
        retroGameView.retroGameView?.let { view ->
            cheatCodes.forEachIndexed { index, code ->
                runCatching { view.setCheat(index, false, code, true) }
            }
            nextCodes.forEachIndexed { index, code ->
                runCatching { view.setCheat(index, true, code, true) }
            }
        }
        cheatCodes = nextCodes
    }

    fun startRewind() {
        if (loadingState.value || rewindHoldJob != null) return
        if (rewindOrigin == null) {
            rewindOrigin =
                runCatching { retroGameView.retroGameView?.serializeState(true) }
                    .getOrNull()
                    ?.takeIf { it.isNotEmpty() }
        }
        if (rewindOrigin == null) return
        rewinding = true
        rewindHoldJob = viewModelScope.launch {
            while (isActive) {
                rewindStep()
                delay(100)
            }
        }
    }

    fun stopRewind() {
        rewinding = false
        rewindHoldJob?.cancel()
        rewindHoldJob = null
    }

    fun startForward() {
        if (loadingState.value) return
        stopRewind()
        val origin = rewindOrigin ?: return
        if (retroGameView.retroGameView?.unserializeState(origin, true) == true) {
            rewindOrigin = null
            rewindStates.clear()
            rewindStates.addLast(origin)
        }
    }

    fun stopForward() = Unit

    private fun startRewindCapture() {
        if (system.id != SystemID.GBA || rewindCaptureJob != null) return
        rewindCaptureJob = viewModelScope.launch {
            retroGameView.waitRetroGameViewInitialized()
            // libretro cores may not have allocated their state buffer until the first frame.
            retroGameView.waitGLEvent<GLRetroView.GLRetroEvents.FrameRendered>()
            while (isActive) {
                if (!rewinding && rewindOrigin == null) {
                    retroGameView.retroGameView?.let { view ->
                        runCatching { view.serializeState(true) }
                            .getOrNull()
                            ?.takeIf { it.isNotEmpty() }
                            ?.let {
                                // ponytail: fixed 60-state window (~30s); tune only after measuring real state sizes.
                                if (rewindStates.size >= 60) rewindStates.removeFirst()
                                rewindStates.addLast(it)
                            }
                    }
                }
                delay(500)
            }
        }
    }

    private fun rewindStep() {
        if (rewindStates.size < 2) return
        val current = rewindStates.removeLast()
        val target = rewindStates.last()
        if (retroGameView.retroGameView?.unserializeState(target, true) != true) {
            rewindStates.addLast(current)
        }
    }

    private fun commitTimeline() {
        rewindOrigin = null
    }

    suspend fun reset() =
        withLoading {
            try {
                persistentFastForward = false
                heldFastForward = false
                updateFastForward()
                commitTimeline()
                delay(appContext.longAnimationDuration().toLong())
                retroGameView.retroGameViewFlow().reset()
            } catch (e: Throwable) {
                Timber.e(e, "Error in reset")
            }
        }

    fun requestFinish() {
        if (loadingState.value) return
        viewModelScope.launch {
            withLoading {
                val snapshot = saves.captureSaveSnapshot(true) ?: return@launch
                saves.writeSaveSnapshot(snapshot)
                sideEffects.requestSuccessfulFinish()
            }
        }
    }

    fun requestBackgroundSave() {
        if (loadingState.value) return
        GameService.schedule {
            val snapshot = saves.captureSaveSnapshot(false)
            saves.writeSaveSnapshot(snapshot)
        }
    }

    fun handleVirtualInputEvent(events: List<InputEvent>) {
        touchControls.handleVirtualInputEvent(events)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        owner.lifecycle.addObserver(tilt)
        owner.lifecycle.addObserver(inputs)
        owner.lifecycle.addObserver(retroGameView)
        owner.lifecycle.addObserver(touchControls)
    }

    fun sendKeyEvent(
        keyCode: Int,
        event: KeyEvent,
    ): Boolean {
        return inputs.sendKeyEvent(keyCode, event)
    }

    fun sendMotionEvent(event: MotionEvent): Boolean {
        return inputs.sendMotionEvent(event)
    }
}
