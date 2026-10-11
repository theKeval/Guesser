package io.keval.apps.guesser.domain.settings

/** Player audio preferences. Both are on until the player turns them off. */
data class AudioSettings(
    val musicEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
)
