package com.satsports247.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.satsports247.constants.Config
import com.satsports247.constants.IntentKeys
import com.satsports247.constants.PreferenceKeys
import com.satsports247.databinding.ActivityFullScreenLiveVideoBinding
import com.universalvideoview.UniversalVideoView.VideoViewCallback
import kotlinx.android.synthetic.main.activity_full_screen_live_video.*


class FullScreenLiveVideoActivity : AppCompatActivity() {

    val TAG: String = "FullScreenLiveVideo"
    lateinit var binding: ActivityFullScreenLiveVideoBinding
    var url = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenLiveVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    fun init() {
        binding.ivBack.setOnClickListener { onBackPressed() }
        if (intent.hasExtra(IntentKeys.liveUrl) && intent.getStringExtra(IntentKeys.liveUrl) != null) {
            url = intent.getStringExtra(IntentKeys.liveUrl)!!
            Config.showSmallProgressDialog(this)
            binding.webview.settings.javaScriptEnabled = true
//            binding.webview.settings.loadWithOverviewMode = true
//            binding.webview.settings.useWideViewPort = true
            binding.webview.settings.builtInZoomControls = true
            binding.webview.settings.domStorageEnabled = true
            binding.webview.settings.allowContentAccess = true
            binding.webview.settings.displayZoomControls = true
//            binding.webview.settings.setSupportZoom(true)
            binding.webview.settings.supportZoom()
            binding.webview.settings.mediaPlaybackRequiresUserGesture = false
            binding.webview.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    Config.hideSmallProgressDialog()
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    Config.hideSmallProgressDialog()
                    super.onReceivedError(view, request, error)
                }
            }
            Log.e(TAG, "url: $url")
            binding.webview.loadUrl(url)

/*//            binding.fullscreenVideo.videoUrl(url).enableAutoStart()
            val videoUri: Uri = Uri.parse(url)
            binding.fullscreenVideoLayout.setVideoURI(videoUri)
            binding.fullscreenVideoLayout.start()

            val width: Int = videoView.width
            val cachedHeight = (width * 405f / 720f).toInt()
            val mediaController = binding.mediaController
            val videoView = binding.videoView
            videoView.setMediaController(mediaController)
//            val layoutParams: ViewGroup.LayoutParams = binding.videoLayout.layoutParams
//            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
//            layoutParams.height = cachedHeight
//            binding.videoLayout.layoutParams = layoutParams
//            videoView.setVideoURI(Uri.parse(url))
            videoView.setVideoPath(url)
            videoView.start()
//            videoView.setFullscreen(true, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            videoView.setVideoViewCallback(object : VideoViewCallback {
                override fun onScaleChange(isFullscreen: Boolean) {
//                    isFullscreen = isFullscreen
                    if (isFullscreen) {
                        val layoutParams: ViewGroup.LayoutParams = binding.videoLayout.layoutParams
                        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                        binding.videoLayout.layoutParams = layoutParams
//                        //GONE the unconcerned views to leave room for video and controller
//                        mBottomLayout.setVisibility(View.GONE)
                    } else {
                        val layoutParams: ViewGroup.LayoutParams = binding.videoLayout.layoutParams
                        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        layoutParams.height = cachedHeight
                        binding.videoLayout.layoutParams = layoutParams
//                        mBottomLayout.setVisibility(View.VISIBLE)
                    }
                }

                override fun onPause(mediaPlayer: MediaPlayer) { // Video pause
                    Log.e(TAG, "onPause UniversalVideoView callback")
                }

                override fun onStart(mediaPlayer: MediaPlayer) { // Video start/resume to play
                    Log.e(TAG, "onStart UniversalVideoView callback")
                }

                override fun onBufferingStart(mediaPlayer: MediaPlayer) { // steam start loading
                    Log.e(TAG, "onBufferingStart UniversalVideoView callback")
                }

                override fun onBufferingEnd(mediaPlayer: MediaPlayer) { // steam end loading
                    Log.e(TAG, "onBufferingEnd UniversalVideoView callback")
                }
            })*/
        }
    }

    override fun onBackPressed() {
        binding.webview.loadUrl("")
        val intent = Intent()
        intent.putExtra(IntentKeys.liveUrl, url)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}