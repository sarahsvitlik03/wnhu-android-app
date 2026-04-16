package com.example.wnhu_android_app

class SongFeedbackRepository {
       suspend fun loadSongRatings(userEmail: String): Result<PullLikedSongsResponse> {
        return SongFeedbackApi.fetchLikedSongs(PullLikedSongsRequest(email = userEmail))
    }

    suspend fun likeSong(userEmail: String, song: SongModel): Result<Unit> {
        return SongFeedbackApi.likeSong(
            SongPreferenceRequest(
                title = song.song,
                artist = song.artist,
                email = userEmail
            )
        )
    }

    suspend fun dislikeSong(userEmail: String, song: SongModel): Result<Unit> {
        return SongFeedbackApi.dislikeSong(
            SongPreferenceRequest(
                title = song.song,
                artist = song.artist,
                email = userEmail
            )
        )
    }
}
