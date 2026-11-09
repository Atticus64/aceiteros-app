package com.example.aceiterosready.data.network

import com.example.aceiterosready.BuildConfig
import com.example.aceiterosready.data.api.ApiService
import com.example.aceiterosready.data.model.RefreshTokenRequest
import com.example.aceiterosready.data.model.TokensDto
import com.example.aceiterosready.data.session.SessionManager
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class ApiClient(
    private val sessionManager: SessionManager,
    baseUrl: String = BuildConfig.API_BASE_URL,
) {
    private val gson = GsonBuilder().create()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)

    private val refreshMutex = Mutex()

    suspend fun <T> publicCall(
        call: suspend () -> Response<ApiEnvelope<T>>,
    ): ApiResult<T> = safeCall(call)

    suspend fun <T> authenticatedCall(
        call: suspend (bearer: String) -> Response<ApiEnvelope<T>>,
    ): ApiResult<T> {
        val current = sessionManager.session.value
            ?: return ApiResult.HttpError(401, null)

        val firstAttempt = safeCall { call("Bearer ${current.accessToken}") }

        if (firstAttempt is ApiResult.HttpError && firstAttempt.status == 401) {
            return when (val refreshed = refreshTokens()) {
                is ApiResult.Success -> safeCall { call("Bearer ${refreshed.data.accessToken}") }
                is ApiResult.HttpError -> refreshed
                is ApiResult.NetworkError -> refreshed
                is ApiResult.Unexpected -> refreshed
            }
        }

        return firstAttempt
    }

    suspend fun authenticatedUnitCall(
        call: suspend (bearer: String) -> Response<Unit>,
    ): ApiResult<Unit> {
        val current = sessionManager.session.value
            ?: return ApiResult.HttpError(401, null)

        val firstAttempt = safeUnitCall { call("Bearer ${current.accessToken}") }

        if (firstAttempt is ApiResult.HttpError && firstAttempt.status == 401) {
            return when (val refreshed = refreshTokens()) {
                is ApiResult.Success -> safeUnitCall { call("Bearer ${refreshed.data.accessToken}") }
                is ApiResult.HttpError -> refreshed
                is ApiResult.NetworkError -> refreshed
                is ApiResult.Unexpected -> refreshed
            }
        }

        return firstAttempt
    }

    private suspend fun refreshTokens(): ApiResult<TokensDto> = refreshMutex.withLock {
        val current = sessionManager.session.value
            ?: return@withLock ApiResult.HttpError(401, null)

        val result = safeCall {
            api.refreshToken(RefreshTokenRequest(current.refreshToken))
        }

        when (result) {
            is ApiResult.Success -> {
                sessionManager.updateTokens(result.data)
                result
            }
            is ApiResult.HttpError -> {
                if (result.status == 401) {
                    sessionManager.clear()
                }
                result
            }
            is ApiResult.NetworkError -> result
            is ApiResult.Unexpected -> result
        }
    }

    private suspend fun <T> safeCall(
        block: suspend () -> Response<ApiEnvelope<T>>,
    ): ApiResult<T> {
        return try {
            block().toApiResult()
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            ApiResult.NetworkError(e)
        } catch (e: Exception) {
            ApiResult.Unexpected(e)
        }
    }

    private suspend fun safeUnitCall(
        block: suspend () -> Response<Unit>,
    ): ApiResult<Unit> {
        return try {
            block().toUnitResult()
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            ApiResult.NetworkError(e)
        } catch (e: Exception) {
            ApiResult.Unexpected(e)
        }
    }
}
