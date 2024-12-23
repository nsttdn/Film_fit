package com.example.filmfit.data.models.popularFilms

data class MovieCollection(
    val id: Int,
    val tmdbId: Int,
    val name: String,
    val overview: String,
    val posterPath: String,
    val backdropPath: String
)
