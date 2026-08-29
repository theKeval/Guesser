package io.keval.apps.guesser.gameplay.about

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.keval.apps.guesser.gameplay.common.PlaceholderScreen

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = "About",
        body = "This is the about placeholder screen. We will add the final about content and credits here.",
        onBack = onBack,
        modifier = modifier,
    )
}
