package com.satsports247.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.satsports247.R
import com.satsports247.constants.Config
import com.satsports247.constants.JsonKeys
import com.satsports247.constants.PreferenceKeys
import com.satsports247.databinding.ActivityChangePasswordBinding
import com.satsports247.responseModels.Common
import com.satsports247.retrofit.RetrofitApiClient
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : AppCompatActivity() {

    val TAG: String = "ChangePasswordActivity"
    lateinit var binding: ActivityChangePasswordBinding
    var oldPass = ""
    var newPass = ""
    var cnfPass = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.tvUsername.text =
            "Username, ${Config.getSharedPreferences(this, PreferenceKeys.Username)}"
        binding.btnSave.setOnClickListener {
            checkValidations()
        }
    }

    private fun checkValidations() {
        clearError()
        when {
            binding.edtOldPass.text.isEmpty() -> {
                binding.tilOldPass.error = getString(R.string.please_enter_old_pass)
                binding.edtOldPass.requestFocus()
            }
            binding.edtNewPass.text.isEmpty() -> {
                binding.tilNewPass.error = getString(R.string.please_enter_new_pass)
                binding.edtNewPass.requestFocus()
            }
            binding.edtCnfPass.text.isEmpty() -> {
                binding.tilCnfPass.error = getString(R.string.please_enter_new_pass_again)
                binding.edtCnfPass.requestFocus()
            }
            binding.edtNewPass.text.toString() != binding.edtCnfPass.text.toString() -> {
                binding.tilCnfPass.error = getString(R.string.pass_not_match)
                binding.edtCnfPass.requestFocus()
            }
            binding.edtNewPass.text.length <= 4 -> {
                binding.tilNewPass.error = getString(R.string.pass_greater_than_4)
                binding.edtNewPass.requestFocus()
            }
            else -> {
                clearError()
                saveNewPassword()
            }
        }
    }

    private fun clearError() {
        binding.tilOldPass.isErrorEnabled = false
        binding.tilNewPass.isErrorEnabled = false
        binding.tilCnfPass.isErrorEnabled = false
    }

    private fun saveNewPassword() {
        val jsonObject: JsonObject = JsonObject()
        jsonObject.addProperty(JsonKeys.OldPassword, binding.edtOldPass.text.toString())
        jsonObject.addProperty(JsonKeys.NewPassword, binding.edtNewPass.text.toString())
        Log.e(TAG, "body: $jsonObject")
        try {
            if (Config.isInternetAvailable(this)) {
                val call: Call<Common> = RetrofitApiClient.getClient.changePassword(
                    Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!, jsonObject
                )
                call.enqueue(object : Callback<Common> {
                    override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                        Log.e(TAG, "code: " + response?.code())
                        if (response != null && response.isSuccessful) {
                            when (response.code()) {
                                200 -> {
                                    val common: Common? = response.body()
                                    Log.e(TAG, "response: " + Gson().toJson(common))
                                    Config.toast(
                                        this@ChangePasswordActivity,
                                        common?.status?.returnMessage
                                    )
                                    when (common?.status?.code) {
                                        0 -> finish()
                                        401 -> {
                                            val realm = Realm.getDefaultInstance()
                                            realm.executeTransaction { realm -> realm.deleteAll() }
                                            Config.clearAllPreferences(this@ChangePasswordActivity)
                                            startActivity(
                                                Intent(
                                                    this@ChangePasswordActivity,
                                                    LoginActivity::class.java
                                                )
                                            )
                                            finish()
                                        }
                                        else -> {
                                            Config.toast(
                                                this@ChangePasswordActivity,
                                                "" + Html.fromHtml(common?.status?.returnMessage)
                                            )
                                        }
                                    }
                                }
                                401 -> {
                                    val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { realm -> realm.deleteAll() }
                                    Config.clearAllPreferences(this@ChangePasswordActivity)
                                    startActivity(
                                        Intent(
                                            this@ChangePasswordActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                            }
                        } else {
                            val realm = Realm.getDefaultInstance()
                            realm.executeTransaction { realm -> realm.deleteAll() }
                            Config.clearAllPreferences(this@ChangePasswordActivity)
                            startActivity(
                                Intent(
                                    this@ChangePasswordActivity,
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

    override fun onBackPressed() {
        super.onBackPressed()
        finishAfterTransition()
    }
}