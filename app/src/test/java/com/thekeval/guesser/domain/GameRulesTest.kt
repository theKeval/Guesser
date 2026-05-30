package com.thekeval.guesser.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameRulesTest {

    @Test
    fun `valid unique number passes validation`() {
        assertTrue(GameRules.isValidUniqueThreeDigitNumber("358"))
    }

    @Test
    fun `duplicate digits fail validation`() {
        assertFalse(GameRules.isValidUniqueThreeDigitNumber("331"))
    }

    @Test
    fun `non-digit input fails validation`() {
        assertFalse(GameRules.isValidUniqueThreeDigitNumber("3a1"))
    }

    @Test
    fun `winner remark when all digits and positions match`() {
        assertEquals("Winner", GameRules.buildRemark(secret = "245", guess = "245"))
    }

    @Test
    fun `correctly marks right and wrong positions`() {
        assertEquals("1R, 1W", GameRules.buildRemark(secret = "245", guess = "325"))
    }

    @Test
    fun `returns smyle when no overlap exists`() {
        assertEquals("SMYLE", GameRules.buildRemark(secret = "358", guess = "147"))
    }
}

