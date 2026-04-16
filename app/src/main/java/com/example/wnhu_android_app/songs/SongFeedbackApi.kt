package com.example.wnhu_android_app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object SongFeedbackApi {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val baseUrl = BuildConfig.FLASK_BASE_URL.trimEnd('/')

    suspend fun likeSong(request: SongPreferenceRequest): Result<Unit> = withContext(Dispatchers.IO) {
        postWithoutResponseBody("/likedSong", json.encodeToString(request))
    }

    suspend fun dislikeSong(request: SongPreferenceRequest): Result<Unit> = withContext(Dispatchers.IO) {
        postWithoutResponseBody("/dislikedSong", json.encodeToString(request))
    }

    suspend fun fetchLikedSongs(request: PullLikedSongsRequest): Result<PullLikedSongsResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = openConnection("$baseUrl/pullLikedSongs", "POST")
            connection.doOutput = true
            connection.outputStream.bufferedWriter().use { writer ->
                writer.write(json.encodeToString(request))
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw IllegalStateException(readError(connection, responseCode))
            }

            val body = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            connection.disconnect()
            if (body.isBlank()) PullLikedSongsResponse()
            else json.decodeFromString<PullLikedSongsResponse>(body)
        }
    }

    private fun postWithoutResponseBody(path: String, payload: String): Result<Unit> {
        return runCatching {
            val connection = openConnection("$baseUrl$path", "POST")
            connection.doOutput = true
            connection.outputStream.bufferedWriter().use { writer ->
                writer.write(payload)
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw IllegalStateException(readError(connection, responseCode))
            }
            connection.disconnect()
        }
    }

    private fun openConnection(url: String, method: String): HttpURLConnection {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = 5_000
        connection.readTimeout = 5_000
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        return connection
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
