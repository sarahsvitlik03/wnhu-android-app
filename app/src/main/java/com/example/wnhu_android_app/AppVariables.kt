package com.example.wnhu_android_app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class AppVariables : ViewModel() {
    var isLoggedIn by mutableStateOf(false)
    var isGuest by mutableStateOf(false)
    var showLoginPage by mutableStateOf(true)
    var showProfileSetup by mutableStateOf(false)
    var selectedTab by mutableStateOf("stream")

    fun enterGuestMode() {
        isGuest = true
        isLoggedIn = false
        showLoginPage = false
        showProfileSetup = false
        selectedTab = "stream"
    }

    fun finishLogin() {
        isLoggedIn = true
        isGuest = false
        showLoginPage = false
        showProfileSetup = false
        selectedTab = "stream"
    }

    fun showProfileSetupPage() {
        showLoginPage = false
        showProfileSetup = true
    }

    fun showLogin() {
        isLoggedIn = false
        isGuest = false
        showLoginPage = true
        showProfileSetup = false
        selectedTab = "stream"
    }
}
