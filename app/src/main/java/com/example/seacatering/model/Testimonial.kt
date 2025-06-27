package com.example.seacatering.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Testimonial(
    val customerName: String = "",
    val review: String = "",
    val rating: Int = 0,
    val user_uid: String = ""
) : Parcelable