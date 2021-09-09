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
import com.satsports247.adapters.InPlayListAdapter
import com.satsports247.databinding.FragmentInPlayBinding

class InPlayFragment : Fragment() {

    var TAG: String = "InPlayFragment"
    lateinit var fragmentBinding: FragmentInPlayBinding
    lateinit var adapter: InPlayListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentBinding = FragmentInPlayBinding.inflate(inflater, container, false)
        val view: View = fragmentBinding.root

        init()

        return view
    }

    private fun init() {
        fragmentBinding.llInPlayLabel.visibility = View.VISIBLE
        fragmentBinding.recyclerMatchlist.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        adapter = InPlayListAdapter(DashboardActivity.inPlayList, requireContext())
        fragmentBinding.recyclerMatchlist.adapter = adapter
        Log.e(TAG, "in play list: " + DashboardActivity.inPlayList.size)

        if (DashboardActivity.inPlayList.size > 0) {
            fragmentBinding.llData.visibility = View.VISIBLE
            fragmentBinding.tvNoData.visibility = View.GONE
        } else {
            fragmentBinding.llData.visibility = View.GONE
            fragmentBinding.tvNoData.visibility = View.VISIBLE
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): InPlayFragment {
            return InPlayFragment()
        }
    }

}