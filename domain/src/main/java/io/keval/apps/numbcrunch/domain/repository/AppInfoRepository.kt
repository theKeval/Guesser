package io.keval.apps.numbcrunch.domain.repository

interface AppInfoRepository {
    fun getWelcomeMessage(): String
}
