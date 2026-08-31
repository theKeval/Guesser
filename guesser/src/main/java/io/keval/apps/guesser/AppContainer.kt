package io.keval.apps.guesser

import io.keval.apps.guesser.data.di.RepositoryModule
import io.keval.apps.guesser.domain.usecase.GetFriendSecretUseCase
import io.keval.apps.guesser.domain.usecase.GetWelcomeMessageUseCase
import io.keval.apps.guesser.domain.usecase.SaveFriendSecretUseCase

class AppContainer {
    private val appInfoRepository = RepositoryModule.createAppInfoRepository()
    private val gameSessionRepository = RepositoryModule.createGameSessionRepository()

    val getWelcomeMessageUseCase: GetWelcomeMessageUseCase =
        GetWelcomeMessageUseCase(appInfoRepository)

    val saveFriendSecretUseCase: SaveFriendSecretUseCase =
        SaveFriendSecretUseCase(gameSessionRepository)

    val getFriendSecretUseCase: GetFriendSecretUseCase =
        GetFriendSecretUseCase(gameSessionRepository)
}
