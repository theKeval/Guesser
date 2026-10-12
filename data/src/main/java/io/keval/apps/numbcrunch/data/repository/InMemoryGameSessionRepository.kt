package io.keval.apps.numbcrunch.data.repository

import io.keval.apps.numbcrunch.domain.repository.GameSessionRepository

class InMemoryGameSessionRepository : GameSessionRepository {
    private var friendSecret: String? = null

    override fun saveFriendSecret(secret: String?) {
        friendSecret = secret
    }

    override fun getFriendSecret(): String? = friendSecret
}
