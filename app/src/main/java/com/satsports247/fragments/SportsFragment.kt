package com.satsports247.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.satsports247.activities.DashboardActivity
import com.satsports247.adapters.SportsListAdapter
import com.satsports247.databinding.FragmentInPlayBinding

class SportsFragment : Fragment() {

    var TAG: String = "SportsFragment"
    lateinit var fragmentBinding: FragmentInPlayBinding
    lateinit var adapter: SportsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentBinding = FragmentInPlayBinding.inflate(inflater, container, false)
        val view: View = fragmentBinding.root

        init()

        return view
    }

    private fun init() {
        fragmentBinding.llInPlayLabel.visibility = View.GONE
        fragmentBinding.recyclerMatchlist.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        adapter = SportsListAdapter(DashboardActivity.sportsList, requireContext())
        fragmentBinding.recyclerMatchlist.adapter = adapter

        Log.e(TAG, "sport list: " + DashboardActivity.sportsList[0].Match.size)
        if (DashboardActivity.sportsList.size > 0) {
            fragmentBinding.llData.visibility = View.VISIBLE
            fragmentBinding.tvNoData.visibility = View.GONE
        } else {
            fragmentBinding.llData.visibility = View.GONE
            fragmentBinding.tvNoData.visibility = View.VISIBLE
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): SportsFragment {
            return SportsFragment()
        }
    }

}