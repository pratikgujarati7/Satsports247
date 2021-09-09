package com.satsports247.dataModels

import androidx.databinding.ObservableField

class MarketModel {
    var MarketId: Int = 0
    var BfMarketId: Int = 0
    var CentralId: Int = 0
    var BettingType: Int = 0
    var MarketStatus: Int = 0
    var MarketBaseRate: Double = 0.0
    var MarketName: String = ""
    var MarketDescription: String = ""
    var Result: String = ""
    var MarketTime: String = ""
    var SuspendTime: String = ""
    var SettleTime: String = ""
    var socketTime: ObservableField<String> = ObservableField("")
    var MarketType: String = ""
    var Regulator: String = ""
    var Wallet: String = ""
    var Rules: String = ""
    var Clarifications: String = ""
    var IsSettled: Boolean = false
    var IsMarketDataDelayed: Boolean = false
    var IsPersistenceEnabled: Boolean = false
    var IsBspMarket: Boolean = false
    var IsTurnInPlayEnabled: Boolean = false
    var IsDiscountAllowed: Boolean = false
    var RulesHasDate: Boolean = false
    var IsInPlay: Boolean = false
    var IsFancy: Boolean = false
    var IsCashoutBet: Boolean = false
    var Runner: ArrayList<RunnerModel> = ArrayList()
    var RunnerPLList: ArrayList<RunnerPLList> = ArrayList()
    var Bet: ArrayList<BetModel> = ArrayList()
    var suspendedText: ObservableField<String> = ObservableField("")
    var showSuspended: ObservableField<Boolean> = ObservableField(false)
}