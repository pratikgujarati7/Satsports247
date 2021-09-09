package com.satsports247.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.satsports247.R
import com.satsports247.dataModels.MatchedBetModel

class UnMatchedListAdapter(
    private val items: ArrayList<MatchedBetModel>?,
    val context: Context
) :
    RecyclerView.Adapter<UnMatchedListAdapter.MyViewHolder>() {

    val TAG: String = "UnMatchedListAdapter"
    var adapter = UnMatchedMarketListAdapter(null, context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.matched_list_layout, parent, false)
        return MyViewHolder(view)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMatchName: TextView = itemView.findViewById(R.id.tv_match_name)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recycler_market)
    }

    override fun getItemCount(): Int {
        return items!!.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val dataModel: MatchedBetModel = items!![position]
        holder.recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL, false
        )
        adapter = UnMatchedMarketListAdapter(dataModel.Market, context)
        holder.recyclerView.adapter = adapter
        holder.tvMatchName.text = dataModel.MatchName
    }
}