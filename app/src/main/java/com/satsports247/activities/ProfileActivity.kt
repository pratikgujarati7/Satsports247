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
import com.satsports247.databinding.ActivityProfileBinding
import com.satsports247.responseModels.Common
import com.satsports247.retrofit.RetrofitApiClient
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    val TAG: String = "ProfileActivity"
    lateinit var binding: ActivityProfileBinding
    var email = ""
    var accountNumber = ""
    var holderName = ""
    var bankName = ""
    var branchIFSC = ""
    var mobileNumber = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {

        getDefaultData()

        setDefaultData()

        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.btnSave.setOnClickListener { checkValidationsAndSave() }
    }

    private fun checkValidationsAndSave() {
        email = binding.edtEmail.text.toString()
        if (email.isEmpty() || !Config.emailValidator(email.trim())) {
            binding.tilEmail.error = getString(R.string.please_enter_valid_email)
            binding.tilEmail.requestFocus()
        } else {
            binding.tilEmail.isErrorEnabled = false
            callUpdateProfileApi()
        }
    }

    private fun callUpdateProfileApi() {
        email = binding.edtEmail.text.toString()
        holderName = binding.edtHolderName.text.toString()
        mobileNumber = binding.edtMobileNumber.text.toString()
        bankName = binding.edtBankName.text.toString()
        accountNumber = binding.edtAccountNumber.text.toString()
        branchIFSC = binding.edtIfsc.text.toString()
        val jsonObject = JsonObject()
        jsonObject.addProperty(JsonKeys.Email, email)
        jsonObject.addProperty(JsonKeys.AccountHolderName, holderName)
        jsonObject.addProperty(JsonKeys.MobileNo, mobileNumber)
        jsonObject.addProperty(JsonKeys.BankIFSC, branchIFSC)
        jsonObject.addProperty(JsonKeys.BankName, bankName)
        jsonObject.addProperty(JsonKeys.BankAccNo, accountNumber)
        Log.e(TAG, "jsonObject: $jsonObject")
        try {
            if (Config.isInternetAvailable(this)) {
                val call: Call<Common> = RetrofitApiClient.getClient.updateProfile(
                    Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!, jsonObject
                )
                call.enqueue(object : Callback<Common> {
                    override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                        val common: Common? = response?.body()
                        Log.e(TAG, "response: " + Gson().toJson(common))
                        if (response != null) {
                            when (response.code()) {
                                200 -> {
                                    Config.toast(
                                        this@ProfileActivity,
                                        common?.status?.returnMessage
                                    )
                                    if (common?.status?.code == 0) {
                                        saveData(common)
                                        finish()
                                    } else if (common?.status?.code == 401) {
                                        val realm = Realm.getDefaultInstance()
                                        realm.executeTransaction { realm -> realm.deleteAll() }
                                        Config.clearAllPreferences(this@ProfileActivity)
                                        startActivity(
                                            Intent(
                                                this@ProfileActivity,
                                                LoginActivity::class.java
                                            )
                                        )
                                        finish()
                                    }
                                }
                                400 -> {
                                    val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { realm -> realm.deleteAll() }
                                    Config.clearAllPreferences(this@ProfileActivity)
                                    startActivity(
                                        Intent(
                                            this@ProfileActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                            }

                        } else {
                            val realm = Realm.getDefaultInstance()
                            realm.executeTransaction { realm -> realm.deleteAll() }
                            Config.clearAllPreferences(this@ProfileActivity)
                            startActivity(
                                Intent(
                                    this@ProfileActivity,
                                    LoginActivity::class.java
                                )
                            )
                            finish()
                        }
                    }

                    override fun onFailure(call: Call<Common>?, t: Throwable?) {
                        Log.e(TAG, "register failed: " + t.toString())
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

    private fun saveData(common: Common) {
        Config.saveSharedPreferences(this, PreferenceKeys.Email, email)
        Config.saveSharedPreferences(this, PreferenceKeys.MobileNo, mobileNumber)
        Config.saveSharedPreferences(this, PreferenceKeys.AccountHolderName, holderName)
        Config.saveSharedPreferences(this, PreferenceKeys.BankName, bankName)
        Config.saveSharedPreferences(this, PreferenceKeys.BankAccNo, accountNumber)
        Config.saveSharedPreferences(this, PreferenceKeys.BankIFSC, branchIFSC)
    }

    private fun setDefaultData() {
        if (email.isNotEmpty())
            binding.edtEmail.setText(email)
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
        if (Config.getSharedPreferences(this, PreferenceKeys.Email) != null)
            email = Config.getSharedPreferences(this, PreferenceKeys.Email)!!
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

    override fun onBackPressed() {
        super.onBackPressed()
        finishAfterTransition()
    }
}