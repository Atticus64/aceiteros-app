package com.example.aceiterosready.data.network

import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class HttpError(val status: Int, val error: ApiError?) : ApiResult<Nothing>
    data class NetworkError(val cause: IOException) : ApiResult<Nothing>
    data class Unexpected(val cause: Throwable) : ApiResult<Nothing>
}

data class ApiError(
    val code: String? = null,
    val message: String? = null,
    val details: List<ApiErrorDetail>? = null,
)

data class ApiErrorDetail(
    val path: String,
    val message: String,
)

data class ApiEnvelope<T>(
    val data: T,
)

data class PageMeta(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int,
)

private data class ApiErrorEnvelope(val error: ApiError?)

private val gson = Gson()

internal fun <T> Response<ApiEnvelope<T>>.toApiResult(): ApiResult<T> {
    if (isSuccessful) {
        val envelope = body()
        return if (envelope != null) {
            ApiResult.Success(envelope.data)
        } else {
            ApiResult.Unexpected(IllegalStateException("Respuesta vacía del servidor"))
        }
    }

    return ApiResult.HttpError(code(), parseApiError(errorBody()?.string()))
}

internal fun Response<Unit>.toUnitResult(): ApiResult<Unit> {
    return if (isSuccessful) {
        ApiResult.Success(Unit)
    } else {
        ApiResult.HttpError(code(), parseApiError(errorBody()?.string()))
    }
}

internal fun parseApiError(raw: String?): ApiError? {
    if (raw.isNullOrBlank()) return null
    return runCatching {
        gson.fromJson(raw, ApiErrorEnvelope::class.java)?.error
    }.getOrNull()
}

fun ApiResult<*>.userMessage(): String = when (this) {
    is ApiResult.HttpError -> when (status) {
        400 -> error?.message?.takeIf { it.isNotBlank() } ?: "Datos inválidos."
        401 -> "Tu sesión expiró. Vuelve a iniciar sesión."
        403 -> "No tienes permisos para esta acción."
        404 -> "El recurso no está disponible."
        409 -> error?.message?.takeIf { it.isNotBlank() } ?: "No se puede completar por un conflicto."
        429 -> "Demasiados intentos. Espera un momento."
        else -> if (status >= 500) {
            "Error del servidor. Inténtalo más tarde."
        } else {
            error?.message?.takeIf { it.isNotBlank() } ?: "No se pudo completar la operación."
        }
    }

    is ApiResult.NetworkError -> "No se pudo conectar al servidor. Revisa tu red."
    is ApiResult.Unexpected -> "Ocurrió un error inesperado."
    is ApiResult.Success -> ""
}
