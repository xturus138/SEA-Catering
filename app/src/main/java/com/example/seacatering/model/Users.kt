package com.example.seacatering.model

data class Users(
    var name: String,
    var email: String,
    var role: Role
)

enum class Role{
    USER,
    ADMIN
}
