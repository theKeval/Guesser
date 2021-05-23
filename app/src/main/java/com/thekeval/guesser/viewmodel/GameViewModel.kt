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


    fun generateRemark(pickedNumber: String, guessedNumber: String): String {
        var remark = ""

        val abc = pickedNumber.toCharArray()
        val xyz = guessedNumber.toCharArray()

        var containCount = 0
        var rCount = 0
        var wCount = 0

        for (i in xyz) {
            if (abc.contains(i)) {
                containCount++
            }
        }

        xyz.forEachIndexed { index, c ->
            if (abc[index] == c) {
                rCount++
            }
        }

        if (containCount == 0) {
            remark = "SMYLE!"   // \nSimply Make Your Life Easy
        } else if (rCount == 3) {
            // Winner
            remark = "Winner"
        } else if (rCount == containCount) {
            remark = rCount.toString() + "R"
        } else if (rCount == 0 && containCount > 0) {
            remark = containCount.toString() + "W"
        } else {
            remark = rCount.toString() + "R, " + (containCount - rCount).toString() + "W"
        }

        return remark
    }

    fun isNotUnique3D(guessedNumber: String): Boolean {
        var a = ""
        var b = ""
        var c = ""

        guessedNumber.forEachIndexed { index, char ->
            if (index == 0)
                a = char.toString()
            if (index == 1)
                b = char.toString()
            if (index == 2)
                c = char.toString()
        }

        if (a == b || b == c || c == a || guessedNumber.length != 3) {
            return true
        }

        return false
    }

    fun autoGen3D(): String {
        var a = (0..9).random()
        var b = (0..9).random()
        var c = (0..9).random()
        while (a == b || b == c || c == a) {

            if (a == b) {
                b = (0..9).random()
            }
            if (a == c) {
                c = (0..9).random()
            }
            if (b == c) {
                c = (0..9).random()
            }
        }
        return a.toString() + b.toString() + c.toString()
    }


    fun generate3UniqueDigits(): String {
        var str = ""

        while (str.length <= 3) {
            val x = xyz(str)
            str += x
        }

        return str
    }

    fun xyz(str: String) : String {
        val a = (0..9).random()
        if (str.contains(a.toChar())){
            xyz(str)
        }

        return a.toString()
    }

}