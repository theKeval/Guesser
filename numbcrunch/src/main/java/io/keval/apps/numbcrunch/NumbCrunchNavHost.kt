package io.keval.apps.numbcrunch

import java.io.IOException
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.keval.apps.numbcrunch.core.ui.theme.NumbCrunchOnWood
import io.keval.apps.numbcrunch.gameplay.R
import io.keval.apps.numbcrunch.gameplay.about.AboutScreen
import io.keval.apps.numbcrunch.gameplay.common.NumbCrunchScreenScaffold
import io.keval.apps.numbcrunch.gameplay.game.GameRoute
import io.keval.apps.numbcrunch.gameplay.home.HomeRoute
import io.keval.apps.numbcrunch.gameplay.info.GameplayInfoScreen
import io.keval.apps.numbcrunch.gameplay.navigation.ABOUT_ROUTE
import io.keval.apps.numbcrunch.gameplay.navigation.FIRST_RUN_TUTORIAL_ROUTE
import io.keval.apps.numbcrunch.gameplay.navigation.GAMEPLAY_INFO_ROUTE
import io.keval.apps.numbcrunch.gameplay.navigation.GAME_ROUTE
import io.keval.apps.numbcrunch.gameplay.navigation.HOME_ROUTE
import io.keval.apps.numbcrunch.gameplay.navigation.LAUNCH_GATE_ROUTE
import io.keval.apps.numbcrunch.gameplay.navigation.MANUAL_TUTORIAL_ROUTE
import io.keval.apps.numbcrunch.gameplay.navigation.PRIVACY_POLICY_ROUTE
import io.keval.apps.numbcrunch.gameplay.navigation.SETTINGS_ROUTE
import io.keval.apps.numbcrunch.gameplay.privacy.PrivacyPolicyScreen
import io.keval.apps.numbcrunch.gameplay.settings.SettingsRoute
import io.keval.apps.numbcrunch.gameplay.navigation.TUTORIAL_ROUTE
import io.keval.apps.numbcrunch.gameplay.tutorial.TutorialScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TRANSITION_DURATION_MS = 320

private fun slideInFromRight(): EnterTransition = slideInHorizontally(
    animationSpec = tween(TRANSITION_DURATION_MS),
    initialOffsetX = { fullWidth -> fullWidth },
)

private fun slideOutToLeft(): ExitTransition = slideOutHorizontally(
    animationSpec = tween(TRANSITION_DURATION_MS),
    targetOffsetX = { fullWidth -> -fullWidth / 4 },
)

private fun slideInFromLeft(): EnterTransition = slideInHorizontally(
    animationSpec = tween(TRANSITION_DURATION_MS),
    initialOffsetX = { fullWidth -> -fullWidth / 4 },
)

private fun slideOutToRight(): ExitTransition = slideOutHorizontally(
    animationSpec = tween(TRANSITION_DURATION_MS),
    targetOffsetX = { fullWidth -> fullWidth },
)

