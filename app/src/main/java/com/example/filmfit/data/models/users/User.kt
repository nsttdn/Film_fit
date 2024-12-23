package com.example.filmfit.data.models.users

data class User(
    val id: Long,
    val username: String,
    val whishlist: Wishlist,
    val followersCount: Int,
    val followingCount: Int
)

