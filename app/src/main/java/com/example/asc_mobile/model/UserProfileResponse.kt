package com.example.asc_mobile.model

data class UserProfileResponse(
    val name: String,
    val login: String,
    val phone: String,
    val role: String,
    val id: String
)