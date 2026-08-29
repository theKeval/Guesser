package io.keval.apps.guesser

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.keval.apps.guesser.gameplay.about.AboutScreen
import io.keval.apps.guesser.gameplay.game.GameScreen
import io.keval.apps.guesser.gameplay.home.HomeRoute
import io.keval.apps.guesser.gameplay.info.GameplayInfoScreen
import io.keval.apps.guesser.gameplay.navigation.ABOUT_ROUTE
import io.keval.apps.guesser.gameplay.navigation.GAMEPLAY_INFO_ROUTE
import io.keval.apps.guesser.gameplay.navigation.GAME_ROUTE
import io.keval.apps.guesser.gameplay.navigation.HOME_ROUTE
import io.keval.apps.guesser.gameplay.navigation.TUTORIAL_ROUTE
import io.keval.apps.guesser.gameplay.tutorial.TutorialScreen

private const val TRANSITION_DURATION_MS = 320

private fun slideInFromRight(): EnterTransition {
    return slideInHorizontally(
        animationSpec = tween(TRANSITION_DURATION_MS),
        initialOffsetX = { fullWidth -> fullWidth },
    )
}

private fun slideOutToLeft(): ExitTransition {
    return slideOutHorizontally(
        animationSpec = tween(TRANSITION_DURATION_MS),
        targetOffsetX = { fullWidth -> -fullWidth / 4 },
    )
}

private fun slideInFromLeft(): EnterTransition {
    return slideInHorizontally(
        animationSpec = tween(TRANSITION_DURATION_MS),
        initialOffsetX = { fullWidth -> -fullWidth / 4 },
    )
}

private fun slideOutToRight(): ExitTransition {
    return slideOutHorizontally(
        animationSpec = tween(TRANSITION_DURATION_MS),
        targetOffsetX = { fullWidth -> fullWidth },
    )
}

@Composable
fun GuesserNavHost(
    navController: NavHostController,
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE,
        modifier = modifier,
    ) {
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
                    val secret = if (homeState.playerMode == io.keval.apps.guesser.gameplay.home.PlayerMode.DOUBLE) {
                        homeState.friendSecretNumber
                    } else {
                        null
                    }
                    appContainer.saveFriendSecretUseCase(secret)
                    navController.navigate(GAME_ROUTE)
                },
                onTutorialClick = { navController.navigate(TUTORIAL_ROUTE) },
                onGameplayClick = { navController.navigate(GAMEPLAY_INFO_ROUTE) },
                onAboutClick = { navController.navigate(ABOUT_ROUTE) },
            )
        }

        composable(
            route = TUTORIAL_ROUTE,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() },
        ) {
            TutorialScreen(onBack = navController::navigateUp)
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
            AboutScreen(onBack = navController::navigateUp)
        }

        composable(
            route = GAME_ROUTE,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() },
        ) {
            GameScreen(
                friendSecretNumber = appContainer.getFriendSecretUseCase(),
                onBack = navController::navigateUp,
            )
        }
    }
}
