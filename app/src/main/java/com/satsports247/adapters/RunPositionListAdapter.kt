package com.satsports247.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.satsports247.R
import com.satsports247.dataModels.RunPositionModel

class RunPositionListAdapter(
    private val items: ArrayList<RunPositionModel>?,
    val context: Context
) :
    RecyclerView.Adapter<RunPositionListAdapter.MyViewHolder>() {

    val TAG: String = "RunPositionListAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.run_position_list_layout, parent, false)
        return MyViewHolder(view)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRun: TextView = itemView.findViewById(R.id.tv_run)
        val tvValue: TextView = itemView.findViewById(R.id.tv_value)
    }

    override fun getItemCount(): Int {
        return items!!.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val dataModel: RunPositionModel = items!![position]
        holder.tvRun.text = "" + dataModel.run
        holder.tvValue.text = "" + dataModel.value
        if (dataModel.value < 0)
            holder.tvValue.setTextColor(context.resources.getColor(R.color.colorRed))
        else
            holder.tvValue.setTextColor(context.resources.getColor(R.color.colorGreen))
    }
}