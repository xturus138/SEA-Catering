package com.example.seacatering.model

data class Meals(
    val name: String = "",
    val price: String = "",
    val descriptionTitle: String = "",
    val details: List<String> = emptyList(),
    val imageResId: String = ""
)

