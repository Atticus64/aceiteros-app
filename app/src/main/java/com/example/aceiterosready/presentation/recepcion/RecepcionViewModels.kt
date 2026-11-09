package com.example.aceiterosready.presentation.recepcion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aceiterosready.data.model.ClienteDto
import com.example.aceiterosready.data.model.CreateClienteRequest
import com.example.aceiterosready.data.model.CreateOrdenRequest
import com.example.aceiterosready.data.model.CreateVehiculoRequest
import com.example.aceiterosready.data.model.DetalleOrdenDto
import com.example.aceiterosready.data.model.OrdenDto
import com.example.aceiterosready.data.model.UpdateClienteRequest
import com.example.aceiterosready.data.model.UpdateOrdenRequest
import com.example.aceiterosready.data.model.UpdateVehiculoRequest
import com.example.aceiterosready.data.model.UserDto
import com.example.aceiterosready.data.model.VehiculoDto
import com.example.aceiterosready.data.network.ApiResult
import com.example.aceiterosready.data.network.userMessage
import com.example.aceiterosready.data.repository.CatalogRepository
import com.example.aceiterosready.data.repository.OrdenRepository
import com.example.aceiterosready.data.repository.UsuarioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private fun ApiResult<*>.recepcionMessage(): String = when (this) {
    is ApiResult.HttpError -> when (status) {
        404 -> "El recurso no está disponible en tu taller."
        409 -> error?.message?.takeIf { it.isNotBlank() }
            ?: "No se puede completar por un conflicto."
        else -> userMessage()
    }
    else -> userMessage()
}

private fun <T> ApiResult<List<T>>.dataOrEmpty(): List<T> =
    (this as? ApiResult.Success<List<T>>)?.data.orEmpty()

data class ClientesState(
    val loading: Boolean = true,
    val clientes: List<ClienteDto> = emptyList(),
    val query: String = "",
    val error: String? = null,
    val message: String? = null,
    val linkEmail: String = "",
    val linkTelefono: String = "",
    val linkBuscando: Boolean = false,
    val linkCliente: ClienteDto? = null,
    val linkError: String? = null,
    val vinculando: Boolean = false,
    val vinculado: Boolean = false,
)

class ClientesViewModel(
    private val usuarioRepository: UsuarioRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ClientesState())
    val state: StateFlow<ClientesState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val result = usuarioRepository.listClientes(
                search = _state.value.query.trim().ifBlank { null },
            )) {
                is ApiResult.Success -> _state.update {
                    it.copy(loading = false, clientes = result.data)
                }
                else -> _state.update {
                    it.copy(loading = false, error = result.recepcionMessage())
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

    fun onLinkEmailChange(value: String) =
        _state.update { it.copy(linkEmail = value, linkError = null) }

    fun onLinkTelefonoChange(value: String) =
        _state.update { it.copy(linkTelefono = value, linkError = null) }

    fun buscarParaVincular() {
        val current = _state.value
        val email = current.linkEmail.trim().ifBlank { null }
        val telefono = current.linkTelefono.trim().ifBlank { null }

        if (email == null && telefono == null) {
            _state.update { it.copy(linkError = "Ingresa un correo o un teléfono.") }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(linkBuscando = true, linkError = null, linkCliente = null)
            }

            when (val result = usuarioRepository.buscarCliente(email, telefono)) {
                is ApiResult.Success -> _state.update {
                    it.copy(linkBuscando = false, linkCliente = result.data)
                }
                else -> _state.update {
                    it.copy(linkBuscando = false, linkError = result.recepcionMessage())
                }
            }
        }
    }

    fun vincular() {
        val cliente = _state.value.linkCliente ?: return
        viewModelScope.launch {
            _state.update { it.copy(vinculando = true, linkError = null) }
            when (val result = usuarioRepository.vincularCliente(
                com.example.aceiterosready.data.model.VincularClienteRequest(cliente.id),
            )) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            vinculando = false,
                            vinculado = true,
                            message = "Cliente vinculado al taller.",
                        )
                    }
                    load()
                }
                else -> _state.update {
                    it.copy(vinculando = false, linkError = result.recepcionMessage())
                }
            }
        }
    }

    fun resetLink() {
        _state.update {
            it.copy(
                linkEmail = "",
                linkTelefono = "",
                linkCliente = null,
                linkError = null,
                linkBuscando = false,
                vinculando = false,
                vinculado = false,
            )
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
}

data class ClienteFormState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val clienteId: String? = null,
    val nombre: String = "",
    val apellido: String = "",
    val telefono: String = "",
    val email: String = "",
    val password: String = "",
    val fieldErrors: Map<String, String> = emptyMap(),
    val error: String? = null,
    val saved: Boolean = false,
) {
    val editando: Boolean get() = clienteId != null
}

