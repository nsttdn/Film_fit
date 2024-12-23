package com.example.filmfit.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object ApiClient {
    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(75, TimeUnit.SECONDS) // Connect timeout
        .readTimeout(75, TimeUnit.SECONDS)    // Read timeout
        .writeTimeout(75, TimeUnit.SECONDS)   // Write timeout
        .build()

    private const val BASE_URL = "https://9db1-77-47-209-103.ngrok-free.app"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}