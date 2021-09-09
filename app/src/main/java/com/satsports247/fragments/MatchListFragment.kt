package com.satsports247.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.satsports247.constants.IntentKeys
import com.satsports247.R
import com.satsports247.activities.DashboardActivity
import com.satsports247.adapters.MatchListAdapter
import com.satsports247.databinding.FragmentMatchListBinding
import com.satsports247.responseModels.Highlights
import com.satsports247.responseModels.Match
import io.realm.Realm

class MatchListFragment : Fragment(), MatchListAdapter.ItemClickListener {

    lateinit var realm: Realm
    var TAG: String = "MatchListFragment"
    private var model: Highlights? = null
    lateinit var fragmentBinding: FragmentMatchListBinding
    lateinit var adapter: MatchListAdapter
    var matchList = ArrayList<Match>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args: Bundle? = arguments
        if (args != null) {
            model = args.getSerializable(IntentKeys.matchListData) as Highlights?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentBinding = FragmentMatchListBinding.inflate(inflater, container, false)
        val view: View = fragmentBinding.root

        init()

        return view
    }

    private fun init() {
        realm = Realm.getDefaultInstance()
        fragmentBinding.recyclerMarketList.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        matchList = model?.Match!!
        adapter = MatchListAdapter(matchList, activity!!.applicationContext)
        adapter.setClickListener(this)
        fragmentBinding.recyclerMarketList.adapter = adapter
        if (matchList.size > 0) {
            fragmentBinding.recyclerMarketList.visibility = View.VISIBLE
            fragmentBinding.tvNoData.visibility = View.GONE
        } else {
            fragmentBinding.recyclerMarketList.visibility = View.GONE
            fragmentBinding.tvNoData.visibility = View.VISIBLE
        }
    }

    override fun onItemClick(view: View?, position: Int) {
        when (view?.id) {
            R.id.iv_pin -> {
                realm.executeTransaction {
                    realm.copyToRealm(matchList[position])
                }
            }
            R.id.iv_pinned -> {
                realm.executeTransaction {
                    realm.where(Match::class.java).equalTo(
                        "MarketId",
                        matchList[position].MarketId
                    ).findFirst()?.deleteFromRealm()
                }
            }
            R.id.rel_name -> {
                DashboardActivity.marketIds = matchList[position].MarketId
//                goTo(MultiMarketFragment(), matchList[position].MarketId)
            }
        }
    }

    private fun goTo(fragment: Fragment, marketIds: String) {
        /*val transaction = fragmentManager?.beginTransaction()
        transaction?.replace(R.id.container, fragment)
//        transaction?.addToBackStack(HomeFragment.javaClass.name)
//        transaction?.addToBackStack(null)
        transaction?.commit()*/
        DashboardActivity.closeApp = false
        DashboardActivity.isMatchClicked = true
//        DashboardActivity.navigationMenu.menu.getItem(2).title = "Single-Market"
//        DashboardActivity.navigationMenu.menu.getItem(2).isChecked = true
//        (activity as DashboardActivity).loadFragment(fragment)
    }
}