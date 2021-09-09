package com.satsports247.dataModels

import androidx.databinding.ObservableField

class MarketDataModel {
    var MatchId: Int = 0
    var TournamentId: Int = 0
    var BfMatchId: Int = 0
    var MatchName: String = ""
    var MatchDescription: String = ""
    var OpenDate: String = ""
    var StreamingUrl: String = ""
    var Market: ArrayList<MarketModel> = ArrayList()
    var isFancyMarket: Boolean = false
    var IsInPlay: Boolean = false
    var inPlay: ObservableField<Boolean> = ObservableField(false)
    var inningType: ObservableField<String> = ObservableField("")
    var battingTeam: ObservableField<String> = ObservableField("")
    var bowlingTeam: ObservableField<String> = ObservableField("")
    var runWicketOverBatT: ObservableField<String> = ObservableField("")
    var runWicketOverBowlT: ObservableField<String> = ObservableField("")
    var tossStatus: ObservableField<String> = ObservableField("")
    var runRate: ObservableField<String> = ObservableField("")
    var requiredRunRate: ObservableField<String> = ObservableField("")
    var requiredRuns: ObservableField<String> = ObservableField("")
    var strikerName: ObservableField<String> = ObservableField("")
    var nonStrikername: ObservableField<String> = ObservableField("")
    var bowler: ObservableField<String> = ObservableField("")
    var runsStriker: ObservableField<String> = ObservableField("")
    var runsNonStriker: ObservableField<String> = ObservableField("")
    var runsBowler: ObservableField<String> = ObservableField("")
    var requiredText: ObservableField<String> = ObservableField("")
    var currentOverRuns: ObservableField<ArrayList<String>> = ObservableField(ArrayList())
    var lastOverRuns: ObservableField<ArrayList<String>> = ObservableField(ArrayList())
    var ScoreUrl: String = ""
}