package io.keval.apps.guesser.gameplay.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FitToHeightTest {

    @Test
    fun `content that already fits keeps its natural size`() {
        assertEquals(1f, largestFittingScale(minScale = 0.8f) { true }, 0f)
    }

    @Test
    fun `overflowing content shrinks only as far as needed`() {
        // Content fits once scaled to 0.9 or below.
        val scale = largestFittingScale(minScale = 0.8f) { it <= 0.9f }
        assertTrue(scale <= 0.9f)
        assertTrue("scale $scale should be close to the largest fitting value", scale > 0.89f)
    }

    @Test
    fun `content that cannot fit at the floor keeps natural size and scrolls`() {
        assertEquals(1f, largestFittingScale(minScale = 0.8f) { it <= 0.5f }, 0f)
    }

    @Test
    fun `readability floor keeps body text at 13 sp and honors large font choices`() {
        assertEquals(13f / 16f, minimumFitScale(fontScale = 1f), 0.0001f)
        // Larger font settings may be reduced by at most a quarter.
        assertEquals(0.75f, minimumFitScale(fontScale = 2f), 0.0001f)
        // Small font settings are never zoomed further below the floor.
        assertEquals(1f, minimumFitScale(fontScale = 0.8f), 0.0001f)
    }
}
