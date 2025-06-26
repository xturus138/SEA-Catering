package com.example.seacatering.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MenuItem(
    val foodName: String = "",
    val freshTag: String = "",
    val nutrition: String = "",
    val restaurant: String = "",
    val foodImage: String = ""
) : Parcelable

