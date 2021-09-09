package com.satsports247.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.satsports247.R
import com.satsports247.activities.BetHistoryActivity
import com.satsports247.activities.DashboardActivity
import com.satsports247.constants.AppConstants
import com.satsports247.constants.Config
import com.satsports247.dataModels.BetHistoryModel
import java.text.SimpleDateFormat

class BetHistoryAdapter(
    private val items: ArrayList<BetHistoryModel>?, val context: Context,
    val recyclerView: RecyclerView, val type: String
) : RecyclerView.Adapter<BetHistoryAdapter.MyViewHolder>() {

    val TAG: String = "BetHistoryAdapter"
    var mClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bet_history_list_layout, parent, false)
        return MyViewHolder(view)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val llMarketName: LinearLayout = itemView.findViewById(R.id.ll_market_name)
        val tvMarketName: TextView = itemView.findViewById(R.id.tv_market_name)
        val tvSelectionName: TextView = itemView.findViewById(R.id.tv_selection)
        val tvOddsLabel: TextView = itemView.findViewById(R.id.tv_odds_label)
        val tvOddsValue: TextView = itemView.findViewById(R.id.tv_odds)
        val tvRateLabel: TextView = itemView.findViewById(R.id.tv_rate_label)
        val tvRateValue: TextView = itemView.findViewById(R.id.tv_rate)
        val tvRunLabel: TextView = itemView.findViewById(R.id.tv_run_label)
        val tvRunValue: TextView = itemView.findViewById(R.id.tv_run)
        val tvStakeValue: TextView = itemView.findViewById(R.id.tv_stake)
        val tvPL: TextView = itemView.findViewById(R.id.tv_pl)
        val tvResult: TextView = itemView.findViewById(R.id.tv_result)
        val tvWonLoss: TextView = itemView.findViewById(R.id.tv_won_loss)
        val tvDrLabel: TextView = itemView.findViewById(R.id.tv_dr_label)
        val tvDrValue: TextView = itemView.findViewById(R.id.tv_dr)
        val tvCrLabel: TextView = itemView.findViewById(R.id.tv_cr_label)
        val tvCrValue: TextView = itemView.findViewById(R.id.tv_cr)
        val tvNetLabel: TextView = itemView.findViewById(R.id.tv_net_label)
        val tvNetValue: TextView = itemView.findViewById(R.id.tv_net)
        val tvType: TextView = itemView.findViewById(R.id.tv_type)
    }

    override fun getItemCount(): Int {
        return items!!.size
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val plReport: BetHistoryModel = items!![position]
        if (!plReport.CreatedDate.isNullOrEmpty()) {
            val localDate = Config.convertIntoLocal(
                Config.stringToDate(plReport.CreatedDate, AppConstants.yyyyMMddTHHmmSS)!!,
                AppConstants.ddMMMyyyyhhmmA
            )
            val date = SimpleDateFormat(AppConstants.ddMMMyyyyhhmmA).format(
                SimpleDateFormat(AppConstants.yyyyMMddTHHmmSS).parse(plReport.CreatedDate)!!
            )
            if (BetHistoryActivity.description.contains(AppConstants.Veronica, true))
                holder.tvDate.text = date
            else
                holder.tvDate.text = localDate
        }
        holder.tvSelectionName.text = plReport.Runner
        holder.tvStakeValue.text = plReport.Stake.toString()
        if (plReport.IsBack)
            holder.tvType.text = context.getString(R.string.back)
        else
            holder.tvType.text = context.getString(R.string.lay)
        if (plReport.IsBetWon)
            holder.tvWonLoss.text = context.getString(R.string.won)
        else
            holder.tvWonLoss.text = context.getString(R.string.loss)

        var plValue = DashboardActivity.decimalFormat0_00.format(
            plReport.PL.toBigDecimal()
        ).toString()
        if (plReport.PL < 0) {
            holder.tvPL.text = plValue.replace("-", "")
            holder.tvDrValue.text = plValue.replace("-", "")
            holder.tvPL.setTextColor(context.resources.getColor(R.color.colorRed))
        } else if (plReport.PL >= 0) {
            holder.tvPL.text = plValue
            holder.tvCrValue.text = plValue
            holder.tvPL.setTextColor(context.resources.getColor(R.color.colorGreen))
        }
        holder.tvResult.text = plReport.Result
        holder.tvOddsValue.text = plReport.Rate.toString()
        holder.tvRateValue.text = plReport.Rate.toString()
        holder.tvRunValue.text = plReport.Run.toString()
        if (plReport.Net != 0.0)
            holder.tvNetValue.text = plReport.Net.toString()
        else
            holder.tvNetValue.text = ""
        if ((plReport.Runner != null && plReport.Runner.contains(AppConstants.Lottery, true)
                    || BetHistoryActivity.description.contains(AppConstants.Veronica, true))
        ) {
            plValue = DashboardActivity.decimalFormat0_00.format(
                plReport.Dr.toBigDecimal()
            ).toString()
            holder.tvDrValue.text = plValue.replace("-", "")
            if (plReport.Cr > 0)
                holder.tvCrValue.text = DashboardActivity.decimalFormat0_00.format(
                    plReport.Cr.toBigDecimal()
                ).toString()
            else
                holder.tvCrValue.text = "-"
            holder.tvResult.text = "" + BetHistoryActivity.roundDataModel.Winner
        }

        hideOtherViews(holder)
    }

    private fun hideOtherViews(holder: MyViewHolder) {
        if (type.equals("PLActivity", true)) {
            holder.llMarketName.visibility = View.GONE
            holder.tvDrLabel.visibility = View.GONE
            holder.tvDrValue.visibility = View.GONE
            holder.tvCrLabel.visibility = View.GONE
            holder.tvCrValue.visibility = View.GONE
            holder.tvNetLabel.visibility = View.GONE
            holder.tvNetValue.visibility = View.GONE
            holder.tvRateLabel.visibility = View.GONE
            holder.tvRateValue.visibility = View.GONE
//            holder.tvRunLabel.visibility = View.GONE
//            holder.tvRunValue.visibility = View.GONE
        } else if (type.equals("StatementActivity", true)) {
            holder.tvOddsLabel.visibility = View.GONE
            holder.tvOddsValue.visibility = View.GONE
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