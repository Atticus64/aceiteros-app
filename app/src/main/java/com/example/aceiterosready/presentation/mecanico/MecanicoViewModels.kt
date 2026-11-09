package com.example.aceiterosready.presentation.mecanico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aceiterosready.data.model.CreateDetalleRequest
import com.example.aceiterosready.data.model.DetalleOrdenDto
import com.example.aceiterosready.data.model.OrdenDto
import com.example.aceiterosready.data.model.RefaccionDto
import com.example.aceiterosready.data.model.ServicioDto
import com.example.aceiterosready.data.model.UpdateDetalleRequest
import com.example.aceiterosready.data.model.UpdateOrdenRequest
import com.example.aceiterosready.data.network.ApiResult
import com.example.aceiterosready.data.network.userMessage
import com.example.aceiterosready.data.repository.CatalogRepository
import com.example.aceiterosready.data.repository.OrdenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private fun ApiResult<*>.mecanicoMessage(): String = when (this) {
    is ApiResult.HttpError -> when (status) {
        409 -> error?.message?.takeIf { it.isNotBlank() }
            ?: "No se puede completar la operación por un conflicto."
        else -> userMessage()
    }
    else -> userMessage()
}

data class MecanicoOrdenesState(
    val loading: Boolean = true,
    val ordenes: List<OrdenDto> = emptyList(),
    val error: String? = null,
)

class MecanicoOrdenesViewModel(
    private val ordenRepository: OrdenRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MecanicoOrdenesState())
    val state: StateFlow<MecanicoOrdenesState> = _state.asStateFlow()

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
                    it.copy(loading = false, error = result.mecanicoMessage())
                }
            }
        }
    }
}

data class DetalleFormState(
    val detalleId: String? = null,
    val tipo: String = "Servicio",
    val servicioId: String? = null,
    val refaccionId: String? = null,
    val descripcion: String = "",
    val cantidad: String = "1",
    val precioUnitario: String = "",
    val error: String? = null,
) {
    val editando: Boolean get() = detalleId != null
}

data class OrdenTrabajoState(
    val loading: Boolean = true,
    val orden: OrdenDto? = null,
    val detalles: List<DetalleOrdenDto> = emptyList(),
    val servicios: List<ServicioDto> = emptyList(),
    val refacciones: List<RefaccionDto> = emptyList(),
    val diagnostico: String = "",
    val saving: Boolean = false,
    val detalleForm: DetalleFormState? = null,
    val detalleAEliminar: DetalleOrdenDto? = null,
    val error: String? = null,
    val message: String? = null,
)

