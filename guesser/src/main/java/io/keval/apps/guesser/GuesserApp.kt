package io.keval.apps.guesser

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController

@Composable
fun GuesserApp(
    appContainer: AppContainer,
) {
    val navController = rememberNavController()
    GuesserNavHost(
        navController = navController,
        appContainer = appContainer,
    )
}
