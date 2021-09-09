package com.satsports247.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.satsports247.R
import com.satsports247.activities.DashboardActivity
import com.satsports247.dataModels.BetModel

class MatchedTeamListAdapter(
    private val items: ArrayList<BetModel>?,
    val context: Context,
) :
    RecyclerView.Adapter<MatchedTeamListAdapter.MyViewHolder>() {

    val TAG: String = "MatchedTeamListAdapter"
    var mClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_row_matched_unmatched, parent, false)
        return MyViewHolder(view)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val relItem: RelativeLayout = itemView.findViewById(R.id.rel_item)
        val tvTeamName: TextView = itemView.findViewById(R.id.tv_team_name)
        val tvOdds: TextView = itemView.findViewById(R.id.tv_odds)
        val tvStake: TextView = itemView.findViewById(R.id.tv_stake)
        val tvPL: TextView = itemView.findViewById(R.id.tv_pl)
        val tvRun: TextView = itemView.findViewById(R.id.tv_runs)
        val ivDelete: ImageView = itemView.findViewById(R.id.iv_delete)
        val tvType: TextView = itemView.findViewById(R.id.tv_type)
    }

    override fun getItemCount(): Int {
        return items!!.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val dataModel: BetModel = items!![position]
        if (dataModel.IsBack) {
            holder.relItem.setBackgroundColor(context.resources.getColor(R.color.colorLightBlue))
            holder.tvType.text = context.getString(R.string.back)
        } else {
            holder.relItem.setBackgroundColor(context.resources.getColor(R.color.colorLightPink))
            holder.tvType.text = context.getString(R.string.lay)
        }
        holder.tvTeamName.text = dataModel.Runner
        var odds = dataModel.Rate.toString()
        if (odds.contains(".0"))
            odds = odds.replace(".0", "")
        holder.tvOdds.text = odds
        holder.tvStake.text = dataModel.Stake.toString()
        var betPL = DashboardActivity.decimalFormat0_0.format(dataModel.BetPL)
        if (betPL.contains(".0"))
            betPL = betPL.replace(".0", "")
        if (dataModel.BetPL < 0) {
            holder.tvPL.text = betPL.toString().replace("-", "")
        } else if (dataModel.BetPL > 0) {
            holder.tvPL.text = betPL.toString()
        }
        if (dataModel.Run != 0)
            holder.tvRun.text = "" + dataModel.Run
        else
            holder.tvRun.text = ""
        holder.ivDelete.visibility = View.GONE
    }

    fun setClickListener(itemClickListener: ItemClickListener) {
        mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}