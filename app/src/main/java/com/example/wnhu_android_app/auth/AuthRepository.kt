package com.example.wnhu_android_app

class AuthRepository {
    suspend fun checkIfUserExists(email: String): Result<Boolean> = AuthApi.checkIfUserExists(email)

    suspend fun createUserMobile(request: CreateUserMobileRequest): Result<Unit> = AuthApi.createUserMobile(request)

    suspend fun logoutMobile(): Result<Unit> = AuthApi.logoutMobile()
}
