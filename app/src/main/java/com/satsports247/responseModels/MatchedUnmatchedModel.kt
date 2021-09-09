package com.satsports247.responseModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.satsports247.dataModels.LiabilityDataModel
import com.satsports247.dataModels.MatchedBetModel

data class MatchedUnmatchedModel(

    @SerializedName("Status")
    @Expose
    val status: Status,

    @SerializedName("RunningBalance")
    @Expose
    val RunningBalance: RunningBalance,

    @SerializedName("Liability")
    @Expose
    val Liability: ArrayList<LiabilityDataModel>,

    @SerializedName("MatchedBetData")
    @Expose
    val MatchedBetData: ArrayList<MatchedBetModel>,

    @SerializedName("UnMatchedBetData")
    @Expose
    val UnMatchedBetData: ArrayList<MatchedBetModel>
)