class OrdenTrabajoViewModel(
    private val ordenRepository: OrdenRepository,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OrdenTrabajoState())
    val state: StateFlow<OrdenTrabajoState> = _state.asStateFlow()

    private var ordenId: String? = null

    fun load(id: String) {
        ordenId = id
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            loadOrden()
            loadDetalles()
            loadCatalogos()
            _state.update { it.copy(loading = false) }
        }
    }

    fun refresh() {
        if (ordenId == null) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            loadOrden()
            loadDetalles()
            loadCatalogos()
            _state.update { it.copy(loading = false) }
        }
    }

    private suspend fun loadOrden() {
        val id = ordenId ?: return
        when (val result = ordenRepository.getOrden(id)) {
            is ApiResult.Success -> _state.update {
                it.copy(orden = result.data, diagnostico = result.data.diagnostico.orEmpty())
            }
            else -> _state.update { it.copy(error = result.mecanicoMessage()) }
        }
    }

    private suspend fun loadDetalles() {
        val id = ordenId ?: return
        when (val result = ordenRepository.listDetalles(id)) {
            is ApiResult.Success -> _state.update { it.copy(detalles = result.data) }
            else -> _state.update { it.copy(detalles = emptyList()) }
        }
    }

    private suspend fun loadCatalogos() {
        val servicios = catalogRepository.listServicios()
        if (servicios is ApiResult.Success) {
            _state.update { it.copy(servicios = servicios.data) }
        }
        val refacciones = catalogRepository.listRefacciones()
        if (refacciones is ApiResult.Success) {
            _state.update { it.copy(refacciones = refacciones.data) }
        }
    }

    fun setDiagnostico(value: String) = _state.update { it.copy(diagnostico = value) }

    fun guardarDiagnostico() {
        val id = ordenId ?: return
        viewModelScope.launch {
            _state.update { it.copy(saving = true, message = null) }
            when (val result = ordenRepository.updateOrden(
                id,
                UpdateOrdenRequest(diagnostico = _state.value.diagnostico.trim()),
            )) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        saving = false,
                        orden = result.data,
                        diagnostico = result.data.diagnostico.orEmpty(),
                        message = "Diagnóstico actualizado.",
                    )
                }
                else -> _state.update {
                    it.copy(saving = false, message = result.mecanicoMessage())
                }
            }
        }
    }

    fun cambiarEstado(estado: String) {
        val id = ordenId ?: return
        val actual = _state.value.orden?.estado
        if (actual == estado) return

        viewModelScope.launch {
            _state.update { it.copy(saving = true, message = null) }
            when (val result = ordenRepository.updateOrden(
                id,
                UpdateOrdenRequest(estado = estado),
            )) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            saving = false,
                            orden = result.data,
                            message = "Estado actualizado a ${com.example.aceiterosready.presentation.common.EstadosOrden.label(estado)}.",
                        )
                    }
                    loadDetalles()
                }
                else -> _state.update {
                    it.copy(saving = false, message = result.mecanicoMessage())
                }
            }
        }
    }

    fun abrirNuevoDetalle() {
        _state.update { it.copy(detalleForm = DetalleFormState()) }
    }

    fun abrirEditarDetalle(detalle: DetalleOrdenDto) {
        val tipo = if (detalle.refaccion != null) "Refaccion" else "Servicio"
        _state.update {
            it.copy(
                detalleForm = DetalleFormState(
                    detalleId = detalle.id,
                    tipo = tipo,
                    servicioId = detalle.servicio?.id,
                    refaccionId = detalle.refaccion?.id,
                    descripcion = detalle.descripcion,
                    cantidad = com.example.aceiterosready.presentation.common.formatCantidad(
                        detalle.cantidad,
                    ),
                    precioUnitario = detalle.precioUnitario.toString(),
                )
            )
        }
    }

    fun cerrarDetalle() = _state.update { it.copy(detalleForm = null) }

    fun setDetalleTipo(tipo: String) = updateDetalleForm {
        it.copy(
            tipo = tipo,
            servicioId = null,
            refaccionId = null,
            precioUnitario = "",
        )
    }

    fun setDetalleServicio(id: String) {
        val servicio = _state.value.servicios.firstOrNull { it.id == id }
        updateDetalleForm {
            it.copy(
                servicioId = id,
                refaccionId = null,
                descripcion = it.descripcion.ifBlank { servicio?.nombre.orEmpty() },
                precioUnitario = servicio?.precio?.toString() ?: it.precioUnitario,
            )
        }
    }

    fun setDetalleRefaccion(id: String) {
        val refaccion = _state.value.refacciones.firstOrNull { it.id == id }
        updateDetalleForm {
            it.copy(
                refaccionId = id,
                servicioId = null,
                descripcion = it.descripcion.ifBlank { refaccion?.nombre.orEmpty() },
                precioUnitario = refaccion?.precio?.toString() ?: it.precioUnitario,
            )
        }
    }

    fun setDetalleDescripcion(v: String) = updateDetalleForm { it.copy(descripcion = v) }
    fun setDetalleCantidad(v: String) =
        updateDetalleForm { it.copy(cantidad = v.filter(Char::isDigit)) }
    fun setDetallePrecio(v: String) = updateDetalleForm { it.copy(precioUnitario = v) }

    private fun updateDetalleForm(block: (DetalleFormState) -> DetalleFormState) {
        _state.update { current ->
            val form = current.detalleForm ?: return@update current
            current.copy(detalleForm = block(form).copy(error = null))
        }
    }

    fun guardarDetalle() {
        val id = ordenId ?: return
        val form = _state.value.detalleForm ?: return
        val cantidad = form.cantidad.toIntOrNull()
        val precio = form.precioUnitario.toDoubleOrNull()

        val error = when {
            !form.editando && form.tipo == "Servicio" && form.servicioId == null ->
                "Selecciona un servicio."
            !form.editando && form.tipo == "Refaccion" && form.refaccionId == null ->
                "Selecciona una refacción."
            cantidad == null || cantidad <= 0 -> "Ingresa una cantidad válida."
            form.precioUnitario.isNotBlank() && (precio == null || precio < 0) ->
                "Ingresa un precio válido."
            else -> null
        }

        if (error != null) {
            updateDetalleForm { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(saving = true) }

            val result: ApiResult<*> = if (form.detalleId == null) {
                ordenRepository.createDetalle(
                    CreateDetalleRequest(
                        ordenId = id,
                        servicioId = form.servicioId,
                        refaccionId = form.refaccionId,
                        descripcion = form.descripcion.trim().ifBlank { null },
                        cantidad = cantidad!!,
                        precioUnitario = precio,
                    ),
                )
            } else {
                ordenRepository.updateDetalle(
                    form.detalleId,
                    UpdateDetalleRequest(
                        descripcion = form.descripcion.trim().ifBlank { null },
                        cantidad = cantidad,
                        precioUnitario = precio,
                    ),
                )
            }

            when (result) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            saving = false,
                            detalleForm = null,
                            message = "Operación guardada.",
                        )
                    }
                    loadOrden()
                    loadDetalles()
                    loadCatalogos()
                }
                else -> {
                    val message = if (result is ApiResult.HttpError && result.status == 409) {
                        "Stock insuficiente para la refacción seleccionada."
                    } else {
                        result.mecanicoMessage()
                    }
                    _state.update { it.copy(saving = false) }
                    updateDetalleForm { it.copy(error = message) }
                }
            }
        }
    }

    fun pedirEliminarDetalle(detalle: DetalleOrdenDto) =
        _state.update { it.copy(detalleAEliminar = detalle) }

    fun cancelarEliminarDetalle() =
        _state.update { it.copy(detalleAEliminar = null) }

    fun eliminarDetalle() {
        val detalle = _state.value.detalleAEliminar ?: return
        viewModelScope.launch {
            _state.update { it.copy(saving = true, detalleAEliminar = null) }
            when (val result = ordenRepository.deleteDetalle(detalle.id)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(saving = false, message = "Operación eliminada.")
                    }
                    loadOrden()
                    loadDetalles()
                    loadCatalogos()
                }
                else -> _state.update {
                    it.copy(saving = false, message = result.mecanicoMessage())
                }
            }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
}
