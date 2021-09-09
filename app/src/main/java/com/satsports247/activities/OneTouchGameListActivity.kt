package com.satsports247.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.satsports247.R
import com.satsports247.adapters.GameListAdapter
import com.satsports247.constants.Config
import com.satsports247.constants.PreferenceKeys
import com.satsports247.dataModels.GameListModel
import com.satsports247.databinding.ActivityOneTouchGameListBinding
import com.satsports247.retrofit.RetrofitApiClient
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OneTouchGameListActivity : AppCompatActivity() {

    var TAG = "OneTouchGameListActivity"
    lateinit var binding: ActivityOneTouchGameListBinding
    var gameList = ArrayList<GameListModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOneTouchGameListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        callGameListApi()
    }

    private fun callGameListApi() {
        try {
            if (Config.isInternetAvailable(this)) {
                val call: Call<JsonArray> =
                    RetrofitApiClient.getClient.getGameList(
                        Config.getSharedPreferences(this, PreferenceKeys.AuthToken)!!
                    )
                call.enqueue(object : Callback<JsonArray> {
                    override fun onResponse(
                        call: Call<JsonArray>?,
                        response: Response<JsonArray>?
                    ) {
                        val common: JsonArray = response?.body()!!
                        Log.e(TAG, "getGameList: " + Gson().toJson(common))
                        if (response.isSuccessful) {
                            when (response.code()) {
                                200 -> {
                                    val gson = GsonBuilder().create()
                                    val list =
                                        gson.fromJson(common, Array<GameListModel>::class.java)
                                            .toList()
                                    gameList.addAll(list)
                                    Log.e(TAG, "getGameList: " + gameList.size)
                                    updateUI()
                                }
                                401 -> {
                                    val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { realm -> realm.deleteAll() }
                                    Config.clearAllPreferences(this@OneTouchGameListActivity)
                                    startActivity(
                                        Intent(
                                            this@OneTouchGameListActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<JsonArray>?,
                        t: Throwable?
                    ) {
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

    fun updateUI() {
        if (gameList.size > 0) {
            binding.tvNoDataFound.visibility = View.GONE
            binding.recyclerGameList.visibility = View.VISIBLE
            setData()
        } else {
            binding.tvNoDataFound.visibility = View.VISIBLE
            binding.recyclerGameList.visibility = View.GONE
        }
    }

    private fun setData() {
        binding.recyclerGameList.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerGameList.adapter = GameListAdapter(gameList, this)
    }
}