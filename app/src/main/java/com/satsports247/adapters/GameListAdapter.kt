package com.satsports247.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.satsports247.R
import com.satsports247.activities.GamesActivity
import com.satsports247.constants.AppConstants
import com.satsports247.constants.IntentKeys
import com.satsports247.constants.UrlConstants
import com.satsports247.dataModels.GameListModel

class GameListAdapter(
    private val items: ArrayList<GameListModel>?, val context: Context
) :
    RecyclerView.Adapter<GameListAdapter.MyViewHolder>() {

    val TAG: String = "GameListAdapter"
    var mClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_game_row, parent, false)
        return MyViewHolder(view)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val relMain: RelativeLayout = itemView.findViewById(R.id.rel_main)
        val ivGame: ImageView = itemView.findViewById(R.id.iv_game)
        val tvName: TextView = itemView.findViewById(R.id.tv_game_name)
    }

    override fun getItemCount(): Int {
        return items!!.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val gameData = items!![position]
        val imageUrl = UrlConstants.baseUrl + "assets/images/one-touch/" + gameData.id + ".jpg"
        Glide.with(context)
            .load(imageUrl)
            .into(holder.ivGame)
        holder.tvName.text = gameData.name
        holder.relMain.setOnClickListener {
            val intent = Intent(context, GamesActivity::class.java)
            intent.putExtra(IntentKeys.title, AppConstants.OneTouch)
            intent.putExtra(IntentKeys.gameName, gameData.name)
            intent.putExtra(IntentKeys.gameID, gameData.id)
            context.startActivity(intent)
        }
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}