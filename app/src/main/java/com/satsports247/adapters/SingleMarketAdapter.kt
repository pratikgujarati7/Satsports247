package com.satsports247.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.satsports247.R
import com.satsports247.constants.AppConstants
import com.satsports247.dataModels.MarketModel
import com.satsports247.databinding.LayoutMarketItemSingleBinding

class SingleMarketViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    val tvSuspended: TextView = itemView.findViewById(R.id.tv_status)
    val tvBack: TextView = itemView.findViewById(R.id.tv_back)
    val tvLay: TextView = itemView.findViewById(R.id.tv_lay)
    val tvSocketTime: TextView = itemView.findViewById(R.id.tv_current_time)
}

class SingleMarketAdapter : ListAdapter<MarketModel, SingleMarketViewHolder>(Companion) {
    private val viewPool = RecyclerView.RecycledViewPool()
    lateinit var context: Context

    companion object : DiffUtil.ItemCallback<MarketModel>() {
        override fun areItemsTheSame(oldItem: MarketModel, newItem: MarketModel): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: MarketModel, newItem: MarketModel): Boolean {
            return oldItem.MarketId == newItem.MarketId
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleMarketViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutMarketItemSingleBinding.inflate(inflater, parent, false)

        return SingleMarketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SingleMarketViewHolder, position: Int) {
        val currentMarket = getItem(position)
        val itemBinding = holder.binding as LayoutMarketItemSingleBinding
        itemBinding.market = currentMarket
        itemBinding.recyclerRunnerList.setRecycledViewPool(viewPool)
        itemBinding.executePendingBindings()
        holder.tvSuspended.setOnClickListener { }
        context = holder.tvBack.context
        if (!currentMarket.IsFancy || currentMarket.MarketType.equals(
                AppConstants.Bookmakers,
                true
            ) || currentMarket.MarketType.equals(AppConstants.ManualOdds, true)
        ) {
            holder.tvBack.text = context.getString(R.string.back)
            holder.tvLay.text = context.getString(R.string.lay)
            holder.tvBack.setBackgroundResource(R.drawable.bg_one_side_curve_blue)
            holder.tvLay.setBackgroundResource(R.drawable.bg_one_side_curve_pink)
            holder.tvSocketTime.visibility = View.VISIBLE
        } else {
            holder.tvBack.text = context.getString(R.string.no_k)
            holder.tvLay.text = context.getString(R.string.yes_l)
            holder.tvBack.setBackgroundResource(R.drawable.bg_left_curve_pink)
            holder.tvLay.setBackgroundResource(R.drawable.bg_right_curve_blue)
            holder.tvSocketTime.visibility = View.GONE
        }
    }
}