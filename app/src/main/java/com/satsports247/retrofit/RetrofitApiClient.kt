package com.satsports247.retrofit

import com.google.gson.GsonBuilder
import com.satsports247.constants.UrlConstants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitApiClient {

    val BASE_URL: String = UrlConstants.URLMobileApi
    val URLMobileMarketApi: String = UrlConstants.URLMobileMarketApi

    var okHttpClient = OkHttpClient().newBuilder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    val getClient: RetrofitApiInterface
        get() {
            val gson = GsonBuilder()
                .setLenient()
                .create()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            return retrofit.create(RetrofitApiInterface::class.java)
        }

    val getMarketApiClient: RetrofitApiInterface
        get() {
            val gson = GsonBuilder()
                .setLenient()
                .create()

            val retrofit = Retrofit.Builder()
                .baseUrl(URLMobileMarketApi)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            return retrofit.create(RetrofitApiInterface::class.java)
        }
}