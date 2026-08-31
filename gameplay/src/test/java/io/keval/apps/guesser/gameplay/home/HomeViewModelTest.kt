package io.keval.apps.guesser.gameplay.home

import io.keval.apps.guesser.domain.repository.AppInfoRepository
import io.keval.apps.guesser.domain.usecase.GetWelcomeMessageUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeViewModelTest {

    private val viewModel = HomeViewModel(
        GetWelcomeMessageUseCase(
            object : AppInfoRepository {
                override fun getWelcomeMessage(): String = "Welcome"
            },
        ),
    )

    @Test
    fun `start keeps the same validation message shown while typing`() {
        viewModel.onPlayerModeSelected(PlayerMode.DOUBLE)
        viewModel.onFriendSecretNumberChanged("11")

        val messageWhileTyping = viewModel.uiState.value.secretValidationMessage
        val canStart = viewModel.canStartGame()

        assertFalse(canStart)
        assertEquals("Digits must be unique", messageWhileTyping)
        assertEquals(messageWhileTyping, viewModel.uiState.value.secretValidationMessage)
    }

    @Test
    fun `empty secret is quiet while typing but required on start`() {
        viewModel.onPlayerModeSelected(PlayerMode.DOUBLE)
        viewModel.onFriendSecretNumberChanged("")

        assertEquals(null, viewModel.uiState.value.secretValidationMessage)
        assertFalse(viewModel.canStartGame())
        assertEquals("Enter exactly 3 digits", viewModel.uiState.value.secretValidationMessage)
    }

    @Test
    fun `three unique digits can start double player game`() {
        viewModel.onPlayerModeSelected(PlayerMode.DOUBLE)
        viewModel.onFriendSecretNumberChanged("258")

        assertTrue(viewModel.canStartGame())
        assertEquals(null, viewModel.uiState.value.secretValidationMessage)
    }
}
