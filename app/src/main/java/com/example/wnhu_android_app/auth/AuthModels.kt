package com.example.wnhu_android_app

import kotlinx.serialization.Serializable

@Serializable
data class CheckUserRequest(
    val email: String
)

@Serializable
data class CreateUserMobileRequest(
    val first: String,
    val last: String,
    val email: String,
    val age: Int,
    val gender: String,
    val isUNHStudent: Boolean,
    val mobile_or_web: Boolean,
    val dateCreated: String,
    val is_admin: Boolean
)

data class MicrosoftAccountProfile(
    val email: String,
    val firstName: String = "",
    val lastName: String = ""
)
