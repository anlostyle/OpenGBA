package com.swordfish.lemuroid.app.mobile.feature.main

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.swordfish.lemuroid.app.mobile.feature.favorites.FavoritesScreen
import com.swordfish.lemuroid.app.mobile.feature.favorites.FavoritesViewModel
import com.swordfish.lemuroid.app.mobile.feature.games.GamesScreen
import com.swordfish.lemuroid.app.mobile.feature.games.GamesViewModel
import com.swordfish.lemuroid.app.mobile.feature.home.HomeScreen
import com.swordfish.lemuroid.app.mobile.feature.home.HomeViewModel
import com.swordfish.lemuroid.app.mobile.feature.search.SearchScreen
import com.swordfish.lemuroid.app.mobile.feature.search.SearchViewModel
import com.swordfish.lemuroid.app.mobile.feature.settings.advanced.AdvancedSettingsScreen
import com.swordfish.lemuroid.app.mobile.feature.settings.advanced.AdvancedSettingsViewModel
import com.swordfish.lemuroid.app.mobile.feature.settings.bios.BiosScreen
import com.swordfish.lemuroid.app.mobile.feature.settings.bios.BiosSettingsViewModel
import com.swordfish.lemuroid.app.mobile.feature.settings.coreselection.CoresSelectionScreen
import com.swordfish.lemuroid.app.mobile.feature.settings.coreselection.CoresSelectionViewModel
import com.swordfish.lemuroid.app.mobile.feature.settings.general.SettingsScreen
import com.swordfish.lemuroid.app.mobile.feature.settings.general.SettingsViewModel
import com.swordfish.lemuroid.app.mobile.feature.settings.inputdevices.InputDevicesSettingsScreen
import com.swordfish.lemuroid.app.mobile.feature.settings.inputdevices.InputDevicesSettingsViewModel
import com.swordfish.lemuroid.app.mobile.feature.settings.savesync.SaveSyncSettingsScreen
import com.swordfish.lemuroid.app.mobile.feature.settings.savesync.SaveSyncSettingsViewModel
import com.swordfish.lemuroid.app.mobile.feature.shortcuts.ShortcutsGenerator
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.AppTheme
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.PixelInk
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.game.BaseGameActivity
import com.swordfish.lemuroid.app.shared.game.GameLauncher
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.main.BusyActivity
import com.swordfish.lemuroid.app.shared.main.GameLaunchTaskHandler
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.common.coroutines.safeLaunch
import com.swordfish.lemuroid.ext.feature.review.ReviewManager
import com.swordfish.lemuroid.lib.android.RetrogradeComponentActivity
import com.swordfish.lemuroid.lib.bios.BiosManager
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.injection.PerActivity
import com.swordfish.lemuroid.lib.library.MetaSystemID
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import dagger.Provides
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
class MainActivity : RetrogradeComponentActivity(), BusyActivity {
    @Inject
    lateinit var gameLaunchTaskHandler: GameLaunchTaskHandler

    @Inject
    lateinit var saveSyncManager: SaveSyncManager

    @Inject
    lateinit var retrogradeDb: RetrogradeDatabase

    @Inject
    lateinit var gameInteractor: GameInteractor

    @Inject
    lateinit var biosManager: BiosManager

    @Inject
    lateinit var coresSelection: CoresSelection

    @Inject
    lateinit var settingsInteractor: SettingsInteractor

    @Inject
    lateinit var inputDeviceManager: InputDeviceManager

