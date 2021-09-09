package com.satsports247.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.satsports247.R
import com.satsports247.constants.AppConstants
import com.satsports247.constants.Config
import com.satsports247.dataModels.MarketDataModel
import com.satsports247.databinding.LayoutMarketDataBinding
import com.satsports247.fragments.MultiMarketFragment
import io.realm.Realm
import java.text.SimpleDateFormat
import java.util.*

class ViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    val tvDate: TextView = itemView.findViewById(R.id.tv_date)
    val tvTime: TextView = itemView.findViewById(R.id.tv_time)
    val tvTimer: TextView = itemView.findViewById(R.id.tv_timer)
    val ivPinned: ImageView = itemView.findViewById(R.id.iv_pinned)
    val ivLive: ImageView = itemView.findViewById(R.id.iv_live)
    val openDetailScore: TextView = itemView.findViewById(R.id.tv_open_detail_score)
    val closeDetailScore: TextView = itemView.findViewById(R.id.tv_close_detail_score)
    val llDetailScore: RelativeLayout = itemView.findViewById(R.id.ll_detailed_score)
    val llInPlay: LinearLayout = itemView.findViewById(R.id.ll_in_play)
    val scoreWeb: WebView = itemView.findViewById(R.id.score_web)
    val tvNoScore: TextView = itemView.findViewById(R.id.tv_no_score_url)
}

class SocketMatchAdapter : ListAdapter<MarketDataModel, ViewHolder>(Companion) {
    private val viewPool = RecyclerView.RecycledViewPool()
    lateinit var realm: Realm
    val TAG = "SocketMatchAdapter"
    lateinit var context: Context

    companion object : DiffUtil.ItemCallback<MarketDataModel>() {
        override fun areItemsTheSame(oldItem: MarketDataModel, newItem: MarketDataModel): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: MarketDataModel,
            newItem: MarketDataModel
        ): Boolean {
            return oldItem.MatchId == newItem.MatchId
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutMarketDataBinding.inflate(inflater, parent, false)

        return ViewHolder(binding)
    }

    @SuppressLint("SimpleDateFormat", "SetJavaScriptEnabled")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        context = holder.llInPlay.context
        val currentMatch = getItem(position)
        if (currentMatch.IsInPlay)
            holder.llInPlay.visibility = View.VISIBLE
        else
            holder.llInPlay.visibility = View.GONE
        val itemBinding = holder.binding as LayoutMarketDataBinding
        itemBinding.match = currentMatch
        itemBinding.recyclerMarketList.setRecycledViewPool(viewPool)

        itemBinding.recyclerCurrentOver.layoutManager = LinearLayoutManager(
            holder.tvDate.context,
            RecyclerView.HORIZONTAL, false
        )
        itemBinding.recyclerCurrentOver.setRecycledViewPool(viewPool)

        itemBinding.recyclerLastOver.layoutManager = LinearLayoutManager(
            holder.tvDate.context,
            RecyclerView.HORIZONTAL, false
        )
        itemBinding.recyclerLastOver.setRecycledViewPool(viewPool)

        itemBinding.executePendingBindings()
        val localDate = Config.convertIntoLocal(
            Config.stringToDate(currentMatch.OpenDate, AppConstants.yyyyMMddTHHmmSS)!!,
            AppConstants.yyyyMMddTHHmmSS
        )
        val date = SimpleDateFormat(AppConstants.ddMMyyyy).format(
            SimpleDateFormat(AppConstants.yyyyMMddTHHmmSS).parse(localDate)
        )
        val time = SimpleDateFormat(AppConstants.HHmmSSa).format(
            SimpleDateFormat(AppConstants.yyyyMMddTHHmmSS).parse(localDate)
        )
        holder.tvDate.text = date
        holder.tvTime.text = SimpleDateFormat(AppConstants.hhmmSSa).format(
            SimpleDateFormat(AppConstants.yyyyMMddTHHmmSS).parse(localDate)
        )
        val matchDate = Config.stringToDate("$date $time", AppConstants.ddMMyyyyHHmmss)
        val currentDate = Calendar.getInstance().time
        if (matchDate?.compareTo(currentDate) == 1 && !currentMatch.isFancyMarket) {
            val diffTime: Long = matchDate.time - currentDate.time
            holder.tvTimer.visibility = View.VISIBLE
            val timer = object : CountDownTimer(diffTime, 1000) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    val seconds = (millisUntilFinished / 1000) % 60
                    val minutes = ((millisUntilFinished / (1000 * 60)) % 60)
                    val hours = ((millisUntilFinished / (1000 * 60 * 60)) % 24)
                    val days = millisUntilFinished / (24 * 60 * 60 * 1000)
                    holder.tvTimer.text = String.format(
                        "%02d" + "d" + " : %02d" + "h" + " : %02d" + "m" + " : %02d" + "s",
                        days,
                        hours,
                        minutes,
                        seconds
                    )
                }

