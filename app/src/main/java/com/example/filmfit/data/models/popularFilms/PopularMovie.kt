package com.example.filmfit.data.models.popularFilms

data class PopularMovie(
    val id: Int,
    val tmdbId: Int,
    val title: String,
    val voteAverage: Double,
    val voteCount: Int,
    val status: String,
    val releaseDate: String,
    val revenue: Long,
    val runtime: Int,
    val adult: Boolean,
    val backdropPath: String,
    val budget: Long,
    val homepage: String,
    val imdbId: String,
    val originalTitle: String,
    val overview: String,
    val popularity: Double,
    val posterPath: String,
    val tagline: String,
    val originalLanguage: String,
    val collection: MovieCollection,
    val keywords: List<String>
)
