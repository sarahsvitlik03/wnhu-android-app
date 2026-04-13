package com.example.wnhu_android_app
data class UserModel(
    var firstName: String = "",
    var lastName: String = "",
    var email: String = ""
) {
    val fullName: String
        get() = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
}
