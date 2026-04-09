package com.example.wnhu_android_app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class AppVariables : ViewModel() {
    var isLoggedIn by mutableStateOf(false)
    var isGuest by mutableStateOf(false)
    var showLoginPage by mutableStateOf(true)
}
