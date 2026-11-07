package com.example.aceiterosready.data

import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.100.100:3000/api/v1/"

    public val serviceApi: ServiceApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServiceApi::class.java)
    }
}

class UserRepository {
    private val api = RetrofitClient.serviceApi
    suspend fun login(user: SignInData) = api.login(user)
    suspend fun logout(user: SignInData) = api.health()
    suspend fun health() = api.health()
}