package com.satsports247.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.satsports247.R
import com.satsports247.dataModels.HighlightDataModel
import io.realm.Realm

class SportsListAdapter(private val items: ArrayList<HighlightDataModel>?, val context: Context) :
    RecyclerView.Adapter<SportsListAdapter.MyViewHolder>(){

    val TAG: String = "SportsListAdapter"
    lateinit var adapter: MatchListAdapter
    lateinit var dataModel: HighlightDataModel
    lateinit var realm: Realm

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.in_play_row_layout, parent, false)
        return MyViewHolder(view)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_sport_name)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recycler_in_play_list)
    }

    override fun getItemCount(): Int {
        return items!!.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        realm = Realm.getDefaultInstance()
        dataModel = items!![position]
        holder.tvName.text = dataModel.SportName
        /*val id: Int = context?.resources!!
            .getIdentifier(
                dataModel.SportName.toLowerCase(),
                "drawable",
                context.packageName
            )
        holder.ivImage.setImageResource(id)*/

        if (dataModel.Match.size > 0) {
//            holder.llSportName.visibility = View.VISIBLE
            holder.recyclerView.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = MatchListAdapter(dataModel.Match, context)
            holder.recyclerView.adapter = adapter
        }
    }
}