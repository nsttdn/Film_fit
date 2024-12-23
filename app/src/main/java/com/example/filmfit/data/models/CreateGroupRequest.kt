package com.example.filmfit.data.models

data class CreateGroupRequest(
    val name: String,
    val userIds: List<Int>
)
