package com.example.wnhu_android_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StreamScreen(userData: UserData) {

    val context = LocalContext.current
    val viewModel: SongDataViewModel = viewModel()
    val song by viewModel.song.collectAsState()
    val currentReaction = userData.reactionForSong(song)
    val isPlaying by RadioPlaybackController.isPlaying.collectAsState()
    var isInfoShowing by remember { mutableStateOf(false) }
    val infoSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        RadioPlaybackService.play(context)
    }

    fun requestPlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            RadioPlaybackService.play(context)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.updateFromAPI()
    }

    LaunchedEffect(song.song, song.artist, isPlaying) {
        if (isPlaying) {
            RadioPlaybackService.updateMetadata(context, song.song, song.artist)
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
                            if (isPlaying) {
                                RadioPlaybackService.pause(context)
                            } else {
                                requestPlay()
                            }
                        },
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

    if (isInfoShowing) {
        ModalBottomSheet(
            onDismissRequest = { isInfoShowing = false },
            sheetState = infoSheetState,
        ) {
            SongInfoSheetContent(
                song = song,
                onDone = { isInfoShowing = false }
            )
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

private fun formatReleaseDate(iso: String): String {
    if (iso.isBlank()) return ""
    return try {
        val instant = Instant.parse(iso)
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()).format(localDate)
    } catch (_: Exception) {
        iso.substringBefore('T').ifBlank { iso }
    }
}

@Composable
private fun SongInfoSheetContent(song: SongModel, onDone: () -> Unit) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .verticalScroll(scroll)
    ) {
        Text(
            text = "Song information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = song.highResArtwork,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .shadow(6.dp, RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.wnhu),
                error = painterResource(id = R.drawable.wnhu)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = song.song.ifBlank { "Unknown title" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = song.artist.ifBlank { "Unknown artist" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (song.album.isNotBlank()) {
                    Text(
                        text = song.album,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (song.genre.isNotBlank()) {
            SongInfoDetailRow(label = "Genre", value = song.genre)
        }
        val release = formatReleaseDate(song.releaseDate)
        if (release.isNotBlank()) {
            SongInfoDetailRow(label = "Release date", value = release)
        }
        if (song.duration > 0) {
            SongInfoDetailRow(label = "Duration", value = formatSongDuration(song.duration))
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = onDone,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Done")
        }
    }
}

@Composable
private fun SongInfoDetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

