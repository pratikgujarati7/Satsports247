package com.satsports247.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class MarketViewPagerAdapter(
    fm: FragmentManager
) : FragmentStatePagerAdapter(fm) {

    var fragmentList: MutableList<Fragment> = ArrayList()
    var fragmentTitleList: MutableList<String?> = ArrayList()
    var context: Context? = null

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentTitleList[position]
    }

    fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        fragmentTitleList.add(title)
    }
}