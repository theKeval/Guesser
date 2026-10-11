package io.keval.apps.guesser

import android.content.Context
import io.keval.apps.guesser.data.di.RepositoryModule
import io.keval.apps.guesser.domain.usecase.CompleteFirstRunTutorialUseCase
import io.keval.apps.guesser.domain.usecase.GetFriendSecretUseCase
import io.keval.apps.guesser.domain.usecase.GetWelcomeMessageUseCase
import io.keval.apps.guesser.domain.usecase.ObserveFirstRunTutorialUseCase
import io.keval.apps.guesser.domain.usecase.SaveFriendSecretUseCase

class AppContainer(context: Context) {
    private val appInfoRepository = RepositoryModule.createAppInfoRepository()
    private val gameSessionRepository = RepositoryModule.createGameSessionRepository()
    private val tutorialRepository = RepositoryModule.createTutorialRepository(context)

    val getWelcomeMessageUseCase: GetWelcomeMessageUseCase =
        GetWelcomeMessageUseCase(appInfoRepository)

    val saveFriendSecretUseCase: SaveFriendSecretUseCase =
        SaveFriendSecretUseCase(gameSessionRepository)

    val getFriendSecretUseCase: GetFriendSecretUseCase =
        GetFriendSecretUseCase(gameSessionRepository)

    val observeFirstRunTutorialUseCase: ObserveFirstRunTutorialUseCase =
        ObserveFirstRunTutorialUseCase(tutorialRepository)

    val completeFirstRunTutorialUseCase: CompleteFirstRunTutorialUseCase =
        CompleteFirstRunTutorialUseCase(tutorialRepository)
}
