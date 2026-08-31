package io.keval.apps.guesser.data.repository

import io.keval.apps.guesser.domain.repository.GameSessionRepository

class InMemoryGameSessionRepository : GameSessionRepository {
    private var friendSecret: String? = null

    override fun saveFriendSecret(secret: String?) {
        friendSecret = secret
    }

    override fun getFriendSecret(): String? = friendSecret
}
