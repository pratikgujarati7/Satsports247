package com.satsports247.retrofit

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.satsports247.constants.JsonKeys
import com.satsports247.responseModels.Common
import com.satsports247.responseModels.DeleteUnMatchedBet
import com.satsports247.responseModels.MatchedUnmatchedModel
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface RetrofitApiInterface {

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("User/Login")
    fun login(@Header(JsonKeys.X_Signature) key: String, @Body jsonObject: JsonObject): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("User/Register")
    fun register(
        @Header(JsonKeys.X_Signature) key: String,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @GET("User/GetBalanceAndLiability")
    fun getBalanceAndLiability(@Header(JsonKeys.AuthToken) key: String?): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("Dashboard/GetDashboardDetail")
    fun getDashboardDetail(
        @Header(JsonKeys.AuthToken) key: String?,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @PUT("User/Logout")
    fun logout(@Header(JsonKeys.AuthToken) key: String): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("User/RedictToThirtPartyGame")
    fun redirectToThirdPartyGame(
        @Header(JsonKeys.AuthToken) key: String,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("Binary/Login")
    fun binaryLogin(
        @Header(JsonKeys.AuthToken) key: String,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("Report/GetAccountReport")
    fun getAccountReport(
        @Header(JsonKeys.AuthToken) key: String,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("Report/GetPLReport")
    fun getPLReport(
        @Header(JsonKeys.AuthToken) key: String,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("Report/GetResultsReport")
    fun getResultsReport(
        @Header(JsonKeys.AuthToken) key: String,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("Market/GetMatchedUnMatchedBetDetail")
    fun getMatchedUnMatchedDetail(
        @Header(JsonKeys.AuthToken) key: String?,
        @Body jsonObject: JsonObject
    ): Call<MatchedUnmatchedModel>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("Market/GetMarketDetail")
    fun getMarketDetail(
        @Header(JsonKeys.AuthToken) key: String?,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @DELETE
    fun deleteUnmatchedBetById(
        @Header(JsonKeys.AuthToken) key: String?,
        @Url url: String
    ): Call<DeleteUnMatchedBet>

    @Headers("Content-type: application/json", "Accept: */*")
    @PUT("User/UpdateChipsSettings")
    fun updateChipsSettings(
        @Header(JsonKeys.AuthToken) key: String,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("User/UpdateProfile")
    fun updateProfile(
        @Header(JsonKeys.AuthToken) key: String,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("Market/PlaceBet")
    fun placeBet(
        @Header(JsonKeys.AuthToken) key: String?,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @PUT("User/ChangePassword")
    fun changePassword(
        @Header(JsonKeys.AuthToken) key: String,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("recharge/PaymentGatwayLogin")
    fun deposit(
        @Header(JsonKeys.AuthToken) key: String,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("recharge/WithdrawRequest")
    fun withdrawRequest(
        @Header(JsonKeys.AuthToken) key: String,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @GET("Common/GetAllSports")
    fun getAllSports(@Header(JsonKeys.AuthToken) key: String?): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @GET
    fun getAllTournaments(@Header(JsonKeys.AuthToken) key: String?, @Url url: String): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @GET
    fun getAllMatches(@Header(JsonKeys.AuthToken) key: String?, @Url url: String): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @GET
    fun getAllMarkets(@Header(JsonKeys.AuthToken) key: String?, @Url url: String): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("Report/GetPLReportHistory")
    fun getPLReportHistory(
        @Header(JsonKeys.AuthToken) key: String,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("Report/GetBetHistoryReport")
    fun getBetHistory(
        @Header(JsonKeys.AuthToken) key: String,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("Lottery/GetBetList")
    fun getLotteryBetHistory(
        @Header(JsonKeys.AuthToken) key: String,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("User/GetBetList")
    fun getVeronicaBetHistory(
        @Header(JsonKeys.AuthToken) key: String,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("Lottery/Login")
    fun lotteryLogin(
        @Header(JsonKeys.AuthToken) key: String,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("SuperSpadeGame/Login")
    fun sscLogin(
        @Header(JsonKeys.AuthToken) key: String,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("TvBet/Login")
    fun tvBetLogin(
        @Header(JsonKeys.AuthToken) key: String
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("Vacuum/Login")
    fun vacuumLogin(
        @Header(JsonKeys.AuthToken) key: String
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("WorldCasino/Login")
    fun worldCasinoLogin(
        @Header(JsonKeys.AuthToken) key: String
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @GET
    fun getScoreUrl(
        @Url api: String,
        @Header(JsonKeys.AuthToken) key: String
    ): Call<Common>

    @Headers("Content-type: application/json", "Accept: */*")
    @GET("OneTouch/GetGameList")
    fun getGameList(
        @Header(JsonKeys.AuthToken) key: String,
    ): Call<JsonArray>

    @Headers("Content-type: application/json", "Accept: */*")
    @POST("OneTouch/GetGameUrl")
    fun getGameUrl(
        @Header(JsonKeys.AuthToken) key: String,
        @Body jsonObject: JsonObject
    ): Call<Common>

    @Multipart
//    @Headers("Content-type: application/json")
    @POST("FileUpload")
    fun uploadFile(
        @Header(JsonKeys.AuthToken) key: String,
        @Part file: MultipartBody.Part
    ): Call<Common>
}

