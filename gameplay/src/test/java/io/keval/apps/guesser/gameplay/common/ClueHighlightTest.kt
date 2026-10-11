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

    @Test
    fun `game details highlight the goal, rules, modes, start and swipe hint without changing text`() {
        val text = "This game is to find a hidden number with 3 unique digits from 0 to 9 (no repeat) by guessing. " +
            "Each guess opens a clue to find the hidden number. \n\nIn single player mode, the player would need " +
            "to find the hidden number by the game. In double player mode, one player will hide the number.\n\n" +
            "Once you are ready to play, select start to begin the game.\n\nSwipe left for play details."
        val highlighted = highlightGameDetails(text)
        val styled = highlighted.spanStyles.map { text.substring(it.start, it.end) }

        assertEquals(text, highlighted.text)
        assertEquals(3, styled.count { it == "hidden number" })
        assertTrue(styled.containsAll(listOf("3 unique digits", "0 to 9", "(no repeat)", "clue", "start", "Swipe left")))
        assertTrue(styled.containsAll(listOf("single player", "double player", "Swipe left for play details.")))
        // Words that only contain a keyword are left alone.
        assertTrue(highlightGameDetails("clues restart starts").spanStyles.isEmpty())
    }
}
