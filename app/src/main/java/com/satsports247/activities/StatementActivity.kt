package com.satsports247.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.satsports247.R
import com.satsports247.adapters.StatementListAdapter
import com.satsports247.constants.*
import com.satsports247.dataModels.AccountReportModel
import com.satsports247.databinding.ActivityStatementBinding
import com.satsports247.responseModels.Common
import com.satsports247.retrofit.RetrofitApiClient
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StatementActivity : AppCompatActivity(), StatementListAdapter.ItemClickListener {

    val TAG: String = "StatementActivity"
    lateinit var binding: ActivityStatementBinding
    private val limit: Int = 10
    private var offset: Int = 0
    private var totalData: Int = 0
    var statementList = ArrayList<AccountReportModel>()
    lateinit var adapter: StatementListAdapter
    var isLoading = false
    val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        callAccountReports(false)

//        updateUi()
    }

    private fun updateUi() {
        if (statementList.size > 0) {
            binding.recyclerStatement.visibility = View.VISIBLE
            binding.tvNoData.visibility = View.GONE
        } else {
            binding.recyclerStatement.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
        }
    }

    private fun callAccountReports(recall: Boolean) {
        if (recall)
            offset += 1
        else {
            this.runOnUiThread {
                offset = 0
                statementList.clear()
//                adapter.notifyDataSetChanged()
            }
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty(JsonKeys.pageIndex, offset)
        jsonObject.addProperty(JsonKeys.pageSize, limit)
        Log.e(TAG, "json $jsonObject")
        try {
            if (Config.isInternetAvailable(this)) {
                binding.relData.visibility = View.VISIBLE
                binding.llNoInternet.visibility = View.GONE
                Config.showSmallProgressDialog(this)
                val call: Call<Common> = RetrofitApiClient.getClient.getAccountReport(
                    Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!, jsonObject
                )
                call.enqueue(object : Callback<Common> {
                    override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                        Config.hideSmallProgressDialog()
                        val common: Common? = response?.body()
                        Log.e(TAG, "getAccountReport: " + Gson().toJson(common))
                        if (response != null) {
                            when (response.code()) {
                                200 -> {
                                    when (common?.status?.code) {
                                        0 -> {
                                            for (i in 0 until common.AccountReport.size - 1)
                                                statementList.add(common.AccountReport[i])
                                            totalData = common.RowCount
                                            adapter.notifyDataSetChanged()
                                            isLoading = false
                                            updateUi()
                                        }
                                        401 -> {
                                            val realm = Realm.getDefaultInstance()
                                            realm.executeTransaction { mRealm -> mRealm.deleteAll() }
                                            Config.clearAllPreferences(this@StatementActivity)
                                            startActivity(
                                                Intent(
                                                    this@StatementActivity,
                                                    LoginActivity::class.java
                                                )
                                            )
                                            finishAffinity()
                                        }
                                        else -> {
                                            revertOffsetSize()
                                            Config.toast(
                                                this@StatementActivity,
                                                common?.status?.returnMessage
                                            )
                                        }
                                    }
                                }
                                401 -> {
                                    val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { mRealm -> mRealm.deleteAll() }
                                    Config.clearAllPreferences(this@StatementActivity)
                                    startActivity(
                                        Intent(
                                            this@StatementActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finishAffinity()
                                }
                            }
                        } else {
                            val realm = Realm.getDefaultInstance()
                            realm.executeTransaction { mRealm -> mRealm.deleteAll() }
                            Config.clearAllPreferences(this@StatementActivity)
                            startActivity(
                                Intent(
                                    this@StatementActivity,
                                    LoginActivity::class.java
                                )
                            )
                            finishAffinity()
                        }
                    }

                    override fun onFailure(call: Call<Common>?, t: Throwable?) {
                        Config.hideSmallProgressDialog()
                        Log.e(TAG, "getAccountReport: " + t.toString())
                    }
                })
            } else {
                Config.hideSmallProgressDialog()
                binding.relData.visibility = View.GONE
                binding.llNoInternet.visibility = View.VISIBLE
                if (offset > 0)
                    offset += -1
            }
        } catch (e: Exception) {
            Config.toast(this, "" + e)
            Log.e(TAG, "" + e)
        }
    }

    private fun setOnScrollListener() {
        var count = 0
        val layoutManager = binding.recyclerStatement.layoutManager as LinearLayoutManager
        binding.recyclerStatement.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                    if (!isLoading && totalItemCount <= (lastVisibleItem + 1)) {
                        if (statementList.size < totalData - 1) {
                            handler.postDelayed(runnable, 2000)
                            isLoading = true
                        } else if (count == 0) {
                            isLoading = false
                            Config.toast(
                                this@StatementActivity,
                                getString(R.string.no_more_data_found)
                            )
                            count = 1
                        }
                    }
                } else
                    count = 0
            }
        })
    }

    var runnable = Runnable { callAccountReports(true) }

    private fun revertOffsetSize() {
        if (offset > 0) {
            offset -= 1
        }
    }

    private fun init() {
        binding.refresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                this,
                R.color.colorBlack
            )
        )
        binding.refresh.setColorSchemeColors(Color.YELLOW)
        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.recyclerStatement.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL, false
        )

        adapter = StatementListAdapter(statementList, this, binding.recyclerStatement)
        binding.recyclerStatement.adapter = adapter
        setOnScrollListener()
        adapter.setClickListener(this)

        binding.btnReloadData.setOnClickListener {
            if (statementList.size < totalData - 1)
                callAccountReports(true)
            else
                callAccountReports(false)
        }
        binding.refresh.setOnRefreshListener {
            if (statementList.size < totalData - 1)
                callAccountReports(true)
            else
                callAccountReports(false)
            binding.refresh.isRefreshing = false
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAfterTransition()
    }

    override fun onItemClick(view: View?, position: Int) {
        val plReportModel = statementList[position]
        val intent = Intent(this, BetHistoryActivity::class.java)
        intent.putExtra(IntentKeys.marketID, plReportModel.MarketID)
        intent.putExtra(IntentKeys.siteTypeID, plReportModel.SiteTypeId)
        intent.putExtra(IntentKeys.description, plReportModel.Description)
        intent.putExtra(IntentKeys.sportName, plReportModel.Sportname)
        intent.putExtra(IntentKeys.data, AppConstants.StatementActivity)
        startActivity(intent)
    }
}