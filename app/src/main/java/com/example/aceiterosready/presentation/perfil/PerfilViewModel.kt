package com.example.aceiterosready.presentation.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aceiterosready.data.auth.AuthRepository
import com.example.aceiterosready.data.model.UserDto
import com.example.aceiterosready.data.network.ApiResult
import com.example.aceiterosready.data.network.userMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilState(
    val nombre: String = "",
    val apellido: String = "",
    val telefono: String = "",
    val email: String = "",
    val rol: String = "",
    val saving: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val currentPassword: String = "",
    val newPassword: String = "",
    val changingPassword: Boolean = false,
    val passwordError: String? = null,
    val passwordMessage: String? = null,
)

class PerfilViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PerfilState())
    val state: StateFlow<PerfilState> = _state.asStateFlow()

    private var initialized = false

    fun init(user: UserDto) {
        if (initialized) return
        initialized = true
        _state.update {
            it.copy(
                nombre = user.nombre,
                apellido = user.apellido.orEmpty(),
                telefono = user.telefono.orEmpty(),
                email = user.email,
                rol = user.rol,
            )
        }
    }

    fun setNombre(v: String) = _state.update { it.copy(nombre = v) }
    fun setApellido(v: String) = _state.update { it.copy(apellido = v) }
    fun setTelefono(v: String) = _state.update { it.copy(telefono = v) }
    fun setCurrentPassword(v: String) = _state.update {
        it.copy(currentPassword = v, passwordError = null)
    }
    fun setNewPassword(v: String) = _state.update {
        it.copy(newPassword = v, passwordError = null)
    }

    fun guardarPerfil() {
        val current = _state.value
        if (current.nombre.isBlank()) {
            _state.update { it.copy(error = "El nombre no puede estar vacío.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null, message = null) }
            when (val result = authRepository.updateProfile(
                nombre = current.nombre.trim(),
                apellido = current.apellido.trim(),
                telefono = current.telefono.trim(),
            )) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            saving = false,
                            message = "Perfil actualizado.",
                            nombre = result.data.nombre,
                            apellido = result.data.apellido.orEmpty(),
                            telefono = result.data.telefono.orEmpty(),
                            email = result.data.email,
                            rol = result.data.rol,
                        )
                    }
                }
                else -> _state.update {
                    it.copy(saving = false, error = result.userMessage())
                }
            }
        }
    }

    fun cambiarPassword() {
        val current = _state.value
        val error = when {
            current.currentPassword.isBlank() -> "Ingresa tu contraseña actual."
            current.newPassword.length !in 8..72 ->
                "La nueva contraseña debe tener entre 8 y 72 caracteres."
            current.currentPassword == current.newPassword ->
                "La nueva contraseña debe ser diferente."
            else -> null
        }

        if (error != null) {
            _state.update { it.copy(passwordError = error, passwordMessage = null) }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    changingPassword = true,
                    passwordError = null,
                    passwordMessage = null,
                )
            }
            when (val result = authRepository.changePassword(
                current.currentPassword,
                current.newPassword,
            )) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        changingPassword = false,
                        currentPassword = "",
                        newPassword = "",
                        passwordMessage = "Contraseña actualizada.",
                    )
                }
                else -> _state.update {
                    it.copy(
                        changingPassword = false,
                        passwordError = result.userMessage(),
                    )
                }
            }
        }
    }

    fun clearMessages() = _state.update {
        it.copy(error = null, message = null, passwordError = null, passwordMessage = null)
    }
}
