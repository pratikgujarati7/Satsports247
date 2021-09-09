package com.satsports247.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.satsports247.constants.AppConstants
import com.satsports247.R
import com.satsports247.activities.DashboardActivity
import com.satsports247.constants.Config
import com.satsports247.dataModels.LiabilityDataModel
import java.text.SimpleDateFormat

class LiabilityListAdapter(
    private val items: ArrayList<LiabilityDataModel>?,
    val context: Context
) :
    RecyclerView.Adapter<LiabilityListAdapter.MyViewHolder>() {

    val TAG: String = "LiabilityListAdapter"
    var mClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.liability_list_layout, parent, false)
        return MyViewHolder(view)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvMarketName: TextView = itemView.findViewById(R.id.tv_market_name)
        val tvLiability: TextView = itemView.findViewById(R.id.tv_liability)
    }

    override fun getItemCount(): Int {
        return items!!.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val dataModel: LiabilityDataModel = items!![position]
        if (dataModel.Date != null) {
            val localDate = Config.convertIntoLocal(
                Config.stringToDate(dataModel.Date, AppConstants.yyyyMMddTHHmmSS)!!,
                AppConstants.ddMMMyyyyhhmmA
            )
            val date = SimpleDateFormat(AppConstants.ddMMMyyyyHHmmA).format(
                SimpleDateFormat(AppConstants.yyyyMMddTHHmmSS).parse(dataModel.Date)
            )
            holder.tvDate.text = localDate
        }

        holder.tvMarketName.text = dataModel.MarketName
        holder.tvLiability.text = DashboardActivity.decimalFormat0_00.format(
            dataModel.Liability.toBigDecimal()
        ).toString()
        holder.tvMarketName.setOnClickListener {
            mClickListener?.onItemClick(it, position)
        }
    }

    fun setClickListener(itemClickListener: ItemClickListener) {
        mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

}