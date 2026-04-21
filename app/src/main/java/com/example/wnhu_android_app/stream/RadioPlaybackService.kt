package com.example.wnhu_android_app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.MainThread
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat.MediaStyle

class RadioPlaybackService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var mediaSession: MediaSessionCompat? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var metadataTitle: String = DEFAULT_TITLE
    private var metadataArtist: String = DEFAULT_ARTIST

    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pausePlayback()
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createChannel()
        mediaSession = MediaSessionCompat(this, "WNHU").apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS
            )
            setCallback(sessionCallback)
            isActive = true
        }
        refreshSessionMetadata()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> play()
            ACTION_PAUSE -> pausePlayback()
            ACTION_TOGGLE -> if (RadioPlaybackController.isPlaying.value) pausePlayback() else play()
            ACTION_UPDATE_METADATA -> {
                metadataTitle = intent.getStringExtra(EXTRA_TITLE) ?: DEFAULT_TITLE
                metadataArtist = intent.getStringExtra(EXTRA_ARTIST) ?: DEFAULT_ARTIST
                refreshSessionMetadata()
                if (RadioPlaybackController.isPlaying.value || !RadioPlaybackController.isPrepared.value) {
                    val nm = getSystemService(NotificationManager::class.java)
                    nm?.notify(NOTIFICATION_ID, buildNotification())
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        abandonAudioFocus()
        mediaSession?.run {
            isActive = false
            release()
        }
        mediaSession = null
        mediaPlayer?.release()
        mediaPlayer = null
        RadioPlaybackController.setPrepared(false)
        RadioPlaybackController.setPlaying(false)
        super.onDestroy()
    }

    private val sessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            play()
        }

        override fun onPause() {
            pausePlayback()
        }
    }

    private fun play() {
        if (!requestAudioFocus()) return
        RadioPlaybackController.setPlaying(true)
        ensurePlayer()
        val player = mediaPlayer ?: return
        if (RadioPlaybackController.isPrepared.value) {
            player.start()
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
            startForeground(NOTIFICATION_ID, buildNotification())
        }
    }

    private fun pausePlayback() {
        mediaPlayer?.pause()
        RadioPlaybackController.setPlaying(false)
        updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
        val nm = getSystemService(NotificationManager::class.java)
        nm?.notify(NOTIFICATION_ID, buildNotification())
        stopForeground(STOP_FOREGROUND_DETACH)
    }

    private fun ensurePlayer() {
        if (mediaPlayer != null) return
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(STREAM_URL)
            setOnPreparedListener {
                RadioPlaybackController.setPrepared(true)
                if (RadioPlaybackController.isPlaying.value) {
                    start()
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                    startForeground(NOTIFICATION_ID, buildNotification())
                } else {
                    updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                }
            }
            setOnErrorListener { _, what, extra ->
                android.util.Log.e("RadioPlaybackService", "MediaPlayer error $what / $extra")
                RadioPlaybackController.setPrepared(false)
                RadioPlaybackController.setPlaying(false)
                false
            }
            prepareAsync()
        }
        updatePlaybackState(PlaybackStateCompat.STATE_BUFFERING)
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    @MainThread
    private fun refreshSessionMetadata() {
        val session = mediaSession ?: return
        session.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, metadataTitle)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, metadataArtist)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, metadataTitle)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, metadataArtist)
                .build()
        )
    }

    private fun updatePlaybackState(@PlaybackStateCompat.State state: Int) {
        val session = mediaSession ?: return
        val actions = PlaybackStateCompat.ACTION_PLAY or
            PlaybackStateCompat.ACTION_PAUSE or
            PlaybackStateCompat.ACTION_PLAY_PAUSE or
            PlaybackStateCompat.ACTION_STOP
        session.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(actions)
                .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
                .build()
        )
    }

    private fun buildNotification(): Notification {
        val session = mediaSession!!
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or pendingImmutable()
        )
        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.wnhu)
        val playing = RadioPlaybackController.isPlaying.value
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(metadataTitle)
            .setContentText(metadataArtist)
            .setSmallIcon(R.drawable.baseline_play_circle_filled_24)
            .setLargeIcon(largeIcon)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(playing)
            .setStyle(
                MediaStyle()
                    .setMediaSession(session.sessionToken)
                    .setShowActionsInCompactView(0)
            )
        if (playing) {
            builder.addAction(
                NotificationCompat.Action(
                    R.drawable.baseline_pause_circle_24,
                    getString(R.string.radio_pause),
                    pausePendingIntent()
                )
            )
        } else {
            builder.addAction(
                NotificationCompat.Action(
                    R.drawable.baseline_play_circle_filled_24,
                    getString(R.string.radio_play),
                    playPendingIntent()
                )
            )
        }
        return builder.build()
    }

    private fun playPendingIntent(): PendingIntent {
        val intent = Intent(this, RadioPlaybackService::class.java).setAction(ACTION_PLAY)
        return PendingIntent.getService(
            this,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or pendingImmutable()
        )
    }

    private fun pausePendingIntent(): PendingIntent {
        val intent = Intent(this, RadioPlaybackService::class.java).setAction(ACTION_PAUSE)
        return PendingIntent.getService(
            this,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or pendingImmutable()
        )
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.radio_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.radio_notification_channel_description)
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    private fun requestAudioFocus(): Boolean {
        val am = audioManager ?: return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener(audioFocusListener)
                .build()
            audioFocusRequest = req
            am.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(
                audioFocusListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(audioFocusListener)
        }
    }

    private fun pendingImmutable(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }

    companion object {
        const val STREAM_URL = "https://wnhu-stream1.newhaven.edu:8051/stationengine"
        private const val CHANNEL_ID = "wnhu_radio_playback"
        private const val NOTIFICATION_ID = 8701
        private const val DEFAULT_TITLE = "WNHU 88.7"
        private const val DEFAULT_ARTIST = "Live stream"

        const val ACTION_PLAY = "com.example.wnhu_android_app.radio.PLAY"
        const val ACTION_PAUSE = "com.example.wnhu_android_app.radio.PAUSE"
        const val ACTION_TOGGLE = "com.example.wnhu_android_app.radio.TOGGLE"
        const val ACTION_UPDATE_METADATA = "com.example.wnhu_android_app.radio.UPDATE_METADATA"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"

        fun updateMetadata(context: Context, title: String, artist: String) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_UPDATE_METADATA
                putExtra(EXTRA_TITLE, title.ifBlank { DEFAULT_TITLE })
                putExtra(EXTRA_ARTIST, artist.ifBlank { DEFAULT_ARTIST })
            }
            context.startService(intent)
        }

        fun play(context: Context) {
            val intent = Intent(context, RadioPlaybackService::class.java).setAction(ACTION_PLAY)
            ContextCompat.startForegroundService(context, intent)
        }

        fun pause(context: Context) {
            val intent = Intent(context, RadioPlaybackService::class.java).setAction(ACTION_PAUSE)
            context.startService(intent)
        }
    }
}
