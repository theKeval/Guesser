package io.keval.apps.guesser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.keval.apps.guesser.core.ui.theme.GuesserTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GuesserTheme {
                GuesserApp(
                    appContainer = (application as GuesserApplication).appContainer,
                )
            }
        }
    }
}
