package com.satsports247.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
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
import com.satsports247.adapters.ResultsListAdapter
import com.satsports247.constants.*
import com.satsports247.dataModels.ResultsModel
import com.satsports247.dataModels.SportsModel
import com.satsports247.databinding.ActivityResultsBinding
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

class ResultsActivity : AppCompatActivity(), View.OnClickListener {

    val TAG: String = "ResultsActivity"
    lateinit var binding: ActivityResultsBinding
    private val limit: Int = 10
    private var offset: Int = 0
    private var totalData: Int = 0
    var resultsList = ArrayList<ResultsModel>()
    lateinit var adapter: ResultsListAdapter
    var isLoading = false
    val handler = Handler()
    lateinit var sportSpinner: MaterialSpinner
    lateinit var tournamentSpinner: MaterialSpinner
    lateinit var matchSpinner: MaterialSpinner
    lateinit var marketSpinner: MaterialSpinner
    private var sportID: Int = 0
    private var tournamentID: Int = 0
    private var matchID: Int = 0
    private var marketID: Int = 0
    private var marketType: String = ""
    var sportsList = ArrayList<SportsModel>()
    var tournamentList = ArrayList<SportsModel>()
    var matchList = ArrayList<SportsModel>()
    var marketList = ArrayList<SportsModel>()
    lateinit var formatddMMyyyy: DateFormat
    lateinit var formatyyyyMMdd: DateFormat
    var fromDate = ""
    var toDate = ""
    lateinit var fromCal: Calendar
    lateinit var toCal: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
//        callResultReport(false)
    }

    @SuppressLint("SimpleDateFormat")
    private fun init() {
        binding.refresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                this,
                R.color.colorBlack
            )
        )
        binding.refresh.setColorSchemeColors(Color.YELLOW)
        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.recyclerResults.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL, false
        )
        adapter = ResultsListAdapter(resultsList, this, binding.recyclerResults)
        binding.recyclerResults.adapter = adapter
        setOnScrollListener()

        binding.btnReloadData.setOnClickListener {
            if (resultsList.size < totalData - 1)
                callResultReport(true)
            else
                callResultReport(false)
        }
        binding.refresh.setOnRefreshListener {
            if (resultsList.size < totalData - 1)
                callResultReport(true)
            else
                callResultReport(false)
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

        setAllSpinners()

        setAllCheckboxCheckedChange()

        getSportList()
    }

    private fun setAllCheckboxCheckedChange() {
        binding.cbMatchOdds.setOnCheckedChangeListener { _, _ ->
            setMarketType()
            callResultReport(false)
        }
        binding.cbBookMaker.setOnCheckedChangeListener { _, _ ->
            setMarketType()
            callResultReport(false)
        }
        binding.cbManualOdds.setOnCheckedChangeListener { _, _ ->
            setMarketType()
            callResultReport(false)
        }
        binding.cbLineMarket.setOnCheckedChangeListener { _, _ ->
            setMarketType()
            callResultReport(false)
        }
        binding.cbAdvSession.setOnCheckedChangeListener { _, _ ->
            setMarketType()
            callResultReport(false)
        }
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
                        if (response != null) {
                            when (response.code()) {
                                200 -> {
                                    when (common?.status?.code) {
                                        0 -> {
                                            sportsList = common.list
                                            setSportsAdapter()
                                        }
                                        401 -> {
                                            val realm = Realm.getDefaultInstance()
                                            realm.executeTransaction { realm -> realm.deleteAll() }
                                            Config.clearAllPreferences(this@ResultsActivity)
                                            startActivity(
                                                Intent(
                                                    this@ResultsActivity,
                                                    LoginActivity::class.java
                                                )
                                            )
                                            finishAffinity()
                                        }
                                        else -> Config.toast(
                                            this@ResultsActivity,
                                            common?.status?.returnMessage
                                        )
                                    }
                                }
                                401 -> {
                                    val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { realm -> realm.deleteAll() }
                                    Config.clearAllPreferences(this@ResultsActivity)
                                    startActivity(
                                        Intent(
                                            this@ResultsActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finishAffinity()
                                }
                            }
                        } else {
                            val realm = Realm.getDefaultInstance()
                            realm.executeTransaction { realm -> realm.deleteAll() }
                            Config.clearAllPreferences(this@ResultsActivity)
                            startActivity(
                                Intent(
                                    this@ResultsActivity,
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

    private fun setSportsAdapter() {
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

    private fun setTournamentAdapter() {
        val sportNameList: ArrayList<String> = ArrayList()
        sportNameList.add("All")
        for (i in 0 until tournamentList.size) {
            sportNameList.add(tournamentList[i].Name!!)
        }
        val sportListAdapter = ArrayAdapter(
            this,
            R.layout.simple_spinner_item, sportNameList
        )
        tournamentSpinner.setAdapter(sportListAdapter)
    }

    private fun setMatchAdapter() {
        val sportNameList: ArrayList<String> = ArrayList()
        sportNameList.add("All")
        for (i in 0 until matchList.size) {
            sportNameList.add(matchList[i].Name!!)
        }
        val sportListAdapter = ArrayAdapter(
            this,
            R.layout.simple_spinner_item, sportNameList
        )
        matchSpinner.setAdapter(sportListAdapter)
    }

    private fun setMarketAdapter() {
        val sportNameList: ArrayList<String> = ArrayList()
        sportNameList.add("All")
        for (i in 0 until marketList.size) {
            sportNameList.add(marketList[i].Name!!)
        }
        val sportListAdapter = ArrayAdapter(
            this,
            R.layout.simple_spinner_item, sportNameList
        )
        marketSpinner.setAdapter(sportListAdapter)
    }

    private fun setAllSpinners() {
        sportSpinner = binding.spinnerSports
        tournamentSpinner = binding.spinnerTournaments
        matchSpinner = binding.spinnerMatch
        marketSpinner = binding.spinnerMarket
        sportSpinner.setLabel(getString(R.string.select_sport_game))
        tournamentSpinner.setLabel(getString(R.string.select_tournament))
        matchSpinner.setLabel(getString(R.string.select_match))
        marketSpinner.setLabel(getString(R.string.select_market))
        sportSpinner.setErrorEnabled(false)
        tournamentSpinner.setErrorEnabled(false)
        matchSpinner.setErrorEnabled(false)
        marketSpinner.setErrorEnabled(false)

        setSpinnerItemSelectedListeners()
    }

    private fun setSpinnerItemSelectedListeners() {
        sportSpinner.setItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    sportID = 0
                    callResultReport(false)
                } else {
                    sportID = sportsList[position - 1].Id
//                    callResultReport(false)
                    getAllTournamentsApi()
                }
            }
        })
        tournamentSpinner.setItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    tournamentID = 0
                    callResultReport(false)
                } else {
                    tournamentID = tournamentList[position - 1].Id
//                    callResultReport(false)
                    getAllMatchApi()
                }
            }
        })
        matchSpinner.setItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    matchID = 0
                    callResultReport(false)
                } else {
                    matchID = matchList[position - 1].Id
//                    callResultReport(false)
                    getAllMarketApi()
                }
            }
        })
        marketSpinner.setItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    marketID = 0
                    callResultReport(false)
                } else {
                    marketID = marketList[position - 1].Id
                    callResultReport(false)
                }
            }
        })
    }

    private fun updateUi() {
        if (resultsList.size > 0) {
            binding.recyclerResults.visibility = View.VISIBLE
            binding.tvNoData.visibility = View.GONE
        } else {
            binding.recyclerResults.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
        }
    }

    private fun setMarketType() {
        val marketTypeList: ArrayList<String> = ArrayList()
        if (binding.cbMatchOdds.isChecked)
            marketTypeList.add(AppConstants.MATCH_ODDS)
        if (binding.cbBookMaker.isChecked)
            marketTypeList.add(AppConstants.Bookmakers)
        if (binding.cbManualOdds.isChecked)
            marketTypeList.add(AppConstants.ManualOdds)
        if (binding.cbLineMarket.isChecked)
            marketTypeList.add(AppConstants.LineMarket)
        if (binding.cbAdvSession.isChecked)
            marketTypeList.add(AppConstants.AdvanceSession)
        marketType = TextUtils.join(",", marketTypeList)
    }

    @SuppressLint("SimpleDateFormat")
    private fun callResultReport(recall: Boolean) {
        if (recall)
            offset += 1
        else {
            this.runOnUiThread {
                offset = 0
                resultsList.clear()
                adapter.notifyDataSetChanged()
            }
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty(JsonKeys.SportID, sportID)
        jsonObject.addProperty(JsonKeys.TournamentID, tournamentID)
        jsonObject.addProperty(JsonKeys.MatchID, matchID)
        jsonObject.addProperty(JsonKeys.MarketId, marketID)
        val df: DateFormat = SimpleDateFormat(AppConstants.yyyyMMdd)
        val calendar = Calendar.getInstance()
        val toDate = df.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val fromDate = df.format(calendar.time)
        jsonObject.addProperty(JsonKeys.FromDate, fromDate)
        jsonObject.addProperty(JsonKeys.ToDate, toDate)
        setMarketType()
        jsonObject.addProperty(JsonKeys.MarketType, marketType)
        jsonObject.addProperty(JsonKeys.pageIndex, offset)
        jsonObject.addProperty(JsonKeys.pageSize, limit)
        Log.e(TAG, "json $jsonObject")
        try {
            if (Config.isInternetAvailable(this)) {
                binding.relData.visibility = View.VISIBLE
                binding.llNoInternet.visibility = View.GONE
                Config.showSmallProgressDialog(this)
                val call: Call<Common> = RetrofitApiClient.getClient.getResultsReport(
                    Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!, jsonObject
                )
                call.enqueue(object : Callback<Common> {
                    override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                        val common: Common? = response?.body()
                        Log.e(TAG, "getResultsReport: " + Gson().toJson(common))
                        Config.hideSmallProgressDialog()
                        if (response != null) {
                            when (response.code()) {
                                200 -> {
                                    when (common?.status?.code) {
                                        0 -> {
                                            resultsList.addAll(common.ResultReport)
                                            totalData = common.RowCount
                                            adapter.notifyDataSetChanged()
                                            isLoading = false
                                            updateUi()
                                        }
                                        401 -> {
                                            val realm = Realm.getDefaultInstance()
                                            realm.executeTransaction { realm -> realm.deleteAll() }
                                            Config.clearAllPreferences(this@ResultsActivity)
                                            startActivity(
                                                Intent(
                                                    this@ResultsActivity,
                                                    LoginActivity::class.java
                                                )
                                            )
                                            finishAffinity()
                                        }
                                        else -> {
                                            revertOffsetSize()
                                            Config.toast(
                                                this@ResultsActivity,
                                                common?.status?.returnMessage
                                            )
                                        }
                                    }
                                }
                                401 -> {
                                    val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { realm -> realm.deleteAll() }
                                    Config.clearAllPreferences(this@ResultsActivity)
                                    startActivity(
                                        Intent(
                                            this@ResultsActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finishAffinity()
                                }
                            }

                        } else {
                            val realm = Realm.getDefaultInstance()
                            realm.executeTransaction { realm -> realm.deleteAll() }
                            Config.clearAllPreferences(this@ResultsActivity)
                            startActivity(
                                Intent(
                                    this@ResultsActivity,
                                    LoginActivity::class.java
                                )
                            )
                            finishAffinity()
                        }
                    }

                    override fun onFailure(call: Call<Common>?, t: Throwable?) {
                        Config.hideSmallProgressDialog()
                        Log.e(TAG, "getResultsReport: " + t.toString())
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
        val layoutManager = binding.recyclerResults.layoutManager as LinearLayoutManager
        binding.recyclerResults.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                    if (!isLoading && totalItemCount <= (lastVisibleItem + 1)) {
                        if (resultsList.size < totalData - 1) {
                            handler.postDelayed(runnable, 2000)
                            isLoading = true
                        } else if (count == 0) {
                            isLoading = false;
                            Config.toast(
                                this@ResultsActivity,
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

    var runnable = Runnable { callResultReport(true) }

    private fun revertOffsetSize() {
        if (offset > 0) {
            offset -= 1
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAfterTransition()
    }

    private fun getAllTournamentsApi() {
        val url: String = UrlConstants.URLMobileApi + "Common/GetAllTournaments?SportID=" + sportID
        Log.e(TAG, "GetAllTournaments: $url")
        try {
            if (Config.isInternetAvailable(this)) {
                val call: Call<Common> = RetrofitApiClient.getClient.getAllTournaments(
                    Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!, url
                )
                call.enqueue(object : Callback<Common> {
                    override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                        val common: Common? = response?.body()
                        Log.e(TAG, "getAllTournaments: " + Gson().toJson(common))
                        if (response != null && response.isSuccessful) {
                            when (common?.status?.code) {
                                0 -> {
                                    tournamentList = common.list
                                    setTournamentAdapter()
                                }
                                401 -> {
                                    val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { realm -> realm.deleteAll() }
                                    Config.clearAllPreferences(this@ResultsActivity)
                                    startActivity(
                                        Intent(
                                            this@ResultsActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finishAffinity()
                                }
                                else -> {
                                    Config.toast(
                                        this@ResultsActivity,
                                        common?.status?.returnMessage
                                    )
                                }
                            }
                        } else {
                            val realm = Realm.getDefaultInstance()
                            realm.executeTransaction { realm -> realm.deleteAll() }
                            Config.clearAllPreferences(this@ResultsActivity)
                            startActivity(
                                Intent(
                                    this@ResultsActivity,
                                    LoginActivity::class.java
                                )
                            )
                            finishAffinity()
                        }
                    }

                    override fun onFailure(call: Call<Common>?, t: Throwable?) {
                        Log.e(TAG, "getAllTournaments: " + t.toString())
                    }
                })
            }
        } catch (e: Exception) {
            Config.toast(this, "" + e)
            Log.e(TAG, "" + e)
        }
    }

    private fun getAllMatchApi() {
        val url: String =
            UrlConstants.URLMobileApi + "Common/GetAllMatches?TournamentID=" + tournamentID
        try {
            if (Config.isInternetAvailable(this)) {
                val call: Call<Common> = RetrofitApiClient.getClient.getAllMatches(
                    Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!, url
                )
                call.enqueue(object : Callback<Common> {
                    override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                        val common: Common? = response?.body()
                        Log.e(TAG, "getAllMatches: " + Gson().toJson(common))
                        if (response != null && response.isSuccessful) {
                            when (common?.status?.code) {
                                0 -> {
                                    matchList = common.list
                                    setMatchAdapter()
                                }
                                401 -> {
                                    val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { realm -> realm.deleteAll() }
                                    Config.clearAllPreferences(this@ResultsActivity)
                                    startActivity(
                                        Intent(
                                            this@ResultsActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finishAffinity()
                                }
                                else -> {
                                    Config.toast(
                                        this@ResultsActivity,
                                        common?.status?.returnMessage
                                    )
                                }
                            }
                        } else {
                            val realm = Realm.getDefaultInstance()
                            realm.executeTransaction { realm -> realm.deleteAll() }
                            Config.clearAllPreferences(this@ResultsActivity)
                            startActivity(
                                Intent(
                                    this@ResultsActivity,
                                    LoginActivity::class.java
                                )
                            )
                            finishAffinity()
                        }
                    }

                    override fun onFailure(call: Call<Common>?, t: Throwable?) {
                        Log.e(TAG, "getAllMatches: " + t.toString())
                    }
                })
            }
        } catch (e: Exception) {
            Config.toast(this, "" + e)
            Log.e(TAG, "" + e)
        }
    }

    private fun getAllMarketApi() {
        val url: String =
            UrlConstants.URLMobileApi + "Common/GetAllMarkets?MatchID=" + matchID
        try {
            if (Config.isInternetAvailable(this)) {
                val call: Call<Common> = RetrofitApiClient.getClient.getAllMarkets(
                    Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!, url
                )
                call.enqueue(object : Callback<Common> {
                    override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                        val common: Common? = response?.body()
                        Log.e(TAG, "getAllMarkets: " + Gson().toJson(common))
                        if (response != null && response.isSuccessful) {
                            when (common?.status?.code) {
                                0 -> {
                                    marketList = common.list
                                    setMarketAdapter()
                                }
                                401 -> {
                                    val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { realm -> realm.deleteAll() }
                                    Config.clearAllPreferences(this@ResultsActivity)
                                    startActivity(
                                        Intent(
                                            this@ResultsActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finishAffinity()
                                }
                                else -> {
                                    Config.toast(
                                        this@ResultsActivity,
                                        common?.status?.returnMessage
                                    )
                                }
                            }
                        } else {
                            val realm = Realm.getDefaultInstance()
                            realm.executeTransaction { realm -> realm.deleteAll() }
                            Config.clearAllPreferences(this@ResultsActivity)
                            startActivity(
                                Intent(
                                    this@ResultsActivity,
                                    LoginActivity::class.java
                                )
                            )
                            finishAffinity()
                        }
                    }

                    override fun onFailure(call: Call<Common>?, t: Throwable?) {
                        Log.e(TAG, "getAllMarkets: " + t.toString())
                    }
                })
            }
        } catch (e: Exception) {
            Config.toast(this, "" + e)
            Log.e(TAG, "" + e)
        }
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
                callResultReport(false)
            }, year, month, day
        )
        dpd.show()
    }
}