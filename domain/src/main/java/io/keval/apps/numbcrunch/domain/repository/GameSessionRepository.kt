package io.keval.apps.numbcrunch.domain.repository

interface GameSessionRepository {
    fun saveFriendSecret(secret: String?)
    fun getFriendSecret(): String?
}
