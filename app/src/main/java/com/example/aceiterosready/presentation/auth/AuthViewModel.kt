package com.example.aceiterosready.presentation.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aceiterosready.data.auth.AuthRepository
import com.example.aceiterosready.data.model.UserDto
import com.example.aceiterosready.data.network.ApiResult
import com.example.aceiterosready.data.network.userMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SessionState {
    data object Restoring : SessionState
    data object Unauthenticated : SessionState
    data class Authenticated(val user: UserDto) : SessionState
}

data class AuthUiState(
    val session: SessionState = SessionState.Restoring,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
)

sealed interface AuthEvent {
    data class Registered(val email: String) : AuthEvent
}

class AuthViewModel(
    private val repository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    init {
        restoreSession()
        viewModelScope.launch {
            repository.session.collect { session ->
                if (session != null) {
                    _uiState.update {
                        it.copy(session = SessionState.Authenticated(session.user))
                    }
                } else if (_uiState.value.session is SessionState.Authenticated) {
                    _uiState.update { it.copy(session = SessionState.Unauthenticated) }
                }
            }
        }
    }

    fun login(email: String, password: String) {
        val fieldErrors = validateLogin(email, password)

        if (fieldErrors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = fieldErrors, errorMessage = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, fieldErrors = emptyMap())
            }

            when (val result = repository.login(email, password)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        session = SessionState.Authenticated(result.data),
                    )
                }

                else -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = messageFor(result),
                        fieldErrors = fieldErrorsFor(result),
                    )
                }
            }
        }
    }

    fun register(
        nombre: String,
        apellido: String,
        telefono: String,
        email: String,
        password: String,
    ) {
        val fieldErrors = validateRegister(nombre, apellido, telefono, email, password)

        if (fieldErrors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = fieldErrors, errorMessage = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, fieldErrors = emptyMap())
            }

            when (val result = repository.register(
                nombre = nombre,
                apellido = apellido,
                telefono = telefono,
                email = email,
                password = password,
            )) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(AuthEvent.Registered(email.trim()))
                }

                else -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = messageFor(result),
                        fieldErrors = fieldErrorsFor(result),
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.logout()
            } finally {
                _uiState.value = AuthUiState(session = SessionState.Unauthenticated)
            }
        }
    }

    fun clearErrors() {
        _uiState.update { it.copy(errorMessage = null, fieldErrors = emptyMap()) }
    }

    private fun restoreSession() {
        viewModelScope.launch {
            when (val result = repository.restoreSession()) {
                is ApiResult.Success -> {
                    val user = result.data
                    _uiState.update {
                        it.copy(
                            session = user?.let(SessionState::Authenticated)
                                ?: SessionState.Unauthenticated,
                        )
                    }
                }

                is ApiResult.NetworkError -> {
                    val savedUser = repository.session.value?.user
                    _uiState.update {
                        it.copy(
                            session = savedUser?.let(SessionState::Authenticated)
                                ?: SessionState.Unauthenticated,
                            errorMessage = if (savedUser != null) {
                                "Sin conexión. Se muestra la sesión guardada."
                            } else {
                                messageFor(result)
                            },
                        )
                    }
                }

                is ApiResult.HttpError -> _uiState.update {
                    it.copy(
                        session = SessionState.Unauthenticated,
                        errorMessage = messageFor(result),
                    )
                }

                is ApiResult.Unexpected -> _uiState.update {
                    it.copy(
                        session = SessionState.Unauthenticated,
                        errorMessage = messageFor(result),
                    )
                }
            }
        }
    }

    private fun validateLogin(email: String, password: String): Map<String, String> = buildMap {
        if (email.isBlank()) {
            put("email", "Ingresa tu correo.")
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            put("email", "Ingresa un correo válido.")
        }

        if (password.isBlank()) {
            put("password", "Ingresa tu contraseña.")
        }
    }

    private fun validateRegister(
        nombre: String,
        apellido: String,
        telefono: String,
        email: String,
        password: String,
    ): Map<String, String> = buildMap {
        if (nombre.isBlank()) {
            put("nombre", "Ingresa tu nombre.")
        } else if (nombre.length > 100) {
            put("nombre", "El nombre no puede superar 100 caracteres.")
        }

        if (apellido.length > 100) {
            put("apellido", "El apellido no puede superar 100 caracteres.")
        }

        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            put("email", "Ingresa un correo válido.")
        }

        if (password.length !in 8..72) {
            put("password", "La contraseña debe tener entre 8 y 72 caracteres.")
        }

        if (telefono.length > 20) {
            put("telefono", "El teléfono no puede superar 20 caracteres.")
        }
    }

    private fun messageFor(result: ApiResult<*>): String = when (result) {
        is ApiResult.HttpError -> {
            val error = result.error
            when {
                error?.code == "UNAUTHORIZED" -> "Correo o contraseña incorrectos."
                error?.code == "CONFLICT" -> "Ya existe una cuenta con ese correo."
                error?.code == "VALIDATION_ERROR" -> "Revisa los campos marcados."
                error?.code == "RATE_LIMITED" || result.status == 429 ->
                    "Demasiados intentos. Espera un momento."
                result.status >= 500 -> "Error del servidor. Inténtalo más tarde."
                else -> result.userMessage()
            }
        }

        is ApiResult.NetworkError -> result.userMessage()
        is ApiResult.Unexpected -> result.userMessage()
        is ApiResult.Success -> ""
    }

    private fun fieldErrorsFor(result: ApiResult<*>): Map<String, String> {
        val error = (result as? ApiResult.HttpError)?.error ?: return emptyMap()
        if (error.code != "VALIDATION_ERROR") return emptyMap()

        return error.details.orEmpty().associate { detail ->
            val field = detail.path.substringAfterLast('.')

            field to when (field) {
                "nombre" -> "Ingresa tu nombre."
                "apellido" -> "El apellido no puede superar 100 caracteres."
                "email" -> "Ingresa un correo válido."
                "password" -> "La contraseña debe tener entre 8 y 72 caracteres."
                "telefono" -> "El teléfono no puede superar 20 caracteres."
                else -> detail.message
            }
        }
    }
}
