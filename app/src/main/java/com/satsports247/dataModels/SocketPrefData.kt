package com.satsports247.dataModels

import androidx.databinding.ObservableField

class SocketPrefData {
    var BackRate: ObservableField<String> = ObservableField("-")
    var LayRate: ObservableField<String> = ObservableField("-")
    var backBFVolume: ObservableField<String> = ObservableField("---")
    var layBFVolume: ObservableField<String> = ObservableField("---")
    var Name: String = ""
    var RunnerId: Int = 0
    var MarketId: Int = 0
    var keyboardStatus: Boolean = false
}