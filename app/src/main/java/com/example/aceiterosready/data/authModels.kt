package com.example.aceiterosready.data

import com.google.gson.annotations.SerializedName
import java.time.Instant
import java.util.Optional

data class SignInData (
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

enum class Rol(value: String) {
    Admin("ADMINISTRADOR"),
    Mecanico("MECANICO"),
    Almacenista("ALMACENISTA"),
    Cliente("CLIENTE")
}

data class UserData (
    val id: String,
    val nombre: String,
    val apellido: Optional<String>,
    val email: String,
    val telefono: Optional<String>,
    val rolId: String,
    val rol: Rol,
    val activo: Boolean,
    val tokenVersion: Int,
    val createdAt: String,
    val updatedAt: String,
)

data class Tokens (
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val refreshExpiresIn: Int
)

data class Error(
    val code: String,
    val message: String
)

data class ErrorResponse (
    val error: Error
)

data class LoginSuccess (
    val user: UserData,
    val tokens: Tokens
)

sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val error: Throwable) : Resource<T>()
    class Loading<T> : Resource<T>()
}

data class LoginResponse (
    val data: LoginSuccess
)
