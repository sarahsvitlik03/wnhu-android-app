package com.example.wnhu_android_app

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.Instant

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    var authError: String? by mutableStateOf(null)
        private set

    var isWorking: Boolean by mutableStateOf(false)
        private set

    fun signIn(activity: Activity, app: AppVariables, userData: UserData) {
        authError = null
        isWorking = true

        AuthManager.signIn(
            activity = activity,
            onSuccess = { profile ->
                userData.user = UserModel(
                    firstName = profile.firstName,
                    lastName = profile.lastName,
                    email = profile.email
                )

                viewModelScope.launch {
                    repository.checkIfUserExists(profile.email)
                        .onSuccess { exists ->
                            isWorking = false
                            if (exists) {
                                app.finishLogin()
                                userData.refreshSongRatings()
                            } else {
                                app.showProfileSetupPage()
                            }
                        }
                        .onFailure {
                            isWorking = false
                            authError = it.message ?: "Could not check your account."
                        }
                }
            },
            onError = { message ->
                isWorking = false
                authError = message
            }
        )
    }

    fun createUser(
        app: AppVariables,
        userData: UserData,
        firstName: String,
        lastName: String,
        age: Int,
        gender: String,
        isUNHStudent: Boolean
    ) {
        val email = userData.user.email.trim()
        if (email.isBlank()) {
            authError = "Sign in with Microsoft before creating a profile."
            return
        }

        authError = null
        isWorking = true

        viewModelScope.launch {
            repository.createUserMobile(
                CreateUserMobileRequest(
                    first = firstName.trim(),
                    last = lastName.trim(),
                    email = email,
                    age = age,
                    gender = gender,
                    isUNHStudent = isUNHStudent,
                    mobile_or_web = false,
                    is_admin = false,
                    dateCreated = Instant.now().toString()
                )
            ).onSuccess {
                userData.user = userData.user.copy(
                    firstName = firstName.trim(),
                    lastName = lastName.trim()
                )
                app.finishLogin()
                userData.refreshSongRatings()
                isWorking = false
            }.onFailure {
                isWorking = false
                authError = it.message ?: "Could not create your profile."
            }
        }
    }

    fun logout(app: AppVariables, userData: UserData) {
        authError = null
        isWorking = true

        viewModelScope.launch {
            repository.logoutMobile()
            AuthManager.signOut(
                onComplete = {
                    isWorking = false
                    userData.clearUser()
                    app.showLogin()
                },
                onError = {
                    isWorking = false
                    userData.clearUser()
                    app.showLogin()
                }
            )
        }
    }
}
