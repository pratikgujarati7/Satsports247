package com.satsports247.responseModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.satsports247.dataModels.ChipSettingsDataModel

data class User (

    @Expose
    @SerializedName("UserId")
    val UserId: Int,
    @Expose
    @SerializedName("Username")
    val Username:String,
    @Expose
    @SerializedName("AuthToken")
    val AuthToken:String,
    @Expose
    @SerializedName("BetToken")
    val BetToken:String,
    @SerializedName("ChipSetting")
    @Expose
    val ChipSetting:ArrayList<ChipSettingsDataModel>,
//    val ChipSetting:String = "",
    @Expose
    @SerializedName("SitePermissionCode")
    val SitePermissionCode:String
)