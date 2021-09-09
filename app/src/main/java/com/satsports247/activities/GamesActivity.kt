package com.satsports247.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.satsports247.R
import com.satsports247.constants.*
import com.satsports247.databinding.ActivityGamesBinding
import com.satsports247.responseModels.Common
import com.satsports247.retrofit.RetrofitApiClient
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GamesActivity : AppCompatActivity() {

    val TAG: String = "DashboardActivity"
    lateinit var binding: ActivityGamesBinding
    lateinit var mWebView: WebView
    var oneTouchGameID = 0
    var jsonObject = JsonObject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGamesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun init() {
        val gameTitle = intent.getStringExtra(IntentKeys.title)
        requestedOrientation = if (gameTitle == AppConstants.Supernowa)
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        else
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        binding.ivBack.setOnClickListener { finish() }
        mWebView = binding.webview
        mWebView.settings.javaScriptEnabled = true
        mWebView.settings.domStorageEnabled = true
        mWebView.settings.mediaPlaybackRequiresUserGesture = false

        if (gameTitle == AppConstants.OneTouch) {
            binding.tvTitle.text = intent.getStringExtra(IntentKeys.gameName)
            oneTouchGameID = intent.getIntExtra(IntentKeys.gameID, 0)
            Log.e(TAG, "oneTouchGameID: $oneTouchGameID")
        } else
            binding.tvTitle.text = gameTitle

        binding.btnReloadData.setOnClickListener { callApi(gameTitle) }

        callApi(gameTitle)
    }

    private fun callApi(gameTitle: String?) {
        val jsonObject = JsonObject()
        try {
            if (Config.isInternetAvailable(this)) {
                binding.webview.visibility = View.VISIBLE
                binding.llNoInternet.visibility = View.GONE
                Config.showSmallProgressDialog(this)
                val call: Call<Common> = when (gameTitle) {
                    AppConstants.Supernowa -> {
                        jsonObject.addProperty(JsonKeys.GameType, AppConstants.Veronica)
                        jsonObject.addProperty(JsonKeys.GameCode, "")
                        jsonObject.addProperty(
                            JsonKeys.ClientUserName,
                            Config.getSharedPreferences(this, PreferenceKeys.Username)
                        )
                        RetrofitApiClient.getClient.redirectToThirdPartyGame(
                            Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!,
                            jsonObject
                        )
                    }
                    AppConstants.Binary -> {
                        jsonObject.addProperty(JsonKeys.GameType, gameTitle)
                        jsonObject.addProperty(JsonKeys.GameCode, "")
                        jsonObject.addProperty(
                            JsonKeys.ClientUserName,
                            Config.getSharedPreferences(this, PreferenceKeys.Username)
                        )
                        RetrofitApiClient.getClient.binaryLogin(
                            Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!,
                            jsonObject
                        )
                    }
                    AppConstants.SSC -> {
                        val requestJson = JsonObject()

                        requestJson.addProperty(
                            JsonKeys.Username,
                            Config.getSharedPreferences(this, PreferenceKeys.Username)
                        )
                        requestJson.addProperty(
                            JsonKeys.Firstname,
                            Config.getSharedPreferences(this, PreferenceKeys.Username)
                        )
                        requestJson.addProperty(
                            JsonKeys.Lastname,
                            Config.getSharedPreferences(this, PreferenceKeys.Username)
                        )
                        jsonObject.add("Request", requestJson)
                        RetrofitApiClient.getClient.sscLogin(
                            Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!,
                            jsonObject
                        )
                    }
                    AppConstants.OneTouch -> {
                        jsonObject.addProperty(
                            JsonKeys.user, Config.getSharedPreferences(
                                this,
                                PreferenceKeys.Username
                            )
                        )
                        jsonObject.addProperty(
                            JsonKeys.lobby_url,
                            "https://satsport247.com/one-touch-lobby"
                        )
                        jsonObject.addProperty(
                            JsonKeys.game_id,
                            oneTouchGameID
                        )
                        jsonObject.addProperty(JsonKeys.Platform, "GPL_DESKTOP")
                        RetrofitApiClient.getClient.getGameUrl(
                            Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!,
                            jsonObject
                        )
                    }
                    AppConstants.PowerGames -> {
                        RetrofitApiClient.getClient.vacuumLogin(
                            Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!
                        )
                    }
                    AppConstants.TvBet -> {
                        RetrofitApiClient.getClient.tvBetLogin(
                            Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!
                        )
                    }
                    AppConstants.WorldCasino -> {
                        RetrofitApiClient.getClient.worldCasinoLogin(
                            Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!
                        )
                    }
                    else -> { //KingRatan (type - lottery)
                        jsonObject.addProperty(
                            JsonKeys.GameType,
                            AppConstants.Lottery.toLowerCase()
                        )
                        jsonObject.addProperty(
                            JsonKeys.ClientUserName,
                            Config.getSharedPreferences(this, PreferenceKeys.Username)
                        )
                        jsonObject.addProperty(
                            JsonKeys.UserId,
                            Config.getSharedPreferences(this, PreferenceKeys.UserId)!!
                        )
                        RetrofitApiClient.getClient.lotteryLogin(
                            Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!,
                            jsonObject
                        )
                    }
                    /*AppConstants.Lottery -> {
                        jsonObject.addProperty(
                            JsonKeys.UserId,
                            Config.getSharedPreferences(this, PreferenceKeys.UserId)!!
                        )
                        RetrofitApiClient.getClient.lotteryLogin(
                            Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!,
                            jsonObject
                        )
                    }*/
                }
                Log.e(TAG, "jsonObject: $jsonObject")
                call.enqueue(object : Callback<Common> {
                    @SuppressLint("SetJavaScriptEnabled")
                    override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                        Log.e(TAG, "url: " + response?.raw()?.request()?.url())
                        val common: Common? = response?.body()
                        Log.e(TAG, "Game api response: " + Gson().toJson(response?.body()))
                        if (response != null && response.isSuccessful) {
                            when(response.code()){
                                200 -> {
                                    if (common?.status?.code == 0 || common?.status?.code == 100) {
                                        if (!common.RedirectUrl.isNullOrEmpty()) {
                                            mWebView.webViewClient = object : WebViewClient() {
                                                override fun onPageFinished(view: WebView, url: String) {
                                                    Config.hideSmallProgressDialog()
                                                }
                                            }
                                            mWebView.loadUrl(common.RedirectUrl)
                                        } else {
                                            Config.hideSmallProgressDialog()
                                            Config.showOkDialog(
                                                this@GamesActivity,
                                                getString(R.string.cannot_open_this_game)
                                            )
                                        }
                                    } else if (!common?.tvBetUrl.isNullOrEmpty()) {
                                        mWebView.webViewClient = object : WebViewClient() {
                                            override fun onPageFinished(view: WebView, url: String) {
                                                Config.hideSmallProgressDialog()
                                            }
                                        }
                                        mWebView.loadUrl(common?.tvBetUrl!!)
                                    } else if (!common?.sscUrl.isNullOrEmpty()) {
                                        mWebView.webViewClient = object : WebViewClient() {
                                            override fun onPageFinished(view: WebView, url: String) {
                                                Config.hideSmallProgressDialog()
                                            }
                                        }
                                        mWebView.loadUrl(common?.sscUrl!!)
                                    } else if (common?.status?.code == 401) {
                                        Config.hideSmallProgressDialog()
                                        val realm = Realm.getDefaultInstance()
                                        realm.executeTransaction { realm -> realm.deleteAll() }
                                        Config.clearAllPreferences(this@GamesActivity)
                                        startActivity(
                                            Intent(
                                                this@GamesActivity,
                                                LoginActivity::class.java
                                            )
                                        )
                                        finish()
                                    } else {
                                        Config.hideSmallProgressDialog()
                                        Config.showOkDialog(
                                            this@GamesActivity,
                                            common?.status?.returnMessage!!
                                        )
                                    }
                                }
                                401 -> {
                                    Config.hideSmallProgressDialog()
                                    val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { realm -> realm.deleteAll() }
                                    Config.clearAllPreferences(this@GamesActivity)
                                    startActivity(
                                        Intent(
                                            this@GamesActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                            }
                        } else {
                            Config.hideSmallProgressDialog()
                            Config.showOkDialog(
                                this@GamesActivity,
                                getString(R.string.something_went_wrong)
                            )
                        }
                    }

                    override fun onFailure(call: Call<Common>?, t: Throwable?) {
                        Config.hideSmallProgressDialog()
                        Log.e(TAG, "redirectToThirdPartGame: " + t.toString())
                    }
                })
            } else {
                Config.hideSmallProgressDialog()
                binding.webview.visibility = View.GONE
                binding.llNoInternet.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Config.toast(this, "" + e)
            Log.e(TAG, "" + e)
        }
    }
}