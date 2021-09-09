package com.satsports247.responseModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.satsports247.dataModels.*

data class Common(

    @SerializedName("StreamingUrl")
    @Expose
    var StreamingUrl: String = "",

    @SerializedName("ScoreUrl")
    @Expose
    val ScoreUrl: String = "",

    @SerializedName("url")
    @Expose
    val tvBetUrl: String = "",

    @SerializedName("redirectUrl")
    @Expose
    val sscUrl: String = "",

    @SerializedName("UserData")
    @Expose
    val user: User,

    @SerializedName("ProfileDetail")
    @Expose
    val profileDetail: ProfileDetail,

    @SerializedName("Status")
    @Expose
    val status: Status,

    @SerializedName("NewsData")
    @Expose
    val NewsData: ArrayList<NewsModel>,

    @SerializedName("RunningBalance")
    @Expose
    val RunningBalance: Float,

    @SerializedName("Liability")
    @Expose
    val Liability: ArrayList<LiabilityDataModel>,

    @SerializedName("DashboardData")
    @Expose
    val DashboardData: DashboardData,

    @SerializedName("data")
    @Expose
    val data: ArrayList<SocketPrefData>,

    @SerializedName("RedirectUrl")
    @Expose
    val RedirectUrl: String = "",

    @SerializedName("MatchedBetData")
    @Expose
    val MatchedBetData: ArrayList<MatchedBetModel>,

    @SerializedName("UnMatchedBetData")
    @Expose
    val UnMatchedBetData: ArrayList<MatchedBetModel>,

    @SerializedName("MarketData")
    @Expose
    val MarketData: ArrayList<MarketDataModel>,

    @SerializedName("FancyMarketData")
    @Expose
    val FancyMarketData: ArrayList<MarketDataModel>,

    @SerializedName("Downline")
    @Expose
    val Downline: DownlineModel,

    @SerializedName("oneClickChips")
    @Expose
    val oneClickChips: Boolean,

    @SerializedName("RowCount")
    @Expose
    val RowCount: Int,

    @SerializedName("AccountReport")
    @Expose
    val AccountReport: ArrayList<AccountReportModel>,

    @Expose
    @SerializedName("Url")
    val Url: String,

    @Expose
    @SerializedName("BetToken")
    val BetToken: String,

    @Expose
    @SerializedName("placeBetList")
    val placeBetList: PlaceBetListModel,

    @Expose
    @SerializedName("UnmatchBetData")
    val UnmatchBetData: ArrayList<UnMatchedBetModel>,

    @Expose
    @SerializedName("PLReport")
    val PLReport: ArrayList<PLReportModel>,

    @Expose
    @SerializedName("ResultReport")
    val ResultReport: ArrayList<ResultsModel>,

    @Expose
    @SerializedName("list")
    val list: ArrayList<SportsModel>,

    @Expose
    @SerializedName("PLHistory")
    val PLHistory: ArrayList<BetHistoryModel>,

    @Expose
    @SerializedName("TotalPL")
    val TotalPL: Double,

    @Expose
    @SerializedName("RoundData")
    val RoundData: RoundDataModel,

    @Expose
    @SerializedName("VeronicaGameList")
    val VeronicaGameList: VeronicaGamesListModel
)

class NewsModel {
    var NewsTitle: String? = null
}