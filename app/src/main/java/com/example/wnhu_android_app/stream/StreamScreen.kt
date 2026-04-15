package com.example.wnhu_android_app

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.shape.CircleShape
import kotlinx.coroutines.delay
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StreamScreen(userData: UserData) {

    val viewModel: SongDataViewModel = viewModel()
    val song by viewModel.song.collectAsState()
    val currentReaction = userData.reactionForSong(song)
    var isPrepared by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var isInfoShowing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.updateFromAPI()
    }


    val context = LocalContext.current
    val streamUrl = "http://wnhu-stream1.newhaven.edu:8050/wnhu"
    val mediaPlayer = remember {
        MediaPlayer().apply {
             setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource("http://wnhu-stream1.newhaven.edu:8050/wnhu")
            setOnPreparedListener {
                isPrepared = true
            }
             setOnErrorListener { _, what, extra ->
                println("MediaPlayer Error: $what, $extra")
                false
            }
            prepareAsync()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

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
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = song.song,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        delayMillis = 2000,
                        initialDelayMillis = 2000,
                        velocity = 40.dp
                    )
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.artist,
                color = Color.Gray,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                text = song.album,
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

             IconButton(onClick = {
                if (currentReaction != SongReaction.DISLIKE) {
                    userData.setSongReaction(song, SongReaction.DISLIKE)
                }
            }) {
                Icon(
                    painter = painterResource(
                        id = if (currentReaction == SongReaction.DISLIKE) R.drawable.baseline_thumb_down_24
                        else R.drawable.baseline_thumb_down_off_alt_24
                    ),
                    contentDescription = "Dislike",
                     tint = if (currentReaction == SongReaction.DISLIKE) Color.Red else Color.White.copy(
                        alpha = 0.7f
                    ),
                    modifier = Modifier.size(32.dp)
                )
            }
            Surface(
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.1f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    IconButton(
                        onClick = {
                            if (isPrepared) {
                                isPlaying = !isPlaying
                                if (isPlaying) mediaPlayer.start() else mediaPlayer.pause()
                            }
                        },
                        enabled = isPrepared,
                        modifier = Modifier.fillMaxSize()
                    ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isPlaying) R.drawable.baseline_pause_circle_24
                                    else R.drawable.baseline_play_circle_filled_24
                                ),
                                contentDescription = "Play/Pause",
                                tint = Color.Red,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                }
            }
            IconButton(onClick = {
                if (currentReaction != SongReaction.LIKE) {
                    userData.setSongReaction(song, SongReaction.LIKE)
                }
            }) {
                Icon(
                    painter = painterResource(
                        id = if (currentReaction == SongReaction.LIKE) R.drawable.baseline_thumb_up_24
                        else R.drawable.baseline_thumb_up_off_alt_24
                    ),
                    contentDescription = "Like",
                    tint = if (currentReaction == SongReaction.LIKE) Color.Red else Color.White.copy(
                        alpha = 0.7f
                    ),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
    LaunchedEffect(Unit) {
        while(true) {
            viewModel.updateFromAPI()
            delay(30000)
        }
    }
}

private fun formatSongDuration(durationMillis: Int): String {
    val totalSeconds = (durationMillis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

