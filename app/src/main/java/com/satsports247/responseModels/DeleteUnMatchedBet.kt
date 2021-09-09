package com.satsports247.responseModels

import com.satsports247.dataModels.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class DeleteUnMatchedBet(

    @SerializedName("UpdateRunnerPL")
    @Expose
    val UpdateRunnerPL: ArrayList<UpdateRunnerPLModel>,

    @SerializedName("Status")
    @Expose
    val status: Status,

    @SerializedName("RunningBalance")
    @Expose
    val RunningBalance: RunningBalance,

    @SerializedName("Liability")
    @Expose
    val Liability: ArrayList<LiabilityDataModel>
)