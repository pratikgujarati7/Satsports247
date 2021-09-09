package com.satsports247.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.satsports247.R
import com.satsports247.constants.Config
import com.satsports247.constants.JsonKeys
import com.satsports247.constants.PreferenceKeys
import com.satsports247.databinding.ActivityWithdrawBinding
import com.satsports247.responseModels.Common
import com.satsports247.retrofit.RetrofitApiClient
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WithdrawActivity : AppCompatActivity() {

    val TAG: String = "WithdrawActivity"
    lateinit var binding: ActivityWithdrawBinding
    var accountNumber = ""
    var holderName = ""
    var bankName = ""
    var branchIFSC = ""
    var mobileNumber = ""
    var amount = ""
    var description = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWithdrawBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        getDefaultData()
        setDefaultData()
        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.btnWithdrawRequest.setOnClickListener { checkValidationsAndSave() }
    }

    private fun setDefaultData() {
        if (accountNumber.isNotEmpty())
            binding.edtAccountNumber.setText(accountNumber)
        if (holderName.isNotEmpty())
            binding.edtHolderName.setText(holderName)
        if (bankName.isNotEmpty())
            binding.edtBankName.setText(bankName)
        if (branchIFSC.isNotEmpty())
            binding.edtIfsc.setText(branchIFSC)
        if (mobileNumber.isNotEmpty())
            binding.edtMobileNumber.setText(mobileNumber)
    }

    private fun getDefaultData() {
        if (Config.getSharedPreferences(this, PreferenceKeys.BankAccNo) != null)
            accountNumber = Config.getSharedPreferences(this, PreferenceKeys.BankAccNo)!!
        if (Config.getSharedPreferences(this, PreferenceKeys.AccountHolderName) != null)
            holderName = Config.getSharedPreferences(this, PreferenceKeys.AccountHolderName)!!
        if (Config.getSharedPreferences(this, PreferenceKeys.BankName) != null)
            bankName = Config.getSharedPreferences(this, PreferenceKeys.BankName)!!
        if (Config.getSharedPreferences(this, PreferenceKeys.BankIFSC) != null)
            branchIFSC = Config.getSharedPreferences(this, PreferenceKeys.BankIFSC)!!
        if (Config.getSharedPreferences(this, PreferenceKeys.MobileNo) != null)
            mobileNumber = Config.getSharedPreferences(this, PreferenceKeys.MobileNo)!!
    }

    private fun checkValidationsAndSave() {
        when {
            binding.edtBankName.text.toString().isEmpty() -> {
                binding.tilBankName.error = getString(R.string.please_enter_bank_name)
                binding.edtBankName.requestFocus()
            }
            binding.edtIfsc.text.toString().isEmpty() -> {
                binding.tilIfsc.error = getString(R.string.please_enter_branch_ifsc)
                binding.edtIfsc.requestFocus()
            }
            binding.edtMobileNumber.text.toString().isEmpty() -> {
                binding.tilMobileNumber.error = getString(R.string.please_enter_mobile_number)
                binding.edtMobileNumber.requestFocus()
            }
            binding.edtAmount.text.toString().isEmpty() -> {
                binding.tilAmount.error = getString(R.string.please_enter_amount)
                binding.edtAmount.requestFocus()
            }
            binding.edtAmount.text.toString().toDouble() > Config.getSharedPreferences(
                this,
                PreferenceKeys.balance
            )?.toDouble()!! -> {
                binding.tilAmount.error = getString(R.string.you_have_sufficient_balance)
                binding.edtAmount.requestFocus()
            }
            else -> {
                withdrawRequest()
            }
        }
    }

    private fun withdrawRequest() {
        bankName = binding.edtBankName.text.toString()
        accountNumber = binding.edtAccountNumber.text.toString()
        holderName = binding.edtHolderName.text.toString()
        branchIFSC = binding.edtIfsc.text.toString()
        mobileNumber = binding.edtMobileNumber.text.toString()
        description = binding.edtDescription.text.toString()
        amount = binding.edtAmount.text.toString()
        val jsonObject = JsonObject()
        jsonObject.addProperty(JsonKeys.WithdrawalAmount, amount)
        jsonObject.addProperty(JsonKeys.BankName, bankName)
        jsonObject.addProperty(JsonKeys.BankAccNo, accountNumber)
        jsonObject.addProperty(JsonKeys.AccountHolderName, holderName)
        jsonObject.addProperty(JsonKeys.BankIFSC, branchIFSC)
        jsonObject.addProperty(JsonKeys.MobileNo, mobileNumber)
        jsonObject.addProperty(JsonKeys.Description, description)
        Log.e(TAG, "jsonObject: $jsonObject")
        try {
            if (Config.isInternetAvailable(this)) {
                val call: Call<Common> = RetrofitApiClient.getClient.withdrawRequest(
                    Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!, jsonObject
                )
                call.enqueue(object : Callback<Common> {
                    override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                        if (response != null) {
                            when (response.code()) {
                                200 -> {
                                    val common: Common? = response.body()
                                    Log.e(TAG, "response: " + Gson().toJson(common))
                                    Config.toast(
                                        this@WithdrawActivity,
                                        common?.status?.returnMessage
                                    )
                                    when (common?.status?.code) {
                                        0 -> {
                                            Config.saveSharedPreferences(
                                                this@WithdrawActivity,
                                                PreferenceKeys.balance,
                                                common.RunningBalance.toString()
                                            )
                                            finish()
                                        }
                                        401 -> {
                                            val realm = Realm.getDefaultInstance()
                                            realm.executeTransaction { mRealm -> mRealm.deleteAll() }
                                            Config.clearAllPreferences(this@WithdrawActivity)
                                            startActivity(
                                                Intent(
                                                    this@WithdrawActivity,
                                                    LoginActivity::class.java
                                                )
                                            )
                                            finishAffinity()
                                        }
                                    }
                                }
                                401 -> {
                                    val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { mRealm -> mRealm.deleteAll() }
                                    Config.clearAllPreferences(this@WithdrawActivity)
                                    startActivity(
                                        Intent(
                                            this@WithdrawActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finishAffinity()
                                }
                            }
                        } else {
                            val realm = Realm.getDefaultInstance()
                            realm.executeTransaction { mRealm -> mRealm.deleteAll() }
                            Config.clearAllPreferences(this@WithdrawActivity)
                            startActivity(
                                Intent(
                                    this@WithdrawActivity,
                                    LoginActivity::class.java
                                )
                            )
                            finishAffinity()
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

    override fun onBackPressed() {
        super.onBackPressed()
        finishAfterTransition()
    }

}