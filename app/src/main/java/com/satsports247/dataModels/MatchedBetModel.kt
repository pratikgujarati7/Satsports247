package com.satsports247.dataModels

import java.io.Serializable

data class MatchedBetModel(
    var MatchName: String? = "",
    var MatchId: Int = 0,
    var Market: ArrayList<MarketModel>
) : Serializable