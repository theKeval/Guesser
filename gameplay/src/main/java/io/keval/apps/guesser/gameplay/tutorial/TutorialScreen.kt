package io.keval.apps.guesser.gameplay.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.keval.apps.guesser.gameplay.common.PlaceholderScreen

@Composable
fun TutorialScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = "Tutorial",
        body = "This is the tutorial placeholder screen. We will add the full learning flow here next.",
        onBack = onBack,
        modifier = modifier,
    )
}
