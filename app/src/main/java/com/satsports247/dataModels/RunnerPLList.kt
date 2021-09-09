package com.satsports247.dataModels

import androidx.databinding.BaseObservable
import androidx.databinding.ObservableField

class RunnerPLList : BaseObservable() {
    var RunnerId: Int = 0
    var RunnerPL: Double = 0.0
    var RunnerPLValue: ObservableField<String> = ObservableField(RunnerPL.toString())
}