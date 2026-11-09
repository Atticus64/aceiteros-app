package com.example.aceiterosready.presentation.cliente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aceiterosready.data.model.DetalleOrdenDto
import com.example.aceiterosready.data.model.OrdenDto
import com.example.aceiterosready.data.model.VehiculoDto
import com.example.aceiterosready.data.network.ApiResult
import com.example.aceiterosready.data.network.userMessage
import com.example.aceiterosready.data.repository.CatalogRepository
import com.example.aceiterosready.data.repository.OrdenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MisVehiculosState(
    val loading: Boolean = true,
    val vehiculos: List<VehiculoDto> = emptyList(),
    val error: String? = null,
)

class MisVehiculosViewModel(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MisVehiculosState())
    val state: StateFlow<MisVehiculosState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val result = catalogRepository.listVehiculos()) {
                is ApiResult.Success -> _state.update {
                    it.copy(loading = false, vehiculos = result.data)
                }
                else -> _state.update {
                    it.copy(loading = false, error = result.userMessage())
                }
            }
        }
    }
}

data class MiVehiculoDetalleState(
    val loading: Boolean = true,
    val vehiculo: VehiculoDto? = null,
    val ordenes: List<OrdenDto> = emptyList(),
    val error: String? = null,
)

class MiVehiculoDetalleViewModel(
    private val catalogRepository: CatalogRepository,
    private val ordenRepository: OrdenRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MiVehiculoDetalleState())
    val state: StateFlow<MiVehiculoDetalleState> = _state.asStateFlow()

    private var vehiculoId: String? = null

    fun load(id: String) {
        vehiculoId = id
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }

            when (val result = catalogRepository.getVehiculo(id)) {
                is ApiResult.Success -> _state.update { it.copy(vehiculo = result.data) }
                else -> _state.update {
                    it.copy(loading = false, error = result.userMessage())
                }
            }

            when (val result = ordenRepository.listOrdenes()) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        loading = false,
                        ordenes = result.data.filter { orden -> orden.vehiculo?.id == id },
                    )
                }
                else -> _state.update {
                    it.copy(loading = false, ordenes = emptyList())
                }
            }

            _state.update { it.copy(loading = false) }
        }
    }

    fun refresh() {
        vehiculoId?.let(::load)
    }
}

data class MisOrdenesState(
    val loading: Boolean = true,
    val ordenes: List<OrdenDto> = emptyList(),
    val error: String? = null,
)

class MisOrdenesViewModel(
    private val ordenRepository: OrdenRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MisOrdenesState())
    val state: StateFlow<MisOrdenesState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val result = ordenRepository.listOrdenes()) {
                is ApiResult.Success -> _state.update {
                    it.copy(loading = false, ordenes = result.data)
                }
                else -> _state.update {
                    it.copy(loading = false, error = result.userMessage())
                }
            }
        }
    }
}

data class MiOrdenDetalleState(
    val loading: Boolean = true,
    val orden: OrdenDto? = null,
    val detalles: List<DetalleOrdenDto> = emptyList(),
    val error: String? = null,
)

class MiOrdenDetalleViewModel(
    private val ordenRepository: OrdenRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MiOrdenDetalleState())
    val state: StateFlow<MiOrdenDetalleState> = _state.asStateFlow()

    private var ordenId: String? = null

    fun load(id: String) {
        ordenId = id
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }

            when (val result = ordenRepository.getOrden(id)) {
                is ApiResult.Success -> _state.update { it.copy(orden = result.data) }
                else -> _state.update {
                    it.copy(loading = false, error = result.userMessage())
                }
            }

            when (val result = ordenRepository.listDetalles(id)) {
                is ApiResult.Success -> _state.update {
                    it.copy(loading = false, detalles = result.data)
                }
                else -> _state.update {
                    it.copy(loading = false, detalles = emptyList())
                }
            }

            _state.update { it.copy(loading = false) }
        }
    }

    fun refresh() {
        ordenId?.let(::load)
    }
}
