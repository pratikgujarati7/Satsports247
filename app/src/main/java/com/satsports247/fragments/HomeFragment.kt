package com.satsports247.fragments

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.satsports247.R
import com.satsports247.activities.DashboardActivity
import com.satsports247.activities.LoginActivity
import com.satsports247.adapters.MatchViewPagerAdapter
import com.satsports247.constants.Config
import com.satsports247.constants.IntentKeys
import com.satsports247.constants.PreferenceKeys
import com.satsports247.dataModels.HighlightDataModel
import com.satsports247.databinding.FragmentHomeBinding
import com.satsports247.responseModels.CasinoGames
import com.satsports247.responseModels.Common
import com.satsports247.responseModels.Highlights
import com.satsports247.responseModels.Match
import com.satsports247.retrofit.RetrofitApiClient
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {
    val TAG: String = "HomeFragment"
    lateinit var fragmentFirstBinding: FragmentHomeBinding
    val mContext: FragmentActivity? = activity
    var casinoGamesList = ArrayList<CasinoGames>()
    var highlightsList = ArrayList<Highlights>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentFirstBinding = FragmentHomeBinding.inflate(inflater, container, false)
        val view: View = fragmentFirstBinding.root

        init()

        return view
    }

    private fun init() {
        fragmentFirstBinding.refresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext(), R.color.colorBlack
            )
        )
        fragmentFirstBinding.refresh.setColorSchemeColors(Color.YELLOW)
        DashboardActivity.inPlayList.clear()

        fragmentFirstBinding.btnReloadData.setOnClickListener {
            getDashboardDetail()
        }

        fragmentFirstBinding.refresh.setOnRefreshListener {
            getDashboardDetail()
            fragmentFirstBinding.refresh.isRefreshing = false
        }

        getDashboardDetail()
    }

    private fun getDashboardDetail() {
        casinoGamesList.clear()
        highlightsList.clear()
        DashboardActivity.sportsList.clear()
        try {
            if (Config.isInternetAvailable(requireContext())) {
                fragmentFirstBinding.llData.visibility = View.VISIBLE
                fragmentFirstBinding.llNoInternet.visibility = View.GONE
                Config.showSmallProgressDialog(requireContext())
                val call: Call<Common> = RetrofitApiClient.getClient.getDashboardDetail(
                    Config.getSharedPreferences(requireContext(), PreferenceKeys.AuthToken),
                    JsonObject()
                )
                call.enqueue(object : Callback<Common> {
                    override fun onResponse(call: Call<Common>?, response: Response<Common>?) {
                        Log.e(TAG, "code: " + response?.code())
                        val common: Common? = response?.body()
                        Log.e(TAG, "getDashboardDetail: " + Gson().toJson(common))
                        Config.hideSmallProgressDialog()
                        if (response != null && response.isSuccessful) {
                            when (response.code()) {
                                200 -> {
                                    when (common?.status?.code) {
                                        0 -> {
//                                    casinoGamesList = common.DashboardData.CasinoGames
//                                    casinoGamesList = common.VeronicaGameList.GameList
                                            highlightsList = common.DashboardData.Highlights
//                                    setHorizontalList()
                                            setupViewPager()
                                            setInPLayList()
                                            setSportsList()
                                        }
                                        else -> Config.toast(
                                            requireContext(),
                                            common?.status?.returnMessage
                                        )
                                    }
                                }
                                401 -> {
                                    val realm = Realm.getDefaultInstance()
                                    realm.executeTransaction { mRealm -> mRealm.deleteAll() }
                                    Config.clearAllPreferences(requireContext())
                                    startActivity(
                                        Intent(
                                            requireContext(),
                                            LoginActivity::class.java
                                        )
                                    )
                                    activity!!.finish()
                                }
                            }
                        } else {
                            Config.toast(
                                requireContext(),
                                getString(R.string.something_went_wrong)
                            )
                        }
                    }

                    override fun onFailure(call: Call<Common>?, t: Throwable?) {
                        Config.hideSmallProgressDialog()
                        Log.e(TAG, "getDashboardDetail: " + t.toString())
                    }
                })
            } else {
                Config.hideSmallProgressDialog()
                fragmentFirstBinding.llData.visibility = View.GONE
                fragmentFirstBinding.llNoInternet.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Config.toast(requireContext(), "" + e)
            Log.e(TAG, "" + e)
        }
    }

    private fun setInPLayList() {
        if (highlightsList.size > 0) {
            for (i in 0 until highlightsList.size) {
                val matchList = ArrayList<Match>()
                matchList.addAll(highlightsList[i].Match)
                val highlights = HighlightDataModel()
                highlights.SportName = highlightsList[i].SportName
                highlights.SportId = highlightsList[i].SportId
                if (matchList.size > 0) {
                    for (i in 0 until matchList.size) {
                        if (matchList[i].IsInPlay)
                            highlights.Match.add(matchList[i])
                    }
                }
                if (highlights.Match.size > 0)
                    DashboardActivity.inPlayList.add(highlights)
            }
        }
    }

    private fun setSportsList() {
        if (highlightsList.size > 0) {
            for (i in 0 until highlightsList.size) {
                val matchList = ArrayList<Match>()
                matchList.addAll(highlightsList[i].Match)
                val highlights = HighlightDataModel()
                highlights.SportName = highlightsList[i].SportName
                highlights.SportId = highlightsList[i].SportId
                if (matchList.size > 0) {
                    for (i in 0 until matchList.size) {
                        highlights.Match.add(matchList[i])
                    }
                }
                DashboardActivity.sportsList.add(highlights)
            }
        }
    }

    /*private fun setHorizontalList() {
        fragmentFirstBinding.recyclerHomeList.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL, false
        )
        val adapter = CasinoGamesAdapter(casinoGamesList, activity!!.applicationContext)
        fragmentFirstBinding.recyclerHomeList.adapter = adapter
        adapter.setClickListener(this)
    }*/

    private fun setupViewPager() {
        if (highlightsList.size > 0) {
            fragmentFirstBinding.viewpager.visibility = View.VISIBLE
            fragmentFirstBinding.tvNoData.visibility = View.GONE
            val viewPagerAdapter = MatchViewPagerAdapter(activity?.supportFragmentManager!!)
            val fragments: ArrayList<Fragment> = ArrayList()
            //dynamically sets the list in viewpager
            for (i in 0 until highlightsList.size) {
                val b = Bundle()
                b.putSerializable(IntentKeys.matchListData, highlightsList[i])
                fragments.add(instantiate(requireContext(), MatchListFragment::class.java.name, b))
                viewPagerAdapter.add(fragments, highlightsList[i].SportName, highlightsList[i])
            }
            viewPagerAdapter.notifyDataSetChanged()
            fragmentFirstBinding.viewpager.adapter = viewPagerAdapter
            fragmentFirstBinding.tabLayout.setupWithViewPager(fragmentFirstBinding.viewpager, true)
//            setUpTabIcons()
        } else {
            fragmentFirstBinding.viewpager.visibility = View.GONE
            fragmentFirstBinding.tvNoData.visibility = View.VISIBLE
        }
    }

    private fun setUpTabIcons() {
        try {
            for (i in 0 until highlightsList.size) {
                var gameName = highlightsList[i].SportName.toLowerCase()
                if (gameName.contains(" "))
                    gameName = gameName.replace(" ", "")
                val id: Int = context?.resources!!
                    .getIdentifier(gameName, "drawable", requireContext().packageName)
                if (id != 0) {
                    fragmentFirstBinding.tabLayout.getTabAt(i)?.setIcon(id)
                    if (i == 0) {
                        fragmentFirstBinding.tabLayout.getTabAt(i)?.icon?.setColorFilter(
                            Color.WHITE,
                            PorterDuff.Mode.SRC_IN
                        )
                    } else {
                        fragmentFirstBinding.tabLayout.getTabAt(i)?.icon?.setColorFilter(
                            Color.BLACK,
                            PorterDuff.Mode.SRC_IN
                        )
                    }
                }
            }

            fragmentFirstBinding.tabLayout.setOnTabSelectedListener(
                object : TabLayout.ViewPagerOnTabSelectedListener(fragmentFirstBinding.viewpager) {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        super.onTabSelected(tab)
                        val tabIconColor =
                            ContextCompat.getColor(context!!, R.color.colorWhite)
                        if (tab.icon != null)
                            tab.icon!!.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {
                        super.onTabUnselected(tab)
                        val tabIconColor =
                            ContextCompat.getColor(context!!, R.color.colorBlack)
                        if (tab.icon != null)
                            tab.icon!!.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
                    }
                }
            )
        } catch (e: Exception) {
            Config.toast(requireContext(), "" + e)
            Log.e(TAG, "" + e)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    /*override fun onItemClick(view: View?, position: Int) {
        val casinoModel = casinoGamesList[position]
        val intent = Intent(requireContext(), CasinoGamesActivity::class.java)
        intent.putExtra(IntentKeys.casinoGame, casinoModel)
        startActivity(intent)
    }*/
}