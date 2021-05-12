package com.thekeval.guesser.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
class GuessModel (
    val guessedNumber: String,
    val remark: String
    ) : Parcelable {

    private var _id: Long = 0L
    val id: Long
        get() = _id

    private var _number: String = guessedNumber
    val number : String
        get() = _number

    private var _output: String = remark
    val output: String
        get() = _output

}