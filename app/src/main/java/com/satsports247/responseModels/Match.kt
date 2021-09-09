package com.satsports247.responseModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import java.io.Serializable

open class Match : RealmObject(), Serializable {

    var SportId: Int = 0

    @Expose
    @SerializedName("TournamentId")
    var TournamentId: Int = 0

    @Expose
    @SerializedName("TournamentName")
    var TournamentName: String = ""

    @Expose
    @SerializedName("MatchId")
    var MatchId: Int = 0

    @Expose
    @SerializedName("MatchName")
    var MatchName: String = ""

    @Expose
    @SerializedName("OpenDate")
    var OpenDate: String = ""

    @Expose
    @SerializedName("BfMatchId")
    var BfMatchId: Long = 0

    @Expose
    @SerializedName("MarketId")
    var MarketId: String = ""

    @Expose
    @SerializedName("MarketCount")
    var MarketCount: String = ""

    @Expose
    @SerializedName("SessionCount")
    var SessionCount: Int = 0

    @Expose
    @SerializedName("BookmakersCount")
    var BookmakersCount: Int = 0

    @Expose
    @SerializedName("ManualOddsCount")
    var ManualOddsCount: Int = 0

    @Expose
    @SerializedName("CompletedMatchCount")
    var CompletedMatchCount: Int = 0

    @Expose
    @SerializedName("CentralId")
    var CentralId: Int = 0

    @Expose
    @SerializedName("MarketName")
    var MarketName: String = ""

    @Expose
    @SerializedName("IsInPlay")
    var IsInPlay: Boolean = false
}