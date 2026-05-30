package com.thekeval.guesser.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.thekeval.guesser.domain.GameRules
import com.thekeval.guesser.model.GuessModel

class GameViewModel : ViewModel() {

    enum class GameState {
        NOT_STARTED,
        STARTED,
        WON,
        ABANDONED,
    }

    private var currentSecret = ""
    private var nextGuessId = 1L

    private val _guesses = MutableLiveData<List<GuessModel>>(emptyList())
    val guesses: LiveData<List<GuessModel>> = _guesses

    private val _gameState = MutableLiveData(GameState.NOT_STARTED)
    val gameState: LiveData<GameState> = _gameState

    fun isValidUnique3Digits(number: String): Boolean {
        return GameRules.isValidUniqueThreeDigitNumber(number)
    }

    fun startAutoGame() {
        currentSecret = GameRules.generateSecretNumber()
        _guesses.value = emptyList()
        _gameState.value = GameState.STARTED
    }

    fun startFriendGame(secret: String): Boolean {
        if (!isValidUnique3Digits(secret)) {
            return false
        }
        currentSecret = secret
        _guesses.value = emptyList()
        _gameState.value = GameState.STARTED
        return true
    }

    fun submitGuess(guess: String): String? {
        if (_gameState.value != GameState.STARTED) {
            return null
        }
        if (!isValidUnique3Digits(guess)) {
            return null
        }

        val remark = GameRules.buildRemark(currentSecret, guess)
        val nextList = _guesses.value.orEmpty() + GuessModel(
            id = nextGuessId++,
            number = guess,
            output = remark,
        )
        _guesses.value = nextList

        if (remark == GameRules.WINNER_REMARK) {
            _gameState.value = GameState.WON
        }
        return remark
    }

    fun revealAndAbandon() {
        if (_gameState.value == GameState.STARTED) {
            _gameState.value = GameState.ABANDONED
        }
    }

    fun resetGame() {
        _guesses.value = emptyList()
        _gameState.value = GameState.NOT_STARTED
    }

    fun getSecretNumber(): String {
        return currentSecret
    }

}