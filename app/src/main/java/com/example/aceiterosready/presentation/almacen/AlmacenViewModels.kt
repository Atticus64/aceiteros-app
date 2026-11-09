package com.example.aceiterosready.presentation.almacen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aceiterosready.data.model.MovimientoInventarioDto
import com.example.aceiterosready.data.model.RefaccionDto
import com.example.aceiterosready.data.network.ApiResult
import com.example.aceiterosready.data.network.userMessage
import com.example.aceiterosready.data.repository.CatalogRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InventarioState(
    val loading: Boolean = true,
    val refacciones: List<RefaccionDto> = emptyList(),
    val query: String = "",
    val tipo: String? = null,
    val error: String? = null,
)

class InventarioViewModel(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(InventarioState())
    val state: StateFlow<InventarioState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val result = catalogRepository.listRefacciones(
                search = _state.value.query.trim().ifBlank { null },
                tipo = _state.value.tipo,
                activo = true,
            )) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        loading = false,
                        refacciones = result.data.sortedWith(
                            compareBy<RefaccionDto> { it.stock > it.stockMinimo }
                                .thenBy { it.nombre.lowercase() },
                        ),
                    )
                }
                else -> _state.update {
                    it.copy(loading = false, error = result.userMessage())
                }
            }
        }
    }

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            load()
        }
    }

    fun onTipoChange(tipo: String?) {
        _state.update { it.copy(tipo = tipo) }
        load()
    }
}

data class RefaccionDetalleState(
    val loading: Boolean = true,
    val refaccion: RefaccionDto? = null,
    val movimientos: List<MovimientoInventarioDto> = emptyList(),
    val error: String? = null,
)

class RefaccionDetalleViewModel(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RefaccionDetalleState())
    val state: StateFlow<RefaccionDetalleState> = _state.asStateFlow()

    private var refaccionId: String? = null

    fun load(id: String) {
        refaccionId = id
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }

            when (val result = catalogRepository.getRefaccion(id)) {
                is ApiResult.Success -> _state.update { it.copy(refaccion = result.data) }
                else -> _state.update {
                    it.copy(loading = false, error = result.userMessage())
                }
            }

            when (val result = catalogRepository.listMovimientos(id)) {
                is ApiResult.Success -> _state.update {
                    it.copy(loading = false, movimientos = result.data)
                }
                else -> _state.update {
                    it.copy(loading = false, movimientos = emptyList())
                }
            }

            _state.update { it.copy(loading = false) }
        }
    }

    fun refresh() {
        refaccionId?.let(::load)
    }
}
