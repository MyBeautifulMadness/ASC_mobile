package com.example.asc_mobile.api

import com.example.asc_mobile.model.LoginRequest
import com.example.asc_mobile.model.LoginResponse
import com.example.asc_mobile.model.SkippingRequestResponse
import com.example.asc_mobile.model.CreateSkippingRequest
import com.example.asc_mobile.model.UserProfileResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Streaming
import okhttp3.ResponseBody
import okhttp3.RequestBody
import okhttp3.MultipartBody

interface AuthApiService {
    @POST("api/account/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
    @GET("api/account/checkToken")
    fun checkToken(@Query("token") token: String): Call<Void>
    @POST("api/account/logout")
    fun logout(@Header("Authorization") token: String): Call<Void>
    @GET("api/skipping-requests/skippingRequestList")
    fun getSkippingRequests(
        @Header("Authorization") authHeader: String,
        @Query("studentId") studentId: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("sortSetting") sortSetting: String? = null,
        @Query("lessonNumber") lessonNumber: Int? = null,
        @Query("isAppruved") isApproved: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Call<SkippingRequestResponse>
    @POST("api/skipping-requests/create")
    fun createSkippingRequest(
        @Header("Authorization") token: String,
        @Body request: CreateSkippingRequest
    ): Call<Void>
    @GET("api/account/profile")
    fun getProfile(@Header("Authorization") authHeader: String): Call<UserProfileResponse>
    @GET("api/skipping-requests/getDocument")
    fun getSkippingRequestDocuments(
        @Header("Authorization") authHeader: String,
        @Query("skippingRequestId") skippingRequestId: String
    ): Call<ResponseBody>
    @Multipart
    @POST("api/skipping-requests/addDocument")
    fun addSkippingRequestDocuments(
        @Header("Authorization") authHeader: String,
        @Part("request") requestId: RequestBody,
        @Part files: List<MultipartBody.Part>
    ): Call<ResponseBody>
    @PUT("/api/skipping-requests/changeDate")
    fun changeSkippingRequestDate(
        @Header("Authorization") authHeader: String,
        @Query("skippingRequestId") skippingRequestId: String,
        @Query("newStartDate") newStartDate: String,
        @Query("newEndDate") newEndDate: String
    ): Call<ResponseBody>
}

