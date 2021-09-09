package com.satsports247.responseModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class DashboardData (
   @SerializedName("Highlights")
   @Expose
   val Highlights:ArrayList<Highlights>,

   @SerializedName("CasinoGames")
   @Expose
   val CasinoGames:ArrayList<CasinoGames>

)