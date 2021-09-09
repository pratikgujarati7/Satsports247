package com.satsports247.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.satsports247.R
import com.satsports247.activities.GamesActivity
import com.satsports247.activities.OneTouchGameListActivity
import com.satsports247.constants.AppConstants
import com.satsports247.constants.IntentKeys

class TopListAdapter(
    private val items: ArrayList<String>?, val context: Context
) : RecyclerView.Adapter<TopListAdapter.MyViewHolder>() {

    val TAG: String = "TopListAdapter"
    var mClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_layout, parent, false)
        return MyViewHolder(view)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_list_name)
        val llMain: LinearLayout = itemView.findViewById(R.id.ll_main)
        val separator: View = itemView.findViewById(R.id.seperator)
    }

    override fun getItemCount(): Int {
        return items!!.size
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val title = items!![position]
        holder.tvTitle.text = title
        if (position == items.size - 1)
            holder.separator.visibility = View.GONE
        else
            holder.separator.visibility = View.VISIBLE

        holder.llMain.setOnClickListener {
            if (title == AppConstants.OneTouch) {
                context.startActivity(Intent(context, OneTouchGameListActivity::class.java))
            } else {
                val intent = Intent(context, GamesActivity::class.java)
                intent.putExtra(IntentKeys.title, title)
                context.startActivity(intent)
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