package com.satsports247.responseModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ProfileDetail (
    @Expose
    @SerializedName("UserDetailId")
    val UserDetailId: Int,
    @Expose
    @SerializedName("UserId")
    val UserId: Int,
    @Expose
    @SerializedName("Email")
    val Email: String,
    @Expose
    @SerializedName("MobileNo")
    val MobileNo: String,
    @Expose
    @SerializedName("BankName")
    val BankName: String,
    @Expose
    @SerializedName("BankAccNo")
    val BankAccNo: String,
    @Expose
    @SerializedName("AccountHolderName")
    val AccountHolderName: String,
    @Expose
    @SerializedName("BankIFSC")
    val BankIFSC: String

)