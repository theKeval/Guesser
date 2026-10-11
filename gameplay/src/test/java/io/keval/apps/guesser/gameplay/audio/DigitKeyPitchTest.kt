package io.keval.apps.guesser.gameplay.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DigitKeyPitchTest {
    @Test
    fun `digits climb in pitch from zero to nine within the SoundPool rate range`() {
        val pitches = ('0'..'9').map(::digitKeyPitch)

        assertEquals(pitches.sorted(), pitches)
        assertEquals(pitches.size, pitches.toSet().size)
        assertTrue(pitches.all { it in 0.5f..2f })
        assertEquals(0.5f, digitKeyPitch('0'), 0.0001f)
        assertEquals(1f, digitKeyPitch('5'), 0.0001f)
    }

    @Test
    fun `non digits play at the natural pitch`() {
        assertEquals(1f, digitKeyPitch('x'), 0f)
    }
}
