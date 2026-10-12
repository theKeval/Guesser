package io.keval.apps.numbcrunch

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NumbCrunchApp(
    appContainer: AppContainer,
) {
    val navController = rememberNavController()
    NumbCrunchNavHost(
        navController = navController,
        appContainer = appContainer,
    )
}
