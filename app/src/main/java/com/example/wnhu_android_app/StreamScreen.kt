package com.example.wnhu_android_app

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState



@Composable
fun StreamScreen() {

    val viewModel: SongDataViewModel = viewModel()
    val song by viewModel.song.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.updateFromAPI()
    }


    val context = LocalContext.current

    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.heavy)
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

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

        AsyncImage(
            model = song.highResArtwork,
            contentDescription = "Artwork",
            modifier = Modifier
                .fillMaxWidth()
                .height(325.dp)
                .padding(horizontal = 30.dp)
                .shadow(8.dp, RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.wnhu),
            error = painterResource(id = R.drawable.wnhu)
        )


        Spacer(modifier = Modifier.height(20.dp))

        Text(song.song, color = Color.White, style = MaterialTheme.typography.titleLarge)
        Text(song.artist, color = Color.Gray, style = MaterialTheme.typography.titleMedium)
        Text(song.album, color = Color.Gray, style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {

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

            IconButton(onClick = {
                isPlaying = !isPlaying
                if (isPlaying) {
                    mediaPlayer.start()
                } else {
                    mediaPlayer.pause()
                }
            }) {
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
            if (isInfoShowing) {
                AlertDialog(
                    onDismissRequest = { isInfoShowing = false },
                    title = { Text(song.song) },
                    text = {
                        Column {
                            Text("Artist: ${song.artist}")
                            Text("Album: ${song.album}")
                            Text("Genre: ${song.genre}")
                            Text("Duration: ${song.duration}")
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