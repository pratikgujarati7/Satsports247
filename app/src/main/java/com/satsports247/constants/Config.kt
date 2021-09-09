package com.satsports247.constants

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.satsports247.R
import com.satsports247.activities.DashboardActivity
import com.satsports247.activities.FullScreenLiveVideoActivity
import com.satsports247.activities.SingleMarketActivity
import com.satsports247.adapters.RunPositionListAdapter
import com.satsports247.components.PlaceBetProgressDialog
import com.satsports247.components.ProgressDialog
import com.satsports247.dataModels.BetModel
import com.satsports247.dataModels.ChipSettingsDataModel
import com.satsports247.dataModels.LiabilityDataModel
import com.satsports247.dataModels.RunPositionModel
import com.satsports247.responseModels.Common
import com.satsports247.retrofit.RetrofitApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList


object Config {

    val TAG: String = "Config"

    fun showIcon(editText: EditText, isCheckedPassword: Boolean) {
        if (isCheckedPassword) {
            editText.transformationMethod =
                HideReturnsTransformationMethod.getInstance() // show password
        } else {
            editText.transformationMethod =
                PasswordTransformationMethod.getInstance() // hide password
        }
        //        imageView.setImageResource(isCheckedPassword ? R.drawable.ic_show_password : R.drawable.ic_hide_password);
//        int img = isCheckedPassword ? R.drawable.ic_show_password : R.drawable.ic_hide_password;
        val img: Int =
            if (isCheckedPassword) R.drawable.password_hidden else R.drawable.password_visible
        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, img, 0)
    }

    fun emailValidator(email: String?): Boolean {
        val EMAIL_PATTERN =
            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        val pattern = Pattern.compile(EMAIL_PATTERN)
        val matcher = pattern.matcher(email)
//        return matcher.matches()
        return email!!.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun mobileNumberValidator(mobileNumber: String?): Boolean {
//        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
//        val pattern = Pattern.compile(ValidatorPattern)
//        val matcher = pattern.matcher(mobileNumber)
        if (!Pattern.matches("[a-zA-Z]+", mobileNumber)) {
            return mobileNumber!!.length > 6 && mobileNumber.length <= 13
        }
        return false
    }

    fun toast(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val n = cm.activeNetwork
            if (n != null) {
                val nc = cm.getNetworkCapabilities(n)
                //It will check for both wifi and cellular network
                return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            }
            return false
        } else {
            val netInfo = cm.activeNetworkInfo
            return netInfo != null && netInfo.isConnectedOrConnecting
        }
    }

    fun saveSharedPreferences(
        context: Context,
        key: String?,
        value: String?
    ) {
        val pref =
            context.getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        if (pref != null) {
            val editor = pref.edit()
            editor.putString(key, value)
            editor.commit()
        }
    }

    fun setUserLoggedIn(
        context: Context,
        key: String?,
        value: Boolean
    ) {
        val pref =
            context.getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        if (pref != null) {
            val editor = pref.edit()
            editor.putBoolean(key, value)
            editor.commit()
        }
    }

    fun getUserLoggedIn(
        context: Context,
        key: String?
    ): Boolean {
        val pref =
            context.getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        return pref.getBoolean(key, false)
//        return pref.getString(key, "");
    }

    fun clearAllPreferences(context: Context) {
        val pref =
            context.getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        if (pref != null) {
            val editor = pref.edit()
            editor.clear()
            editor.apply()
        }
    }

    fun clearSocketPreferences(context: Context) {
        val pref =
            context.getSharedPreferences(PreferenceKeys.socketPref, Context.MODE_PRIVATE)
        if (pref != null) {
            val editor = pref.edit()
            editor.clear()
            editor.apply()
        }
    }

    fun getSharedPreferences(
        context: Context,
        key: String?
    ): String? {
        val pref =
            context.getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        return pref.getString(key, null)
//        return pref.getString(key, "");
    }

    fun saveKeyboardStatus(
        context: Context,
        keyboardKey: String?,
        value: Boolean?,
        marketKey: String?,
        marketID: Int?,
        teamKey: String?,
        teamPosition: Int?
    ) {
        val pref =
            context.getSharedPreferences(PreferenceKeys.socketPref, Context.MODE_PRIVATE)
        if (pref != null) {
            val editor = pref.edit()
            editor.putBoolean(keyboardKey, value!!)
            editor.putInt(marketKey, marketID!!)
            editor.putInt(teamKey, teamPosition!!)
            editor.apply()
        }
    }

    private var okButtonClicklistner: OkButtonClicklistner? = null

    interface OkButtonClicklistner {
        fun OkButtonClick()
    }

    fun showLogoutConfirmationDialog(context: Context, listener: OkButtonClicklistner?) {
        val successDialog = Dialog(context)
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        successDialog.setContentView(R.layout.confirm_dialouge)
        successDialog.setCancelable(false)
        successDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        okButtonClicklistner = listener

        val titleTv = successDialog.findViewById<TextView>(R.id.titleTv)
        val messageTv = successDialog.findViewById<TextView>(R.id.messageTv)
        val yesBtn = successDialog.findViewById<TextView>(R.id.yesBtn)
        val noBtn = successDialog.findViewById<TextView>(R.id.noBtn)
        titleTv.visibility = View.VISIBLE
        titleTv.text = context.getString(R.string.logout)
        messageTv.text = context.getString(R.string.are_you_sure_want_to_logout)
        yesBtn.setOnClickListener {
            successDialog.dismiss()
            okButtonClicklistner?.OkButtonClick()
        }
        noBtn.setOnClickListener { successDialog.dismiss() }

        val width = (context.resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        successDialog.window!!.setLayout(width, height)

        if (!(context as Activity).isFinishing) {
            successDialog.show()
        }
    }

    fun showOkDialog(context: Context, message: String) {
        val successDialog = Dialog(context)
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        successDialog.setContentView(R.layout.ok_dialog_layout)
        successDialog.setCancelable(false)
        successDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val messageTv = successDialog.findViewById<TextView>(R.id.messageTv)
        val okBtn = successDialog.findViewById<TextView>(R.id.ok_btn)
        messageTv.text = message
        okBtn.setOnClickListener { successDialog.dismiss() }

        val width = (context.resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        successDialog.window!!.setLayout(width, height)
        if (!(context as Activity).isFinishing) {
            successDialog.show()
        }
    }

    private var smallProgressDialogBox: ProgressDialog? = null

    fun showSmallProgressDialog(context: Context) {
        try {
            if (smallProgressDialogBox == null) {
                smallProgressDialogBox = ProgressDialog(context)
                smallProgressDialogBox?.setCancelable(true)
                smallProgressDialogBox?.setCanceledOnTouchOutside(false)
                smallProgressDialogBox?.getWindow()?.setDimAmount(0.0f)
                smallProgressDialogBox?.show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "showSmallProgressDialog $e")
        }
    }

    fun hideSmallProgressDialog() {
        try {
            if (smallProgressDialogBox != null && smallProgressDialogBox!!.isShowing) {
                smallProgressDialogBox!!.dismiss()
                smallProgressDialogBox = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "hideSmallProgressDialog $e")
        }
    }

    private var placeBetProgress: PlaceBetProgressDialog? = null

    fun showPlaceBetProgress(context: Context) {
        try {
            if (placeBetProgress == null) {
                placeBetProgress = PlaceBetProgressDialog(context)
                placeBetProgress?.setCancelable(true)
                placeBetProgress?.setCanceledOnTouchOutside(false)
                placeBetProgress?.window?.setDimAmount(0.0f)
                placeBetProgress?.show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "placeBetProgress $e")
        }
    }

    fun hidePlaceBetProgress() {
        try {
            if (placeBetProgress != null && placeBetProgress!!.isShowing()) {
                placeBetProgress!!.dismiss()
                placeBetProgress = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "hidePlaceBetProgress $e")
        }
    }

    fun saveChipSettings(context: Context, modelList: ArrayList<ChipSettingsDataModel>) {
        val gson = Gson()
        val json = gson.toJson(modelList)
        val sharedPreferences = context.getSharedPreferences(
            "My Pref",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putString(PreferenceKeys.ChipSetting, json)
        editor.apply()
    }

    fun getChipSettings(context: Context): ArrayList<ChipSettingsDataModel>? {
        val sharedPreferences = context.getSharedPreferences(
            "My Pref",
            Context.MODE_PRIVATE
        )
        val jsonCart =
            sharedPreferences.getString(PreferenceKeys.ChipSetting, null)
        val gson = Gson()
        val type: Type = object : TypeToken<List<ChipSettingsDataModel?>?>() {}.type
        val models: ArrayList<ChipSettingsDataModel> = gson.fromJson(jsonCart, type)
        return models
    }

    fun stringToDate(date: String?, format: String?): Date? {
        val pos = ParsePosition(0)
        val simpledateformat = SimpleDateFormat(format)
        return simpledateformat.parse(date, pos)
    }

    fun convertIntoLocal(date: Date, format: String?): String {
        val simpledateformat = SimpleDateFormat(format)
        simpledateformat.timeZone = TimeZone.getDefault()
        val timeZone = Calendar.getInstance().timeZone.id
        return simpledateformat.format(
            Date(
                date.time + TimeZone.getTimeZone(timeZone).getOffset(date.time)
            )
        )
    }

    fun showRunPositionDialog(context: Context, betList: ArrayList<BetModel>) {
        val successDialog = Dialog(context)
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        successDialog.setContentView(R.layout.run_position_dialog_layout)
        successDialog.setCancelable(false)
        successDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val ivClose: ImageView = successDialog.findViewById(R.id.iv_close)
        ivClose.setOnClickListener { successDialog.dismiss() }
        val runPositionRecycler: RecyclerView =
            successDialog.findViewById(R.id.run_position_recycler)
        runPositionRecycler.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL, false
        )

        val runList = ArrayList<Int>()
        val runPositionList = ArrayList<RunPositionModel>()
        for (i in 0 until betList.size) {
            runList.add(betList[i].Run)
        }
        val minRun = runList.min()
        val maxRun = runList.max()
        if (minRun == maxRun && betList.size > 0) {
            val isBack = betList[0].IsBack
            if (isBack) {
                val runPositionModel = RunPositionModel()
                runPositionModel.run = minRun!!.minus(1)
                runPositionList.add(runPositionModel)
            }
            val runPositionModel1 = RunPositionModel()
            runPositionModel1.run = minRun!!.plus(if (isBack) 0 else -1)
            runPositionList.add(runPositionModel1)
            val runPositionModel = RunPositionModel()
            runPositionModel.run = minRun.plus(if (!isBack) 0 else 1)
            runPositionList.add(runPositionModel)
        } else {
            for (i in minRun!! - 1..maxRun!! + 1) {
                val runPositionModel = RunPositionModel()
                runPositionModel.run = i
                runPositionList.add(runPositionModel)
            }
        }

        Log.e(TAG, "runPositionList " + runPositionList.size)
        if (runPositionList.size > 0) {
            for (i in 0 until runPositionList.size) {
                var Value = 0
                for (j in 0 until betList.size) {
                    val betModel = betList[j]
                    Value += if (betModel.IsBack && runPositionList[i].run >= betModel.Run)
                        (betModel.Stake * (betModel.Rate / 100)).toInt()
                    else if (!betModel.IsBack && runPositionList[i].run < betModel.Run)
                        betModel.Stake
                    else {
                        if (betModel.IsBack)
                            -1 * betModel.Stake
                        else
                            -1 * (betModel.Stake * (betModel.Rate / 100)).toInt()
                    }
                }
                runPositionList[i].value = Value
            }
        }
        Log.e(TAG, "runPositionList $runPositionList")
        val runPositionAdapter = RunPositionListAdapter(runPositionList, context)
        runPositionRecycler.adapter = runPositionAdapter
        val width = (context.resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        successDialog.window!!.setLayout(width, height)
        if (!(context as Activity).isFinishing) {
            successDialog.show()
        }
    }

    fun saveBalanceAndLiability(
        runningBalance: Float, LiabilityList: ArrayList<LiabilityDataModel>, context: Context
    ) {
        DashboardActivity.liabilityList.clear()
        DashboardActivity.decimalFormat0_00.format(
            runningBalance.toDouble().toBigDecimal()
        )
        DashboardActivity.tvBalance.text = DashboardActivity.decimalFormat0_00.format(
            runningBalance.toDouble().toBigDecimal()
        ).toString()
        var liability = 0.0
        DashboardActivity.liabilityList = LiabilityList
        if (DashboardActivity.liabilityList.size > 0) {
            for (i in 0 until DashboardActivity.liabilityList.size) {
                liability += DashboardActivity.liabilityList[i].Liability
            }
        }
        DashboardActivity.tvLiability.text = DashboardActivity.decimalFormat0_00.format(
            liability.toBigDecimal()
        ).toString()
        saveSharedPreferences(
            context,
            PreferenceKeys.liability,
            DashboardActivity.tvLiability.text.toString()
        )
        saveSharedPreferences(
            context,
            PreferenceKeys.balance,
            DashboardActivity.tvBalance.text.toString()
        )
//        DashboardActivity.tvBalanceAcc.text =
//            getSharedPreferences(context, PreferenceKeys.balance)
//        DashboardActivity.tvLiabilityAcc.text =
//            getSharedPreferences(context, PreferenceKeys.liability)
    }

    fun setStayConnected(
        context: Context, usernameKey: String?, username: String, passwordKey: String,
        password: String
    ) {
        val pref =
            context.getSharedPreferences("StayConnected", Context.MODE_PRIVATE)
        if (pref != null) {
            val editor = pref.edit()
            editor.putString(usernameKey, username)
            editor.putString(passwordKey, password)
            editor.commit()
        }
    }

    fun getLoggedInUserName(context: Context): String? {
        val pref =
            context.getSharedPreferences("StayConnected", Context.MODE_PRIVATE)
        return pref.getString(PreferenceKeys.loggedInUserName, "");
    }

    fun getLoggedInUserPass(context: Context): String? {
        val pref =
            context.getSharedPreferences("StayConnected", Context.MODE_PRIVATE)
        return pref.getString(PreferenceKeys.loggedInUserPass, "");
    }

    fun clearStayConnected(context: Context) {
        val pref =
            context.getSharedPreferences("StayConnected", Context.MODE_PRIVATE)
        if (pref != null) {
            val editor = pref.edit()
            editor.clear()
            editor.apply()
        }
    }

    fun showConfirmationDialog(context: Context, message: String, listener: OkButtonClicklistner?) {
        val successDialog = Dialog(context)
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        successDialog.setContentView(R.layout.confirm_dialouge)
        successDialog.setCancelable(false)
        successDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        okButtonClicklistner = listener

        val titleTv = successDialog.findViewById<TextView>(R.id.titleTv)
        titleTv.text = context.getString(R.string.confirmation)
        val messageTv = successDialog.findViewById<TextView>(R.id.messageTv)
        messageTv.text = message
        val yesBtn = successDialog.findViewById<TextView>(R.id.yesBtn)
        val noBtn = successDialog.findViewById<TextView>(R.id.noBtn)
        titleTv.visibility = View.VISIBLE
        yesBtn.setOnClickListener {
            successDialog.dismiss()
            okButtonClicklistner?.OkButtonClick()
        }
        noBtn.setOnClickListener { successDialog.dismiss() }

        val width = (context.resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        successDialog.window!!.setLayout(width, height)

        if (!(context as Activity).isFinishing) {
            successDialog.show()
        }
    }

    fun saveBalAndLiabilityInSingle(
        runningBalance: Float, LiabilityList: ArrayList<LiabilityDataModel>, context: Context
    ) {
        DashboardActivity.liabilityList = LiabilityList
        var liability = 0.0
        if (LiabilityList.size > 0) {
            for (i in 0 until LiabilityList.size) {
                liability += LiabilityList[i].Liability
            }
        }
        saveSharedPreferences(
            context,
            PreferenceKeys.liability,
            DashboardActivity.decimalFormat0_00.format(
                liability.toBigDecimal()
            ).toString()
        )
        saveSharedPreferences(
            context,
            PreferenceKeys.balance,
            DashboardActivity.decimalFormat0_00.format(
                runningBalance.toDouble().toBigDecimal()
            ).toString()
        )
        SingleMarketActivity.tvBalance.text = context.resources.getString(R.string.balance) + ": " +
                getSharedPreferences(context, PreferenceKeys.balance)
        SingleMarketActivity.tvLiability.text =
            context.resources.getString(R.string.credit) + ": " +
                    getSharedPreferences(context, PreferenceKeys.liability)
    }

    fun showLiveVideo(context: Context, url: String) {
        val successDialog = Dialog(context)
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        successDialog.setContentView(R.layout.live_video_dialog_layout)
        successDialog.setCancelable(false)
        successDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvFullScreen = successDialog.findViewById<TextView>(R.id.tv_full_screen)
        tvFullScreen.setOnClickListener {
            val intent = Intent(context, FullScreenLiveVideoActivity::class.java)
            intent.putExtra(IntentKeys.liveUrl, url)
            context.startActivity(intent)
        }
        val ivClose = successDialog.findViewById<ImageView>(R.id.iv_close)
        ivClose.setOnClickListener { successDialog.dismiss() }
        val webView = successDialog.findViewById<WebView>(R.id.webview)
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.domStorageEnabled = true
        webView.settings.builtInZoomControls = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.webViewClient = WebViewClient()
        webView.loadUrl(url)
        Log.e("showLiveVideo", "url: $url")

        val width = (context.resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        successDialog.window!!.setLayout(width, height)
        if (!(context as Activity).isFinishing) {
            successDialog.show()
        }
    }

    fun getScoreUrlFromScoreApi(
        context: Context,
        api: String
    ): String {
        var scoreUrl = ""
        if (isInternetAvailable(context)) {
            val call: Call<Common> = RetrofitApiClient.getMarketApiClient.getScoreUrl(
                api,
                getSharedPreferences(context, PreferenceKeys.AuthToken)!!
            )
            call.enqueue(object : Callback<Common> {
                override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                    Log.e(TAG, "getScoreUrl: " + Gson().toJson(response?.body()))
                    val common: Common? = response?.body()
                    if (response != null && response.isSuccessful) {
                        hideSmallProgressDialog()
                        when (common?.status?.code) {
                            0 -> {
                                if (common.ScoreUrl != "")
                                    scoreUrl = common.ScoreUrl
                            }
                            401 -> {
                            }
                            else -> {
                            }
                        }
                    } else {
                    }
                }

                override fun onFailure(call: Call<Common>?, t: Throwable?) {
                    hideSmallProgressDialog()
                    Log.e(TAG, "getScoreUrl: " + t.toString())
                }
            })
        } else {
            hideSmallProgressDialog()
        }
        Log.e(TAG, "getScoreUrlFromScoreApi: $scoreUrl")
        return scoreUrl
    }

    fun betSuccessToast(context: Context?, message: String) {
        val customLayout: View = LayoutInflater.from(context).inflate(
            R.layout.bet_success_custom_toast,
            null, false
        )
        val tvMessage: TextView = customLayout.findViewById(R.id.tv_message)
        tvMessage.text = message
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = customLayout
        toast.show()
    }

    fun betErrorToast(context: Context?, message: String) {
        val customLayout: View = LayoutInflater.from(context).inflate(
            R.layout.bet_error_custom_toast,
            null, false
        )
        val tvErrorMessage: TextView = customLayout.findViewById(R.id.tv_error_text)
        tvErrorMessage.text = message
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = customLayout
        toast.show()
    }
}