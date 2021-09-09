package com.satsports247.dataModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SportsModel(
    @Expose
    @SerializedName("Name")
    var Name: String? = "",

    @Expose
    @SerializedName("Id")
    var Id: Int = 0
) : Serializable