class ClienteFormViewModel(
    private val usuarioRepository: UsuarioRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ClienteFormState())
    val state: StateFlow<ClienteFormState> = _state.asStateFlow()

    private var initialized = false

    fun load(id: String?) {
        if (initialized) return
        initialized = true
        _state.update { it.copy(loading = true, clienteId = id) }

        if (id == null) {
            _state.update { it.copy(loading = false) }
            return
        }

        viewModelScope.launch {
            when (val result = usuarioRepository.getCliente(id)) {
                is ApiResult.Success -> {
                    val cliente = result.data
                    _state.update {
                        it.copy(
                            loading = false,
                            nombre = cliente.nombre,
                            apellido = cliente.apellido.orEmpty(),
                            telefono = cliente.telefono.orEmpty(),
                        )
                    }
                }
                else -> _state.update {
                    it.copy(loading = false, error = result.recepcionMessage())
                }
            }
        }
    }

    fun setNombre(v: String) = _state.update { it.copy(nombre = v) }
    fun setApellido(v: String) = _state.update { it.copy(apellido = v) }
    fun setTelefono(v: String) = _state.update { it.copy(telefono = v) }
    fun setEmail(v: String) = _state.update { it.copy(email = v) }
    fun setPassword(v: String) = _state.update { it.copy(password = v) }

    fun save() {
        val current = _state.value
        val fieldErrors = buildMap {
            if (current.nombre.isBlank()) put("nombre", "Ingresa el nombre.")
            if (!current.editando) {
                if (current.email.isBlank()) put("email", "Ingresa el correo.")
                if (current.password.length !in 8..72) {
                    put("password", "La contraseña debe tener entre 8 y 72 caracteres.")
                }
            }
        }

        if (fieldErrors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = fieldErrors, error = null) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }

            val result: ApiResult<*> = if (current.clienteId == null) {
                usuarioRepository.createCliente(
                    CreateClienteRequest(
                        nombre = current.nombre.trim(),
                        apellido = current.apellido.trim().ifBlank { null },
                        email = current.email.trim(),
                        password = current.password,
                        telefono = current.telefono.trim().ifBlank { null },
                    ),
                )
            } else {
                usuarioRepository.updateCliente(
                    current.clienteId,
                    UpdateClienteRequest(
                        nombre = current.nombre.trim(),
                        apellido = current.apellido.trim().ifBlank { null },
                        telefono = current.telefono.trim().ifBlank { null },
                    ),
                )
            }

            when (result) {
                is ApiResult.Success -> _state.update {
                    it.copy(saving = false, saved = true)
                }
                else -> _state.update {
                    it.copy(saving = false, error = result.recepcionMessage())
                }
            }
        }
    }
}

data class VehiculosState(
    val loading: Boolean = true,
    val vehiculos: List<VehiculoDto> = emptyList(),
    val clientes: List<ClienteDto> = emptyList(),
    val query: String = "",
    val error: String? = null,
    val message: String? = null,
)

class VehiculosViewModel(
    private val catalogRepository: CatalogRepository,
    private val usuarioRepository: UsuarioRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(VehiculosState())
    val state: StateFlow<VehiculosState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val result = catalogRepository.listVehiculos(
                search = _state.value.query.trim().ifBlank { null },
            )) {
                is ApiResult.Success -> _state.update {
                    it.copy(loading = false, vehiculos = result.data)
                }
                else -> _state.update {
                    it.copy(loading = false, error = result.recepcionMessage())
                }
            }

            val clientes = usuarioRepository.listClientes()
            if (clientes is ApiResult.Success) {
                _state.update { it.copy(clientes = clientes.data) }
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

    fun clearMessage() = _state.update { it.copy(message = null) }
}

data class VehiculoFormState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val vehiculoId: String? = null,
    val placa: String = "",
    val vin: String = "",
    val marca: String = "",
    val modelo: String = "",
    val anio: String = "",
    val color: String = "",
    val kilometraje: String = "0",
    val clienteId: String? = null,
    val clientes: List<ClienteDto> = emptyList(),
    val fieldErrors: Map<String, String> = emptyMap(),
    val error: String? = null,
    val saved: Boolean = false,
) {
    val editando: Boolean get() = vehiculoId != null
}

