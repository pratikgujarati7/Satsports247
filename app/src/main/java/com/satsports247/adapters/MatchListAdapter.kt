package com.satsports247.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.satsports247.R
import com.satsports247.activities.SingleMarketActivity
import com.satsports247.constants.AppConstants
import com.satsports247.constants.Config
import com.satsports247.constants.IntentKeys
import com.satsports247.constants.PreferenceKeys
import com.satsports247.responseModels.Match
import io.realm.Realm
import io.realm.RealmResults
import java.text.SimpleDateFormat
import java.util.*


class MatchListAdapter(val items: ArrayList<Match>?, val context: Context) :
    RecyclerView.Adapter<MatchListAdapter.MyViewHolder>() {

    var TAG: String = "MatchListAdapter"
    var mClickListener: ItemClickListener? = null
    lateinit var realm: Realm
    lateinit var pinnedList: RealmResults<Match>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.match_list_row_layout, parent, false)
        return MyViewHolder(view)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvTournamentName: TextView = itemView.findViewById(R.id.tv_tournament_name)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        val tvCounts: TextView = itemView.findViewById(R.id.tv_counts)
        val ivPin: ImageView = itemView.findViewById(R.id.iv_pin)
        val ivPinned: ImageView = itemView.findViewById(R.id.iv_pinned)
        val ivInPlayDot: ImageView = itemView.findViewById(R.id.iv_dot_in_play)
        val ivDot: ImageView = itemView.findViewById(R.id.iv_dot_grey)
        val relItem: RelativeLayout = itemView.findViewById(R.id.rel_name)
    }

    override fun getItemCount(): Int {
        return items!!.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val matchData: Match = items!![position]
        Realm.init(context)
        realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            pinnedList = realm.where(Match::class.java).findAll()
        }
        holder.tvName.text = matchData.MatchName
        holder.tvTournamentName.text = matchData.TournamentName
        val localDate = Config.convertIntoLocal(
            Config.stringToDate(matchData.OpenDate, AppConstants.yyyyMMddTHHmmSS)!!,
            AppConstants.yyyyMMddTHHmmSS
        )
        val date = SimpleDateFormat(AppConstants.ddMMyyyy).format(
            SimpleDateFormat(AppConstants.yyyyMMddTHHmmSS).parse(localDate)
        )
        val time = SimpleDateFormat(AppConstants.hhmmSSa).format(
            SimpleDateFormat(AppConstants.yyyyMMddTHHmmSS).parse(localDate)
        )
//        holder.tvDate.text = date
//        holder.tvTime.text = time

        val matchDate = Config.stringToDate("$date $time", AppConstants.ddMMyyyyHHmmss)
        val isToday = isToday(matchDate!!)
        val isTomorrow = isTomorrow(matchDate)
        when {
            isToday -> {
                holder.tvDate.text = context.getString(R.string.today) + " " + time
                holder.tvTime.visibility = View.GONE
            }
            isTomorrow -> {
                holder.tvDate.text = context.getString(R.string.tomorrow) + " " + time
                holder.tvTime.visibility = View.GONE
            }
            else -> {
                holder.tvTime.visibility = View.VISIBLE
                holder.tvDate.text = date
                holder.tvTime.text = time
            }
        }

        val counts: String =
            "[MK " + matchData.MarketCount + " | BK " + matchData.BookmakersCount +
                    " | S " + matchData.SessionCount + " | MO " + matchData.ManualOddsCount + "]"
        holder.tvCounts.text = counts

        if (pinnedList.size > 0) {
            for (i in 0 until pinnedList.size) {
                if (matchData.MatchId == pinnedList[i]?.MatchId) {
                    holder.ivPinned.visibility = View.VISIBLE
                    holder.ivPin.visibility = View.GONE
                    break
                } else {
                    holder.ivPinned.visibility = View.GONE
                    holder.ivPin.visibility = View.VISIBLE
                }
            }
        }

        holder.ivPin.setOnClickListener {
            holder.ivPinned.visibility = View.VISIBLE
            holder.ivPin.visibility = View.GONE
            realm.executeTransaction {
                realm.copyToRealm(matchData)
            }
//            mClickListener?.onItemClick(holder.ivPin, position)
        }

        holder.ivPinned.setOnClickListener {
            holder.ivPinned.visibility = View.GONE
            holder.ivPin.visibility = View.VISIBLE
            realm.executeTransaction {
                realm.where(Match::class.java).equalTo(
                    "MarketId",
                    matchData.MarketId
                ).findFirst()?.deleteFromRealm()
            }
//            mClickListener?.onItemClick(holder.ivPinned, position)
        }

        holder.relItem.setOnClickListener {
//            mClickListener?.onItemClick(holder.relItem, position)
            AppConstants.matchClickedMarket = true
            AppConstants.liabilityClickedMarket = false
            Config.saveSharedPreferences(
                context,
                PreferenceKeys.matchMarketList,
                matchData.MarketId
            )
            val intent = Intent(context, SingleMarketActivity::class.java)
//            intent.putExtra(IntentKeys.marketID, matchData.MarketId)
            intent.putExtra(IntentKeys.matchName, matchData.MatchName)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        if (matchData.IsInPlay) {
            holder.ivInPlayDot.visibility = View.VISIBLE
            holder.ivDot.visibility = View.GONE
            holder.tvDate.text = context.resources.getString(R.string.in_play)
            holder.tvTime.visibility = View.GONE
        } else {
            holder.ivInPlayDot.visibility = View.GONE
            holder.ivDot.visibility = View.VISIBLE
        }
    }

    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    private fun isTomorrow(d: Date): Boolean {
        return DateUtils.isToday(d.time - DateUtils.DAY_IN_MILLIS)
    }

    private fun isYesterday(d: Date): Boolean {
        return DateUtils.isToday(d.time + DateUtils.DAY_IN_MILLIS)
    }

    private fun isToday(d: Date): Boolean {
        return DateUtils.isToday(d.time)
    }
}