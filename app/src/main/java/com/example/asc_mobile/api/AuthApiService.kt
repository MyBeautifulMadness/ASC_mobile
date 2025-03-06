package com.example.asc_mobile.api

import com.example.asc_mobile.model.LoginRequest
import com.example.asc_mobile.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header

interface AuthApiService {
    @POST("api/account/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
    @GET("api/account/checkToken")
    fun checkToken(@Query("token") token: String): Call<Void>
    @POST("api/account/logout")
    fun logout(@Header("Authorization") token: String): Call<Void>
}

