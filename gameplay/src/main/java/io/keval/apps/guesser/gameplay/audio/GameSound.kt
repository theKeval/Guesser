package io.keval.apps.guesser.gameplay.audio

import android.view.HapticFeedbackConstants
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import io.keval.apps.guesser.gameplay.R
import kotlin.math.pow

/** Short effects bundled in `res/raw`, with each one's playback volume. */
enum class GameSound(@RawRes val resId: Int, val volume: Float) {
    /** Generic button tap. */
    Tap(R.raw.sfx_tap, 0.55f),

    /** Keypad digit pluck; pitched per digit with [digitKeyPitch]. */
    Key(R.raw.sfx_key, 0.6f),
    Delete(R.raw.sfx_delete, 0.55f),

    /** One guess tile flipping over during analysis. */
    Flip(R.raw.sfx_flip, 0.45f),

    /** R/W remark revealed. */
    Clue(R.raw.sfx_clue, 0.6f),
    Smyle(R.raw.sfx_smyle, 0.75f),
    Applause(R.raw.sfx_applause, 0.85f),
}

interface GameSoundPlayer {
    /** [pitch] is a playback rate between 0.5 and 2.0. */
    fun play(sound: GameSound, pitch: Float = 1f)

    companion object {
        val Silent: GameSoundPlayer = object : GameSoundPlayer {
            override fun play(sound: GameSound, pitch: Float) = Unit
        }
    }
}

/** Provided by the activity; previews and tests fall back to silence. */
val LocalGameSoundPlayer = staticCompositionLocalOf { GameSoundPlayer.Silent }

// Semitones from the C6 sample: digits climb a C-major pentatonic scale from C5 (0) to A6 (9).
private val DigitSemitones = intArrayOf(-12, -10, -8, -5, -3, 0, 2, 4, 7, 9)

/** Playback rate that gives each keypad digit its own note. */
fun digitKeyPitch(digit: Char): Float {
    val index = digit - '0'
    if (index !in DigitSemitones.indices) return 1f
    return 2.0.pow(DigitSemitones[index] / 12.0).toFloat()
}

/** Returns a callback that plays [sound] with a light tap haptic. */
@Composable
fun rememberTapFeedback(sound: GameSound = GameSound.Tap, pitch: Float = 1f): () -> Unit {
    val player = LocalGameSoundPlayer.current
    val view = LocalView.current
    return remember(player, view, sound, pitch) {
        {
            player.play(sound, pitch)
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }
}
