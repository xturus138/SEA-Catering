package com.example.seacatering.model

data class Users(
    var uid: String = "",
    var name: String = "",
    var email: String = "",
    var address: String = "",
    var noHp: String = "",
    var role: Role = Role.USER,
    var profileImageUrl: String = ""
)

enum class Role{
    USER,
    ADMIN
}