    private val reviewManager = ReviewManager()

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.Factory(applicationContext, saveSyncManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUi()

        GlobalScope.safeLaunch {
            reviewManager.initialize(applicationContext)
        }

        setContent {
            val navController = rememberNavController()
            MainScreen(navController)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUi()
    }

    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    @Composable
    private fun MainScreen(navController: NavHostController) {
        AppTheme {
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry.value?.destination
            val currentRoute =
                currentDestination?.route
                    ?.let { MainRoute.findByRoute(it) }
                    ?: MainRoute.HOME

            LaunchedEffect(currentRoute) {
                mainViewModel.changeRoute(currentRoute)
            }

            val selectedGameState =
                remember {
                    mutableStateOf<Game?>(null)
                }

            val onGameLongClick = { game: Game ->
                selectedGameState.value = game
            }

            val onGameClick = { game: Game ->
                gameInteractor.onGamePlay(game)
            }

            val onGameFavoriteToggle = { game: Game, isFavorite: Boolean ->
                gameInteractor.onFavoriteToggle(game, isFavorite)
            }

            val mainUIState =
                mainViewModel.state
                    .collectAsState(MainViewModel.UiState())
                    .value

            val configuration = LocalConfiguration.current
            val wideLayout = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(PixelInk)
                        .onPreviewKeyEvent { event ->
                            if (
                                event.type == KeyEventType.KeyUp &&
                                event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BUTTON_B &&
                                currentRoute != MainRoute.HOME
                            ) {
                                navController.popBackStack()
                                true
                            } else {
                                false
                            }
                        },
            ) {
                MainTopBar(
                    currentRoute = currentRoute,
                    navController = navController,
                    mainUIState = mainUIState,
                    onUpdateQueryString = { mainViewModel.changeQueryString(it) },
                )

                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    if (wideLayout) {
                        MainNavigationRail(currentRoute, navController)
                    }

                    NavHost(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        navController = navController,
                        startDestination = MainRoute.HOME.route,
                    ) {
                        composable(MainRoute.HOME) {
                            HomeScreen(
                                modifier = Modifier,
                                viewModel =
                                    viewModel(
                                        factory =
                                            HomeViewModel.Factory(
                                                applicationContext,
                                                retrogradeDb,
                                                coresSelection,
                                            ),
                                    ),
                                onGameClick = onGameClick,
                                onGameLongClick = onGameLongClick,
                            )
                        }
                        composable(MainRoute.FAVORITES) {
                            FavoritesScreen(
                                modifier = Modifier,
                                viewModel =
                                    viewModel(
                                        factory = FavoritesViewModel.Factory(retrogradeDb),
                                    ),
                                onGameClick = onGameClick,
                                onGameLongClick = onGameLongClick,
                            )
                        }
                        composable(MainRoute.SEARCH) {
                            SearchScreen(
                                modifier = Modifier,
                                viewModel =
                                    viewModel(
                                        factory = SearchViewModel.Factory(retrogradeDb),
                                    ),
                                searchQuery = mainUIState.searchQuery,
                                onGameClick = onGameClick,
                                onGameLongClick = onGameLongClick,
                                onGameFavoriteToggle = onGameFavoriteToggle,
                                onResetSearchQuery = { mainViewModel.changeQueryString("") },
                            )
                        }
                        composable(MainRoute.SYSTEMS) {
                            GamesScreen(
                                modifier = Modifier,
                                viewModel =
                                    viewModel(
                                        factory = GamesViewModel.Factory(retrogradeDb, MetaSystemID.GBA),
                                    ),
                                onGameClick = onGameClick,
                                onGameLongClick = onGameLongClick,
                                onSearchClick = { navController.navigateToRoute(MainRoute.SEARCH) },
                            )
                        }
                        composable(MainRoute.SYSTEM_GAMES) { entry ->
                            val metaSystemId = entry.arguments?.getString("metaSystemId")
                            GamesScreen(
                                modifier = Modifier,
                                viewModel =
                                    viewModel(
                                        factory =
                                            GamesViewModel.Factory(
                                                retrogradeDb,
                                                MetaSystemID.valueOf(metaSystemId!!),
                                            ),
                                    ),
                                onGameClick = onGameClick,
                                onGameLongClick = onGameLongClick,
                                onSearchClick = { navController.navigateToRoute(MainRoute.SEARCH) },
                            )
                        }
                        composable(MainRoute.SETTINGS) {
                            SettingsScreen(
                                modifier = Modifier,
                                viewModel =
                                    viewModel(
                                        factory =
                                            SettingsViewModel.Factory(
                                                applicationContext,
                                                settingsInteractor,
                                                saveSyncManager,
                                                FlowSharedPreferences(
                                                    SharedPreferencesHelper.getLegacySharedPreferences(
                                                        applicationContext,
                                                    ),
                                                ),
                                            ),
                                    ),
                                navController = navController,
                            )
                        }
                        composable(MainRoute.SETTINGS_ADVANCED) {
                            AdvancedSettingsScreen(
                                modifier = Modifier,
                                viewModel =
                                    viewModel(
                                        factory =
                                            AdvancedSettingsViewModel.Factory(
                                                applicationContext,
                                                settingsInteractor,
                                            ),
                                    ),
                                navController = navController,
                            )
                        }
                        composable(MainRoute.SETTINGS_BIOS) {
                            BiosScreen(
                                modifier = Modifier,
                                viewModel =
                                    viewModel(
                                        factory = BiosSettingsViewModel.Factory(biosManager),
                                    ),
                            )
                        }
                        composable(MainRoute.SETTINGS_CORES_SELECTION) {
                            CoresSelectionScreen(
                                modifier = Modifier,
                                viewModel =
                                    viewModel(
                                        factory =
                                            CoresSelectionViewModel.Factory(
                                                applicationContext,
                                                coresSelection,
                                            ),
                                    ),
                            )
                        }
                        composable(MainRoute.SETTINGS_INPUT_DEVICES) {
                            InputDevicesSettingsScreen(
                                modifier = Modifier,
                                viewModel =
                                    viewModel(
                                        factory =
                                            InputDevicesSettingsViewModel.Factory(
                                                applicationContext,
                                                inputDeviceManager,
                                            ),
                                    ),
                            )
                        }
                        composable(MainRoute.SETTINGS_SAVE_SYNC) {
                            SaveSyncSettingsScreen(
                                modifier = Modifier,
                                viewModel =
                                    viewModel(
                                        factory =
                                            SaveSyncSettingsViewModel.Factory(
                                                application,
                                                saveSyncManager,
                                            ),
                                    ),
                            )
                        }
                    }
                }

                if (wideLayout) {
                    LauncherControlBar(currentRoute)
                } else {
                    MainNavigationBar(currentRoute, navController)
                }
            }

            MainGameContextActions(
                selectedGameState = selectedGameState,
                shortcutSupported = gameInteractor.supportShortcuts(),
                onGamePlay = { gameInteractor.onGamePlay(it) },
                onGameRestart = { gameInteractor.onGameRestart(it) },
                onFavoriteToggle = { game: Game, isFavorite: Boolean ->
                    gameInteractor.onFavoriteToggle(game, isFavorite)
                },
                onCreateShortcut = { gameInteractor.onCreateShortcut(it) },
            )
        }
    }

    override fun activity(): Activity = this

    override fun isBusy(): Boolean = mainViewModel.state.value.operationInProgress ?: false

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            BaseGameActivity.REQUEST_PLAY_GAME -> {
                GlobalScope.safeLaunch {
                    gameLaunchTaskHandler.handleGameFinish(
                        true,
                        this@MainActivity,
                        resultCode,
                        data,
                    )
                }
            }
        }
    }

    @dagger.Module
    abstract class Module {
        @dagger.Module
        companion object {
            @Provides
            @PerActivity
            @JvmStatic
            fun settingsInteractor(
                activity: MainActivity,
                directoriesManager: DirectoriesManager,
            ) = SettingsInteractor(activity, directoriesManager)

            @Provides
            @PerActivity
            @JvmStatic
            fun gameInteractor(
                activity: MainActivity,
                retrogradeDb: RetrogradeDatabase,
                shortcutsGenerator: ShortcutsGenerator,
                gameLauncher: GameLauncher,
            ) = GameInteractor(activity, retrogradeDb, false, shortcutsGenerator, gameLauncher)
        }
    }
}
