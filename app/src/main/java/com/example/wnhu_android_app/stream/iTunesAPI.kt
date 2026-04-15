package com.example.wnhu_android_app

import com.example.wnhu_android_app.songs.IcecastStatus
import com.example.wnhu_android_app.songs.SourceInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.decodeFromString
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

suspend fun fetchLiveMetadata(): SourceInfo? {
    return withContext(Dispatchers.IO) {
        try {
            val urlString = "http://wnhu-stream1.newhaven.edu:8050/status-json.xsl"
            val response = URL(urlString).readText()

            // Log this so you can see the actual JSON structure in Logcat
            println("ICECAST RESPONSE: $response")

            val result = json.decodeFromString<IcecastStatus>(response)
            // Get the first active stream
            result.icestats.source?.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}