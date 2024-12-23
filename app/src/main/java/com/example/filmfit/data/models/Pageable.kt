package com.example.filmfit.data.models

data class Pageable(
    val page: Int = 0,
    val size: Int = 10,
    val sort: List<String> = listOf("popularity") // Or change the sorting criteria as needed
)
