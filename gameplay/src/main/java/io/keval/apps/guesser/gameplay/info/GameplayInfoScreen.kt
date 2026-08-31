package io.keval.apps.guesser.gameplay.info

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.keval.apps.guesser.gameplay.common.PlaceholderScreen

@Composable
fun GameplayInfoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = "Gameplay",
        body = "This is the gameplay information placeholder screen. We will add full rules and examples here.",
        onBack = onBack,
        modifier = modifier,
    )
}
