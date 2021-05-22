package com.thekeval.guesser.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.thekeval.guesser.model.GuessModel

class GameViewModel: ViewModel() {

    private var _lstGuesses = MutableLiveData<MutableList<GuessModel>>()
    val lstGuesses: LiveData<MutableList<GuessModel>>
        get() = _lstGuesses

    private var _hideRV = MutableLiveData<Boolean>()
    val hideRV: LiveData<Boolean>
        get() = _hideRV

    init {
        // var abc = ArrayList<GuessModel>()
//        abc.add(GuessModel("234", "1R"))
//        abc.add(GuessModel("321", "2W, 1R"))
//        abc.add(GuessModel("432", "2W"))

        _lstGuesses.value = mutableListOf()

    }

    fun addGuess(number: String, remark: String) {
        _lstGuesses.value?.add(GuessModel(number, remark))
        _hideRV.value = false
    }

    fun resetGuesses() {
        _lstGuesses.value?.clear()
        _hideRV.value = true
    }

}