class VehiculoFormViewModel(
    private val catalogRepository: CatalogRepository,
    private val usuarioRepository: UsuarioRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(VehiculoFormState())
    val state: StateFlow<VehiculoFormState> = _state.asStateFlow()

    private var initialized = false

    fun load(id: String?) {
        if (initialized) return
        initialized = true
        _state.update { it.copy(loading = true, vehiculoId = id) }

        viewModelScope.launch {
            val clientes = usuarioRepository.listClientes()
            if (clientes is ApiResult.Success) {
                _state.update { it.copy(clientes = clientes.data) }
            }

            if (id == null) {
                _state.update { it.copy(loading = false) }
                return@launch
            }

            when (val result = catalogRepository.getVehiculo(id)) {
                is ApiResult.Success -> {
                    val v = result.data
                    _state.update {
                        it.copy(
                            loading = false,
                            placa = v.placa,
                            vin = v.vin.orEmpty(),
                            marca = v.marca,
                            modelo = v.modelo,
                            anio = v.anio.toString(),
                            color = v.color.orEmpty(),
                            kilometraje = v.kilometraje.toString(),
                            clienteId = v.clienteId,
                        )
                    }
                }
                else -> _state.update {
                    it.copy(loading = false, error = result.recepcionMessage())
                }
            }
        }
    }

    fun setPlaca(v: String) = _state.update { it.copy(placa = v.uppercase()) }
    fun setVin(v: String) = _state.update { it.copy(vin = v.uppercase()) }
    fun setMarca(v: String) = _state.update { it.copy(marca = v) }
    fun setModelo(v: String) = _state.update { it.copy(modelo = v) }
    fun setAnio(v: String) = _state.update { it.copy(anio = v.filter(Char::isDigit)) }
    fun setColor(v: String) = _state.update { it.copy(color = v) }
    fun setKilometraje(v: String) =
        _state.update { it.copy(kilometraje = v.filter(Char::isDigit)) }
    fun setCliente(v: String?) = _state.update { it.copy(clienteId = v) }

    fun save() {
        val current = _state.value
        val anio = current.anio.toIntOrNull()
        val kilometraje = current.kilometraje.toIntOrNull() ?: 0

        val fieldErrors = buildMap {
            if (!current.placa.matches(Regex("^[A-Za-z0-9-]+$"))) {
                put("placa", "Usa solo letras, números y guiones.")
            }
            if (current.marca.isBlank()) put("marca", "Ingresa la marca.")
            if (current.modelo.isBlank()) put("modelo", "Ingresa el modelo.")
            if (anio == null || anio < 1886) put("anio", "Ingresa un año válido.")
        }

        if (fieldErrors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = fieldErrors, error = null) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }

            val result: ApiResult<*> = if (current.vehiculoId == null) {
                catalogRepository.createVehiculo(
                    CreateVehiculoRequest(
                        placa = current.placa.trim(),
                        vin = current.vin.trim().ifBlank { null },
                        marca = current.marca.trim(),
                        modelo = current.modelo.trim(),
                        anio = anio!!,
                        color = current.color.trim().ifBlank { null },
                        kilometraje = kilometraje,
                        clienteId = current.clienteId,
                    ),
                )
            } else {
                catalogRepository.updateVehiculo(
                    current.vehiculoId,
                    UpdateVehiculoRequest(
                        placa = current.placa.trim(),
                        vin = current.vin.trim().ifBlank { null },
                        marca = current.marca.trim(),
                        modelo = current.modelo.trim(),
                        anio = anio,
                        color = current.color.trim().ifBlank { null },
                        kilometraje = kilometraje,
                        clienteId = current.clienteId,
                    ),
                )
            }

            when (result) {
                is ApiResult.Success -> _state.update {
                    it.copy(saving = false, saved = true)
                }
                else -> _state.update {
                    it.copy(saving = false, error = result.recepcionMessage())
                }
            }
        }
    }
}

data class OrdenesState(
    val loading: Boolean = true,
    val ordenes: List<OrdenDto> = emptyList(),
    val estado: String? = null,
    val error: String? = null,
    val message: String? = null,
)

class OrdenesViewModel(
    private val ordenRepository: OrdenRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OrdenesState())
    val state: StateFlow<OrdenesState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val result = ordenRepository.listOrdenes(
                estado = _state.value.estado,
            )) {
                is ApiResult.Success -> _state.update {
                    it.copy(loading = false, ordenes = result.data)
                }
                else -> _state.update {
                    it.copy(loading = false, error = result.recepcionMessage())
                }
            }
        }
    }

    fun onEstadoChange(estado: String?) {
        _state.update { it.copy(estado = estado) }
        load()
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
}

data class OrdenFormState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val vehiculos: List<VehiculoDto> = emptyList(),
    val clientes: List<ClienteDto> = emptyList(),
    val mecanicos: List<UserDto> = emptyList(),
    val vehiculoId: String? = null,
    val clienteId: String? = null,
    val mecanicoId: String? = null,
    val diagnostico: String = "",
    val fieldErrors: Map<String, String> = emptyMap(),
    val error: String? = null,
    val saved: Boolean = false,
)

