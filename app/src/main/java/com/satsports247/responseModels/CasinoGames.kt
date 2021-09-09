package com.satsports247.responseModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CasinoGames(
    @Expose
    @SerializedName("GameID")
    val GameID: Int = 0,
    @Expose
    @SerializedName("Name")
    val Name: String = "",
    @Expose
    @SerializedName("ImagePath")
    val Image: String = "",
    @Expose
    @SerializedName("SiteTypeId")
    val SiteTypeId: Int,
    @Expose
    @SerializedName("SiteGameId")
    val SiteGameId: Int,
    @Expose
    @SerializedName("GameCode")
    val GameCode: String = "",
    @Expose
    @SerializedName("GameType")
    var GameType: String = "",
    @Expose
    @SerializedName("DisplayOrder")
    val DisplayOrder: Int,
    @Expose
    @SerializedName("IsLive")
    val IsLive: Boolean,
    @Expose
    @SerializedName("IsStop")
    val IsStop: Boolean

) : Serializable