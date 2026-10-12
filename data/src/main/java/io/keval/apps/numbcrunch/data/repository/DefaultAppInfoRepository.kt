package io.keval.apps.numbcrunch.data.repository

import io.keval.apps.numbcrunch.domain.repository.AppInfoRepository

class DefaultAppInfoRepository : AppInfoRepository {
    override fun getWelcomeMessage(): String = "Welcome"
}
