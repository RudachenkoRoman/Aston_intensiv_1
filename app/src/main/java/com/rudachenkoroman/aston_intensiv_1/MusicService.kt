package com.rudachenkoroman.aston_intensiv_1

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.util.concurrent.Executors


class MusicService : Service() {

    private var mediaPlayer = MediaPlayer()
    private val songList = listOf(R.raw.music_one, R.raw.music_two, R.raw.music_three, R.raw.music_four, R.raw.music_five)
    private val nameList = listOf("music_one", "music_two", "music_three", "music_four", "music_five")
    private var songIndex = 0
    private var isPause = false

    companion object {
        const val NOTIFICATION_ID = 1
    }

    enum class Actions {
        START, PAUSE, NEXT, PREVIOUS
    }

    inner class MyBinder : Binder() {
        fun getService(): MusicService {
            return this@MusicService
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return MyBinder()
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener { nextSong() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, getNotification())
        Executors.newSingleThreadExecutor().execute {
            when (intent?.action) {
                Actions.START.toString() -> startSong()
                Actions.PAUSE.toString() -> pauseSong()
                Actions.NEXT.toString() -> nextSong()
                Actions.PREVIOUS.toString() -> previousSong()
            }
        }
        return START_NOT_STICKY
    }

    fun startSong() {
        if (!isPause) {
            prepareSong()
            mediaPlayer.start()
        } else {
            mediaPlayer.start()
            isPause = false
        }
    }

    fun pauseSong() {
        mediaPlayer.pause()
        isPause = true
    }

    fun nextSong() {
        isPause = false
        songIndex++
        prepareSong()
        mediaPlayer.start()
    }

    fun previousSong() {
        isPause = false
        songIndex--
        prepareSong()
        mediaPlayer.start()
    }

    private fun prepareSong() {
        mediaPlayer.reset()
        if (songIndex >= songList.size) {
            songIndex = 0
        } else if (songIndex < 0) {
            songIndex = songList.size - 1
        }
        val songId = songList[songIndex]
        val songUri = Uri.parse("android.resource://${packageName}/$songId")

        mediaPlayer.let {
            it.setDataSource(applicationContext, songUri)
            it.prepare()
        }
    }

    fun songName(): String {
        if (songIndex >= nameList.size) {
            songIndex = 0
        } else if (songIndex < 0) {
            songIndex = nameList.size - 1
        }
        return nameList[songIndex]
    }

    private fun getNotification(): Notification {

        val previousIntent = Intent(this, MusicService::class.java)
        previousIntent.setAction(Actions.PREVIOUS.toString())
        val previousPendingIntent = PendingIntent.getService(this, 1, previousIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val playIntent = Intent(this, MusicService::class.java)
        playIntent.setAction(Actions.PAUSE.toString())
        val playPendingIntent = PendingIntent.getService(this, 1, playIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(this, MusicService::class.java)
        nextIntent.setAction(Actions.NEXT.toString())
        val nextPendingIntent = PendingIntent.getService(this, 1, nextIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, "running chanel")
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle("Music Player")
            .setContentText("Music Player is active")
            .setAutoCancel(true)
            .addAction(R.drawable.baseline_skip_previous_24, "Previous" , previousPendingIntent)
            .addAction(R.drawable.baseline_pause_24, "Play" , playPendingIntent)
            .addAction(R.drawable.baseline_skip_previous_24, "Next" , nextPendingIntent)

        return notification.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}

