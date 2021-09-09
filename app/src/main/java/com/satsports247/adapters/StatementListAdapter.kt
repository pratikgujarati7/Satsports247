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
import com.satsports247.dataModels.AccountReportModel
import java.text.SimpleDateFormat

class StatementListAdapter(
    private val items: ArrayList<AccountReportModel>?, val context: Context,
    val recyclerView: RecyclerView
) : RecyclerView.Adapter<StatementListAdapter.MyViewHolder>() {

    val TAG: String = "StatementListAdapter"
    var mClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.statement_list_layout, parent, false)
        return MyViewHolder(view)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvType: TextView = itemView.findViewById(R.id.tv_type)
        val tvSportName: TextView = itemView.findViewById(R.id.tv_sport_name)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        val tvDR: TextView = itemView.findViewById(R.id.tv_dr)
        val tvCR: TextView = itemView.findViewById(R.id.tv_cr_value)
        val tvBalance: TextView = itemView.findViewById(R.id.tv_balance)
    }

    override fun getItemCount(): Int {
        return items!!.size
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val accountReport: AccountReportModel = items!![position]
        if (!accountReport.CreatedDate.isNullOrEmpty()) {
            val localDate = Config.convertIntoLocal(
                Config.stringToDate(accountReport.CreatedDate, AppConstants.yyyyMMddTHHmmSS)!!,
                AppConstants.yyyyMMddTHHmmSS
            )
            val date = SimpleDateFormat(AppConstants.ddMMMyyyyhhmmA).format(
                SimpleDateFormat(AppConstants.yyyyMMddTHHmmSS).parse(localDate)!!
            )
            holder.tvDate.text = date
        }
        holder.tvSportName.text = accountReport.Sportname
        holder.tvType.text = accountReport.WalletType
        holder.tvDescription.text = accountReport.Description
        holder.tvDescription.setOnClickListener {
            mClickListener?.onItemClick(it, position)
        }
        if (accountReport.PL < 0) {
            holder.tvDR.text = accountReport.PL.toString().replace("-", "")
            holder.tvCR.text = "0"
        } else if (accountReport.PL >= 0) {
            holder.tvCR.text = accountReport.PL.toString()
            holder.tvDR.text = "0"
        }
        holder.tvBalance.text = DashboardActivity.decimalFormat0_00.format(
            accountReport.Balance.toBigDecimal()
        ).toString()
    }

    fun setClickListener(itemClickListener: ItemClickListener) {
        mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}