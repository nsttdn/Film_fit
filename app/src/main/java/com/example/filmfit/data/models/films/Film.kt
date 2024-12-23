package com.example.filmfit.data.models.films

import com.example.filmfit.data.models.popularFilms.MovieCollection
import com.example.filmfit.data.models.popularFilms.OriginalLanguage
import java.time.LocalDate

data class Film(
    val id: Long,
    val title: String,
    val posterPath: String?,
    val voteAverage: Double,
    val overview: String,
    val releaseDate: String,
    val director: String?
)