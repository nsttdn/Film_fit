package com.example.filmfit.data.models.films

data class FilmResponse(
    val totalElements: Int,
    val totalPages: Int,
    val content: List<Film>
)