package com.satsports247.responseModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class VeronicaGamesListModel(
    @Expose
    @SerializedName("GameList")
    val GameList: ArrayList<CasinoGames>

) : Serializable