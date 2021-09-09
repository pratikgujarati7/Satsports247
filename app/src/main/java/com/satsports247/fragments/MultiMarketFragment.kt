package com.satsports247.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.satsports247.R
import com.satsports247.activities.DashboardActivity
import com.satsports247.activities.FullScreenLiveVideoActivity
import com.satsports247.activities.LoginActivity
import com.satsports247.adapters.MarketViewPagerAdapter
import com.satsports247.constants.*
import com.satsports247.dataModels.MatchedBetModel
import com.satsports247.databinding.FragmentMultiMarketBinding
import com.satsports247.fragments.multiMarketFragments.MatchedFragment
import com.satsports247.fragments.multiMarketFragments.SocketMarketFragment
import com.satsports247.fragments.multiMarketFragments.UnmatchedFragment
import com.satsports247.responseModels.Match
import com.satsports247.responseModels.MatchedUnmatchedModel
import com.satsports247.retrofit.RetrofitApiClient
import io.realm.Realm
import io.realm.RealmResults
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.java_websocket.client.WebSocketClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MultiMarketFragment : BaseFragment(), View.OnTouchListener {

    val TAG = "MultiMarketFragment"
    lateinit var fragmentBinding: FragmentMultiMarketBinding
    lateinit var realm: Realm
    lateinit var pinnedList: RealmResults<Match>
    var centralIds: String = ""
    var marketIds: String = ""
    var socketMarketFragment = SocketMarketFragment()
    var matchedFragment = MatchedFragment()
    var unMatchedFragment = UnmatchedFragment()
    var marketIDList = ArrayList<String>()
    var centralIDList = ArrayList<Int>()
    lateinit var mActivity: Activity
    var matchUrl = ""

    //    lateinit var viewPagerAdapter: MarketViewPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentBinding = FragmentMultiMarketBinding.inflate(inflater, container, false)
        val view: View = fragmentBinding.root
        init()
        return view
    }

    override fun frgInternetAvailable() {
        loadData()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        mActivity = requireActivity()
        Realm.init(requireContext())
        realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            pinnedList = realm.where(Match::class.java).findAll()
        }
        fragmentBinding.refresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(requireContext(), R.color.colorBlack)
        )
        fragmentBinding.refresh.setColorSchemeColors(Color.YELLOW)

        centralIds = ""
        if (pinnedList.size > 0)
            for (i in 0 until pinnedList.size) {
                marketIDList.add(pinnedList[i]?.MarketId!!)
            }
        marketIds = TextUtils.join(",", marketIDList)

        loadData()

        fragmentBinding.btnReloadData.setOnClickListener {
            loadData()
        }
        swipeRefreshLayout = fragmentBinding.refresh
        swipeRefreshLayout.setOnRefreshListener {
            if (isActive) {
                webSocket.close(1000, null)
                isActive = false
            }
            loadData()
            swipeRefreshLayout.isRefreshing = false
        }
        swipeRefreshLayout.isEnabled = false
        relLiveMatch = fragmentBinding.relLiveMatch
        webView = fragmentBinding.webview
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.domStorageEnabled = true
        webView.settings.builtInZoomControls = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                fragmentBinding.progress.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String) {
                matchUrl = url
                fragmentBinding.progress.visibility = View.GONE
            }
        }
        webView.setOnTouchListener(this)
        fragmentBinding.ivCloseMatch.setOnClickListener {
            fragmentBinding.relLiveMatch.visibility = View.GONE
            webView.loadUrl("")
        }

