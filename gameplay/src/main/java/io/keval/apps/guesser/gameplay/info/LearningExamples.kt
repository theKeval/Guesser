package io.keval.apps.guesser.gameplay.info

import io.keval.apps.guesser.domain.game.GuessRules

internal object LearningExamples {
    const val SECRET = "245"
    val guesses = listOf("325", "542", "316", "245")
    val evaluations = guesses.map { GuessRules.evaluate(SECRET, it) }
}
