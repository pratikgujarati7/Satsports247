package com.satsports247.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.satsports247.R
import com.satsports247.constants.AppConstants
import com.satsports247.constants.Config
import com.satsports247.dataModels.ResultsModel
import java.text.SimpleDateFormat

class ResultsListAdapter(
    private val items: ArrayList<ResultsModel>?, val context: Context,
    val recyclerView: RecyclerView
) : RecyclerView.Adapter<ResultsListAdapter.MyViewHolder>() {

    val TAG: String = "ResultsListAdapter"
    var mClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.result_list_layout, parent, false)
        return MyViewHolder(view)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvMarketType: TextView = itemView.findViewById(R.id.tv_market_type)
        val tvMarketName: TextView = itemView.findViewById(R.id.tv_market_name)
        val tvResult: TextView = itemView.findViewById(R.id.tv_result)
    }

    override fun getItemCount(): Int {
        return items!!.size
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val resultsReport: ResultsModel = items!![position]
        if (!resultsReport.CreatedDate.isNullOrEmpty()) {
            val localDate = Config.convertIntoLocal(
                Config.stringToDate(resultsReport.CreatedDate, AppConstants.yyyyMMddTHHmmSS)!!,
                AppConstants.yyyyMMddTHHmmSS
            )
            val date = SimpleDateFormat(AppConstants.ddMMMyyyyhhmmA).format(
                SimpleDateFormat(AppConstants.yyyyMMddTHHmmSS).parse(localDate)
            )
            holder.tvDate.text = date
        }
        holder.tvMarketType.text = resultsReport.MarketType
        holder.tvMarketName.text = resultsReport.Description
        holder.tvResult.text = resultsReport.Result
    }

    fun setClickListener(itemClickListener: ItemClickListener) {
        mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}