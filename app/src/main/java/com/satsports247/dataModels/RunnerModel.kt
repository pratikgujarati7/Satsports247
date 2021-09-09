package com.satsports247.dataModels

import androidx.databinding.BaseObservable
import androidx.databinding.ObservableField
import com.satsports247.R

class RunnerModel : BaseObservable() {
    var RunnerId: Int = 0
    var MarketId: Int = 0
    var BfRunnerId: Long = 0
    var Name: String = ""
    var ProfitValue: ObservableField<String> = ObservableField("0")
    var profitTextColor: ObservableField<Int> = ObservableField(R.color.colorGreen)
    var backRate: ObservableField<String> = ObservableField("-")
    var layRate: ObservableField<String> = ObservableField("-")
    var backBFVolume: ObservableField<String> = ObservableField("---")
    var layBFVolume: ObservableField<String> = ObservableField("---")
    var keyboardOpen: ObservableField<Boolean> = ObservableField(false)
    var changeBackBgColor: ObservableField<Boolean> = ObservableField(false)
    var changeLayBgColor: ObservableField<Boolean> = ObservableField(false)
}