                override fun onFinish() {
                    holder.tvTimer.visibility = View.GONE
                }
            }
            timer.start()
        } else
            holder.tvTimer.visibility = View.GONE

        holder.ivPinned.visibility = View.VISIBLE
        holder.ivPinned.setOnClickListener {
            mClickListener?.onItemClick(holder.ivPinned, position)
        }
        val streamingUrl = currentMatch.StreamingUrl
        if (streamingUrl == null || streamingUrl == "") {
            holder.ivLive.visibility = View.GONE
        } else
            holder.ivLive.visibility = View.VISIBLE

        holder.ivLive.setOnClickListener {
            Log.e(TAG, "streamingUrl: $streamingUrl")
            MultiMarketFragment.relLiveMatch.visibility = View.VISIBLE
            MultiMarketFragment.webView.loadUrl(streamingUrl)
        }

        holder.scoreWeb.settings.javaScriptEnabled = true
        holder.scoreWeb.settings.loadWithOverviewMode = true
        holder.scoreWeb.settings.useWideViewPort = true
        holder.scoreWeb.settings.domStorageEnabled = true
//        holder.scoreWeb.setInitialScale(100)

        holder.scoreWeb.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                Log.e(TAG, "contentHeight: " + holder.scoreWeb.contentHeight)
                val newHeight = holder.scoreWeb.contentHeight
                holder.scoreWeb.layoutParams = LinearLayout.LayoutParams(
                    context.resources.displayMetrics.widthPixels,
                    (newHeight * context.resources.displayMetrics.density).toInt()
                )
                holder.llDetailScore.visibility = View.VISIBLE
            }
        }
        if (currentMatch.ScoreUrl == "")
            holder.llDetailScore.visibility = View.GONE
        else {
            holder.llDetailScore.visibility = View.VISIBLE
            val iFrameUrl = "<iframe width=\"100%\" height=\"100%\" src=\"" +
                    currentMatch.ScoreUrl + "\" ></iframe>"
            Log.e(TAG, "iFrame url: $iFrameUrl")
//        holder.scoreWeb.loadUrl(currentMatch.ScoreUrll̥)
            holder.scoreWeb.loadData(iFrameUrl, "text/html", null)

            Log.e(TAG, "contentHeight: " + holder.scoreWeb.contentHeight)
        }

        /*holder.openDetailScore.setOnClickListener {
            holder.openDetailScore.visibility = View.GONE
            holder.closeDetailScore.visibility = View.VISIBLE
            holder.llDetailScore.visibility = View.VISIBLE

        }

        holder.closeDetailScore.setOnClickListener {
            holder.closeDetailScore.visibility = View.GONE
            holder.llDetailScore.visibility = View.GONE
            holder.openDetailScore.visibility = View.VISIBLE
        }*/

        if (holder.llDetailScore.isVisible && currentMatch.ScoreUrl == "") {
            holder.scoreWeb.visibility = View.GONE
            holder.tvNoScore.visibility = View.VISIBLE
        }
    }

    var mClickListener: ItemClickListener? = null

    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    /*fun addIframeResizeEventListener() {
        window.addEventListener('message', (event) => {
            log(event.data.scoreWidgetHeight)
            // Update height logic here
        });
    }*/
}