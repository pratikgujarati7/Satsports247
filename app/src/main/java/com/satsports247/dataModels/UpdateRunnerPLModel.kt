package com.satsports247.dataModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UpdateRunnerPLModel(
    @Expose
    @SerializedName("MatchId")
    var MatchId: Int? = 0,
    @Expose
    @SerializedName("MarketId")
    var MarketId: Int? = 0,
    @Expose
    @SerializedName("MarketName")
    var MarketName: String? = "",
    @Expose
    @SerializedName("Liability")
    var Liability: Double = 0.0,
    @Expose
    @SerializedName("Date")
    var Date: String? = ""
) : Serializable