package com.thekeval.guesser.domain

import kotlin.random.Random

object GameRules {

    const val WINNER_REMARK = "Winner"
    const val SMYLE_REMARK = "SMYLE"

    fun isValidUniqueThreeDigitNumber(value: String): Boolean {
        return value.length == 3 && value.all(Char::isDigit) && value.toSet().size == 3
    }

    fun generateSecretNumber(random: Random = Random.Default): String {
        return (0..9)
            .shuffled(random)
            .take(3)
            .joinToString(separator = "")
    }

    fun buildRemark(secret: String, guess: String): String {
        val right = guess.indices.count { index -> guess[index] == secret[index] }
        val overlap = guess.count { digit -> secret.contains(digit) }
        val wrong = overlap - right

        return when {
            right == 3 -> WINNER_REMARK
            overlap == 0 -> SMYLE_REMARK
            wrong == 0 -> "${right}R"
            right == 0 -> "${wrong}W"
            else -> "${right}R, ${wrong}W"
        }
    }
}

