package io.keval.apps.guesser

import android.content.Context
import io.keval.apps.guesser.data.di.RepositoryModule
import io.keval.apps.guesser.domain.usecase.CompleteFirstRunTutorialUseCase
import io.keval.apps.guesser.domain.usecase.GetFriendSecretUseCase
import io.keval.apps.guesser.domain.usecase.GetWelcomeMessageUseCase
import io.keval.apps.guesser.domain.usecase.ObserveAudioSettingsUseCase
import io.keval.apps.guesser.domain.usecase.ObserveFirstRunTutorialUseCase
import io.keval.apps.guesser.domain.usecase.SaveFriendSecretUseCase
import io.keval.apps.guesser.domain.usecase.SetMusicEnabledUseCase
import io.keval.apps.guesser.domain.usecase.SetSoundEffectsEnabledUseCase
import io.keval.apps.guesser.gameplay.audio.GuesserAudio

class AppContainer(context: Context) {
    private val appInfoRepository = RepositoryModule.createAppInfoRepository()
    private val gameSessionRepository = RepositoryModule.createGameSessionRepository()
    private val tutorialRepository = RepositoryModule.createTutorialRepository(context)
    private val settingsRepository = RepositoryModule.createSettingsRepository(context)

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

    val observeAudioSettingsUseCase: ObserveAudioSettingsUseCase =
        ObserveAudioSettingsUseCase(settingsRepository)

    val setMusicEnabledUseCase: SetMusicEnabledUseCase =
        SetMusicEnabledUseCase(settingsRepository)

    val setSoundEffectsEnabledUseCase: SetSoundEffectsEnabledUseCase =
        SetSoundEffectsEnabledUseCase(settingsRepository)

    val audio: GuesserAudio by lazy { GuesserAudio(context) }
}
