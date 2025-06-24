package com.example.seacatering.model

data class Users(
    var name: String = "",
    var email: String = "",
    var address: String = "",
    var noHp: String = "",
    var role: Role = Role.USER
)

enum class Role{
    USER,
    ADMIN
}
