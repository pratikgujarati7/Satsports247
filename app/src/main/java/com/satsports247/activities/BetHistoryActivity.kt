package com.satsports247.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.satsports247.R
import com.satsports247.adapters.BetHistoryAdapter
import com.satsports247.constants.*
import com.satsports247.dataModels.BetHistoryModel
import com.satsports247.dataModels.RoundDataModel
import com.satsports247.databinding.ActivityBetHistoryBinding
import com.satsports247.responseModels.Common
import com.satsports247.retrofit.RetrofitApiClient
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BetHistoryActivity : AppCompatActivity() {

    val TAG: String = "BetHistoryActivity"
    lateinit var binding: ActivityBetHistoryBinding
    var betHistoryList = ArrayList<BetHistoryModel>()
    val avgList = ArrayList<BetHistoryModel>()
    lateinit var adapter: BetHistoryAdapter
    var type = ""
    var sportname = ""

    companion object {
        var roundDataModel = RoundDataModel()
        var description = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBetHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        type = intent.getStringExtra(IntentKeys.data)!!
        binding.recyclerBetDetail.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL, false
        )
        adapter = BetHistoryAdapter(betHistoryList, this, binding.recyclerBetDetail, type)
        binding.recyclerBetDetail.adapter = adapter

        if (intent.hasExtra(IntentKeys.sportName))
            sportname = intent.getStringExtra(IntentKeys.sportName)!!
        if (intent.hasExtra(IntentKeys.description))
            description = intent.getStringExtra(IntentKeys.description)!!
        if (type.equals(AppConstants.PLActivity, true)) {
            binding.llAverage.visibility = View.VISIBLE
            binding.llAvgData.visibility = View.VISIBLE
            binding.llMarketName.visibility = View.GONE
        } else if (type.equals(AppConstants.StatementActivity, true)) {
            binding.llAverage.visibility = View.GONE
            binding.llAvgData.visibility = View.GONE
            binding.llMarketName.visibility = View.VISIBLE
            binding.tvMarketName.text = intent.getStringExtra(IntentKeys.description)
        }
        val marketID = intent.getStringExtra(IntentKeys.marketID)!!
        val siteTypeId = intent.getIntExtra(IntentKeys.siteTypeID, 0)

        getBetHistory(marketID, siteTypeId)

        binding.refresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                this,
                R.color.colorBlack
            )
        )
        binding.refresh.setColorSchemeColors(Color.YELLOW)
        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.btnReloadData.setOnClickListener {
            getBetHistory(marketID, siteTypeId)
        }
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radio_on) {
                adapter = BetHistoryAdapter(
                    avgList, this@BetHistoryActivity,
                    binding.recyclerBetDetail, type
                )
                adapter.notifyDataSetChanged()
                binding.recyclerBetDetail.adapter = adapter
            } else if (checkedId == R.id.radio_off) {
                adapter = BetHistoryAdapter(
                    betHistoryList, this@BetHistoryActivity,
                    binding.recyclerBetDetail, type
                )
                adapter.notifyDataSetChanged()
                binding.recyclerBetDetail.adapter = adapter
            }
        }
        binding.refresh.setOnRefreshListener {
            getBetHistory(marketID, siteTypeId)
            binding.refresh.isRefreshing = false
        }
    }

    private fun getBetHistory(marketID: String, typeId: Int) {
        betHistoryList.clear()
        val jsonObject = JsonObject()
        try {
            if (Config.isInternetAvailable(this)) {
                binding.relData.visibility = View.VISIBLE
                binding.llNoInternet.visibility = View.GONE
                Config.showSmallProgressDialog(this)
                var call: Call<Common>? = null
                when {
                    //Veronica games bet
                    description.contains(AppConstants.Veronica, true) -> {
                        jsonObject.addProperty(
                            JsonKeys.clientUserName,
                            Config.getSharedPreferences(this, PreferenceKeys.Username)
                        )
                        jsonObject.addProperty(JsonKeys.roundSummaryID, marketID)
                        call = RetrofitApiClient.getClient.getVeronicaBetHistory(
                            Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!,
                            jsonObject
                        )
                    }
                    //Lottery bet
                    sportname.equals(AppConstants.Lottery, true) -> {
                        jsonObject.addProperty(
                            JsonKeys.clientUserName,
                            Config.getSharedPreferences(this, PreferenceKeys.Username)
                        )
                        jsonObject.addProperty(JsonKeys.roundSummaryID, marketID)
                        call = RetrofitApiClient.getClient.getLotteryBetHistory(
                            Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!,
                            jsonObject
                        )
                    }
                    //PL Bet
                    type.equals(AppConstants.PLActivity, true) -> {
                        jsonObject.addProperty(JsonKeys.MarketId, marketID)
                        jsonObject.addProperty(JsonKeys.SiteTypeId, typeId)
                        call = RetrofitApiClient.getClient.getPLReportHistory(
                            Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!,
                            jsonObject
                        )
                    }
                    //Statement Bet
                    type.equals(AppConstants.StatementActivity, true) -> {
                        jsonObject.addProperty(JsonKeys.MarketId, marketID)
                        jsonObject.addProperty(JsonKeys.SiteTypeId, typeId)
                        call = RetrofitApiClient.getClient.getBetHistory(
                            Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!,
                            jsonObject
                        )
                    }
                }
                Log.e(TAG, "json: $jsonObject")
                call?.enqueue(object : Callback<Common> {
                    override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                        Log.e(TAG, "history url: " + response?.raw()?.request()?.url())
                        val common: Common? = response?.body()
                        Log.e(TAG, "getHistory: " + Gson().toJson(common))
                        if (response != null && response.isSuccessful) {
                            Config.hideSmallProgressDialog()
                            when (response.code()) {
                                200 -> {
                                    when (common?.status?.code) {
                                        0 -> {
                                            betHistoryList.addAll(common.PLHistory)
                                            if (description.contains(AppConstants.Veronica, true) ||
                                                sportname.equals(AppConstants.Lottery, true)
                                            )
                                                roundDataModel = common.RoundData
                                            adapter.notifyDataSetChanged()
                                            calculateAverageTotal()
                                            updateUi()
                                        }
                                        100 -> {
                                            betHistoryList.addAll(common.PLHistory)
                                            roundDataModel = common.RoundData
                                            adapter.notifyDataSetChanged()
                                            calculateAverageTotal()
                                            Log.e(TAG, "" + betHistoryList.size)
                                            updateUi()
                                        }
                                        401 -> {
                                            val realm = Realm.getDefaultInstance()
                                            realm.executeTransaction { mRealm -> mRealm.deleteAll() }
                                            Config.clearAllPreferences(this@BetHistoryActivity)
                                            startActivity(
                                                Intent(
                                                    this@BetHistoryActivity,
                                                    LoginActivity::class.java
                                                )
                                            )
                                            finishAffinity()
                                        }
                                        else -> {
                                            Config.toast(
                                                this@BetHistoryActivity,
                                                common?.status?.returnMessage
                                            )
                                        }
                                    }
                                }
                                401 -> {
                                    val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { mRealm -> mRealm.deleteAll() }
                                    Config.clearAllPreferences(this@BetHistoryActivity)
                                    startActivity(
                                        Intent(
                                            this@BetHistoryActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finishAffinity()
                                }
                            }
                        } else {
                            val realm = Realm.getDefaultInstance()
                            realm.executeTransaction { realm -> realm.deleteAll() }
                            Config.clearAllPreferences(this@BetHistoryActivity)
                            startActivity(
                                Intent(
                                    this@BetHistoryActivity,
                                    LoginActivity::class.java
                                )
                            )
                            finishAffinity()
                        }
                    }

                    override fun onFailure(call: Call<Common>?, t: Throwable?) {
                        Config.hideSmallProgressDialog()
                        Log.e(TAG, "getBetHistory: " + t.toString())
                    }
                })
            } else {
                Config.hideSmallProgressDialog()
                binding.relData.visibility = View.GONE
                binding.llNoInternet.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Config.toast(this, "" + e)
            Log.e(TAG, "" + e)
        }
    }

    private fun updateUi() {
        if (betHistoryList.size > 0) {
            binding.recyclerBetDetail.visibility = View.VISIBLE
            binding.tvNoData.visibility = View.GONE
        } else {
            binding.recyclerBetDetail.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
        }
    }

    private fun calculateAverageTotal() {
        avgList.clear()
        var backTotal = 0.0
        var layTotal = 0.0
        var marketTotal = 0.0
        var total = 0.0

        var backWonTotal = 0.0
        var layWonTotal = 0.0
        var backLossTotal = 0.0
        var layLossTotal = 0.0
        var backWonRateTotal = 0.0
        var backWonStakeTotal = 0
        var backLossRateTotal = 0.0
        var backLossStakeTotal = 0
        var layWonRateTotal = 0.0
        var layWonStakeTotal = 0
        var layLossRateTotal = 0.0
        var layLossStakeTotal = 0

        var backWonSelection = ""
        var backLossSelection = ""
        var layWonSelection = ""
        var layLossSelection = ""
        var result = ""

        val backWonModel = BetHistoryModel()
        backWonModel.IsBack = true
        backWonModel.IsBetWon = true
        val backLossModel = BetHistoryModel()
        backLossModel.IsBack = true
        backLossModel.IsBetWon = false
        val layWonModel = BetHistoryModel()
        layWonModel.IsBack = false
        layWonModel.IsBetWon = true
        val layLossModel = BetHistoryModel()
        layLossModel.IsBack = false
        layLossModel.IsBetWon = false

        var backWon = false
        var backLoss = false
        var layWon = false
        var layLoss = false
        for (i in 0 until betHistoryList.size) {
            if (betHistoryList[i].IsBack) {
                backTotal += betHistoryList[i].PL
                if (betHistoryList[i].IsBetWon) {
                    backWon = true
                    backWonTotal += betHistoryList[i].PL
                    backWonRateTotal += betHistoryList[i].Rate
                    backWonStakeTotal += betHistoryList[i].Stake
                    if (betHistoryList[i].Runner != null)
                        backWonSelection = betHistoryList[i].Runner
                    result = betHistoryList[i].Result
                } else {
                    backLoss = true
                    backLossTotal += betHistoryList[i].PL
                    backLossRateTotal += betHistoryList[i].Rate
                    backLossStakeTotal += betHistoryList[i].Stake
                    if (betHistoryList[i].Runner != null)
                        backLossSelection = betHistoryList[i].Runner
                    result = betHistoryList[i].Result
                }
            } else if (!betHistoryList[i].IsBack) {
                layTotal += betHistoryList[i].PL
                if (betHistoryList[i].IsBetWon) {
                    layWon = true
                    layWonTotal += betHistoryList[i].PL
                    layWonRateTotal += betHistoryList[i].Rate
                    layWonStakeTotal += betHistoryList[i].Stake
                    if (betHistoryList[i].Runner != null)
                        layWonSelection = betHistoryList[i].Runner
                    result = betHistoryList[i].Result
                } else {
                    layLoss = true
                    layLossTotal += betHistoryList[i].PL
                    layLossRateTotal += betHistoryList[i].Rate
                    layLossStakeTotal += betHistoryList[i].Stake
                    if (betHistoryList[i].Runner != null)
                        layLossSelection = betHistoryList[i].Runner
                    result = betHistoryList[i].Result
                }
            }
        }
        marketTotal = backTotal + layTotal
        total = marketTotal
        binding.tvBackTotal.text = backTotal.toString()
        binding.tvLayTotal.text = layTotal.toString()
        binding.tvMarketTotal.text = marketTotal.toString()
        binding.tvTotal.text = total.toString()

        backWonModel.Rate = DashboardActivity.decimalFormat0_00.format(backWonRateTotal).toDouble()
        backWonModel.Stake = backWonStakeTotal
        backWonModel.PL = backWonTotal
        backWonModel.Runner = backWonSelection
        backWonModel.Result = result

        backLossModel.Rate =
            DashboardActivity.decimalFormat0_00.format(backLossRateTotal).toDouble()
        backLossModel.Stake = backLossStakeTotal
        backLossModel.PL = backLossTotal
        backLossModel.Runner = backLossSelection
        backLossModel.Result = result

        layWonModel.Rate = DashboardActivity.decimalFormat0_00.format(layWonRateTotal).toDouble()
        layWonModel.Stake = layWonStakeTotal
        layWonModel.PL = layWonTotal
        layWonModel.Runner = layWonSelection
        layWonModel.Result = result

        layLossModel.Rate = DashboardActivity.decimalFormat0_00.format(layLossRateTotal).toDouble()
        layLossModel.Stake = layLossStakeTotal
        layLossModel.PL = layLossTotal
        layLossModel.Runner = layLossSelection
        layLossModel.Result = result

        if (backWon)
            avgList.add(backWonModel)
        if (backLoss)
            avgList.add(backLossModel)
        if (layWon)
            avgList.add(layWonModel)
        if (layLoss)
            avgList.add(layLossModel)
    }
}