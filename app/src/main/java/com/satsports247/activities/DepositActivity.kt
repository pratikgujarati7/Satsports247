package com.satsports247.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.satsports247.R
import com.satsports247.constants.Config
import com.satsports247.constants.IntentKeys
import com.satsports247.constants.JsonKeys
import com.satsports247.constants.PreferenceKeys
import com.satsports247.dataModels.LiabilityDataModel
import com.satsports247.databinding.ActivityDepositBinding
import com.satsports247.responseModels.Common
import com.satsports247.retrofit.RetrofitApiClient
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DepositActivity : AppCompatActivity() {

    val TAG: String = "DepositActivity"
    lateinit var binding: ActivityDepositBinding
    var email = ""
    var mobileNumber = ""
    var liabilityList = ArrayList<LiabilityDataModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.btnSave.setOnClickListener { checkValidation() }
    }

    override fun onResume() {
        super.onResume()
        getBalanceAndLiability()
    }

    private fun checkValidation() {
        email = Config.getSharedPreferences(this, PreferenceKeys.Email).toString()
        mobileNumber = Config.getSharedPreferences(this, PreferenceKeys.MobileNo).toString()
        when {
            email.isEmpty() -> {
                Config.toast(this, getString(R.string.enter_email_from_profile))
            }
            !Config.emailValidator(email) -> {
                Config.toast(this, getString(R.string.valid_email_from_profile))
            }
            mobileNumber.isEmpty() -> {
                Config.toast(this, getString(R.string.enter_mobile_from_profile))
            }
            !Config.mobileNumberValidator(mobileNumber) -> {
                Config.toast(this, getString(R.string.valid_mobile_from_profile))
            }
            binding.edtAmount.text.toString().isEmpty() -> {
                binding.tilAmount.error = getString(R.string.please_enter_amount)
                binding.edtAmount.requestFocus()
            }
            else -> {
                depositAmount()
            }
        }
    }

    private fun depositAmount() {
        val jsonObject = JsonObject()
        jsonObject.addProperty(JsonKeys.Amount, binding.edtAmount.text.toString())
        jsonObject.addProperty(JsonKeys.Email, email)
        jsonObject.addProperty(JsonKeys.MobileNo, mobileNumber)
        Log.e(TAG, "body: $jsonObject")
        try {
            if (Config.isInternetAvailable(this)) {
                val call: Call<Common> = RetrofitApiClient.getClient.deposit(
                    Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!, jsonObject
                )
                call.enqueue(object : Callback<Common> {
                    override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                        Log.e(TAG, "response: " + Gson().toJson(response?.body()))
                        if (response != null && response.isSuccessful) {
                            when (response.code()) {
                                200 -> {
                                    val common: Common? = response.body()
                                    when (common?.status?.code) {
                                        0 -> {
//                                    val openURL = Intent(Intent.ACTION_VIEW)
//                                    openURL.data = Uri.parse(common.Url)
//                                    startActivity(openURL)
                                            val intent = Intent(
                                                this@DepositActivity,
                                                DepositWebViewActivity::class.java
                                            )
                                            intent.putExtra(IntentKeys.depositUrl, common.Url)
                                            startActivityForResult(intent, 3)
                                        }
                                        401 -> {
                                            val realm = Realm.getDefaultInstance()
                                            realm.executeTransaction { realm -> realm.deleteAll() }
                                            Config.clearAllPreferences(this@DepositActivity)
                                            startActivity(
                                                Intent(
                                                    this@DepositActivity,
                                                    LoginActivity::class.java
                                                )
                                            )
                                            finish()
                                        }
                                        else -> {
                                            Config.toast(
                                                this@DepositActivity,
                                                common?.status?.returnMessage
                                            )
                                        }
                                    }
                                }
                                401 -> {
                                    val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { realm -> realm.deleteAll() }
                                    Config.clearAllPreferences(this@DepositActivity)
                                    startActivity(
                                        Intent(
                                            this@DepositActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                            }
                        } else {
                            val realm = Realm.getDefaultInstance()
                            realm.executeTransaction { realm -> realm.deleteAll() }
                            Config.clearAllPreferences(this@DepositActivity)
                            startActivity(
                                Intent(
                                    this@DepositActivity,
                                    LoginActivity::class.java
                                )
                            )
                            finish()
                        }
                    }

                    override fun onFailure(call: Call<Common>?, t: Throwable?) {
                        Log.e(TAG, "failed: " + t.toString())
                    }
                })
            } else {
                Config.toast(this, getString(R.string.please_check_internet_connection))
            }
        } catch (e: Exception) {
            Config.toast(this, "" + e)
            Log.e(TAG, "" + e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3 && resultCode == RESULT_OK) {
            onBackPressed()
        }
    }

    private fun getBalanceAndLiability() {
        try {
            if (Config.isInternetAvailable(this)) {
                val call: Call<Common> = RetrofitApiClient.getClient.getBalanceAndLiability(
                    Config.getSharedPreferences(this, PreferenceKeys.AuthToken)
                )
                call.enqueue(object : Callback<Common> {
                    override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                        val common: Common? = response?.body()
                        Log.e(TAG, "getBalanceAndLiability: " + Gson().toJson(common))
                        if (response != null && response.isSuccessful) {
                            when (response.code()) {
                                200 -> {
                                    saveBalanceAndLiability(
                                        common?.RunningBalance!!,
                                        common.Liability
                                    )
                                }
                                401 -> {
                                    val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { realm -> realm.deleteAll() }
                                    Config.clearAllPreferences(this@DepositActivity)
                                    startActivity(
                                        Intent(
                                            this@DepositActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<Common>?, t: Throwable?) {
                        Log.e(TAG, "getBalanceAndLiability: " + t.toString())
                    }
                })
            } else {
                Config.toast(this, getString(R.string.please_check_internet_connection))
            }
        } catch (e: Exception) {
            Config.toast(this, "" + e)
            Log.e(TAG, "" + e)
        }
    }

    fun saveBalanceAndLiability(
        runningBalance: Float,
        LiabilityList: ArrayList<LiabilityDataModel>
    ) {
        var liability = 0.0
        liabilityList = LiabilityList
        if (liabilityList.size > 0) {
            for (i in 0 until liabilityList.size) {
                liability += liabilityList[i].Liability
            }
        }
        Config.saveSharedPreferences(
            this,
            PreferenceKeys.liability,
            liability.toString()
        )
        Config.saveSharedPreferences(
            this,
            PreferenceKeys.balance,
            runningBalance.toString()
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAfterTransition()
    }
}