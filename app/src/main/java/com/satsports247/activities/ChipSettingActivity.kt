package com.satsports247.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.satsports247.R
import com.satsports247.constants.Config
import com.satsports247.constants.JsonKeys
import com.satsports247.constants.PreferenceKeys
import com.satsports247.dataModels.ChipSettingsDataModel
import com.satsports247.databinding.ActivityChipSettingBinding
import com.satsports247.responseModels.Common
import com.satsports247.retrofit.RetrofitApiClient
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChipSettingActivity : AppCompatActivity() {

    lateinit var chipList: ArrayList<ChipSettingsDataModel>
    val TAG: String = "ChipSettingActivity"
    lateinit var binding: ActivityChipSettingBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChipSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        setChipData()
        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.btnUpdateChip.setOnClickListener { updateChipSettings() }
    }

    private fun updateChipSettings() {
        Config.showSmallProgressDialog(this)
        val chipUpdatedArray = ArrayList<ChipSettingsDataModel>()
        chipUpdatedArray.add(
            ChipSettingsDataModel(
                binding.edtChipName1.text.toString(),
                binding.edtChipValue1.text.toString().toInt()
            )
        )
        chipUpdatedArray.add(
            ChipSettingsDataModel(
                binding.edtChipName2.text.toString(),
                binding.edtChipValue2.text.toString().toInt()
            )
        )
        chipUpdatedArray.add(
            ChipSettingsDataModel(
                binding.edtChipName3.text.toString(),
                binding.edtChipValue3.text.toString().toInt()
            )
        )
        chipUpdatedArray.add(
            ChipSettingsDataModel(
                binding.edtChipName4.text.toString(),
                binding.edtChipValue4.text.toString().toInt()
            )
        )
        chipUpdatedArray.add(
            ChipSettingsDataModel(
                binding.edtChipName5.text.toString(),
                binding.edtChipValue5.text.toString().toInt()
            )
        )
        chipUpdatedArray.add(
            ChipSettingsDataModel(
                binding.edtChipName6.text.toString(),
                binding.edtChipValue6.text.toString().toInt()
            )
        )
        val jsonObject = JsonObject()
        val jsonArray = JsonArray()
        for (i in 0 until chipUpdatedArray.size) {
            val jsonData: JsonObject = JsonObject()
            jsonData.addProperty(JsonKeys.name, chipUpdatedArray[i].name)
            jsonData.addProperty(JsonKeys.value, chipUpdatedArray[i].value)
            jsonArray.add(jsonData)
        }
        jsonObject.add(JsonKeys.chipSetting, jsonArray)
        Log.e(TAG, "updateChipSettings: $jsonObject")
        try {
            if (Config.isInternetAvailable(this)) {
                val call: Call<Common> = RetrofitApiClient.getClient.updateChipsSettings(
                    Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!, jsonObject
                )
                call.enqueue(object : Callback<Common> {
                    override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                        if (response != null && response.isSuccessful) {
                            Config.hideSmallProgressDialog()
                            Log.e(TAG, "code: " + response.code())
                            val common: Common? = response.body()
                            Log.e(TAG, "Response: " + Gson().toJson(common))
                            when (response.code()) {
                                200 -> {
                                    when (common?.status?.code) {
                                        0 -> {
                                            Config.toast(
                                                this@ChipSettingActivity,
                                                common.status.returnMessage
                                            )
                                            Config.saveChipSettings(
                                                this@ChipSettingActivity,
                                                chipUpdatedArray
                                            )
                                            setChipData()
                                        }
                                        401 -> {
                                            val realm = Realm.getDefaultInstance()
                                            realm.executeTransaction { realm -> realm.deleteAll() }
                                            Config.clearAllPreferences(this@ChipSettingActivity)
                                            startActivity(
                                                Intent(
                                                    this@ChipSettingActivity,
                                                    LoginActivity::class.java
                                                )
                                            )
                                            finishAffinity()
                                        }
                                        else -> {
                                            Config.toast(
                                                this@ChipSettingActivity,
                                                common?.status?.returnMessage
                                            )
                                        }
                                    }
                                }
                                401 -> {
                                    val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { realm -> realm.deleteAll() }
                                    Config.clearAllPreferences(this@ChipSettingActivity)
                                    startActivity(
                                        Intent(
                                            this@ChipSettingActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finishAffinity()
                                }
                            }
                        } else {
                            val realm = Realm.getDefaultInstance()
                            realm.executeTransaction { realm -> realm.deleteAll() }
                            Config.clearAllPreferences(this@ChipSettingActivity)
                            startActivity(
                                Intent(
                                    this@ChipSettingActivity,
                                    LoginActivity::class.java
                                )
                            )
                            finishAffinity()
                        }
                    }

                    override fun onFailure(call: Call<Common>?, t: Throwable?) {
                        Config.hideSmallProgressDialog()
                        Log.e(TAG, "onFailure: " + t.toString())
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

    private fun setChipData() {
        chipList = Config.getChipSettings(this)!!
        binding.edtChipName1.setText(chipList[0].name)
        binding.edtChipName2.setText(chipList[1].name)
        binding.edtChipName3.setText(chipList[2].name)
        binding.edtChipName4.setText(chipList[3].name)
        binding.edtChipName5.setText(chipList[4].name)
        binding.edtChipName6.setText(chipList[5].name)

        binding.edtChipValue1.setText(chipList[0].value.toString())
        binding.edtChipValue2.setText(chipList[1].value.toString())
        binding.edtChipValue3.setText(chipList[2].value.toString())
        binding.edtChipValue4.setText(chipList[3].value.toString())
        binding.edtChipValue5.setText(chipList[4].value.toString())
        binding.edtChipValue6.setText(chipList[5].value.toString())
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAfterTransition()
    }
}