package com.example.wnhu_android_app

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.net.URLEncoder

object iTunesAPI {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchSongInfo(title: String, artist: String): SongModel? {
        return withContext(Dispatchers.IO) {
            val query = URLEncoder.encode("$artist $title", "UTF-8")
            val urlString = "https://itunes.apple.com/search?term=$query&entity=song&limit=1"

            val response = URL(urlString).readText()
            println("iTunes RESPONSE: $response")

            val result = json.decodeFromString<SearchResult>(response)
            result.results.firstOrNull()
        }
    }
}

@Serializable
data class SearchResult(
    val resultCount: Int = 0,
    val results: List<SongModel> = emptyList()
)
