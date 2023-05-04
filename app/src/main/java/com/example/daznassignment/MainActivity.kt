package com.example.daznassignment

import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.firebase.analytics.FirebaseAnalytics

class MainActivity : AppCompatActivity() {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var playerView: StyledPlayerView
    private lateinit var firebaseAnalytics : FirebaseAnalytics
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var isFullscreen = false
    private var isPlayerPlaying = true
    private val mediaItem = MediaItem.Builder()
        .setUri(MPD)
        .setMimeType(MimeTypes.APPLICATION_MPD)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        playerView = findViewById(R.id.player_view)
//        if (savedInstanceState != null) {
//            currentWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW)
//            playbackPosition = savedInstanceState.getLong(STATE_RESUME_POSITION)
//            isFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN)
//            isPlayerPlaying = savedInstanceState.getBoolean(STATE_PLAYER_PLAYING)
//        }
        initFirebase()
    }

    private fun initPlayer() {
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            playWhenReady = isPlayerPlaying
            seekTo(currentWindow, playbackPosition)
            setMediaItem(mediaItem, false)
            prepare()
        }
        playerView.player = exoPlayer

        exoPlayer.addListener(object : Player.Listener {
            private var playbackStartedAt = 0L
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        var count = 0
                        if (playWhenReady && playbackStartedAt == 0L) {
                            // Playback started
                            count = count++
                            playbackStartedAt = System.currentTimeMillis()
                            val params = Bundle().apply {
                                putString("video_url", "https://example.com/video.mp4")
                                putString("event_name", "playback_started")
                            }
                            firebaseAnalytics.logEvent("video_playback", params)
                        } else if (!playWhenReady && playbackStartedAt > 0L) {
                            // Playback paused
                            val playbackDuration = System.currentTimeMillis() - playbackStartedAt
                            val params = Bundle().apply {
                                putString("video_url", "https://example.com/video.mp4")
                                putString("event_name", "playback_paused")
                                putLong("playback_duration", playbackDuration)
                            }
                            firebaseAnalytics.logEvent("video_playback", params)
                            playbackStartedAt = 0L
                        }
                    }
                    Player.STATE_ENDED -> {
                        // Log playback completed event
                    }
                }
            }
        })


    }

    private fun releasePlayer() {
        isPlayerPlaying = exoPlayer.playWhenReady
        playbackPosition = exoPlayer.currentPosition
        currentWindow = exoPlayer.currentMediaItemIndex
        exoPlayer.release()
    }

    override fun onSaveInstanceState(outState: Bundle) {
//        outState.putInt(STATE_RESUME_WINDOW, exoPlayer.currentMediaItemIndex)
//        outState.putLong(STATE_RESUME_POSITION, exoPlayer.currentPosition)
//        outState.putBoolean(STATE_PLAYER_FULLSCREEN, isFullscreen)
//        outState.putBoolean(STATE_PLAYER_PLAYING, isPlayerPlaying)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initPlayer()
            playerView.onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23) {
            initPlayer()
            playerView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            playerView.onPause()
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            playerView.onPause()
            releasePlayer()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            supportActionBar?.hide()
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            supportActionBar?.show()
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        }

    }


    companion object {
        const val MPD = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd"
        const val STATE_RESUME_WINDOW = "resumeWindow"
        const val STATE_RESUME_POSITION = "resumePosition"
        const val STATE_PLAYER_FULLSCREEN = "playerFullscreen"
        const val STATE_PLAYER_PLAYING = "playerOnPlay"
    }




    private fun initFirebase() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val analyticsViewModel = ViewModelProvider(this).get(AnalyticsViewModel::class.java)
        analyticsViewModel.buttonClickCount.observe(this) { count ->
            // Update the UI with the button click count
        }

        val bundle = Bundle()
        bundle.putString("button_id", "play")
        firebaseAnalytics.logEvent("button_click", bundle)


    }
}

