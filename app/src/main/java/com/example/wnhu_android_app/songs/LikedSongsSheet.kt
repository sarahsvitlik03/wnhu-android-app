package com.example.wnhu_android_app

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LikedSongsSheet(userData: UserData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text("Liked Songs", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        userData.songs.forEach { song: LikedSongModel ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("${song.songName} — ${song.artistName}")
                }
            }
        }
    }
}
