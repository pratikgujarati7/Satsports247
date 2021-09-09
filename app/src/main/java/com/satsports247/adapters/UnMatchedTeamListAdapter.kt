package com.satsports247.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.satsports247.R
import com.satsports247.activities.DashboardActivity
import com.satsports247.activities.LoginActivity
import com.satsports247.constants.Config
import com.satsports247.constants.PreferenceKeys
import com.satsports247.constants.UrlConstants
import com.satsports247.dataModels.BetModel
import com.satsports247.responseModels.DeleteUnMatchedBet
import com.satsports247.retrofit.RetrofitApiClient
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UnMatchedTeamListAdapter(
    private val items: ArrayList<BetModel>?,
    val context: Context
) :
    RecyclerView.Adapter<UnMatchedTeamListAdapter.MyViewHolder>() {

    val TAG: String = "UnMatchedTeamList"
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
        val ivDelete: ImageView = itemView.findViewById(R.id.iv_delete)
        val tvRun: TextView = itemView.findViewById(R.id.tv_runs)
    }

    override fun getItemCount(): Int {
        return items!!.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val dataModel: BetModel = items!![position]
        if (dataModel.IsBack)
            holder.relItem.setBackgroundColor(context.resources.getColor(R.color.colorLightBlue))
        else
            holder.relItem.setBackgroundColor(context.resources.getColor(R.color.colorLightPink))
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
        holder.ivDelete.visibility = View.VISIBLE
        holder.ivDelete.setOnClickListener {
            mClickListener?.onItemClick(it, position)
            val url =
                UrlConstants.URLMobileMarketApi + "Market/DeleteUnmatchedBetById?betId=" + dataModel.BetId
            Log.e("unMatchedAdapter", "url $url")
            if (Config.isInternetAvailable(context)) {
                val call: Call<DeleteUnMatchedBet> =
                    RetrofitApiClient.getMarketApiClient.deleteUnmatchedBetById(
                        Config.getSharedPreferences(context, PreferenceKeys.AuthToken), url
                    )
                call.enqueue(object : Callback<DeleteUnMatchedBet> {
                    override fun onResponse(
                        call: Call<DeleteUnMatchedBet>?,
                        response: Response<DeleteUnMatchedBet>?
                    ) {
                        val common: DeleteUnMatchedBet? = response?.body()
                        Log.e(
                            "unMatchedAdapter",
                            "deleteUnmatchedBetById: " + Gson().toJson(common)
                        )
                        if (response != null && response.isSuccessful) {
                            if (common?.status?.code == 0) {
                                items.remove(dataModel)
                                notifyDataSetChanged()
                            } else {
                                val realm = Realm.getDefaultInstance()
                                realm.executeTransaction { realm -> realm.deleteAll() }
                                Config.clearAllPreferences(context)
                                context.startActivity(Intent(context, LoginActivity::class.java))
                            }
                        }
                    }

                    override fun onFailure(call: Call<DeleteUnMatchedBet>?, t: Throwable?) {
                        Log.e("unMatchedAdapter", "deleteUnmatchedBetById: " + t.toString())
                    }
                })
            } else {
                Config.toast(context, context.getString(R.string.please_check_internet_connection))
            }
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