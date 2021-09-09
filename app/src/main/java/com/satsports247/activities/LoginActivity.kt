package com.satsports247.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.satsports247.R
import com.satsports247.constants.*
import com.satsports247.dataModels.ChipSettingsDataModel
import com.satsports247.databinding.ActivityLoginBinding
import com.satsports247.responseModels.Common
import com.satsports247.retrofit.RetrofitApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.experimental.and

class LoginActivity : AppCompatActivity() {

    var TAG: String = "LoginActivity"
    var isCheckedPassword = false
    private lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        binding.edtPassword.typeface = Typeface.DEFAULT
        binding.edtPassword.transformationMethod = PasswordTransformationMethod()
        binding.edtPassword.setOnTouchListener(View.OnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.edtPassword.right -
                            binding.edtPassword.compoundDrawables[DRAWABLE_RIGHT].bounds.width() - 5)
                ) {
                    isCheckedPassword = !isCheckedPassword
                    Config.showIcon(binding.edtPassword, isCheckedPassword)
                    binding.edtPassword.setSelection(binding.edtPassword.length())
                    return@OnTouchListener true
                }
            }
            return@OnTouchListener false
        })

        binding.btnLogin.setOnClickListener {
            checkValidations()
        }
        binding.tvRegister.setOnClickListener { v: View? ->
            startActivityForResult(Intent(this, RegisterActivity::class.java), 1)
        }

        //Stay Connected logic
        binding.cbConnected.setOnCheckedChangeListener { _, isChecked ->
            AppConstants.stayConnected = isChecked
            if (!isChecked) {
                Config.clearStayConnected(this)
            }
        }
