package io.keval.apps.guesser.domain.repository

interface GameSessionRepository {
    fun saveFriendSecret(secret: String?)
    fun getFriendSecret(): String?
}
