package com.satsports247.fragments.singleMarketFragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableArrayList
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.satsports247.R
import com.satsports247.activities.LoginActivity
import com.satsports247.activities.SingleMarketActivity
import com.satsports247.adapters.CurrentOverAdapter
import com.satsports247.adapters.SingleMarketAdapter
import com.satsports247.adapters.SingleMatchAdapter
import com.satsports247.adapters.SingleRunnerAdapter
import com.satsports247.constants.*
import com.satsports247.dataModels.DownlineModel
import com.satsports247.dataModels.MarketDataModel
import com.satsports247.dataModels.MarketModel
import com.satsports247.dataModels.RunnerModel
import com.satsports247.databinding.LayoutSocketDataBinding
import com.satsports247.responseModels.Common
import com.satsports247.retrofit.RetrofitApiClient
import io.realm.Realm
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SingleMarketFragment : Fragment() {

    val TAG: String = "SingleMarketFragment"
    lateinit var fragmentBinding: LayoutSocketDataBinding
    var marketIds: String = ""
    lateinit var socketMatchAdp: SingleMatchAdapter
    var centralIds: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentBinding = LayoutSocketDataBinding.inflate(inflater, container, false)
        fragmentBinding.lifecycleOwner = this.viewLifecycleOwner
        val view: View = fragmentBinding.root

        init()

        return view
    }

    private fun init() {
        marketIds = SingleMarketActivity.marketIds
        socketMatchAdp = SingleMatchAdapter()

        fragmentBinding.refresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(requireContext(), R.color.colorBlack)
        )
        fragmentBinding.refresh.setColorSchemeColors(Color.YELLOW)
        fragmentBinding.refresh.setOnRefreshListener {
            if (SingleMarketActivity.isActive) {
                SingleMarketActivity.webSocket.close(1000, null)
                SingleMarketActivity.isActive = false
            }
            callMarketDetail()
            fragmentBinding.refresh.isRefreshing = false
        }

        callMarketDetail()
    }

    private fun updateUI() {
        if (marketList.size > 0) {
            fragmentBinding.recyclerMarketList.visibility = View.VISIBLE
            fragmentBinding.tvNoData.visibility = View.GONE
            for (i in 0 until marketList.size) {
                val model: MarketDataModel = marketList[i]
                if (!model.isFancyMarket && model.IsInPlay) {
                    val scoreApi = UrlConstants.scoreApi + marketList[i].BfMatchId
                    val call: Call<Common> =
                        RetrofitApiClient.getMarketApiClient.getScoreUrl(
                            scoreApi,
                            Config.getSharedPreferences(
                                requireContext(),
                                PreferenceKeys.AuthToken
                            )!!
                        )
                    call.enqueue(object : Callback<Common> {
                        override fun onResponse(
                            call: Call<Common>?,
                            response: Response<Common>?
                        ) {
                            Log.e(
                                Config.TAG, "getScoreUrl: " + Gson().toJson(response?.body())
                            )
                            val common: Common? = response?.body()
                            if (response != null && response.isSuccessful) {
                                Config.hideSmallProgressDialog()
                                when (common?.status?.code) {
                                    0 -> {
                                        if (common.ScoreUrl != null && common.ScoreUrl != "") {
                                            val scoreUrl = common.ScoreUrl
                                            model.ScoreUrl = scoreUrl
                                        }
                                        if (common.StreamingUrl != null && common.StreamingUrl != "") {
                                            val streamingUrl = common.StreamingUrl
                                            model.StreamingUrl = streamingUrl
                                        }
                                    }
                                }
                                marketList[i] = model
                                socketMatchAdp.submitList(marketList)
                                fragmentBinding.recyclerMarketList.adapter =
                                    socketMatchAdp
                            }
                        }

                        override fun onFailure(
                            call: Call<Common>?,
                            t: Throwable?
                        ) {
                            Config.hideSmallProgressDialog()
                            Log.e(Config.TAG, "getScoreUrl: " + t.toString())
                        }
                    })
                }
            }

            if (centralIds.isNotEmpty()) {
                SingleMarketActivity.isActive = true
                connectWebSocket()
            }
        } else {
            fragmentBinding.recyclerMarketList.visibility = View.GONE
            fragmentBinding.tvNoData.visibility = View.VISIBLE
        }
    }

    private fun callMarketDetail() {
        marketList.clear()
        val jsonObject = JsonObject()
        jsonObject.addProperty(JsonKeys.MarketIds, marketIds)
        Log.e(TAG, "jsonObject: $jsonObject")
        if (Config.isInternetAvailable(requireContext())) {
            Config.showSmallProgressDialog(requireContext())
            val call: Call<Common> = RetrofitApiClient.getClient.getMarketDetail(
                Config.getSharedPreferences(requireContext(), PreferenceKeys.AuthToken), jsonObject
            )
            call.enqueue(object : Callback<Common> {
                override fun onResponse(call: Call<Common>, response: Response<Common>) {
                    Log.e(TAG, "code: " + response.code())
                    Log.e(TAG, "getMarketDetail: " + Gson().toJson(response.body()))
                    val common: Common? = response.body()
                    if (response != null && response.isSuccessful) {
                        Config.hideSmallProgressDialog()
                        when (response.code()) {
                            200 -> {
                                when (common?.status?.code) {
                                    0 -> {
                                        val centralIDList = ArrayList<Int>()
                                        for (i in 0 until common.MarketData.size) {
                                            val model: MarketDataModel = common.MarketData[i]
                                            model.isFancyMarket = false
                                            if (common.MarketData[i].Market.size > 0)
                                                model.IsInPlay =
                                                    common.MarketData[i].Market[0].IsInPlay
                                            marketList.add(model)
                                            for (j in 0 until common.MarketData[i].Market.size) {
                                                centralIDList.add(common.MarketData[i].Market[j].CentralId)
                                            }
                                        }
                                        for (i in 0 until common.FancyMarketData.size) {
                                            val model: MarketDataModel = common.FancyMarketData[i]
                                            model.isFancyMarket = true
                                            if (common.FancyMarketData[i].Market.size > 0)
                                                model.IsInPlay =
                                                    common.FancyMarketData[i].Market[0].IsInPlay
                                            marketList.add(model)
                                            for (j in 0 until common.FancyMarketData[i].Market.size) {
                                                centralIDList.add(common.FancyMarketData[i].Market[j].CentralId)
                                            }
                                        }
                                        Log.e(TAG, "marketList: " + marketList.size)

                                        centralIds = TextUtils.join(",", centralIDList)

                                        DownLineData = common.Downline
                                        socketMatchAdp.submitList(marketList)
                                        fragmentBinding.recyclerMarketList.adapter = socketMatchAdp
                                        updateUI()
                                    }
                                    else ->
                                        Config.toast(
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
                                activity!!.finish()
                            }
                        }
                    } else {
                        val realm = Realm.getDefaultInstance()
                        realm.executeTransaction { realm -> realm.deleteAll() }
                        Config.clearAllPreferences(requireContext())
                        startActivity(Intent(requireContext(), LoginActivity::class.java))
                        activity!!.finish()
                    }
                }

                override fun onFailure(call: Call<Common>?, t: Throwable?) {
                    Config.hideSmallProgressDialog()
                    Log.e(TAG, "getMarketDetail: " + t.toString())
                }
            })
        } else {
            Config.hideSmallProgressDialog()
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun updateSocketData(data: JsonObject, messageType: String) {
        isSocketOn = true
//        Log.e(TAG, "updateSocketData:  $data")
        try {
            if (messageType == JsonKeys.score) {
                for (i in 0 until marketList.size) {
                    val match = marketList[i]
                    if (TextUtils.isEmpty(match.ScoreUrl))
                        for (k in 0 until marketList[i].Market.size) {
                            val marketModel = marketList[i].Market[k]
                            if (data.get(JsonKeys.centralId).asString == "" + marketModel.CentralId) {
                                val score =
                                    data.get(JsonKeys.data).asJsonObject.get(JsonKeys.score).asJsonObject
                                if (score.has(JsonKeys.required_runs)) {
                                    match.inningType.set(getString(R.string.innings_2))
                                    if (!score.get(JsonKeys.required_balls).isJsonNull)
                                        match.requiredText.set(
                                            "Need " + score.get(JsonKeys.required_runs).asInt
                                                    + " in " + score.get(JsonKeys.required_balls).asInt + " balls."
                                        )
                                    else
                                        match.requiredText.set(
                                            "Need " + score.get(JsonKeys.required_runs).asInt + "."
                                        )
                                    if (!score.get(JsonKeys.required_run_rate).isJsonNull)
                                        match.requiredRunRate.set("RRR: " + score.get(JsonKeys.required_run_rate).asFloat)
                                    match.requiredRuns.set("RR: " + score.get(JsonKeys.required_runs).asInt)
                                } else
                                    match.inningType.set(getString(R.string.innings_1))
                                if (score.get(JsonKeys.status).asString.equals(
                                        JsonKeys.in_play, true
                                    )
                                ) {
                                    val teamA = score.get(JsonKeys.team_a).asJsonObject
                                    val teamB = score.get(JsonKeys.team_b).asJsonObject
                                    match.inPlay.set(true)
                                    //set batting team data
                                    if (score.get(JsonKeys.batting_team).asString.equals(
                                            teamA.get(JsonKeys.name).asString,
                                            true
                                        )
                                    ) {
                                        match.battingTeam.set(teamA.get(JsonKeys.name).asString)
//                                    match.inningType.set("Innings 1")
                                        val inning1 = teamA.get(JsonKeys.innings_1).asJsonObject
                                        val rwo: String =
                                            inning1.get(JsonKeys.runs).asString + "/" +
                                                    inning1.get(JsonKeys.wickets).asString +
                                                    "  (" +
                                                    inning1.get(JsonKeys.overs).asString + ")"
                                        match.runWicketOverBatT.set(rwo)
                                    } else if (score.get(JsonKeys.batting_team).asString.equals(
                                            teamB.get(JsonKeys.name).asString,
                                            true
                                        )
                                    ) {
                                        match.battingTeam.set(teamB.get(JsonKeys.name).asString)
//                                    match.inningType.set("Innings 1")
                                        val inning1 = teamB.get(JsonKeys.innings_1).asJsonObject
                                        val rwo: String =
                                            inning1.get(JsonKeys.runs).asString + "/" +
                                                    inning1.get(JsonKeys.wickets).asString + "  (" +
                                                    inning1.get(JsonKeys.overs).asString + ")"
                                        match.runWicketOverBatT.set(rwo)
                                    }
                                    //set bowling team data
                                    if (score.get(JsonKeys.bowling_team).asString.equals(
                                            teamA.get(JsonKeys.name).asString,
                                            true
                                        )
                                    ) {
                                        match.bowlingTeam.set(teamA.get(JsonKeys.name).asString)
                                        val inning1 = teamA.get(JsonKeys.innings_1).asJsonObject
                                        val rwo: String =
                                            inning1.get(JsonKeys.runs).asString + "/" +
                                                    inning1.get(JsonKeys.wickets).asString + "  (" +
                                                    inning1.get(JsonKeys.overs).asString + ")"
                                        match.runWicketOverBowlT.set(rwo)
                                    } else if (score.get(JsonKeys.bowling_team).asString.equals(
                                            teamB.get(JsonKeys.name).asString,
                                            true
                                        )
                                    ) {
                                        match.bowlingTeam.set(teamB.get(JsonKeys.name).asString)
                                        val inning1 = teamB.get(JsonKeys.innings_1).asJsonObject
                                        val rwo: String =
                                            inning1.get(JsonKeys.runs).asString + "/" +
                                                    inning1.get(JsonKeys.wickets).asString + "  (" +
                                                    inning1.get(JsonKeys.overs).asString + ")"
                                        match.runWicketOverBowlT.set(rwo)
                                    }
                                    match.tossStatus.set(score.get(JsonKeys.toss).asString)

                                    //Detailed score data
                                    match.runRate.set("CRR: " + score.get(JsonKeys.run_rate).asString)
                                    //Overs data
                                    if (!score.get(JsonKeys.last_overs).isJsonNull) {
                                        val currentOversList = score.get(JsonKeys.last_overs)
                                            .asJsonArray[0].asJsonArray[1].asJsonArray
                                        val co: ArrayList<String> = ArrayList()
                                        for (i in 0 until currentOversList.size()) {
                                            co.add(currentOversList[i].asString)
                                        }
                                        match.currentOverRuns.set(co)
                                        if (score.get(JsonKeys.last_overs)
                                                .asJsonArray.size() > 1
                                        ) {
                                            val lastOversList = score.get(JsonKeys.last_overs)
                                                .asJsonArray[1].asJsonArray[1].asJsonArray
                                            val lo: ArrayList<String> = ArrayList()
                                            for (i in 0 until lastOversList.size()) {
                                                lo.add(lastOversList[i].asString)
                                            }
                                            match.lastOverRuns.set(lo)
                                        }
                                    }
                                    if (score.get(JsonKeys.striker).asJsonObject
                                            .has(JsonKeys.name) && score.get(JsonKeys.striker).asJsonObject
                                            .get(JsonKeys.name) != null
                                    ) {
                                        match.strikerName.set(
                                            score.get(JsonKeys.striker).asJsonObject
                                                .get(JsonKeys.name).asString + " *"
                                        )
                                    } else
                                        match.strikerName.set("-")

                                    if (score.get(JsonKeys.nonstriker).asJsonObject
                                            .has(JsonKeys.name) && score.get(JsonKeys.nonstriker).asJsonObject
                                            .get(JsonKeys.name) != null
                                    ) {
                                        match.nonStrikername.set(
                                            score.get(JsonKeys.nonstriker).asJsonObject
                                                .get(JsonKeys.name).asString
                                        )
                                    } else
                                        match.nonStrikername.set("-")

                                    if (score.get(JsonKeys.bowler).asJsonObject
                                            .has(JsonKeys.name) && score.get(JsonKeys.bowler).asJsonObject
                                            .get(JsonKeys.name) != null
                                    ) {
                                        match.bowler.set(
                                            score.get(JsonKeys.bowler).asJsonObject
                                                .get(JsonKeys.name).asString
                                        )
                                    } else
                                        match.bowler.set("-")

                                    val striker = score.get(JsonKeys.striker).asJsonObject
                                    if (striker.has(JsonKeys.score)) {
                                        match.runsStriker.set(
                                            striker.get(JsonKeys.score).asJsonObject.get(JsonKeys.runs).asString +
                                                    "(" + striker.get(JsonKeys.score).asJsonObject
                                                .get(JsonKeys.balls).asString + ")"
                                        )
                                    }

                                    val nonStriker = score.get(JsonKeys.nonstriker).asJsonObject
                                    if (nonStriker.has(JsonKeys.score)) {
                                        match.runsNonStriker.set(
                                            nonStriker.get(JsonKeys.score).asJsonObject.get(JsonKeys.runs).asString +
                                                    "(" + nonStriker.get(JsonKeys.score).asJsonObject
                                                .get(JsonKeys.balls).asString + ")"
                                        )
                                    }

                                    val bowler = score.get(JsonKeys.bowler).asJsonObject
                                    if (bowler.has(JsonKeys.score)) {
                                        match.runsBowler.set(
                                            bowler.get(JsonKeys.score).asJsonObject.get(JsonKeys.wickets).asString +
                                                    "-" + bowler.get(JsonKeys.score).asJsonObject.get(
                                                JsonKeys.runs
                                            ).asString +
                                                    "(" + bowler.get(JsonKeys.score).asJsonObject.get(
                                                JsonKeys.overs
                                            ).asString + ")"
                                        )
                                    }
                                    break
                                } else
                                    match.inPlay.set(false)
                            }
                        }
                }
            } else if (isSocketOn && context != null) {
                for (j in 0 until marketList.size) {
                    for (k in 0 until marketList[j].Market.size) {
                        val marketModel = marketList[j].Market[k]
//                        marketModel.socketTime.set(SingleMarketActivity.socketTime)
                        when (marketModel.MarketStatus) {
                            3 -> {
                                marketModel.suspendedText.set(getString(R.string.suspended))
                                marketModel.showSuspended.set(true)
                                for (a in 0 until marketModel.Runner.size) {
                                    marketModel.Runner[a].keyboardOpen.set(false)
                                }
                            }
                            9 -> {
                                /* marketModel.suspendedText.set(getString(R.string.ball_started))
                                 marketModel.showSuspended.set(true)
                                 for (a in 0 until marketModel.Runner.size) {
                                     marketModel.Runner[a].keyboardOpen.set(false)
                                 }*/
                            }
                            4 -> {
                                marketModel.suspendedText.set(getString(R.string.closed))
                                marketModel.showSuspended.set(true)
                                for (a in 0 until marketModel.Runner.size) {
                                    marketModel.Runner[a].keyboardOpen.set(false)
                                }
                            }
                        }
                        if (marketModel.CentralId == data.get(JsonKeys.appMarketID).asInt) {
                            socketModel = data
                            val time = SimpleDateFormat(AppConstants.ddMMMyyyyHHmmssSSS).format(
                                SimpleDateFormat(AppConstants.yyyyMMddTHHmmSS).parse(
                                    Config.convertIntoLocal(
                                        Config.stringToDate(
                                            socketModel.get(JsonKeys.appGetRecordTime).asString,
                                            AppConstants.yyyyMMddTHHmmSS
                                        )!!,
                                        AppConstants.yyyyMMddTHHmmSS
                                    )
                                )!!
                            )
//                            marketModel.socketTime.set("Time : $time")
                            /*marketStatus: 1-Open, 9-Ball Started,4-Closed, 3-Suspended*/
                            when (socketModel.get(JsonKeys.appMarketStatus).asString) {
                                "1" -> {
                                    marketModel.showSuspended.set(false)
                                    if (!socketModel.get(JsonKeys.appRate).isJsonNull) {
                                        //time when rate update
                                        marketModel.socketTime.set(SingleMarketActivity.socketTime)
                                        socketAppRateArray =
                                            socketModel.get(JsonKeys.appRate).asJsonArray
                                        for (a in 0 until marketModel.Runner.size) {
                                            var isBackDone = false
                                            var isLayDone = false
                                            val runnerModel = marketModel.Runner[a]
                                            val runnable = Runnable {
                                                runnerModel.changeBackBgColor.set(
                                                    false
                                                )
                                                runnerModel.changeLayBgColor.set(
                                                    false
                                                )
                                            }
                                            if (marketModel.MarketType.equals(
                                                    AppConstants.Bookmakers,
                                                    true
                                                )
                                            ) {
                                                runnerModel.backRate.set("-")
                                                runnerModel.backBFVolume.set("---")
                                                runnerModel.layRate.set("-")
                                                runnerModel.layBFVolume.set("---")
                                            }
                                            for (x in 0 until socketAppRateArray.size()) {
                                                if (isBackDone && isLayDone)
                                                    break
                                                else if (socketAppRateArray[x].asJsonObject.get(
                                                        JsonKeys.appIsBack
                                                    ).asBoolean
                                                ) {
                                                    if (!isBackDone) {
                                                        if (runnerModel.BfRunnerId == socketAppRateArray[x]
                                                                .asJsonObject.get(JsonKeys.appSelectionID_BF).asLong
                                                        ) {
                                                            var appRate =
                                                                "" + socketAppRateArray[x].asJsonObject.get(
                                                                    JsonKeys.appRate
                                                                )
                                                            appRate =
                                                                appRate.replace("\"", "")
                                                            if (appRate.toDouble() <= 1 || appRate == "0")
                                                                appRate = "-"
                                                            /*if (!appRate.equals(
                                                                    runnerModel.backRate.get(),
                                                                    true)) {
                                                                runnerModel.changeBackBgColor.set(
                                                                    true
                                                                )
                                                                val handler = Handler()
                                                                handler.postDelayed(runnable, 50)
                                                            }*/
                                                            runnerModel.backRate.set("" + appRate)

                                                            //Back rate, For markets other than Fancy
                                                            if (messageType != JsonKeys.fancy ||
                                                                marketModel.MarketType.equals(
                                                                    AppConstants.Bookmakers,
                                                                    true
                                                                ) || marketModel.MarketType.equals(
                                                                    AppConstants.ManualOdds,
                                                                    true
                                                                )
                                                            ) {
                                                                runnerModel.backBFVolume.set(
                                                                    "" + Math.round(
                                                                        socketAppRateArray[x].asJsonObject.get(
                                                                            JsonKeys.appBFVolume
                                                                        ).asDouble
                                                                    )
                                                                )
                                                            } else if (messageType == JsonKeys.fancy) {
                                                                runnerModel.backBFVolume.set(
                                                                    "" + socketAppRateArray[x].asJsonObject.get(
                                                                        JsonKeys.appPoint
                                                                    ).asInt
                                                                )
                                                            } else {
                                                                runnerModel.backBFVolume.set(
                                                                    "" + Math.round(
                                                                        socketAppRateArray[x].asJsonObject.get(
                                                                            JsonKeys.appBFVolume
                                                                        ).asDouble
                                                                    )
                                                                )
                                                            }
                                                            isBackDone = true
                                                        } else {
                                                            if (marketModel.MarketType.equals(
                                                                    AppConstants.Bookmakers,
                                                                    true
                                                                )
                                                            ) {
                                                                runnerModel.backRate.set("-")
                                                                runnerModel.backBFVolume.set("---")
                                                                runnerModel.layRate.set("-")
                                                                runnerModel.layBFVolume.set("---")
                                                            }
                                                        }
                                                    }
                                                } else if (!isLayDone) {
                                                    if (runnerModel.BfRunnerId == socketAppRateArray[x].asJsonObject.get(
                                                            JsonKeys.appSelectionID_BF
                                                        ).asLong
                                                    ) {
                                                        var appRate =
                                                            "" + socketAppRateArray[x].asJsonObject.get(
                                                                JsonKeys.appRate
                                                            )
                                                        appRate = appRate.replace("\"", "")
                                                        if (appRate.toDouble() <= 1 || appRate == "0")
                                                            appRate = "-"
                                                        /*if (!appRate.equals(
                                                                runnerModel.layRate.get(),
                                                                true)) {
                                                            runnerModel.changeLayBgColor.set(
                                                                true
                                                            )
                                                            val handler = Handler()
                                                            handler.postDelayed(runnable, 100)
                                                        }*/
                                                        runnerModel.layRate.set("" + appRate)

                                                        //Lay rate, For markets other than Fancy
                                                        if (messageType != JsonKeys.fancy || marketModel.MarketType.equals(
                                                                AppConstants.Bookmakers,
                                                                true
                                                            ) || marketModel.MarketType.equals(
                                                                AppConstants.ManualOdds,
                                                                true
                                                            )
                                                        ) {
                                                            runnerModel.layBFVolume.set(
                                                                "" + Math.round(
                                                                    socketAppRateArray[x].asJsonObject.get(
                                                                        JsonKeys.appBFVolume
                                                                    ).asDouble
                                                                )
                                                            )
                                                        } else if (messageType == JsonKeys.fancy) {
                                                            runnerModel.layBFVolume.set(
                                                                "" + socketAppRateArray[x].asJsonObject.get(
                                                                    JsonKeys.appPoint
                                                                ).asInt
                                                            )
                                                        } else {
                                                            runnerModel.layBFVolume.set(
                                                                "" + Math.round(
                                                                    socketAppRateArray[x].asJsonObject.get(
                                                                        JsonKeys.appBFVolume
                                                                    ).asDouble
                                                                )
                                                            )
                                                        }
                                                        isLayDone = true
                                                    } else {
                                                        if (marketModel.MarketType.equals(
                                                                AppConstants.Bookmakers,
                                                                true
                                                            )
                                                        ) {
                                                            runnerModel.backRate.set("-")
                                                            runnerModel.backBFVolume.set("---")
                                                            runnerModel.layRate.set("-")
                                                            runnerModel.layBFVolume.set("---")
                                                        }
                                                    }
                                                }
                                            }
                                            marketModel.Runner[a] = runnerModel
                                        }
                                    }
                                }
                                "9" -> {
                                    marketModel.suspendedText.set(getString(R.string.ball_started))
                                    marketModel.showSuspended.set(true)
                                    for (a in 0 until marketModel.Runner.size) {
                                        marketModel.Runner[a].keyboardOpen.set(false)
                                    }
                                }
                                "4" -> {
                                    marketModel.suspendedText.set(getString(R.string.closed))
                                    marketModel.showSuspended.set(true)
                                    for (a in 0 until marketModel.Runner.size) {
                                        marketModel.Runner[a].keyboardOpen.set(false)
                                    }
//                                    SocketMarketFragment.marketList[j].Market.remove(marketModel)
                                    break
                                }
                                "3" -> {
                                    marketModel.suspendedText.set(getString(R.string.suspended))
                                    marketModel.showSuspended.set(true)
                                    for (a in 0 until marketModel.Runner.size) {
                                        marketModel.Runner[a].keyboardOpen.set(false)
                                    }
                                }
                            }
                        }
                        marketList[j].Market[k] = marketModel
                    }
                }
            }
        } catch (e: Exception) {
//            Config.toast(requireContext(), "" + e)
            Log.e(TAG, "updateSocketData: $e")
        }
    }

    companion object {
        var marketList = ObservableArrayList<MarketDataModel>()
        var socketModel = JsonObject()
        var socketAppRateArray = JsonArray()
        var DownLineData = DownlineModel()
        var isSocketOn = false

        @JvmStatic
        fun newInstance(): SingleMarketFragment {
            return SingleMarketFragment()
        }
    }

    private fun connectWebSocket() {
        try {
            val webUrl: String = AppConstants.socketUrl +
                    Config.getSharedPreferences(requireContext(), PreferenceKeys.Username)
            Log.e(TAG, "socket url: $webUrl")
            val request: Request = Request.Builder().url(webUrl).build()
            SingleMarketActivity.webSocket = SingleMarketActivity.okHttpClient.newWebSocket(
                request,
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                        val jsonObject = JsonObject()
                        jsonObject.addProperty("action", "set")
                        jsonObject.addProperty("markets", centralIds)
                        Log.e(TAG, "connectWebSocket json: $jsonObject")
                        webSocket.send(jsonObject.toString())
                    }

                    override fun onMessage(webSocket: WebSocket, s: String) {
                        if (SingleMarketActivity.isActive
                        ) {
                            val df: DateFormat = SimpleDateFormat(AppConstants.ddMMMyyyyHHmmssSSS)
                            SingleMarketActivity.socketTime =
                                "Time : " + df.format(Calendar.getInstance().time)
                            /*val score = "{" +
                                    "  \"data\": {" +
                                    "    \"score\": {" +
                                    "      \"key\": \"ausind_2020_t20_03\"," +
                                    "      \"status\": \"in_play\"," +
                                    "      \"status_text\": \"In play\"," +
                                    "      \"run_rate\": \"8.07\"," +
                                    "      \"required_runs\": 148," +
                                    "      \"team_a\": {" +
                                    "        \"name\": \"Australia\"," +
                                    "        \"innings_1\": {" +
                                    "          \"runs\": 186," +
                                    "          \"wickets\": 5," +
                                    "          \"overs\": \"20.0\"" +
                                    "        }" +
                                    "      }," +
                                    "      \"batting_team\": \"India\"," +
                                    "      \"bowling_team\": \"Australia\"," +
                                    "      \"day\": \"\"," +
                                    "      \"trail_by_str\": null," +
                                    "      \"team_b\": {" +
                                    "        \"name\": \"India\"," +
                                    "        \"innings_1\": {" +
                                    "          \"runs\": 39," +
                                    "          \"wickets\": 1," +
                                    "          \"overs\": \"4.5\"" +
                                    "        }" +
                                    "      }," +
                                    "      \"toss\": \"India won the toss and chose to bowl first\"," +
                                    "      \"striker\": {" +
                                    "        \"name\": \"Virat Kohli\"," +
                                    "        \"score\": {" +
                                    "          \"dismissed\": false," +
                                    "          \"dots\": 2," +
                                    "          \"sixes\": 0," +
                                    "          \"runs\": 24," +
                                    "          \"balls\": 17," +
                                    "          \"fours\": 2," +
                                    "          \"strike_rate\": 141.18" +
                                    "        }" +
                                    "      }," +
                                    "      \"nonstriker\": {" +
                                    "        \"name\": \"Shikhar Dhawan\"," +
                                    "        \"score\": {" +
                                    "          \"dismissed\": false," +
                                    "          \"dots\": 0," +
                                    "          \"sixes\": 0," +
                                    "          \"runs\": 9," +
                                    "          \"balls\": 10," +
                                    "          \"fours\": 0," +
                                    "          \"strike_rate\": 90" +
                                    "        }" +
                                    "      }," +
                                    "      \"bowler\": {" +
                                    "        \"name\": \"AJ Tye\"," +
                                    "        \"score\": {" +
                                    "          \"dots\": 2," +
                                    "          \"runs\": 6," +
                                    "          \"fours\": 1," +
                                    "          \"sixes\": 0," +
                                    "          \"balls\": 5," +
                                    "          \"maiden_overs\": 0," +
                                    "          \"wickets\": 0," +
                                    "          \"extras\": 0," +
                                    "          \"extras_wide\": 0," +
                                    "          \"extras_noball\": 0," +
                                    "          \"overs\": \"0.5\"," +
                                    "          \"economy\": 7.2" +
                                    "        }" +
                                    "      }," +
                                    "      \"required_run_rate\": 9.76," +
                                    "      \"required_balls\": 91," +
                                    "      \"last_overs\": [" +
                                    "        [" +
                                    "          5," +
                                    "          [" +
                                    "            \"1\"," +
                                    "            \"1\"," +
                                    "            \"0\"," +
                                    "            \"~4\"," +
                                    "            \"0\"" +
                                    "          ]" +
                                    "        ]," +
                                    "        [" +
                                    "          4," +
                                    "          [" +
                                    "            \"~4\"," +
                                    "            \"1\"," +
                                    "            \"1\"," +
                                    "            \"1wd\"," +
                                    "            \"1\"," +
                                    "            \"1by\"," +
                                    "            \"1\"" +
                                    "          ]" +
                                    "        ]," +
                                    "        [" +
                                    "          3," +
                                    "          [" +
                                    "            \"1\"," +
                                    "            \"2wd\"," +
                                    "            \"1\"," +
                                    "            \"1\"," +
                                    "            \"1\"," +
                                    "            \"1\"," +
                                    "            \"1\"" +
                                    "          ]" +
                                    "        ]" +
                                    "      ]" +
                                    "    }" +
                                    "  }," +
                                    "  \"centralId\": \"22098\"," +
                                    "  \"messageType\": \"score\"" +
                                    "}"
                            val str5: String = score.replace("\\", "")
                            val str6: String = str5.replace("\"[", "[")
                            val str7: String = str6.replace("]\"", "]")
                            val str8: String = str7.replace("}\"", "}")
                            val scoreString: String = str8.replace("\"{", "{")
                            val jsonScore = Gson().fromJson(scoreString, JsonObject::class.java)
                            parseData(jsonScore)*/
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

    private fun parseData(jsonObject: JsonObject) {
        SingleMarketActivity.jsonArray.add(jsonObject)
//        Log.e(TAG, "socket data: $jsonObject")
        if (jsonObject.get("data").isJsonObject && jsonObject.get("data").asJsonObject.has("score")) {
            val runnable = Runnable {
                updateSocketData(
                    jsonObject,
                    jsonObject.get("messageType").asString
                )
            }
            requireActivity().runOnUiThread(runnable)
        } else if (jsonObject.get("data").isJsonArray) {
            val data = jsonObject.get("data").asJsonArray[0].asJsonObject
            val runnable = Runnable {
                updateSocketData(
                    data,
                    jsonObject.get("messageType").asString
                )
            }
            requireActivity().runOnUiThread(runnable)
        }
    }
}

@BindingAdapter(value = ["setSingleMarkets"])
fun RecyclerView.setMarkets(markets: List<MarketModel>?) {
    this.run {
        if (markets != null) {
            val marketAdapter = SingleMarketAdapter()
            marketAdapter.submitList(markets)
            adapter = marketAdapter
        }
    }
}

@BindingAdapter(value = ["setSingleRunners"])
fun RecyclerView.setRunners(runners: List<RunnerModel>?) {
    this.run {
        if (runners != null) {
            val runnerAdapter = SingleRunnerAdapter()
            runnerAdapter.submitList(runners)
            this.adapter = runnerAdapter
        }
    }
}

@BindingAdapter(value = ["setCurrentOvers"])
fun RecyclerView.setCurrentOvers(runs: List<String>?) {
    this.run {
        if (runs != null) {
            val marketAdapter = CurrentOverAdapter()
            marketAdapter.submitList(runs)
            adapter = marketAdapter
        }
    }
}

@BindingAdapter(value = ["setLastOvers"])
fun RecyclerView.setLastOvers(runs: List<String>?) {
    this.run {
        if (runs != null) {
            val marketAdapter = CurrentOverAdapter()
            marketAdapter.submitList(runs)
            adapter = marketAdapter
        }
    }
}
