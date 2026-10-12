package io.keval.apps.numbcrunch.gameplay.home

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FriendSecretEntryTest {

    @Test
    fun `a digit can be added until the secret is full`() {
        assertTrue(canAddSecretDigit("", '5'))
        assertTrue(canAddSecretDigit("57", '0'))
        assertFalse(canAddSecretDigit("573", '1'))
    }

    @Test
    fun `used digits and non digits are rejected so the secret stays valid`() {
        assertFalse(canAddSecretDigit("57", '5'))
        assertFalse(canAddSecretDigit("5", 'x'))
        assertFalse(canAddSecretDigit("5", '٣'))
    }
}
