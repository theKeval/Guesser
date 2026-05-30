package com.thekeval.guesser

import com.thekeval.guesser.domain.GameRules
import org.junit.Test
import org.junit.Assert.assertEquals

class ExampleUnitTest {
    @Test
    fun remark_for_partial_match_is_correct() {
        assertEquals("1W", GameRules.buildRemark(secret = "245", guess = "321"))
    }
}