class OrdenFormViewModel(
    private val ordenRepository: OrdenRepository,
    private val catalogRepository: CatalogRepository,
    private val usuarioRepository: UsuarioRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OrdenFormState())
    val state: StateFlow<OrdenFormState> = _state.asStateFlow()

    private var initialized = false

    fun load() {
        if (initialized) return
        initialized = true

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }

            val vehiculos = catalogRepository.listVehiculos()
            val clientes = usuarioRepository.listClientes()
            val mecanicos = usuarioRepository.listMecanicos()

            _state.update {
                it.copy(
                    loading = false,
                    vehiculos = vehiculos.dataOrEmpty(),
                    clientes = clientes.dataOrEmpty(),
                    mecanicos = mecanicos.dataOrEmpty(),
                )
            }

            val failed = listOf(vehiculos, clientes, mecanicos)
                .firstOrNull { it !is ApiResult.Success<*> }
            if (failed != null) {
                _state.update { it.copy(error = failed.recepcionMessage()) }
            }
        }
    }

    fun setVehiculo(id: String) {
        val vehiculo = _state.value.vehiculos.firstOrNull { it.id == id }
        _state.update {
            it.copy(
                vehiculoId = id,
                clienteId = vehiculo?.clienteId ?: it.clienteId,
            )
        }
    }

    fun setCliente(id: String?) = _state.update { it.copy(clienteId = id) }
    fun setMecanico(id: String) = _state.update { it.copy(mecanicoId = id) }
    fun setDiagnostico(v: String) = _state.update { it.copy(diagnostico = v) }

    fun save() {
        val current = _state.value
        val fieldErrors = buildMap {
            if (current.vehiculoId == null) {
                put("vehiculoId", "Selecciona un vehículo.")
            }
            if (current.mecanicoId == null) {
                put("mecanicoId", "Asigna un mecánico.")
            }
        }

        if (fieldErrors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = fieldErrors, error = null) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            when (val result = ordenRepository.createOrden(
                CreateOrdenRequest(
                    vehiculoId = current.vehiculoId!!,
                    mecanicoId = current.mecanicoId,
                    clienteId = current.clienteId,
                    diagnostico = current.diagnostico.trim().ifBlank { null },
                    estado = "Recepcion",
                ),
            )) {
                is ApiResult.Success -> _state.update {
                    it.copy(saving = false, saved = true)
                }
                else -> _state.update {
                    it.copy(saving = false, error = result.recepcionMessage())
                }
            }
        }
    }
}

data class OrdenDetalleState(
    val loading: Boolean = true,
    val orden: OrdenDto? = null,
    val detalles: List<DetalleOrdenDto> = emptyList(),
    val mecanicos: List<UserDto> = emptyList(),
    val saving: Boolean = false,
    val error: String? = null,
    val message: String? = null,
)

class OrdenDetalleViewModel(
    private val ordenRepository: OrdenRepository,
    private val usuarioRepository: UsuarioRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OrdenDetalleState())
    val state: StateFlow<OrdenDetalleState> = _state.asStateFlow()

    private var ordenId: String? = null

    fun load(id: String) {
        ordenId = id
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            refreshInternal()
        }
    }

    fun refresh() {
        val id = ordenId ?: return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            refreshInternal()
        }
    }

    private suspend fun refreshInternal() {
        val id = ordenId ?: return

        when (val orden = ordenRepository.getOrden(id)) {
            is ApiResult.Success -> _state.update {
                it.copy(orden = orden.data)
            }
            else -> _state.update {
                it.copy(loading = false, error = orden.recepcionMessage())
            }
        }

        when (val detalles = ordenRepository.listDetalles(id)) {
            is ApiResult.Success -> _state.update {
                it.copy(loading = false, detalles = detalles.data)
            }
            else -> _state.update {
                it.copy(loading = false, detalles = emptyList())
            }
        }

        when (val mecanicos = usuarioRepository.listMecanicos()) {
            is ApiResult.Success -> _state.update { it.copy(mecanicos = mecanicos.data) }
            else -> Unit
        }

        _state.update { it.copy(loading = false) }
    }

    fun asignarMecanico(mecanicoId: String) {
        val id = ordenId ?: return
        viewModelScope.launch {
            _state.update { it.copy(saving = true, message = null) }
            when (val result = ordenRepository.updateOrden(
                id,
                UpdateOrdenRequest(mecanicoId = mecanicoId),
            )) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            saving = false,
                            orden = result.data,
                            message = "Mecánico asignado.",
                        )
                    }
                }
                else -> _state.update {
                    it.copy(saving = false, message = result.recepcionMessage())
                }
            }
        }
    }

    fun confirmarEntrega() {
        val id = ordenId ?: return
        viewModelScope.launch {
            _state.update { it.copy(saving = true, message = null) }
            when (val result = ordenRepository.confirmarEntrega(id)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            saving = false,
                            orden = result.data,
                            message = "Entrega confirmada.",
                        )
                    }
                    refreshInternal()
                }
                else -> _state.update {
                    it.copy(saving = false, message = result.recepcionMessage())
                }
            }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
}
