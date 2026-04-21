package com.example.wnhu_android_app

import com.example.wnhu_android_app.songs.IcecastStatus
import com.example.wnhu_android_app.songs.SourceInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.net.URLEncoder

object iTunesAPI {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchSongInfo(title: String, artist: String): SongModel? {
        return withContext(Dispatchers.IO) {
            val query = URLEncoder.encode("$artist $title".trim(), "UTF-8")
            val urlString = "https://itunes.apple.com/search?term=$query&entity=song&limit=5"

            val response = URL(urlString).readText()
            println("iTunes RESPONSE: $response")

            val result = json.decodeFromString<SearchResult>(response)
            result.results.firstOrNull { it.imageURL.isNotBlank() } ?: result.results.firstOrNull()
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
            val urlString = "https://wnhu-stream1.newhaven.edu:8051/status-json.xsl"
            val response = URL(urlString).readText()

            // Log this so you can see the actual JSON structure in Logcat
            println("ICECAST RESPONSE: $response")

            val result = json.decodeFromString<IcecastStatus>(response)
            result.icestats.source
                ?.firstOrNull { it.server_name == "WNHU Station Engine" }
                ?: result.icestats.source?.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun parseIcecastTrack(source: SourceInfo): Pair<String, String> {
    val raw = source.currently_playing
        ?.takeIf { it.isNotBlank() }
        ?: source.title.orEmpty()

    val parts = raw.split("-", limit = 2).map { it.trim() }
    return if (parts.size == 2) {
        parts[0] to parts[1]
    } else {
        "" to raw.trim()
    }
}

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}
