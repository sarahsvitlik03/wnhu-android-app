package com.example.wnhu_android_app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.wnhu_android_app.R
import com.example.wnhu_android_app.SongModel

@Composable
fun StreamScreen() {

    // Fake data for now — replace later
    val song = SongModel(
        song = "Heavy",
        artist = "The Marias",
        album = "Cinema",
        genre = "Alternative",
        releaseDate = "2021",
        duration = 180000,
        imageURL = "https://i.scdn.co/image/ab67616d0000b273657d6776f64aa731c8d1748b"
    )

    var isPlaying by remember { mutableStateOf(false) }
    var isThumbsUp by remember { mutableStateOf(false) }
    var isThumbsDown by remember { mutableStateOf(false) }
    var isInfoShowing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Info button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(onClick = { isInfoShowing = true }) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Info",
                    tint = Color.Gray
                )
            }
        }

        // Logo
        Image(
            painter = painterResource(id = R.drawable.wnhu),
            contentDescription = "Logo",
            modifier = Modifier
                .width(100.dp)
                //.height(125.dp)
                .shadow(8.dp, RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Artwork
        AsyncImage(
            model = song.highResArtwork,
            contentDescription = "Artwork",
            modifier = Modifier
                .fillMaxWidth()
                .height(325.dp)
                .padding(horizontal = 30.dp)
                .shadow(8.dp, RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.wnhu)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Song info
        Text(song.song, color = Color.White, style = MaterialTheme.typography.titleLarge)
        Text(song.artist, color = Color.Gray, style = MaterialTheme.typography.titleMedium)
        Text(song.album, color = Color.Gray, style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(30.dp))

        // Buttons row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {

            // Thumbs down
            // Thumbs down
            IconButton(onClick = {
                isThumbsDown = !isThumbsDown
                if (isThumbsUp) isThumbsUp = false
            }) {
                Icon(
                    painter = painterResource(
                        id = if (isThumbsDown) R.drawable.baseline_thumb_down_24
                        else R.drawable.baseline_thumb_down_off_alt_24
                    ),
                    contentDescription = "Dislike",
                    tint = Color.Red,
                    modifier = Modifier.size(30.dp)
                )
            }

// Play / Pause
            IconButton(onClick = { isPlaying = !isPlaying }) {
                Icon(
                    painter = painterResource(
                        id = if (isPlaying) R.drawable.baseline_pause_circle_24
                        else R.drawable.baseline_play_circle_filled_24
                    ),
                    contentDescription = "Play/Pause",
                    tint = Color.Red,
                    modifier = Modifier.size(100.dp)
                )
            }

// Thumbs up
            IconButton(onClick = {
                isThumbsUp = !isThumbsUp
                if (isThumbsDown) isThumbsDown = false
            }) {
                Icon(
                    painter = painterResource(
                        id = if (isThumbsUp) R.drawable.baseline_thumb_up_24
                        else R.drawable.baseline_thumb_up_off_alt_24
                    ),
                    contentDescription = "Like",
                    tint = Color.Red,
                    modifier = Modifier.size(30.dp)
                )
            }


            // Info popup
            if (isInfoShowing) {
                AlertDialog(
                    onDismissRequest = { isInfoShowing = false },
                    title = { Text(song.song) },
                    text = {
                        Column {
                            Text("Artist: ${song.artist}")
                            Text("Album: ${song.album}")
                            Text("Genre: ${song.genre}")
                            Text("Duration: ${song.formattedDuration}")
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { isInfoShowing = false }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}