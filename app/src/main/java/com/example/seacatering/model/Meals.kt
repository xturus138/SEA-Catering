package com.example.seacatering.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Meals(
    val id: String = "",
    val name: String = "",
    val price: String = "",
    val descriptionTitle: String = "",
    val details: List<String> = emptyList(),
    val imageResId: String = "",
    val menuList: List<MenuItem> = emptyList()
) : Parcelable



