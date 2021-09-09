package com.satsports247.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.materialspinner.MaterialSpinner
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.satsports247.R
import com.satsports247.adapters.PLReportListAdapter
import com.satsports247.constants.*
import com.satsports247.dataModels.PLReportModel
import com.satsports247.dataModels.SportsModel
import com.satsports247.databinding.ActivityPlBinding
import com.satsports247.responseModels.Common
import com.satsports247.retrofit.RetrofitApiClient
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PLActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, View.OnClickListener,
    PLReportListAdapter.ItemClickListener {

    val TAG: String = "PLActivity"
    lateinit var binding: ActivityPlBinding
    private val limit: Int = 10
    private var offset: Int = 0
    private var totalData: Int = 0
    private var sportID: Int = 0

    var plReportList = ArrayList<PLReportModel>()
    lateinit var adapter: PLReportListAdapter
    var isLoading = false
    val handler = Handler()
    lateinit var sportSpinner: MaterialSpinner
    var sportsList = ArrayList<SportsModel>()
    lateinit var formatddMMyyyy: DateFormat
    lateinit var formatyyyyMMdd: DateFormat
    var fromDate = ""
    var toDate = ""
    lateinit var fromCal: Calendar
    lateinit var toCal: Calendar

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

//        callPLReports(false)

        getSportList()
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
        binding.recyclerPlReport.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL, false
        )
        adapter = PLReportListAdapter(plReportList, this, binding.recyclerPlReport)
        adapter.setClickListener(this)
        binding.recyclerPlReport.adapter = adapter
        setOnScrollListener()

        binding.btnReloadData.setOnClickListener {
            if (plReportList.size < totalData - 1)
                callPLReports(true)
            else
                callPLReports(false)
            binding.refresh.isRefreshing = false
        }

        binding.refresh.setOnRefreshListener {
            if (plReportList.size < totalData - 1)
                callPLReports(true)
            else
                callPLReports(false)
            binding.refresh.isRefreshing = false
        }

        formatddMMyyyy = SimpleDateFormat(AppConstants.ddMMyyyy)
        formatyyyyMMdd = SimpleDateFormat(AppConstants.yyyyMMdd)

        toCal = Calendar.getInstance()
        toDate = formatyyyyMMdd.format(toCal.time)
        binding.edtToDate.setText(formatddMMyyyy.format(toCal.time))

        fromCal = Calendar.getInstance()
        fromCal.add(Calendar.DAY_OF_YEAR, -7)
        fromDate = formatyyyyMMdd.format(fromCal.time)
        binding.edtFromDate.setText(formatddMMyyyy.format(fromCal.time))

        binding.edtFromDate.setOnClickListener(this)
        binding.edtToDate.setOnClickListener(this)
        binding.edtFromDate.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus)
                openDatePicker(binding.edtFromDate, 0, fromCal)
        }
        binding.edtToDate.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus)
                openDatePicker(binding.edtToDate, 1, toCal)
        }
    }

    private fun callPLReports(recall: Boolean) {
        if (recall)
            offset += 1
        else {
            this.runOnUiThread {
                offset = 0
                plReportList.clear()
                adapter.notifyDataSetChanged()
            }
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty(JsonKeys.SportID, sportID)
//        jsonObject.addProperty(JsonKeys.TournamentID, tournamentID)
//        jsonObject.addProperty(JsonKeys.MatchID, "0")
//        jsonObject.addProperty(JsonKeys.MarketId, "0")
        val df: DateFormat = SimpleDateFormat(AppConstants.yyyyMMdd)
        jsonObject.addProperty(JsonKeys.FromDate, fromDate)
        jsonObject.addProperty(JsonKeys.ToDate, toDate)
//        jsonObject.addProperty(JsonKeys.MarketType, "")
        jsonObject.addProperty(JsonKeys.pageIndex, offset)
        jsonObject.addProperty(JsonKeys.pageSize, limit)
        Log.e(TAG, "json $jsonObject")
        try {
            if (Config.isInternetAvailable(this)) {
                binding.relData.visibility = View.VISIBLE
                binding.llNoInternet.visibility = View.GONE
                Config.showSmallProgressDialog(this)
                val call: Call<Common> = RetrofitApiClient.getClient.getPLReport(
                    Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!, jsonObject
                )
                call.enqueue(object : Callback<Common> {
                    override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                        Config.hideSmallProgressDialog()
                        val common: Common? = response?.body()
                        Log.e(TAG, "getPLReport: " + Gson().toJson(common))
                        if (response != null) {
                            when (response.code()) {
                                200 -> {
                                    when (common?.status?.code) {
                                        0 -> {
                                            plReportList.addAll(common.PLReport)
                                            totalData = common.RowCount
                                            adapter.notifyDataSetChanged()
                                            isLoading = false
                                            updateUi()
                                            setTotalPLValue(common.TotalPL)
                                        }
                                        401 -> {
                                            val realm = Realm.getDefaultInstance()
                                            realm.executeTransaction { mRealm -> mRealm.deleteAll() }
                                            Config.clearAllPreferences(this@PLActivity)
                                            startActivity(
                                                Intent(
                                                    this@PLActivity,
                                                    LoginActivity::class.java
                                                )
                                            )
                                            finishAffinity()
                                        }
                                        else -> {
                                            revertOffsetSize()
                                            Config.toast(
                                                this@PLActivity,
                                                common?.status?.returnMessage
                                            )
                                        }
                                    }
                                }
                                401 -> {
                                    val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { mRealm -> mRealm.deleteAll() }
                                    Config.clearAllPreferences(this@PLActivity)
                                    startActivity(
                                        Intent(
                                            this@PLActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finishAffinity()
                                }
                            }
                        } else {
                            val realm = Realm.getDefaultInstance()
                            realm.executeTransaction { mRealm -> mRealm.deleteAll() }
                            Config.clearAllPreferences(this@PLActivity)
                            startActivity(
                                Intent(
                                    this@PLActivity,
                                    LoginActivity::class.java
                                )
                            )
                            finishAffinity()
                        }
                    }

                    override fun onFailure(call: Call<Common>?, t: Throwable?) {
                        Config.hideSmallProgressDialog()
                        Log.e(TAG, "getPLReport: " + t.toString())
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

    private fun setTotalPLValue(totalPL: Double) {
        var strTotalPL: String = totalPL.toString()
        strTotalPL = strTotalPL.replace(".0", "")
        if (totalPL >= 0)
            binding.tvTotalPl.setTextColor(resources.getColor(R.color.colorGreen))
        else {
            strTotalPL = strTotalPL.replace("-", "")
            binding.tvTotalPl.setTextColor(resources.getColor(R.color.colorRed))
        }
        binding.tvTotalPl.text = strTotalPL
    }

    private fun getSportList() {
        try {
            if (Config.isInternetAvailable(this)) {
                Config.showSmallProgressDialog(this)
                val call: Call<Common> = RetrofitApiClient.getClient.getAllSports(
                    Config.getSharedPreferences(this, PreferenceKeys.AuthToken)
                )
                call.enqueue(object : Callback<Common> {
                    override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                        val common: Common? = response?.body()
                        Log.e(TAG, "getAllSports: " + Gson().toJson(common))
                        Config.hideSmallProgressDialog()
                        if (response != null && response.isSuccessful) {
                            when (response.code()) {
                                200 -> {
                                    when (common?.status?.code) {
                                        0 -> {
                                            sportsList = common.list
                                            setSportSpinner()
                                        }
                                        401 -> {
                                            val realm = Realm.getDefaultInstance()
                                            realm.executeTransaction { mRealm -> mRealm.deleteAll() }
                                            Config.clearAllPreferences(this@PLActivity)
                                            startActivity(
                                                Intent(
                                                    this@PLActivity,
                                                    LoginActivity::class.java
                                                )
                                            )
                                            finishAffinity()
                                        }
                                        else -> Config.toast(
                                            this@PLActivity,
                                            common?.status?.returnMessage
                                        )
                                    }
                                }
                                401 -> {
                                    val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { mRealm -> mRealm.deleteAll() }
                                    Config.clearAllPreferences(this@PLActivity)
                                    startActivity(
                                        Intent(
                                            this@PLActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finishAffinity()
                                }
                            }

                        } else {
                            val realm = Realm.getDefaultInstance()
                            realm.executeTransaction { mRealm -> mRealm.deleteAll() }
                            Config.clearAllPreferences(this@PLActivity)
                            startActivity(
                                Intent(
                                    this@PLActivity,
                                    LoginActivity::class.java
                                )
                            )
                            finishAffinity()
                        }
                    }

                    override fun onFailure(call: Call<Common>?, t: Throwable?) {
                        Config.hideSmallProgressDialog()
                        Log.e(TAG, "getAllSports: " + t.toString())
                    }
                })
            } else {
                Config.hideSmallProgressDialog()
            }
        } catch (e: Exception) {
            Config.toast(this, "" + e)
            Log.e(TAG, "" + e)
        }
    }

    private fun setSportSpinner() {
        sportSpinner = binding.materialSpinner
        sportSpinner.getSpinner().onItemSelectedListener = this
        sportSpinner.setLabel(getString(R.string.select_sport_game))
        sportSpinner.setErrorEnabled(false)
        val sportNameList: ArrayList<String> = ArrayList()
        sportNameList.add("All")
        for (i in 0 until sportsList.size) {
            sportNameList.add(sportsList[i].Name!!)
        }
        val sportListAdapter = ArrayAdapter(
            this,
            R.layout.simple_spinner_item, sportNameList
        )
        sportSpinner.setAdapter(sportListAdapter)
    }

    private fun updateUi() {
        if (plReportList.size > 0) {
            binding.recyclerPlReport.visibility = View.VISIBLE
            binding.tvNoData.visibility = View.GONE
        } else {
            binding.recyclerPlReport.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
        }
    }

    private fun setOnScrollListener() {
        var count = 0
        val layoutManager = binding.recyclerPlReport.layoutManager as LinearLayoutManager
        binding.recyclerPlReport.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                    if (!isLoading && totalItemCount <= (lastVisibleItem + 1)) {
                        if (plReportList.size < totalData - 1) {
                            handler.postDelayed(runnable, 2000)
                            isLoading = true
                        } else if (count == 0) {
                            isLoading = false;
                            Config.toast(
                                this@PLActivity,
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

    var runnable = Runnable { callPLReports(true) }

    private fun revertOffsetSize() {
        if (offset > 0) {
            offset -= 1
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAfterTransition()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position == 0) {
            sportID = 0
            callPLReports(false)
        } else {
            sportID = sportsList[position - 1].Id
            callPLReports(false)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.edt_from_date)
            openDatePicker(binding.edtFromDate, 0, fromCal)
        else if (v?.id == R.id.edt_to_date)
            openDatePicker(binding.edtToDate, 1, toCal)
    }

    private fun openDatePicker(edtDate: EditText, type: Int, calendar: Calendar) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            this, R.style.datepicker,
            { _, year, monthOfYear, dayOfMonth ->

                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                edtDate.setText(formatddMMyyyy.format(cal.time))
                if (type == 0) {
                    fromCal = cal
                    fromDate = formatyyyyMMdd.format(cal.time)
                } else {
                    toCal = cal
                    toDate = formatyyyyMMdd.format(cal.time)
                }
                callPLReports(false)
            }, year, month, day
        )
        dpd.show()
    }

    override fun onItemClick(view: View?, position: Int) {
        val plReportModel = plReportList[position]
        val intent = Intent(this, BetHistoryActivity::class.java)
        intent.putExtra(IntentKeys.marketID, plReportModel.MarketID)
        intent.putExtra(IntentKeys.siteTypeID, plReportModel.SiteTypeId)
        intent.putExtra(IntentKeys.sportName, plReportModel.Sportname)
        intent.putExtra(IntentKeys.description, plReportModel.Description)
        intent.putExtra(IntentKeys.data, AppConstants.PLActivity)
        startActivity(intent)
    }
}