@Composable
fun NumbCrunchNavHost(
    navController: NavHostController,
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = LAUNCH_GATE_ROUTE,
        modifier = modifier,
    ) {
        composable(LAUNCH_GATE_ROUTE) {
            FirstRunLaunchGate(appContainer) { tutorialCompleted ->
                navController.navigate(if (tutorialCompleted) HOME_ROUTE else FIRST_RUN_TUTORIAL_ROUTE) {
                    popUpTo(LAUNCH_GATE_ROUTE) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        composable(
            route = HOME_ROUTE,
            enterTransition = { slideInFromLeft() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() },
        ) {
            HomeRoute(
                getWelcomeMessageUseCase = appContainer.getWelcomeMessageUseCase,
                onStartClick = { homeState ->
                    val secret = if (homeState.playerMode == io.keval.apps.numbcrunch.gameplay.home.PlayerMode.DOUBLE) {
                        homeState.friendSecretNumber
                    } else {
                        null
                    }
                    appContainer.saveFriendSecretUseCase(secret)
                    navController.navigate(GAME_ROUTE)
                },
                onTutorialClick = { navController.navigate(MANUAL_TUTORIAL_ROUTE) },
                onGameplayClick = { navController.navigate(GAMEPLAY_INFO_ROUTE) },
                onAboutClick = { navController.navigate(ABOUT_ROUTE) },
                onSettingsClick = { navController.navigate(SETTINGS_ROUTE) { launchSingleTop = true } },
            )
        }

        composable(
            route = TUTORIAL_ROUTE,
            arguments = listOf(navArgument("firstRun") {
                type = NavType.BoolType
                defaultValue = false
            }),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() },
        ) { entry ->
            val firstRun = entry.arguments?.getBoolean("firstRun") == true
            val scope = rememberCoroutineScope()
            var isCompleting by rememberSaveable { mutableStateOf(false) }
            var completionError by rememberSaveable { mutableStateOf(false) }
            val completeFirstRun = {
                if (firstRun && !isCompleting) {
                    scope.launch {
                        isCompleting = true
                        completionError = false
                        try {
                            appContainer.completeFirstRunTutorialUseCase()
                            navController.navigate(HOME_ROUTE) {
                                popUpTo(TUTORIAL_ROUTE) { inclusive = true }
                                launchSingleTop = true
                            }
                        } catch (_: IOException) {
                            completionError = true
                        } finally {
                            isCompleting = false
                        }
                    }
                }
            }
            TutorialScreen(
                onBack = {
                    if (firstRun) completeFirstRun() else navController.navigateUp()
                },
                onPlay = {
                    appContainer.saveFriendSecretUseCase(null)
                    navController.navigate(GAME_ROUTE) {
                        popUpTo(TUTORIAL_ROUTE) { inclusive = true }
                    }
                },
                isFirstRun = firstRun,
                onCompleteFirstRun = completeFirstRun,
                isCompletingFirstRun = isCompleting,
                firstRunCompletionError = completionError,
            )
        }

        composable(
            route = GAMEPLAY_INFO_ROUTE,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() },
        ) {
            GameplayInfoScreen(onBack = navController::navigateUp)
        }

        composable(
            route = ABOUT_ROUTE,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() },
        ) {
            AboutScreen(
                onBack = navController::navigateUp,
                onPrivacyPolicyClick = { navController.navigate(PRIVACY_POLICY_ROUTE) { launchSingleTop = true } },
            )
        }

        composable(
            route = PRIVACY_POLICY_ROUTE,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() },
        ) {
            PrivacyPolicyScreen(onBack = navController::navigateUp)
        }

        composable(
            route = SETTINGS_ROUTE,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() },
        ) {
            SettingsRoute(
                observeAudioSettings = appContainer.observeAudioSettingsUseCase,
                setMusicEnabled = appContainer.setMusicEnabledUseCase,
                setSoundEffectsEnabled = appContainer.setSoundEffectsEnabledUseCase,
                onBack = navController::navigateUp,
            )
        }

        composable(
            route = GAME_ROUTE,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() },
        ) {
            GameRoute(
                friendSecretNumber = appContainer.getFriendSecretUseCase(),
                onExitToHome = {
                    // Home is normally below Game; if not, open it so leaving can never strand the player.
                    if (!navController.popBackStack(HOME_ROUTE, inclusive = false)) {
                        navController.navigate(HOME_ROUTE) {
                            popUpTo(GAME_ROUTE) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                // The Game entry stays on the back stack, so its ViewModel (and round) survive Help.
                onHelp = { navController.navigate(GAMEPLAY_INFO_ROUTE) { launchSingleTop = true } },
                onSettings = { navController.navigate(SETTINGS_ROUTE) { launchSingleTop = true } },
            )
        }
    }
}

@Composable
private fun FirstRunLaunchGate(
    appContainer: AppContainer,
    onResolved: (Boolean) -> Unit,
) {
    var retry by remember { mutableIntStateOf(0) }
    var loadFailed by remember { mutableStateOf(false) }
    LaunchedEffect(retry) {
        try {
            onResolved(appContainer.observeFirstRunTutorialUseCase().first())
        } catch (_: IOException) {
            loadFailed = true
        }
    }
    NumbCrunchScreenScaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                stringResource(if (loadFailed) R.string.tutorial_load_error else R.string.tutorial_loading),
                color = NumbCrunchOnWood,
            )
            if (loadFailed) {
                Button(onClick = {
                    loadFailed = false
                    retry++
                }) {
                    Text(stringResource(R.string.tutorial_retry))
                }
            }
        }
    }
}
