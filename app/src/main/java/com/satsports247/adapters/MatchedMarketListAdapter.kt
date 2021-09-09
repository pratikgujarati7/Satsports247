package com.satsports247.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.satsports247.R
import com.satsports247.dataModels.MarketModel

class MatchedMarketListAdapter(
    private val items: ArrayList<MarketModel>?,
    val context: Context
) :
    RecyclerView.Adapter<MatchedMarketListAdapter.MyViewHolder>() {

    val TAG: String = "MatchedMarketListAdapter"
    var adapter = MatchedTeamListAdapter(null, context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_row_matched_market, parent, false)
        return MyViewHolder(view)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMarket: TextView = itemView.findViewById(R.id.tv_type)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recycler_market)
    }

    override fun getItemCount(): Int {
        return items!!.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val dataModel: MarketModel = items!![position]
        holder.recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL, false
        )
        adapter = MatchedTeamListAdapter(dataModel.Bet, context)
        holder.recyclerView.adapter = adapter
        holder.tvMarket.text = dataModel.MarketName
    }
}