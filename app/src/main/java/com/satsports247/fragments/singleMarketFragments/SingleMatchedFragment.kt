package com.satsports247.fragments.singleMarketFragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.satsports247.R
import com.satsports247.activities.LoginActivity
import com.satsports247.activities.SingleMarketActivity
import com.satsports247.adapters.MatchedListAdapter
import com.satsports247.constants.Config
import com.satsports247.constants.JsonKeys
import com.satsports247.constants.PreferenceKeys
import com.satsports247.dataModels.MatchedBetModel
import com.satsports247.databinding.FragmentMatchedBinding
import com.satsports247.responseModels.Match
import com.satsports247.responseModels.MatchedUnmatchedModel
import com.satsports247.retrofit.RetrofitApiClient
import io.realm.Realm
import io.realm.RealmResults
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SingleMatchedFragment : Fragment() {

    val TAG = "SingleMatchedFragment"
    lateinit var fragmentBinding: FragmentMatchedBinding
    lateinit var listAdapter: MatchedListAdapter
    var matchedList = ArrayList<MatchedBetModel>()
    lateinit var realm: Realm
    lateinit var pinnedList: RealmResults<Match>
    var marketIds: String = ""

    companion object;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentBinding = FragmentMatchedBinding.inflate(inflater, container, false)

        return fragmentBinding.root
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            init()
        }
    }

    private fun init() {
        fragmentBinding.recyclerMatchedList.layoutManager = LinearLayoutManager(
            context, RecyclerView.VERTICAL, false
        )

        callMatchedUnMatchedDetail()

        fragmentBinding.refresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(requireContext(), R.color.colorBlack)
        )
        fragmentBinding.refresh.setColorSchemeColors(Color.YELLOW)
        fragmentBinding.refresh.setOnRefreshListener {
            callMatchedUnMatchedDetail()
            fragmentBinding.refresh.isRefreshing = false
        }
    }

    private fun updateUI() {
        if (matchedList.size > 0) {
            fragmentBinding.recyclerMatchedList.visibility = View.VISIBLE
            fragmentBinding.tvNoData.visibility = View.GONE
        } else {
            fragmentBinding.recyclerMatchedList.visibility = View.GONE
            fragmentBinding.tvNoData.visibility = View.VISIBLE
        }
    }

    private fun callMatchedUnMatchedDetail() {
        marketIds = SingleMarketActivity.marketIds
        val jsonObject = JsonObject()
        jsonObject.addProperty(JsonKeys.MarketIds, marketIds)
        try {
            val call: Call<MatchedUnmatchedModel> =
                RetrofitApiClient.getMarketApiClient.getMatchedUnMatchedDetail(
                    Config.getSharedPreferences(requireContext(), PreferenceKeys.AuthToken),
                    jsonObject
                )
            call.enqueue(object : Callback<MatchedUnmatchedModel> {
                override fun onResponse(
                    call: Call<MatchedUnmatchedModel>?,
                    response: Response<MatchedUnmatchedModel>?
                ) {
                    Log.e(TAG, "code: " + response?.code())
                    val common: MatchedUnmatchedModel? = response?.body()
                    Log.e(TAG, "getMatchedUnMatchedDetail: " + Gson().toJson(response?.body()))
                    if (response != null) {
                        when (response.code()) {
                            200 -> {
                                when (common?.status?.code) {
                                    0 -> {
//                                        MultiMarketFragment.matchedList = common.MatchedBetData
                                        matchedList = common.MatchedBetData
                                        listAdapter =
                                            MatchedListAdapter(
                                                matchedList,
                                                activity!!.applicationContext
                                            )
                                        fragmentBinding.recyclerMatchedList.adapter = listAdapter
                                        updateUI()
                                    }
                                    else -> {
                                        Config.toast(
                                            requireContext(),
                                            common?.status?.returnMessage
                                        )
                                    }
                                }
                            }
                            401 -> {
                                val realm = Realm.getDefaultInstance()
                                realm.executeTransaction { realm -> realm.deleteAll() }
                                Config.clearAllPreferences(requireContext())
                                startActivity(Intent(requireContext(), LoginActivity::class.java))
                                requireActivity().finish()
                            }
                        }
                    } else {
                        val realm = Realm.getDefaultInstance()
                        realm.executeTransaction { realm -> realm.deleteAll() }
                        Config.clearAllPreferences(requireContext())
                        startActivity(Intent(requireContext(), LoginActivity::class.java))
                        requireActivity().finish()
                    }
                }

                override fun onFailure(call: Call<MatchedUnmatchedModel>?, t: Throwable?) {
                    Log.e(TAG, "getMatchedUnMatchedDetail: " + t.toString())
                }
            })
        } catch (e: Exception) {
            Config.toast(requireContext(), "" + e)
            Log.e(TAG, "" + e)
        }
    }
}