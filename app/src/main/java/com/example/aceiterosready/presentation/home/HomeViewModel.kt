package com.example.aceiterosready.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aceiterosready.data.model.OrdenDto
import com.example.aceiterosready.data.model.RefaccionDto
import com.example.aceiterosready.data.model.ServicioDto
import com.example.aceiterosready.data.model.UserDto
import com.example.aceiterosready.data.model.VehiculoDto
import com.example.aceiterosready.data.network.ApiResult
import com.example.aceiterosready.data.network.userMessage
import com.example.aceiterosready.data.repository.CatalogRepository
import com.example.aceiterosready.data.repository.OrdenRepository
import com.example.aceiterosready.data.repository.UsuarioRepository
import com.example.aceiterosready.presentation.common.Roles
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeState(
    val loading: Boolean = true,
    val ordenes: List<OrdenDto> = emptyList(),
    val vehiculos: List<VehiculoDto> = emptyList(),
    val refacciones: List<RefaccionDto> = emptyList(),
    val servicios: List<ServicioDto> = emptyList(),
    val usuarios: List<UserDto> = emptyList(),
    val error: String? = null,
)

class HomeViewModel(
    private val ordenRepository: OrdenRepository,
    private val catalogRepository: CatalogRepository,
    private val usuarioRepository: UsuarioRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var lastUser: UserDto? = null

    fun load(user: UserDto) {
        lastUser = user
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (user.rol) {
                Roles.ADMINISTRADOR -> loadAdmin()
                Roles.RECEPCIONISTA -> loadRecepcion()
                Roles.MECANICO -> loadMecanico()
                Roles.ALMACENISTA -> loadAlmacen()
                Roles.CLIENTE -> loadCliente()
            }
            _state.update { it.copy(loading = false) }
        }
    }

    fun refresh() {
        lastUser?.let(::load)
    }

    private suspend fun loadAdmin() {
        val usuarios = usuarioRepository.listUsuarios()
        val servicios = catalogRepository.listServicios()
        val refacciones = catalogRepository.listRefacciones()

        _state.update {
            it.copy(
                usuarios = usuarios.listOrEmpty(),
                servicios = servicios.listOrEmpty(),
                refacciones = refacciones.listOrEmpty(),
            )
        }

        firstError(usuarios, servicios, refacciones)?.let { message ->
            _state.update { it.copy(error = message) }
        }
    }

    private suspend fun loadRecepcion() {
        val ordenes = ordenRepository.listOrdenes()
        val vehiculos = catalogRepository.listVehiculos()

        _state.update {
            it.copy(
                ordenes = ordenes.listOrEmpty(),
                vehiculos = vehiculos.listOrEmpty(),
            )
        }

        firstError(ordenes, vehiculos)?.let { message ->
            _state.update { it.copy(error = message) }
        }
    }

    private suspend fun loadMecanico() {
        val ordenes = ordenRepository.listOrdenes()
        _state.update {
            it.copy(ordenes = ordenes.listOrEmpty())
        }
        firstError(ordenes)?.let { message ->
            _state.update { it.copy(error = message) }
        }
    }

    private suspend fun loadAlmacen() {
        val refacciones = catalogRepository.listRefacciones(activo = true)
        _state.update {
            it.copy(refacciones = refacciones.listOrEmpty())
        }
        firstError(refacciones)?.let { message ->
            _state.update { it.copy(error = message) }
        }
    }

    private suspend fun loadCliente() {
        val ordenes = ordenRepository.listOrdenes()
        val vehiculos = catalogRepository.listVehiculos()

        _state.update {
            it.copy(
                ordenes = ordenes.listOrEmpty(),
                vehiculos = vehiculos.listOrEmpty(),
            )
        }

        firstError(ordenes, vehiculos)?.let { message ->
            _state.update { it.copy(error = message) }
        }
    }

    private fun firstError(vararg results: ApiResult<*>): String? =
        results.firstOrNull { it !is ApiResult.Success<*> }?.userMessage()

    private fun <T> ApiResult<List<T>>.listOrEmpty(): List<T> =
        (this as? ApiResult.Success<List<T>>)?.data.orEmpty()
}
