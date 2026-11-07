package com.example.aceiterosready.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class serverStatus (
    val status: String
)
interface ServiceApi {
    @POST("auth/login")
    suspend fun login(@Body data: SignInData): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body data: SignInData): Response<LoginResponse>

    @GET("health")
    suspend fun health(): serverStatus

}