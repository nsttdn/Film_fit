package com.example.filmfit.data.models.users

import com.example.filmfit.data.models.films.Film

data class WishlistResponse(
    val id: Long,
    val films: List<Film>
)
