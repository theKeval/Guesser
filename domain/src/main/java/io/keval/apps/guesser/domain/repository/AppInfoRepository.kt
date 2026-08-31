package io.keval.apps.guesser.domain.repository

interface AppInfoRepository {
    fun getWelcomeMessage(): String
}
