package io.keval.apps.guesser.gameplay.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.keval.apps.guesser.core.ui.theme.GuesserOnWood
import io.keval.apps.guesser.gameplay.common.GuesserBackButton
import io.keval.apps.guesser.gameplay.common.GuesserScreenScaffold

@Composable
fun GameScreen(
    friendSecretNumber: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GuesserScreenScaffold(modifier = modifier) {
        GuesserBackButton(onClick = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Game",
                style = MaterialTheme.typography.headlineLarge,
                color = GuesserOnWood,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = if (friendSecretNumber.isNullOrBlank()) {
                    "Single-player placeholder screen. Gameplay UI will be designed next."
                } else {
                    "Double-player placeholder screen. Secret number saved for later validation."
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = GuesserOnWood,
                textAlign = TextAlign.Center,
            )
        }
    }
}
