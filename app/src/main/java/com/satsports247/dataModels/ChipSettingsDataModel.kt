package com.satsports247.dataModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ChipSettingsDataModel(
    @Expose
    @SerializedName("name")
    var name: String? = "",

    @Expose
    @SerializedName("value")
    var value: Int = 0
) : Serializable