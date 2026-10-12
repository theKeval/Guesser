package io.keval.apps.numbcrunch.domain.usecase

import io.keval.apps.numbcrunch.domain.repository.AppInfoRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class GetWelcomeMessageUseCaseTest {

    @Test
    fun `returns repository welcome message`() {
        val repository = object : AppInfoRepository {
            override fun getWelcomeMessage(): String = "Welcom"
        }

        val useCase = GetWelcomeMessageUseCase(repository)

        assertEquals("Welcom", useCase())
    }
}
