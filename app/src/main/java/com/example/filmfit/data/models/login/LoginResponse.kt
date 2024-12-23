package com.example.filmfit.data.models.login


data class LoginResponse(
    val token: String, // додаємо токен
    val message: String
)