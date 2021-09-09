package com.satsports247.responseModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Status(
    @Expose
    @SerializedName("returnMessage")
    val returnMessage: String = "",

    @Expose
    @SerializedName("code")
    val code: Int
)