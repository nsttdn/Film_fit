package com.example.filmfit.data.models.folowing

import com.example.filmfit.data.models.users.Wishlist

data class UserFollowing(
    val username: String,
    val whishlist: Wishlist,
    val followersCount: Int,
    val followingCount: Int
)