//        binding.cbConnected.isChecked = AppConstants.stayConnected
        if (AppConstants.stayConnected) {
            binding.edtUsername.setText(Config.getLoggedInUserName(this))
            binding.edtPassword.setText(Config.getLoggedInUserPass(this))
        }
    }

    private fun getSecretKey(bodyAndKey: String): String {
        Log.e(TAG, "bodyAndKey: $bodyAndKey")
        val md: MessageDigest = MessageDigest.getInstance("SHA-256")
        val hash: ByteArray = md.digest(bodyAndKey.toByteArray(StandardCharsets.UTF_8))
//        val number = BigInteger(1, hash)
//        val hexString = StringBuilder(number.toString(16))
//        while (hexString.length < 32) {
//        hexString.insert(0, '0')
//        }

        val hexString = StringBuilder(2 * hash.size)
        for (b in hash) {
            hexString.append(String.format("%02x", b and 0xff.toByte()))
        }

        Log.e(TAG, "X-Signature: $hexString")
        return hexString.toString()
    }

    private fun checkValidations() {
        clearErrorViews()
        when {
            binding.edtUsername.text.isEmpty() -> {
                binding.tvErrorName.visibility = View.VISIBLE
                binding.edtUsername.background =
                    ContextCompat.getDrawable(this, R.drawable.border_error_edittext)
                binding.edtUsername.requestFocus()
            }
            binding.edtPassword.text.isEmpty() -> {
                binding.tvErrorPassword.visibility = View.VISIBLE
                binding.edtPassword.background =
                    ContextCompat.getDrawable(this, R.drawable.border_error_edittext)
                binding.edtPassword.requestFocus()
            }
            else -> {
                clearErrorViews()
                login()
            }
        }
    }

    private fun clearErrorViews() {
        binding.tvErrorName.visibility = View.GONE
        binding.tvErrorPassword.visibility = View.GONE
        binding.edtUsername.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
        binding.edtPassword.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
    }

    private fun login() {
        val jsonObject = JsonObject()
        jsonObject.addProperty(JsonKeys.Username, binding.edtUsername.text.toString())
        jsonObject.addProperty(JsonKeys.Password, binding.edtPassword.text.toString())
        Log.e(TAG, "login body: $jsonObject")
        val signature: String = getSecretKey(jsonObject.toString() + AppConstants.SecretKey)
        try {
            if (Config.isInternetAvailable(this)) {
                Config.showSmallProgressDialog(this)
                val call: Call<Common> = RetrofitApiClient.getClient.login(
                    signature, jsonObject
                )
                call.enqueue(object : Callback<Common> {
                    override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                        if (response != null && response.isSuccessful) {
                            Config.hideSmallProgressDialog()
                            val common: Common? = response.body()
                            Log.e(TAG, "login response: " + Gson().toJson(common))
                            val message: String? = common?.status?.returnMessage
                            val code: Int? = common?.status?.code
                            if (code == 0) {
                                saveDataInPreference(common)
                                if (AppConstants.stayConnected) {
                                    Config.setStayConnected(
                                        this@LoginActivity,
                                        PreferenceKeys.loggedInUserName,
                                        binding.edtUsername.text.toString(),
                                        PreferenceKeys.loggedInUserPass,
                                        binding.edtPassword.text.toString()
                                    )
                                }
                            } else
                                Config.toast(this@LoginActivity, message)
                        }
                    }

                    override fun onFailure(call: Call<Common>?, t: Throwable?) {
                        Config.hideSmallProgressDialog()
                        Log.e(TAG, "login failed: " + t.toString())
                    }
                })
            } else {
                Config.hideSmallProgressDialog()
                Config.toast(this, getString(R.string.please_check_internet_connection))
            }
        } catch (e: Exception) {
            Config.toast(this, "" + e)
            Log.e(TAG, "" + e)
        }
    }

    private fun saveDataInPreference(common: Common?) {
        Config.saveSharedPreferences(
            this@LoginActivity,
            PreferenceKeys.UserId, common?.user?.UserId.toString()
        )
        Config.saveSharedPreferences(
            this@LoginActivity,
            PreferenceKeys.Username, common?.user?.Username.toString()
        )
        Config.saveSharedPreferences(
            this@LoginActivity,
            PreferenceKeys.AuthToken, common?.user?.AuthToken
        )
        Config.saveSharedPreferences(
            this@LoginActivity,
            PreferenceKeys.BetToken, common?.user?.BetToken
        )
        val defaultChipSettings = ArrayList<ChipSettingsDataModel>()
        defaultChipSettings.add(ChipSettingsDataModel("50", 50))
        defaultChipSettings.add(ChipSettingsDataModel("100", 100))
        defaultChipSettings.add(ChipSettingsDataModel("150", 150))
        defaultChipSettings.add(ChipSettingsDataModel("200", 200))
        defaultChipSettings.add(ChipSettingsDataModel("250", 250))
        defaultChipSettings.add(ChipSettingsDataModel("300", 300))
        Config.saveChipSettings(this@LoginActivity, defaultChipSettings)
        try {
            if (!common?.user?.ChipSetting.isNullOrEmpty()) {
                Config.saveChipSettings(this, common?.user?.ChipSetting!!)
                /*val str1: String? = common?.user?.ChipSetting?.replace("\"[", "[")
                val str2: String? = str1?.replace("]\"", "]")
                val jsonArray: JsonArray = Gson().fromJson(str2, JsonArray::class.java)
                if (jsonArray.size() > 0) {
                    val chipSettings = ArrayList<ChipSettingsDataModel>()
                    for (i in 0 until jsonArray.size()) {
                        chipSettings.add(
                            ChipSettingsDataModel(
                                jsonArray[i].asJsonObject.get("name").asString,
                                jsonArray[i].asJsonObject.get("value").asInt
                            )
                        )
                    }
                    Config.saveChipSettings(this@LoginActivity, chipSettings)
                }*/
            }
        } catch (e: Exception) {
            Config.toast(this, "" + e)
            Log.e(TAG, "" + e)
        }
        Config.saveSharedPreferences(
            this@LoginActivity,
            PreferenceKeys.SitePermissionCode, common?.user?.SitePermissionCode.toString()
        )
        Config.saveSharedPreferences(
            this@LoginActivity,
            PreferenceKeys.UserDetailId, common?.profileDetail?.UserDetailId.toString()
        )
        if (common?.profileDetail?.Email != null)
            Config.saveSharedPreferences(
                this@LoginActivity,
                PreferenceKeys.Email, common?.profileDetail?.Email.toString()
            )
        if (common?.profileDetail?.MobileNo != null)
            Config.saveSharedPreferences(
                this@LoginActivity,
                PreferenceKeys.MobileNo, common?.profileDetail?.MobileNo.toString()
            )
        if (common?.profileDetail?.BankName != null)
            Config.saveSharedPreferences(
                this@LoginActivity,
                PreferenceKeys.BankName, common?.profileDetail?.BankName.toString()
            )
        if (common?.profileDetail?.BankAccNo != null)
            Config.saveSharedPreferences(
                this@LoginActivity,
                PreferenceKeys.BankAccNo, common?.profileDetail?.BankAccNo.toString()
            )
        if (common?.profileDetail?.AccountHolderName != null)
            Config.saveSharedPreferences(
                this@LoginActivity,
                PreferenceKeys.AccountHolderName,
                common?.profileDetail?.AccountHolderName.toString()
            )
        if (common?.profileDetail?.BankIFSC != null)
            Config.saveSharedPreferences(
                this@LoginActivity,
                PreferenceKeys.BankIFSC, common?.profileDetail?.BankIFSC.toString()
            )
        if (common?.NewsData?.size!! > 0)
            Config.saveSharedPreferences(
                this,
                PreferenceKeys.newsTitle,
                common.NewsData[0].NewsTitle
            )
        Config.hideSmallProgressDialog()
        startActivity(Intent(this@LoginActivity, TermsAndConditionsActivity::class.java))
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data!!.hasExtra(IntentKeys.username))
            if (resultCode == RESULT_OK) {
                binding.edtUsername.setText(data.getStringExtra(IntentKeys.username))
                binding.edtPassword.setText(data.getStringExtra(IntentKeys.password))
            }
    }
}