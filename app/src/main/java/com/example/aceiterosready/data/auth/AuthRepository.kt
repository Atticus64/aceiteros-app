package com.example.aceiterosready.data.auth

import com.example.aceiterosready.data.model.ChangePasswordRequest
import com.example.aceiterosready.data.model.LoginRequest
import com.example.aceiterosready.data.model.RefreshTokenRequest
import com.example.aceiterosready.data.model.RegisterRequest
import com.example.aceiterosready.data.model.UpdateProfileRequest
import com.example.aceiterosready.data.model.UserDto
import com.example.aceiterosready.data.network.ApiClient
import com.example.aceiterosready.data.network.ApiResult
import com.example.aceiterosready.data.session.Session
import com.example.aceiterosready.data.session.SessionManager
import kotlinx.coroutines.flow.StateFlow

class AuthRepository(
    private val apiClient: ApiClient,
    private val sessionManager: SessionManager,
) {
    val session: StateFlow<Session?> = sessionManager.session

    suspend fun login(email: String, password: String): ApiResult<UserDto> {
        val result = apiClient.publicCall {
            apiClient.api.login(LoginRequest(email = email.trim(), password = password))
        }

        return when (result) {
            is ApiResult.Success -> {
                sessionManager.save(result.data.tokens, result.data.user)
                ApiResult.Success(result.data.user)
            }
            is ApiResult.HttpError -> result
            is ApiResult.NetworkError -> result
            is ApiResult.Unexpected -> result
        }
    }

    suspend fun register(
        nombre: String,
        apellido: String?,
        telefono: String?,
        email: String,
        password: String,
    ): ApiResult<UserDto> {
        return apiClient.publicCall {
            apiClient.api.register(
                RegisterRequest(
                    nombre = nombre.trim(),
                    apellido = apellido?.trim()?.ifBlank { null },
                    email = email.trim(),
                    password = password,
                    telefono = telefono?.trim()?.ifBlank { null },
                ),
            )
        }
    }

    suspend fun restoreSession(): ApiResult<UserDto?> {
        sessionManager.restore() ?: return ApiResult.Success(null)

        return when (val result = apiClient.authenticatedCall { bearer ->
            apiClient.api.me(bearer)
        }) {
            is ApiResult.Success -> {
                sessionManager.updateUser(result.data)
                ApiResult.Success(result.data)
            }
            is ApiResult.HttpError -> {
                if (result.status == 401) {
                    sessionManager.clear()
                    ApiResult.Success(null)
                } else {
                    result
                }
            }
            is ApiResult.NetworkError -> result
            is ApiResult.Unexpected -> result
        }
    }

    suspend fun updateProfile(
        nombre: String?,
        apellido: String?,
        telefono: String?,
    ): ApiResult<UserDto> {
        val result = apiClient.authenticatedCall { bearer ->
            apiClient.api.updateMe(
                bearer,
                UpdateProfileRequest(
                    nombre = nombre?.trim()?.ifBlank { null },
                    apellido = apellido?.trim(),
                    telefono = telefono?.trim(),
                ),
            )
        }

        if (result is ApiResult.Success) {
            sessionManager.updateUser(result.data)
        }
        return result
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
    ): ApiResult<Unit> {
        return apiClient.authenticatedUnitCall { bearer ->
            apiClient.api.changePassword(
                bearer,
                ChangePasswordRequest(currentPassword, newPassword),
            )
        }
    }

    suspend fun logout() {
        val current = sessionManager.session.value
        if (current != null) {
            apiClient.authenticatedUnitCall { bearer ->
                apiClient.api.logout(
                    bearer,
                    RefreshTokenRequest(current.refreshToken),
                )
            }
        }
        sessionManager.clear()
    }
}
