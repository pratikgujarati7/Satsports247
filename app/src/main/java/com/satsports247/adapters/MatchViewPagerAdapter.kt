package com.satsports247.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.satsports247.responseModels.Highlights

class MatchViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    var fragmentList: MutableList<Fragment> = ArrayList()
    var fragmentTitleList: MutableList<String?> = ArrayList()
    var highlightsData: MutableList<Highlights?> = ArrayList()
    var context: Context? = null

    override fun getItem(position: Int): Fragment {
        return fragmentList.get(position)
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentTitleList.get(position)
    }

    fun add(myFrags: ArrayList<Fragment>, title: String?, dataModel: Highlights?) {
        fragmentList = myFrags
        fragmentTitleList.add(title)
        highlightsData.add(dataModel)
    }
}