package com.example.wnhu_android_app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object AuthApi {
    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl = BuildConfig.FLASK_BASE_URL.trimEnd('/')

    suspend fun checkIfUserExists(email: String): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val response = postJson("/checkUser", json.encodeToString(CheckUserRequest(email = email)))
            response.trim() == "exists"
        }
    }

    suspend fun createUserMobile(request: CreateUserMobileRequest): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            postJson("/createUserMobile", json.encodeToString(request))
            Unit
        }
    }

    suspend fun logoutMobile(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            postJson("/mobileLogout", "{}")
            Unit
        }
    }

    private fun postJson(path: String, body: String): String {
        val connection = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 5_000
            readTimeout = 5_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }

        connection.outputStream.bufferedWriter().use { it.write(body) }
        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            throw IllegalStateException(readError(connection, responseCode))
        }

        val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
        connection.disconnect()
        return response
    }

    private fun readError(connection: HttpURLConnection, responseCode: Int): String {
        val message = connection.errorStream
            ?.let { InputStreamReader(it).buffered().use(BufferedReader::readText) }
            ?.takeIf { it.isNotBlank() }
            ?: "Request failed with HTTP $responseCode"
        connection.disconnect()
        return message
    }
}
