package io.keval.apps.guesser.data.repository

import io.keval.apps.guesser.domain.repository.AppInfoRepository

class DefaultAppInfoRepository : AppInfoRepository {
    override fun getWelcomeMessage(): String = "Welcom"
}
