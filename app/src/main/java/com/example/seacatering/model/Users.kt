package com.example.seacatering.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Users(
    var uid: String = "",
    var name: String = "",
    var email: String = "",
    var address: String = "",
    var noHp: String = "",
    var role: Role = Role.USER,
    var profileImageUrl: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
) : Parcelable

enum class Role{
    USER,
    ADMIN
}