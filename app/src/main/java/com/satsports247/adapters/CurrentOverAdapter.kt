package com.satsports247.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.satsports247.R
import com.satsports247.databinding.LayoutOversBinding

class CurrentOverViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    val tvName: TextView = itemView.findViewById(R.id.tv_run)
    val llData: LinearLayout = itemView.findViewById(R.id.ll_data)
}

class CurrentOverAdapter : ListAdapter<String, CurrentOverViewHolder>(Companion) {

    val TAG = "CurrentOverAdapter"
    lateinit var context: Context

    companion object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: String,
            newItem: String
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentOverViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutOversBinding.inflate(inflater, parent, false)

        return CurrentOverViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CurrentOverViewHolder, position: Int) {
        var run = getItem(position)
        val itemBinding = holder.binding as LayoutOversBinding
        itemBinding.executePendingBindings()
        context = holder.tvName.context
        run = run.replace("~", "")
        holder.tvName.text = run
        if (run == "4" || run == "6") {
            holder.llData.background = context.resources.getDrawable(R.drawable.bg_green_circular)
        } else {
            holder.llData.background = context.resources.getDrawable(R.drawable.bg_white_circular)
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
}