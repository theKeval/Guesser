package io.keval.apps.numbcrunch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.keval.apps.numbcrunch.core.ui.theme.GuesserTheme
import io.keval.apps.numbcrunch.gameplay.audio.LocalGameSoundPlayer
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val appContainer: AppContainer
        get() = (application as GuesserApplication).appContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val audio = appContainer.audio
        // Background music follows the saved settings only while the app is visible.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                audio.setInForeground(true)
                try {
                    appContainer.observeAudioSettingsUseCase().collect(audio::applySettings)
                } finally {
                    audio.setInForeground(false)
                }
            }
        }
        setContent {
            CompositionLocalProvider(LocalGameSoundPlayer provides audio) {
                GuesserTheme {
                    GuesserApp(appContainer = appContainer)
                }
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing) appContainer.audio.releaseMusic()
        super.onDestroy()
    }
}