//        setupViewPager()
    }

    private fun loadData() {
        if (Config.isInternetAvailable(requireContext())) {
//            fragmentBinding.llData.visibility = View.VISIBLE
//            fragmentBinding.llNoInternet.visibility = View.GONE
//            callMarketDetail()
            callMatchedUnMatchedDetail()
            setupViewPager()
        } else {
            showAlertForInternet()
//            if (webSocket != null)
//                webSocket.cancel()
//            fragmentBinding.llData.visibility = View.GONE
//            fragmentBinding.llNoInternet.visibility = View.VISIBLE
        }
    }

    private fun callMatchedUnMatchedDetail() {
        val jsonObject = JsonObject()
        jsonObject.addProperty(JsonKeys.MarketIds, marketIds)
        Log.e(TAG, "callMatchedUnMatchedDetail:  $jsonObject")
        try {
            Config.showSmallProgressDialog(requireContext())
            val call: Call<MatchedUnmatchedModel> =
                RetrofitApiClient.getMarketApiClient.getMatchedUnMatchedDetail(
                    Config.getSharedPreferences(requireContext(), PreferenceKeys.AuthToken),
                    jsonObject
                )
            call.enqueue(object : Callback<MatchedUnmatchedModel> {
                override fun onResponse(
                    call: Call<MatchedUnmatchedModel>?,
                    response: Response<MatchedUnmatchedModel>?
                ) {
                    val common: MatchedUnmatchedModel? = response?.body()
                    Log.e(TAG, "code: " + response?.code())
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
                                    else -> Config.toast(
                                        requireContext(),
                                        common?.status?.returnMessage
                                    )
                                }
                            }
                            401 -> {
                                val realm = Realm.getDefaultInstance()
                                realm.executeTransaction { realm -> realm.deleteAll() }
                                Config.clearAllPreferences(requireContext())
                                startActivity(Intent(requireContext(), LoginActivity::class.java))
                                mActivity.finish()
                            }
                        }
                    } else {
                        val realm = Realm.getDefaultInstance()
                        realm.executeTransaction { realm -> realm.deleteAll() }
                        Config.clearAllPreferences(requireContext())
                        startActivity(Intent(requireContext(), LoginActivity::class.java))
                        mActivity.finish()
                    }
                }

                override fun onFailure(call: Call<MatchedUnmatchedModel>?, t: Throwable?) {
                    Config.hideSmallProgressDialog()
                    Log.e(TAG, "getMatchedUnMatchedDetail: " + t.toString())
                }
            })
        } catch (e: Exception) {
            Config.toast(requireContext(), "" + e)
            Log.e(TAG, "" + e)
        }
    }

    private fun setupViewPager() {
        viewPagerAdapter = MarketViewPagerAdapter(requireActivity().supportFragmentManager)
        viewPagerAdapter.addFragment(
            socketMarketFragment, getString(R.string.markets)
        )
        viewPagerAdapter.addFragment(matchedFragment, getString(R.string.matched))
        viewPagerAdapter.addFragment(unMatchedFragment, getString(R.string.unmatched))
        fragmentBinding.viewpager.offscreenPageLimit = 2
        fragmentBinding.viewpager.adapter = viewPagerAdapter
        fragmentBinding.tabLayout.setupWithViewPager(fragmentBinding.viewpager, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(): MultiMarketFragment {
            return MultiMarketFragment()
        }

        var okHttpClient: OkHttpClient = OkHttpClient()
        lateinit var webSocket: WebSocket
        lateinit var webSocketClient: WebSocketClient
        var socketTime: String = ""
        var matchedList = ArrayList<MatchedBetModel>()
        var unMatchedList = ArrayList<MatchedBetModel>()
        var jsonArray = JsonArray()
        lateinit var viewPagerAdapter: MarketViewPagerAdapter
        lateinit var swipeRefreshLayout: SwipeRefreshLayout
        var isActive = false
        lateinit var webView: WebView
        lateinit var relLiveMatch: RelativeLayout
    }

    private fun connectWebSocket() {
        try {
            val webUrl: String = AppConstants.socketUrl +
                    Config.getSharedPreferences(requireContext(), PreferenceKeys.Username)
            Log.e(TAG, "socket url: $webUrl")
            val request: Request = Request.Builder().url(webUrl).build()
            webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("action", "set")
                    jsonObject.addProperty("markets", centralIds)
                    Log.e(TAG, "connectWebSocket json: $jsonObject")
                    webSocket.send(jsonObject.toString())
                }

                override fun onMessage(webSocket: WebSocket, s: String) {
                    if (DashboardActivity.navigationMenu.menu.findItem(
                            DashboardActivity.navigationMenu.selectedItemId
                        ).title == "Multi-Market"
                    ) {
                        val df: DateFormat = SimpleDateFormat(AppConstants.ddMMMyyyyHHmmssSSS)
                        socketTime = "Time : " + df.format(Calendar.getInstance().time)
                        val str1: String = s.replace("\\", "")
                        val str2: String = str1.replace("\"[", "[")
                        val str3: String = str2.replace("]\"", "]")
                        val str4: String = str3.replace("}\"", "}")
                        val finalString: String = str4.replace("\"{", "{")
                        val jsonObject = Gson().fromJson(finalString, JsonObject::class.java)
                        parseData(jsonObject)
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    webSocket.close(1000, null)

                }

                override fun onFailure(
                    webSocket: WebSocket, t: Throwable, response: okhttp3.Response?
                ) {
                    Log.e(TAG, "onFailure: $t")
                    connectWebSocket()
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.e(TAG, "onClosed: $reason")
                }

            })
        } catch (e: Exception) {
            Log.e(TAG, "connectWebSocket: $e")
            return
        }
    }

    /* private fun connectWebSocket() {
         try {
             val webUrl: String = AppConstants.socketUrl +
                     Config.getSharedPreferences(requireContext(), PreferenceKeys.Username)
             Log.e(TAG, "socket url: $webUrl")
             val uri: URI
             uri = URI(webUrl)
             webSocketClient = object : WebSocketClient(uri) {
                 override fun onOpen(serverHandshake: ServerHandshake) {
                     val jsonObject = JsonObject()
                     jsonObject.addProperty("action", "set")
                     jsonObject.addProperty("markets", centralIds)
                     Log.e(TAG, "connectWebSocket json: $jsonObject")
                     webSocketClient.send(jsonObject.toString())
                 }

                 @SuppressLint("SimpleDateFormat")
                 override fun onMessage(s: String) {
                     if (DashboardActivity.navigationMenu.menu.findItem(
                             DashboardActivity.navigationMenu.selectedItemId
                         ).title == "Multi-Market"
                     ) {
                         val df: DateFormat = SimpleDateFormat(AppConstants.ddMMMyyyyHHmmssSSS)
                         socketTime = "Time : " + df.format(Calendar.getInstance().time)
                         val str1: String = s.replace("\\", "")
                         val str2: String = str1.replace("\"[", "[")
                         val str3: String = str2.replace("]\"", "]")
                         val str4: String = str3.replace("}\"", "}")
                         val finalString: String = str4.replace("\"{", "{")
                         val jsonObject = Gson().fromJson(finalString, JsonObject::class.java)
                         parseData(jsonObject)
                     }
                 }

                 override fun onClose(i: Int, s: String, b: Boolean) {
                     Log.e(TAG, "onClose $s")
                     val jsonObject = JsonObject()
                    jsonObject.addProperty("action", "unset")
                    jsonObject.addProperty("markets", centralIds)
                    webSocketClient.send(jsonObject.toString())
                    webSocketClient.close()
                }

                override fun onError(e: Exception) {

                    Log.e(TAG, "onError $e")
                    if (DashboardActivity.navigationMenu.menu.findItem(
                            DashboardActivity.navigationMenu.selectedItemId
                        ).title == "Multi-Market"
                    ) {
                        val runnable = Runnable { connectWebSocket() }
//                        Handler().postDelayed(runnable, 1000)
                        mActivity.runOnUiThread(runnable)
                    }
                }
            }
            webSocketClient.connect()

        } catch (e: Exception) {
            Log.e(TAG, "connectWebSocket: $e")
            return
        }
    }*/

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
            mActivity.runOnUiThread(runnable)
        } else if (jsonObject.get("data").isJsonArray) {
            val data = jsonObject.get("data").asJsonArray[0].asJsonObject
            val runnable = Runnable {
                socketMarketFragment.updateSocketData(
                    data,
                    jsonObject.get("messageType").asString
                )
            }
            mActivity.runOnUiThread(runnable)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isActive) {
            webSocket.close(1000, null)
            isActive = false
        }
        webView.loadUrl("")
    }

    private var dX: Float = 0f
    private var dY: Float = 0f
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val metrics = resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHight = metrics.heightPixels
        val duration = event.eventTime - event.downTime
        val newX: Float
        val newY: Float

        if (event.action == MotionEvent.ACTION_UP && duration < 109) {
            val intent = Intent(requireContext(), FullScreenLiveVideoActivity::class.java)
            intent.putExtra(IntentKeys.liveUrl, matchUrl)
            intent.putExtra(IntentKeys.title, "Multi")
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
//        DashboardActivity.ivBanner.visibility = View.GONE
        if (data!!.hasExtra(IntentKeys.liveUrl)) {
            relLiveMatch.visibility = View.VISIBLE
            webView.loadUrl(data.getStringExtra(IntentKeys.liveUrl)!!)
        } else {
            relLiveMatch.visibility = View.GONE
            webView.loadUrl("")
        }
    }
}