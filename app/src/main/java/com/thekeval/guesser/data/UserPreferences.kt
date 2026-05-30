package com.thekeval.guesser.data

import android.content.Context

object UserPreferences {
    private const val PREFS_NAME = "guesser_prefs"
    private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
    private const val KEY_SOUND_ENABLED = "sound_enabled"
    private const val KEY_GUESS_SOUND_ENABLED = "guess_sound_enabled"
    private const val KEY_KEYPAD_SOUND_ENABLED = "keypad_sound_enabled"

    fun isVibrationEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
    }

    fun setVibrationEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
    }

    fun isSoundEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SOUND_ENABLED, true)
    }

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    fun isGuessSoundEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.contains(KEY_GUESS_SOUND_ENABLED)) {
            return prefs.getBoolean(KEY_GUESS_SOUND_ENABLED, true)
        }
        return prefs.getBoolean(KEY_SOUND_ENABLED, true)
    }

    fun setGuessSoundEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_GUESS_SOUND_ENABLED, enabled).apply()
    }

    fun isKeypadSoundEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.contains(KEY_KEYPAD_SOUND_ENABLED)) {
            return prefs.getBoolean(KEY_KEYPAD_SOUND_ENABLED, true)
        }
        return prefs.getBoolean(KEY_SOUND_ENABLED, true)
    }

    fun setKeypadSoundEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_KEYPAD_SOUND_ENABLED, enabled).apply()
    }
}

