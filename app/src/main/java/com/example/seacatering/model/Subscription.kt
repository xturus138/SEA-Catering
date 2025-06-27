package com.example.seacatering.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Subscription(
    val documentId: String = "",
    val allergies: String = "",
    val delivery_days: String = "",
    val end_date: Timestamp? = null,
    val meal_type: String = "",
    val pause_periode_end: Timestamp? = null,
    val pause_periode_start: Timestamp? = null,
    val phone_number: String = "",
    val plan_id: String = "",
    val plan_name: String = "",
    val status: String = "",
    val user_uid: String = "",
    val total_price: Double = 0.0
) : Parcelable