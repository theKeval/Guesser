package io.keval.apps.guesser.gameplay.common

import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClueHighlightTest {

    private fun styledWords(text: String) = highlightClueWords(text).spanStyles
        .map { text.substring(it.start, it.end) to it.item }

    @Test
    fun `right and wrong clue words are bold and colored without changing the text`() {
        val text = "The clue showing 1R or 2R means the RIGHT place. 1W, 2W or 3W means the WRONG place."
        val highlighted = highlightClueWords(text)
        val styled = styledWords(text)

        assertEquals(text, highlighted.text)
        assertEquals(setOf("1R", "2R", "RIGHT", "1W", "2W", "3W", "WRONG"), styled.map { it.first }.toSet())
        assertTrue(styled.all { it.second.fontWeight == FontWeight.Bold })

        val right = styled.filter { it.first in setOf("1R", "2R", "RIGHT") }.map { it.second.color }.toSet()
        val wrong = styled.filter { it.first in setOf("1W", "2W", "3W", "WRONG") }.map { it.second.color }.toSet()
        assertEquals(1, right.size)
        assertEquals(1, wrong.size)
        assertTrue(right != wrong)
    }

    @Test
    fun `only whole words are highlighted`() {
        assertTrue(styledWords("BRIGHT, WRONGLY, 11R, 3WX, right and wrong").isEmpty())
    }

    @Test
    fun `SMYLE is bold teal and its meaning bold yellow`() {
        val text = "the player is rewarded with SMYLE, \"Simply Make Your Life Easy\", and ignore these 3 digits."
        val styled = styledWords(text)

        assertEquals(text, highlightClueWords(text).text)
        assertEquals(listOf("SMYLE", "Simply Make Your Life Easy"), styled.map { it.first })
        assertTrue(styled.all { it.second.fontWeight == FontWeight.Bold })
        assertTrue(styled[0].second.color != styled[1].second.color)
    }
}
