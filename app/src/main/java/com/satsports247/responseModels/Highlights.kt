package com.satsports247.responseModels


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Highlights(
    @Expose
    @SerializedName("SportId")
    var SportId: Int = 0,
    @Expose
    @SerializedName("SportName")
    var SportName: String = "",
    @Expose
    @SerializedName("Icon")
    var Icon: String = "",
    @Expose
    @SerializedName("Match")
    var Match: ArrayList<Match>
) : Serializable