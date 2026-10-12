package io.keval.apps.numbcrunch.gameplay.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import io.keval.apps.numbcrunch.domain.settings.AudioSettings
import io.keval.apps.numbcrunch.gameplay.R

private const val MUSIC_VOLUME = 0.32f
private const val MUSIC_FADE_STEPS = 15
private const val MUSIC_FADE_STEP_MS = 100L

/**
 * App-wide audio: pooled sound effects plus a looping background track that plays only while
 * the app is in the foreground and music is enabled. Call from the main thread.
 */
class GuesserAudio(context: Context) : GameSoundPlayer {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val handler = Handler(Looper.getMainLooper())

    private val effectAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
    private val musicAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(effectAttributes)
        .build()
    private val soundIds = GameSound.entries.associateWith { soundPool.load(appContext, it.resId, 1) }

    private var soundEffectsEnabled = true
    // Unknown until the saved settings arrive, so a player who turned music off never hears it.
    private var musicEnabled = false
    private var inForeground = false
    private var musicPlayer: MediaPlayer? = null
    private var fadeStep = 0
    private val fadeIn = object : Runnable {
        override fun run() {
            val player = musicPlayer ?: return
            fadeStep++
            val volume = MUSIC_VOLUME * fadeStep / MUSIC_FADE_STEPS
            player.setVolume(volume, volume)
            if (fadeStep < MUSIC_FADE_STEPS) handler.postDelayed(this, MUSIC_FADE_STEP_MS)
        }
    }

    override fun play(sound: GameSound, pitch: Float) {
        if (!soundEffectsEnabled) return
        val id = soundIds[sound] ?: return
        soundPool.play(id, sound.volume, sound.volume, 1, 0, pitch.coerceIn(0.5f, 2f))
    }

    fun applySettings(settings: AudioSettings) {
        soundEffectsEnabled = settings.soundEffectsEnabled
        musicEnabled = settings.musicEnabled
        updateMusic()
    }

    fun setInForeground(foreground: Boolean) {
        inForeground = foreground
        updateMusic()
    }

    /** Frees the music player; it is recreated when music is needed again. */
    fun releaseMusic() {
        handler.removeCallbacks(fadeIn)
        musicPlayer?.release()
        musicPlayer = null
    }

    private fun updateMusic() {
        when {
            !musicEnabled -> releaseMusic()
            !inForeground -> {
                handler.removeCallbacks(fadeIn)
                musicPlayer?.takeIf { it.isPlaying }?.pause()
            }
            else -> startMusic()
        }
    }

    private fun startMusic() {
        val current = musicPlayer
        if (current?.isPlaying == true) return
        val player = current ?: MediaPlayer.create(
            appContext,
            R.raw.music_background,
            musicAttributes,
            audioManager.generateAudioSessionId(),
        )?.apply { isLooping = true } ?: return
        musicPlayer = player
        player.setVolume(0f, 0f)
        player.start()
        fadeStep = 0
        handler.removeCallbacks(fadeIn)
        handler.post(fadeIn)
    }
}
