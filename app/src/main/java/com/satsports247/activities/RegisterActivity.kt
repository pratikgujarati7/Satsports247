package com.satsports247.activities

import android.annotation.SuppressLint
import android.app.Activity
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
import com.satsports247.constants.AppConstants
import com.satsports247.constants.Config
import com.satsports247.constants.IntentKeys
import com.satsports247.constants.JsonKeys
import com.satsports247.databinding.ActivityRegisterBinding
import com.satsports247.responseModels.Common
import com.satsports247.retrofit.RetrofitApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.experimental.and

class RegisterActivity : AppCompatActivity() {

    var TAG: String = "RegisterActivity"
    var isCheckedPassword = false
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        setPasswordField()
        binding.tvLogin.setOnClickListener { finish() }
        binding.btnRegister.setOnClickListener {
            checkValidations()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setPasswordField() {
        binding.edtPassword.setTypeface(Typeface.DEFAULT)
        binding.edtPassword.transformationMethod = PasswordTransformationMethod()
        binding.edtPassword.setOnTouchListener(View.OnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.edtPassword.getRight() - binding.edtPassword.getCompoundDrawables()
                        .get(DRAWABLE_RIGHT).getBounds().width() - 5)
                ) {
                    isCheckedPassword = !isCheckedPassword
                    Config.showIcon(binding.edtPassword, isCheckedPassword)
                    binding.edtPassword.setSelection(binding.edtPassword.length())
                    return@OnTouchListener true
                }
            }
            return@OnTouchListener false
        })

        binding.edtCnfPassword.setTypeface(Typeface.DEFAULT)
        binding.edtCnfPassword.transformationMethod = PasswordTransformationMethod()
        binding.edtCnfPassword.setOnTouchListener(View.OnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.edtCnfPassword.getRight() - binding.edtCnfPassword.getCompoundDrawables()
                        .get(DRAWABLE_RIGHT).getBounds().width() - 5)
                ) {
                    isCheckedPassword = !isCheckedPassword
                    Config.showIcon(binding.edtCnfPassword, isCheckedPassword)
                    binding.edtCnfPassword.setSelection(binding.edtCnfPassword.length())
                    return@OnTouchListener true
                }
            }
            return@OnTouchListener false
        })
    }

    private fun checkValidations() {
        clearErrorViews()
        if (binding.edtUsername.text.isEmpty()) {
            binding.tvErrorName.visibility = View.VISIBLE
            binding.edtUsername.background =
                ContextCompat.getDrawable(this, R.drawable.border_error_edittext)
            binding.tvErrorName.setText(getString(R.string.please_enter_username))
            binding.edtUsername.requestFocus()
        } else if (binding.edtMobileNumber.text.isEmpty()) {
            binding.tvErrorNumber.visibility = View.VISIBLE
            binding.edtMobileNumber.background =
                ContextCompat.getDrawable(this, R.drawable.border_error_edittext)
            binding.tvErrorNumber.setText(getString(R.string.please_enter_mobile_number))
            binding.edtMobileNumber.requestFocus()
        } else if (binding.edtMobileNumber.text.length < 10) {
            binding.tvErrorNumber.visibility = View.VISIBLE
            binding.edtMobileNumber.background =
                ContextCompat.getDrawable(this, R.drawable.border_error_edittext)
            binding.tvErrorNumber.setText(getString(R.string.please_enter_valid_mobile))
            binding.edtMobileNumber.requestFocus()
        } else if (!binding.edtEmail.text.isEmpty() && !Config.emailValidator(binding.edtEmail.text.toString())) {
            binding.tvErrorEmail.visibility = View.VISIBLE
            binding.edtEmail.background =
                ContextCompat.getDrawable(this, R.drawable.border_error_edittext)
            binding.tvErrorEmail.setText(getString(R.string.please_enter_valid_email))
            binding.edtEmail.requestFocus()
        } else if (binding.edtPassword.text.isEmpty()) {
            binding.tvErrorPassword.visibility = View.VISIBLE
            binding.edtPassword.background =
                ContextCompat.getDrawable(this, R.drawable.border_error_edittext)
            binding.tvErrorPassword.setText(getString(R.string.please_enter_pass))
            binding.edtPassword.requestFocus()
        } else if (binding.edtCnfPassword.text.isEmpty()) {
            binding.tvErrorCnfPassword.visibility = View.VISIBLE
            binding.edtCnfPassword.background =
                ContextCompat.getDrawable(this, R.drawable.border_error_edittext)
            binding.tvErrorCnfPassword.setText(getString(R.string.please_enter_pass_again))
            binding.edtCnfPassword.requestFocus()
        } else if (binding.edtPassword.text.toString() != binding.edtCnfPassword.text.toString()
        ) {
            binding.tvErrorCnfPassword.visibility = View.VISIBLE
            binding.edtCnfPassword.background =
                ContextCompat.getDrawable(this, R.drawable.border_error_edittext)
            binding.tvErrorCnfPassword.text = getString(R.string.pass_not_match)
            binding.edtCnfPassword.requestFocus()
        } else if (binding.edtPassword.text.length <= 4) {
            binding.tvErrorPassword.visibility = View.VISIBLE
            binding.tvErrorCnfPassword.visibility = View.VISIBLE
            binding.edtPassword.background =
                ContextCompat.getDrawable(this, R.drawable.border_error_edittext)
            binding.tvErrorPassword.text = getString(R.string.pass_greater_than_4)
            binding.edtCnfPassword.background =
                ContextCompat.getDrawable(this, R.drawable.border_error_edittext)
            binding.tvErrorCnfPassword.text = getString(R.string.pass_greater_than_4)
            binding.edtPassword.requestFocus()
        } else {
            clearErrorViews()
            register()
        }
    }

    private fun clearErrorViews() {
        binding.tvErrorName.visibility = View.GONE
        binding.tvErrorNumber.visibility = View.GONE
        binding.tvErrorEmail.visibility = View.GONE
        binding.tvErrorPassword.visibility = View.GONE
        binding.tvErrorCnfPassword.visibility = View.GONE
        binding.edtUsername.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
        binding.edtMobileNumber.background =
            ContextCompat.getDrawable(this, R.drawable.bg_border)
        binding.edtEmail.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
        binding.edtPassword.background = ContextCompat.getDrawable(this, R.drawable.bg_border)
        binding.edtCnfPassword.background =
            ContextCompat.getDrawable(this, R.drawable.bg_border)
    }

    private fun register() {
        val jsonObject = JsonObject()
        jsonObject.addProperty(JsonKeys.Username, binding.edtUsername.text.toString())
        jsonObject.addProperty(JsonKeys.Password, binding.edtPassword.text.toString())
        jsonObject.addProperty(JsonKeys.MobileNo, binding.edtMobileNumber.text.toString())
        jsonObject.addProperty(JsonKeys.Email, binding.edtEmail.text.toString())
        Log.e(TAG, "body: $jsonObject")

        val signature: String = getSecretKey(jsonObject.toString() + AppConstants.SecretKey)
        try {
            if (Config.isInternetAvailable(this)) {
                val call: Call<Common> = RetrofitApiClient.getClient.register(
                    signature, jsonObject
                )
                call.enqueue(object : Callback<Common> {
                    override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                        if (response != null && response.isSuccessful) {
                            val common: Common? = response.body()
                            Log.e(TAG, "register response: " + Gson().toJson(common))
                            Config.toast(this@RegisterActivity, common?.status?.returnMessage)
                            if (common?.status?.code == 0) {
                                val intent = Intent()
                                intent.putExtra(
                                    IntentKeys.username,
                                    binding.edtUsername.text.toString()
                                )
                                intent.putExtra(
                                    IntentKeys.password,
                                    binding.edtPassword.text.toString()
                                )
                                setResult(Activity.RESULT_OK, intent)
                                finish()
                            }
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
}