package com.satsports247.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.satsports247.R
import com.satsports247.activities.DashboardActivity
import com.satsports247.constants.AppConstants
import com.satsports247.constants.Config
import com.satsports247.dataModels.PLReportModel
import java.text.SimpleDateFormat

class PLReportListAdapter(
    private val items: ArrayList<PLReportModel>?, val context: Context,
    val recyclerView: RecyclerView
) : RecyclerView.Adapter<PLReportListAdapter.MyViewHolder>() {

    val TAG: String = "PLReportListAdapter"
    var mClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pl_report_list_layout, parent, false)
        return MyViewHolder(view)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvSportName: TextView = itemView.findViewById(R.id.tv_sport_name)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        val tvPL: TextView = itemView.findViewById(R.id.tv_pl)
        val tvResult: TextView = itemView.findViewById(R.id.tv_result)
    }

    override fun getItemCount(): Int {
        return items!!.size
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val plReport: PLReportModel = items!![position]
        if (!plReport.CreatedDate.isNullOrEmpty()) {
            val localDate = Config.convertIntoLocal(
                Config.stringToDate(plReport.CreatedDate, AppConstants.yyyyMMddTHHmmSS)!!,
                AppConstants.yyyyMMddTHHmmSS
            )
            val date = SimpleDateFormat(AppConstants.ddMMMyyyyhhmmA).format(
                SimpleDateFormat(AppConstants.yyyyMMddTHHmmSS).parse(localDate)
            )
            holder.tvDate.text = date
        }
        holder.tvSportName.text = plReport.Sportname
        holder.tvDescription.text = plReport.Description
        holder.tvDescription.setOnClickListener {
            mClickListener?.onItemClick(it, position)
        }
        val plValue = DashboardActivity.decimalFormat0_00.format(
            plReport.PL.toBigDecimal()
        ).toString()
        if (plReport.PL < 0) {
            holder.tvPL.text = plValue.replace("-", "")
            holder.tvPL.setTextColor(context.resources.getColor(R.color.colorRed))
        } else if (plReport.PL >= 0) {
            holder.tvPL.text = plValue
            holder.tvPL.setTextColor(context.resources.getColor(R.color.colorGreen))
        }
        holder.tvResult.text = plReport.Result
    }

    fun setClickListener(itemClickListener: ItemClickListener) {
        mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}