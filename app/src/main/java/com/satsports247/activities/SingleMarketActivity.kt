package com.satsports247.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.satsports247.R
import com.satsports247.adapters.MarketViewPagerAdapter
import com.satsports247.constants.*
import com.satsports247.dataModels.MatchedBetModel
import com.satsports247.databinding.ActivitySingleMarketBinding
import com.satsports247.fragments.singleMarketFragments.SingleMarketFragment
import com.satsports247.fragments.singleMarketFragments.SingleMatchedFragment
import com.satsports247.fragments.singleMarketFragments.SingleUnmatchedFragment
import com.satsports247.responseModels.MatchedUnmatchedModel
import com.satsports247.retrofit.RetrofitApiClient
import io.realm.Realm
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SingleMarketActivity : AppCompatActivity(), View.OnTouchListener {

    val TAG: String = "SingleMarketActivity"
    lateinit var binding: ActivitySingleMarketBinding
    lateinit var realm: Realm
    var centralIds: String = ""
    var socketMarketFragment = SingleMarketFragment()
    var matchedFragment = SingleMatchedFragment()
    var unMatchedFragment = SingleUnmatchedFragment()
    var centralIDList = ArrayList<Int>()
    var matchUrl = ""
    var isMatchViewOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleMarketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    companion object {
        var okHttpClient: OkHttpClient = OkHttpClient()
        lateinit var webSocket: WebSocket
        var socketTime: String = ""
        var matchedList = ArrayList<MatchedBetModel>()
        var unMatchedList = ArrayList<MatchedBetModel>()
        var jsonArray = JsonArray()

        lateinit var viewPagerAdapter: MarketViewPagerAdapter
        var marketIds: String = ""
        lateinit var tvBalance: TextView
        lateinit var tvLiability: TextView
        lateinit var swipeRefreshLayout: SwipeRefreshLayout
        var isActive = false
        lateinit var webView: WebView
        lateinit var relLiveMatch: RelativeLayout
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        tvBalance = binding.tvBalanceAcc
        tvLiability = binding.tvLiabilityAcc
        swipeRefreshLayout = binding.refresh
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                this,
                R.color.colorBlack
            )
        )
        swipeRefreshLayout.setColorSchemeColors(Color.YELLOW)
        if (intent.hasExtra(IntentKeys.matchName) && intent.getStringExtra(IntentKeys.matchName) != null) {
            binding.tvTitle.text = intent.getStringExtra(IntentKeys.matchName)!!
        }
        setBalanceAndLiability()

        binding.btnReloadData.setOnClickListener {
            if (isActive) {
                webSocket.close(1000, null)
                isActive = false
            }
            setBalanceAndLiability()
            loadData()
        }
        binding.ivBack.setOnClickListener { onBackPressed() }
        swipeRefreshLayout.setOnRefreshListener {
            if (isActive) {
                webSocket.close(1000, null)
                isActive = false
            }
            setBalanceAndLiability()
            loadData()
            swipeRefreshLayout.isRefreshing = false
        }
        swipeRefreshLayout.isEnabled = false
        relLiveMatch = binding.relLiveMatch
        webView = binding.webview
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.domStorageEnabled = true
        webView.settings.builtInZoomControls = true
        webView.settings.allowContentAccess = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.progress.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String) {
                matchUrl = url
                binding.progress.visibility = View.GONE
            }
        }
        webView.setOnTouchListener(this)
        binding.ivCloseMatch.setOnClickListener {
            binding.relLiveMatch.visibility = View.GONE
            webView.loadUrl("")
        }
    }

    override fun onResume() {
        super.onResume()
        marketIds = ""
        if (isActive) {
            webSocket.close(1000, null)
            isActive = false
        }
        if (AppConstants.matchClickedMarket) {
            marketIds = Config.getSharedPreferences(this, PreferenceKeys.matchMarketList)!!
        } else if (AppConstants.liabilityClickedMarket) {
            marketIds = Config.getSharedPreferences(this, PreferenceKeys.liabilityMarketList)!!
        }
        loadData()
    }

    @SuppressLint("SetTextI18n")
    private fun setBalanceAndLiability() {
        binding.tvBalanceAcc.text =
            getString(R.string.balance) + ": " + Config.getSharedPreferences(
                this,
                PreferenceKeys.balance
            )
        binding.tvLiabilityAcc.text =
            getString(R.string.credit) + ": " + Config.getSharedPreferences(
                this,
                PreferenceKeys.liability
            )
        binding.tvLiabilityAcc.setOnClickListener {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                binding.tvLiabilityAcc, "liability_transition"
            )
            val intent = Intent(this, LiabilityDetailActivity::class.java)
            intent.putExtra(IntentKeys.liabilityData, DashboardActivity.liabilityList)
            intent.putExtra(IntentKeys.data, marketIds)
            startActivity(intent, options.toBundle())
        }
    }

    private fun loadData() {
        if (Config.isInternetAvailable(this)) {
            binding.llData.visibility = View.VISIBLE
            binding.llNoInternet.visibility = View.GONE
//            callMarketDetail()
            callMatchedUnMatchedDetail()
            setupViewPager()
        } else {
            binding.llData.visibility = View.GONE
            binding.llNoInternet.visibility = View.VISIBLE
        }
    }

    private fun callMatchedUnMatchedDetail() {
        val jsonObject = JsonObject()
        jsonObject.addProperty(JsonKeys.MarketIds, marketIds)
        try {
            Config.showSmallProgressDialog(this)
            val call: Call<MatchedUnmatchedModel> =
                RetrofitApiClient.getMarketApiClient.getMatchedUnMatchedDetail(
                    Config.getSharedPreferences(this, PreferenceKeys.AuthToken),
                    jsonObject
                )
            call.enqueue(object : Callback<MatchedUnmatchedModel> {
                override fun onResponse(
                    call: Call<MatchedUnmatchedModel>?,
                    response: Response<MatchedUnmatchedModel>?
                ) {
                    val common: MatchedUnmatchedModel? = response?.body()
                    Log.e(TAG, "getMatchedUnMatchedDetail: " + Gson().toJson(response?.body()))
                    Config.hideSmallProgressDialog()
                    if (response != null) {
                        when (response.code()) {
                            200 -> {
                                when (common?.status?.code) {
                                    0 -> {
                                        matchedList = common.MatchedBetData
                                        unMatchedList = common.UnMatchedBetData
                                    }
                                    else -> {
                                        val realm = Realm.getDefaultInstance()
                                        realm.executeTransaction { realm -> realm.deleteAll() }
                                        Config.clearAllPreferences(this@SingleMarketActivity)
                                        startActivity(
                                            Intent(
                                                this@SingleMarketActivity,
                                                LoginActivity::class.java
                                            )
                                        )
                                        finish()
                                    }
                                }
                            }
                            401 -> {
                                val realm = Realm.getDefaultInstance()
                                realm.executeTransaction { realm -> realm.deleteAll() }
                                Config.clearAllPreferences(this@SingleMarketActivity)
                                startActivity(
                                    Intent(
                                        this@SingleMarketActivity,
                                        LoginActivity::class.java
                                    )
                                )
                                finish()
                            }
                        }
                    } else {
                        val realm = Realm.getDefaultInstance()
                        realm.executeTransaction { realm -> realm.deleteAll() }
                        Config.clearAllPreferences(this@SingleMarketActivity)
                        startActivity(Intent(this@SingleMarketActivity, LoginActivity::class.java))
                        finish()
                    }
                }

                override fun onFailure(call: Call<MatchedUnmatchedModel>?, t: Throwable?) {
                    Config.hideSmallProgressDialog()
                    Log.e(TAG, "getMatchedUnMatchedDetail: " + t.toString())
                }
            })
        } catch (e: Exception) {
            Config.toast(this, "" + e)
            Log.e(TAG, "" + e)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun parseData(jsonObject: JsonObject) {
        jsonArray.add(jsonObject)
//        Log.e(TAG, "socket data: $jsonObject")
        if (jsonObject.get("data").isJsonObject && jsonObject.get("data").asJsonObject.has("score")) {
            val runnable = Runnable {
                socketMarketFragment.updateSocketData(
                    jsonObject,
                    jsonObject.get("messageType").asString
                )
            }
            runOnUiThread(runnable)
        } else if (jsonObject.get("data").isJsonArray) {
            val data = jsonObject.get("data").asJsonArray[0].asJsonObject
            val runnable = Runnable {
                socketMarketFragment.updateSocketData(
                    data,
                    jsonObject.get("messageType").asString
                )
            }
            runOnUiThread(runnable)
        }
    }

    private fun setupViewPager() {
        viewPagerAdapter = MarketViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(
            socketMarketFragment, getString(R.string.markets)
        )
        viewPagerAdapter.addFragment(
            matchedFragment,
            getString(R.string.matched)
        )
        viewPagerAdapter.addFragment(
            unMatchedFragment,
            getString(R.string.unmatched)
        )
        binding.viewpager.offscreenPageLimit = 2
        binding.viewpager.adapter = viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewpager, false)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isActive) {
            webSocket.close(1000, null)
            isActive = false
        }
        webView.loadUrl("")
        finish()
    }

    private var dX: Float = 0f
    private var dY: Float = 0f
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val metrics = resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHight = metrics.heightPixels
        val duration = event.eventTime - event.downTime
        val newX: Float
        val newY: Float

        if (event.action == MotionEvent.ACTION_UP && duration < 109) {
            webView.loadUrl("")
            val intent = Intent(this, FullScreenLiveVideoActivity::class.java)
            intent.putExtra(IntentKeys.liveUrl, matchUrl)
            intent.putExtra(IntentKeys.title, "Single")
            startActivityForResult(intent, 9)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dX = relLiveMatch.x - event.rawX
                dY = relLiveMatch.y - event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                newX = event.rawX + dX
                newY = event.rawY + dY
                if ((relLiveMatch.layoutParams.width == screenWidth) &&
                    (screenHight == relLiveMatch.layoutParams.height)
                ) {
                } else {
                    relLiveMatch.animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
                    if (newX + relLiveMatch.layoutParams.width > screenWidth) {
                        relLiveMatch.animate()
                            .x(event.rawX + dX)
                            .setDuration(0)
                            .start()
                    }
                    if (newY + relLiveMatch.layoutParams.height > screenHight) {
                        relLiveMatch.animate()
                            .y(event.rawY + dY)
                            .setDuration(0)
                            .start()
                    }
                    if (newX < 0)
                        relLiveMatch.animate()
                            .x(0f)
                            .setDuration(0)
                            .start()
                    if (newY < 0)
                        relLiveMatch.animate()
                            .y(0f)
                            .setDuration(0)
                            .start()
                }
            }
            else -> return false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data!!.hasExtra(IntentKeys.liveUrl)) {
            relLiveMatch.visibility = View.VISIBLE
            webView.loadUrl(data.getStringExtra(IntentKeys.liveUrl)!!)
        } else {
            relLiveMatch.visibility = View.GONE
            webView.loadUrl("")
        }
    }
}