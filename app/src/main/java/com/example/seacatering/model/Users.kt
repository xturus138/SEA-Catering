package com.example.seacatering.model

data class Users(
    var id: Int,
    var name: String,
    var email: String,
    var password: String,
    var role: Role
)

enum class Role{
    USER,
    ADMIN
}
