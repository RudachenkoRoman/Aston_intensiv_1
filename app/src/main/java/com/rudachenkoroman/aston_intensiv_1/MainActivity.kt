package com.rudachenkoroman.aston_intensiv_1

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rudachenkoroman.aston_intensiv_1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var serviceIntent: Intent = Intent()
    private var musicService: MusicService = MusicService()
    private var isPlaying: Boolean = false
    private var serviceStarted: Boolean = false
    private var mBound: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        Thread.sleep(1500)
        installSplashScreen()
        setContentView(binding.root)

        serviceIntent = Intent(this, MusicService::class.java)

        binding.apply {
            buttonPlayAndStop.setOnClickListener {
                if (isPlaying) {
                    binding.buttonPlayAndStop.setImageResource(R.drawable.baseline_play_arrow_24)
                    musicService.pauseSong()
                } else {
                    if (!serviceStarted) {
                        binding.buttonPlayAndStop.setImageResource(R.drawable.baseline_pause_24)
                        Intent(applicationContext, MusicService::class.java).also {
                            it.action = MusicService.Actions.START.toString()
                            startService(it)
                            serviceStarted = true
                        }
                    } else {
                        musicService.startSong()
                        binding.buttonPlayAndStop.setImageResource(R.drawable.baseline_pause_24)
                    }
                }
                isPlaying = !isPlaying
            }

            buttonNext.setOnClickListener {
                if (serviceStarted) {
                    musicService.nextSong()
                    buttonPlayAndStop.setImageResource(R.drawable.baseline_pause_24)
                    isPlaying = true
                } else {
                    Intent(applicationContext, MusicService::class.java).also {
                        it.action = MusicService.Actions.NEXT.toString()
                        startService(it)
                        buttonPlayAndStop.setImageResource(R.drawable.baseline_pause_24)
                        isPlaying = true
                        serviceStarted = true
                    }
                }
            }

            buttonBack.setOnClickListener {
                if (serviceStarted) {
                    musicService.previousSong()
                    buttonPlayAndStop.setImageResource(R.drawable.baseline_pause_24)
                    isPlaying = true
                } else {
                    Intent(applicationContext, MusicService::class.java).also {
                        it.action = MusicService.Actions.PREVIOUS.toString()
                        startService(it)
                        buttonPlayAndStop.setImageResource(R.drawable.baseline_pause_24)
                        isPlaying = true
                        serviceStarted = true
                    }
                }
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicService.MyBinder
            musicService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
        stopService(serviceIntent)
    }
}
