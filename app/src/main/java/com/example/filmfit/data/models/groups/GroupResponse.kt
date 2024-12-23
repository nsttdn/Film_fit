package com.example.filmfit.data.models.groups

import com.example.filmfit.data.models.users.User


data class GroupResponse(
    val id: Long,
    val name: String,
    val users: List<User>
)
