package com.example.daznassignment

import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.example.daznassignment.databinding.ActivityMainBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.firebase.analytics.FirebaseAnalytics

class MainActivity : BaseActivity<ActivityMainBinding, AnalyticsViewModel>() {

    private var activityMainBinding: ActivityMainBinding? = null
    private var analyticsViewModel: AnalyticsViewModel? = null
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var isPlayerPlaying = true
    private val mediaItem = MediaItem.Builder()
        .setUri(MPD)
        .setMimeType(MimeTypes.APPLICATION_MPD)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = getViewDataBinding()
        analyticsViewModel = getViewModel()
        activityMainBinding!!.analyticsViewModel = analyticsViewModel

//        playerView = findViewById(R.id.player_view)
        initFirebase()
    }

    override fun getBindingVariable(): Int {
        return BR.analyticsViewModel
    }

    override fun getLayout(): Int {
        return R.layout.activity_main
    }

    override fun getViewModel(): AnalyticsViewModel {
        analyticsViewModel = ViewModelProvider(this).get(AnalyticsViewModel::class.java)
        return analyticsViewModel!!
    }

    private fun initPlayer() {
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            playWhenReady = isPlayerPlaying
            seekTo(currentWindow, playbackPosition)
            setMediaItem(mediaItem, false)
            prepare()
        }
        activityMainBinding?.playerView?.player = exoPlayer

        exoPlayer.addListener(object : Player.Listener {
            private var playbackStartedAt = 0L
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        var pCount = 0
                        var playCount = 0
                        if (playWhenReady && playbackStartedAt == 0L) {
                            // Playback started
                            playCount++
                            analyticsViewModel?.fCount?.value=playCount
                            playbackStartedAt = System.currentTimeMillis()
                            val params = Bundle().apply {
                                putString("event_name", "playback_started")
                                putString("count", "$playCount")
                            }
                            firebaseAnalytics.logEvent("video_playback", params)
                        } else if (!playWhenReady && playbackStartedAt > 0L) {
                            // Playback paused
                            pCount++
                            analyticsViewModel?.pCount?.value=pCount
                            val params = Bundle().apply {
                                putString("event_name", "playback_paused")
                                putString("count", "$pCount")

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

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initPlayer()
            activityMainBinding?.playerView?.onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23) {
            initPlayer()
            activityMainBinding?.playerView?.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            activityMainBinding?.playerView?.onPause()
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            activityMainBinding?.playerView?.onPause()
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
    }


    private fun initFirebase() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("button_id", "")
        firebaseAnalytics.logEvent("button_click", bundle)


    }
}

