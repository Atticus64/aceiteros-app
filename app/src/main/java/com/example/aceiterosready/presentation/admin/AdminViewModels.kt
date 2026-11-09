package com.example.aceiterosready.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aceiterosready.data.model.CreateRefaccionRequest
import com.example.aceiterosready.data.model.CreateServicioRequest
import com.example.aceiterosready.data.model.CreateUsuarioRequest
import com.example.aceiterosready.data.model.RefaccionDto
import com.example.aceiterosready.data.model.ServicioDto
import com.example.aceiterosready.data.model.TallerDto
import com.example.aceiterosready.data.model.UpdateRefaccionRequest
import com.example.aceiterosready.data.model.UpdateServicioRequest
import com.example.aceiterosready.data.model.UpdateUsuarioRequest
import com.example.aceiterosready.data.model.UserDto
import com.example.aceiterosready.data.network.ApiResult
import com.example.aceiterosready.data.network.userMessage
import com.example.aceiterosready.data.repository.CatalogRepository
import com.example.aceiterosready.data.repository.UsuarioRepository
import com.example.aceiterosready.presentation.common.Roles
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private fun ApiResult<*>.adminMessage(): String = when (this) {
    is ApiResult.HttpError -> when (status) {
        409 -> error?.message?.takeIf { it.isNotBlank() }
            ?: "Ya existe un registro con esos datos."
        else -> userMessage()
    }
    else -> userMessage()
}

data class AdminUsuariosState(
    val loading: Boolean = true,
    val usuarios: List<UserDto> = emptyList(),
    val talleres: List<TallerDto> = emptyList(),
    val query: String = "",
    val rol: String? = null,
    val error: String? = null,
    val message: String? = null,
)

class AdminUsuariosViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUsuariosState())
    val state: StateFlow<AdminUsuariosState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }

            val usuarios = usuarioRepository.listUsuarios(
                rol = _state.value.rol,
                search = _state.value.query.trim().ifBlank { null },
            )

            when (usuarios) {
                is ApiResult.Success -> _state.update {
                    it.copy(loading = false, usuarios = usuarios.data)
                }
                else -> _state.update {
                    it.copy(loading = false, error = usuarios.adminMessage())
                }
            }

            val talleres = catalogRepository.listTalleres()
            if (talleres is ApiResult.Success) {
                _state.update { it.copy(talleres = talleres.data) }
            }
        }
    }

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            refreshUsuarios()
        }
    }

    fun onRolChange(rol: String?) {
        _state.update { it.copy(rol = rol) }
        refreshUsuarios()
    }

    private fun refreshUsuarios() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val result = usuarioRepository.listUsuarios(
                rol = _state.value.rol,
                search = _state.value.query.trim().ifBlank { null },
            )) {
                is ApiResult.Success -> _state.update {
                    it.copy(loading = false, usuarios = result.data)
                }
                else -> _state.update {
                    it.copy(loading = false, error = result.adminMessage())
                }
            }
        }
    }

    fun desactivar(id: String) {
        viewModelScope.launch {
            when (val result = usuarioRepository.deactivateUsuario(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(message = "Usuario desactivado.") }
                    refreshUsuarios()
                }
                else -> _state.update { it.copy(message = result.adminMessage()) }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}

data class UsuarioFormState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val usuarioId: String? = null,
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val telefono: String = "",
    val password: String = "",
    val rol: String = Roles.CLIENTE,
    val tallerId: String? = null,
    val activo: Boolean = true,
    val roles: List<String> = Roles.TODOS,
    val talleres: List<TallerDto> = emptyList(),
    val fieldErrors: Map<String, String> = emptyMap(),
    val error: String? = null,
    val saved: Boolean = false,
) {
    val editando: Boolean get() = usuarioId != null
    val requiereTaller: Boolean get() = Roles.requiereTaller(rol)
}

class UsuarioFormViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UsuarioFormState())
    val state: StateFlow<UsuarioFormState> = _state.asStateFlow()

    private var initialized = false

    fun load(id: String?) {
        if (initialized) return
        initialized = true

        _state.update { it.copy(loading = true, usuarioId = id) }

        viewModelScope.launch {
            val roles = usuarioRepository.listRoles()
            if (roles is ApiResult.Success) {
                _state.update { it.copy(roles = roles.data.map { rol -> rol.nombre }) }
            }

            val talleres = catalogRepository.listTalleres()
            if (talleres is ApiResult.Success) {
                _state.update { it.copy(talleres = talleres.data) }
            }

            if (id != null) {
                when (val result = usuarioRepository.getUsuario(id)) {
                    is ApiResult.Success -> {
                        val user = result.data
                        _state.update {
                            it.copy(
                                loading = false,
                                nombre = user.nombre,
                                apellido = user.apellido.orEmpty(),
                                email = user.email,
                                telefono = user.telefono.orEmpty(),
                                rol = user.rol,
                                tallerId = user.tallerId,
                                activo = user.activo,
                            )
                        }
                    }
                    else -> _state.update {
                        it.copy(loading = false, error = result.adminMessage())
                    }
                }
            } else {
                _state.update { it.copy(loading = false) }
            }
        }
    }

    fun setNombre(v: String) = _state.update { it.copy(nombre = v) }
    fun setApellido(v: String) = _state.update { it.copy(apellido = v) }
    fun setEmail(v: String) = _state.update { it.copy(email = v) }
    fun setTelefono(v: String) = _state.update { it.copy(telefono = v) }
    fun setPassword(v: String) = _state.update { it.copy(password = v) }
    fun setRol(v: String) = _state.update {
        it.copy(
            rol = v,
            tallerId = if (Roles.requiereTaller(v)) it.tallerId else it.tallerId,
        )
    }
    fun setTaller(v: String?) = _state.update { it.copy(tallerId = v) }
    fun setActivo(v: Boolean) = _state.update { it.copy(activo = v) }

    fun save() {
        val current = _state.value
        val fieldErrors = buildMap {
            if (current.nombre.isBlank()) put("nombre", "Ingresa el nombre.")
            if (current.email.isBlank()) put("email", "Ingresa el correo.")
            if (!current.editando && current.password.length !in 8..72) {
                put("password", "La contraseña debe tener entre 8 y 72 caracteres.")
            }
            if (current.requiereTaller && current.tallerId == null) {
                put("tallerId", "Selecciona un taller.")
            }
        }

        if (fieldErrors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = fieldErrors, error = null) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null, fieldErrors = emptyMap()) }

            val result: ApiResult<*>
            if (current.usuarioId == null) {
                result = usuarioRepository.createUsuario(
                    CreateUsuarioRequest(
                        nombre = current.nombre.trim(),
                        apellido = current.apellido.trim().ifBlank { null },
                        email = current.email.trim(),
                        password = current.password,
                        telefono = current.telefono.trim().ifBlank { null },
                        rol = current.rol,
                        tallerId = current.tallerId,
                    ),
                )
            } else {
                result = usuarioRepository.updateUsuario(
                    current.usuarioId,
                    UpdateUsuarioRequest(
                        nombre = current.nombre.trim(),
                        apellido = current.apellido.trim().ifBlank { null },
                        email = current.email.trim(),
                        telefono = current.telefono.trim().ifBlank { null },
                        rol = current.rol,
                        tallerId = current.tallerId,
                        activo = current.activo,
                    ),
                )
            }

            when (result) {
                is ApiResult.Success -> _state.update {
                    it.copy(saving = false, saved = true)
                }
                else -> _state.update {
                    it.copy(saving = false, error = result.adminMessage())
                }
            }
        }
    }
}

data class ServiciosAdminState(
    val loading: Boolean = true,
    val servicios: List<ServicioDto> = emptyList(),
    val query: String = "",
    val error: String? = null,
    val message: String? = null,
)

class ServiciosAdminViewModel(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ServiciosAdminState())
    val state: StateFlow<ServiciosAdminState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val result = catalogRepository.listServicios(
                search = _state.value.query.trim().ifBlank { null },
            )) {
                is ApiResult.Success -> _state.update {
                    it.copy(loading = false, servicios = result.data)
                }
                else -> _state.update {
                    it.copy(loading = false, error = result.adminMessage())
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

    fun desactivar(id: String) {
        viewModelScope.launch {
            when (val result = catalogRepository.deactivateServicio(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(message = "Servicio desactivado.") }
                    load()
                }
                else -> _state.update { it.copy(message = result.adminMessage()) }
            }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
}

data class ServicioFormState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val servicioId: String? = null,
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val precio: String = "",
    val duracionMin: String = "",
    val activo: Boolean = true,
    val fieldErrors: Map<String, String> = emptyMap(),
    val error: String? = null,
    val saved: Boolean = false,
) {
    val editando: Boolean get() = servicioId != null
}

class ServicioFormViewModel(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ServicioFormState())
    val state: StateFlow<ServicioFormState> = _state.asStateFlow()

    private var initialized = false

    fun load(id: String?) {
        if (initialized) return
        initialized = true
        _state.update { it.copy(loading = true, servicioId = id) }

        if (id == null) {
            _state.update { it.copy(loading = false) }
            return
        }

        viewModelScope.launch {
            when (val result = catalogRepository.getServicio(id)) {
                is ApiResult.Success -> {
                    val s = result.data
                    _state.update {
                        it.copy(
                            loading = false,
                            nombre = s.nombre,
                            descripcion = s.descripcion.orEmpty(),
                            categoria = s.categoria,
                            precio = s.precio.toString(),
                            duracionMin = s.duracionMin.toString(),
                            activo = s.activo,
                        )
                    }
                }
                else -> _state.update {
                    it.copy(loading = false, error = result.adminMessage())
                }
            }
        }
    }

    fun setNombre(v: String) = _state.update { it.copy(nombre = v) }
    fun setDescripcion(v: String) = _state.update { it.copy(descripcion = v) }
    fun setCategoria(v: String) = _state.update { it.copy(categoria = v) }
    fun setPrecio(v: String) = _state.update { it.copy(precio = v) }
    fun setDuracion(v: String) = _state.update { it.copy(duracionMin = v) }
    fun setActivo(v: Boolean) = _state.update { it.copy(activo = v) }

    fun save() {
        val current = _state.value
        val precio = current.precio.toDoubleOrNull()
        val duracion = current.duracionMin.toIntOrNull()

        val fieldErrors = buildMap {
            if (current.nombre.isBlank()) put("nombre", "Ingresa el nombre.")
            if (current.categoria.isBlank()) put("categoria", "Ingresa la categoría.")
            if (precio == null || precio < 0) put("precio", "Ingresa un precio válido.")
            if (duracion == null || duracion <= 0) {
                put("duracionMin", "Ingresa una duración en minutos.")
            }
        }

        if (fieldErrors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = fieldErrors, error = null) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null, fieldErrors = emptyMap()) }

            val result: ApiResult<*> = if (current.servicioId == null) {
                catalogRepository.createServicio(
                    CreateServicioRequest(
                        nombre = current.nombre.trim(),
                        descripcion = current.descripcion.trim().ifBlank { null },
                        categoria = current.categoria.trim(),
                        precio = precio!!,
                        duracionMin = duracion!!,
                    ),
                )
            } else {
                catalogRepository.updateServicio(
                    current.servicioId,
                    UpdateServicioRequest(
                        nombre = current.nombre.trim(),
                        descripcion = current.descripcion.trim().ifBlank { null },
                        categoria = current.categoria.trim(),
                        precio = precio,
                        duracionMin = duracion,
                    ),
                )
            }

            when (result) {
                is ApiResult.Success -> _state.update {
                    it.copy(saving = false, saved = true)
                }
                else -> _state.update {
                    it.copy(saving = false, error = result.adminMessage())
                }
            }
        }
    }
}

data class RefaccionesAdminState(
    val loading: Boolean = true,
    val refacciones: List<RefaccionDto> = emptyList(),
    val talleres: List<TallerDto> = emptyList(),
    val query: String = "",
    val tipo: String? = null,
    val error: String? = null,
    val message: String? = null,
)

class RefaccionesAdminViewModel(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RefaccionesAdminState())
    val state: StateFlow<RefaccionesAdminState> = _state.asStateFlow()

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
            )) {
                is ApiResult.Success -> _state.update {
                    it.copy(loading = false, refacciones = result.data)
                }
                else -> _state.update {
                    it.copy(loading = false, error = result.adminMessage())
                }
            }

            val talleres = catalogRepository.listTalleres()
            if (talleres is ApiResult.Success) {
                _state.update { it.copy(talleres = talleres.data) }
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

    fun desactivar(id: String) {
        viewModelScope.launch {
            when (val result = catalogRepository.deactivateRefaccion(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(message = "Refacción desactivada.") }
                    load()
                }
                else -> _state.update { it.copy(message = result.adminMessage()) }
            }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
}

data class RefaccionFormState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val refaccionId: String? = null,
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val marca: String = "",
    val tipo: String = "Refaccion",
    val stock: String = "0",
    val stockMinimo: String = "0",
    val precio: String = "",
    val tallerId: String? = null,
    val talleres: List<TallerDto> = emptyList(),
    val fieldErrors: Map<String, String> = emptyMap(),
    val error: String? = null,
    val saved: Boolean = false,
) {
    val editando: Boolean get() = refaccionId != null
}

class RefaccionFormViewModel(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RefaccionFormState())
    val state: StateFlow<RefaccionFormState> = _state.asStateFlow()

    private var initialized = false

    fun load(id: String?) {
        if (initialized) return
        initialized = true
        _state.update { it.copy(loading = true, refaccionId = id) }

        viewModelScope.launch {
            val talleres = catalogRepository.listTalleres()
            if (talleres is ApiResult.Success) {
                _state.update { it.copy(talleres = talleres.data) }
            }

            if (id == null) {
                _state.update {
                    it.copy(
                        loading = false,
                        tallerId = it.talleres.firstOrNull()?.id,
                    )
                }
                return@launch
            }

            when (val result = catalogRepository.getRefaccion(id)) {
                is ApiResult.Success -> {
                    val r = result.data
                    _state.update {
                        it.copy(
                            loading = false,
                            codigo = r.codigo,
                            nombre = r.nombre,
                            descripcion = r.descripcion.orEmpty(),
                            marca = r.marca.orEmpty(),
                            tipo = r.tipo,
                            stock = r.stock.toString(),
                            stockMinimo = r.stockMinimo.toString(),
                            precio = r.precio.toString(),
                            tallerId = r.tallerId,
                        )
                    }
                }
                else -> _state.update {
                    it.copy(loading = false, error = result.adminMessage())
                }
            }
        }
    }

    fun setCodigo(v: String) = _state.update { it.copy(codigo = v) }
    fun setNombre(v: String) = _state.update { it.copy(nombre = v) }
    fun setDescripcion(v: String) = _state.update { it.copy(descripcion = v) }
    fun setMarca(v: String) = _state.update { it.copy(marca = v) }
    fun setTipo(v: String) = _state.update { it.copy(tipo = v) }
    fun setStock(v: String) = _state.update { it.copy(stock = v) }
    fun setStockMinimo(v: String) = _state.update { it.copy(stockMinimo = v) }
    fun setPrecio(v: String) = _state.update { it.copy(precio = v) }
    fun setTaller(v: String?) = _state.update { it.copy(tallerId = v) }

    fun save() {
        val current = _state.value
        val stock = current.stock.toIntOrNull()
        val stockMinimo = current.stockMinimo.toIntOrNull()
        val precio = current.precio.toDoubleOrNull()

        val fieldErrors = buildMap {
            if (current.codigo.isBlank()) put("codigo", "Ingresa el código.")
            if (current.nombre.isBlank()) put("nombre", "Ingresa el nombre.")
            if (stock == null || stock < 0) put("stock", "Stock inválido.")
            if (stockMinimo == null || stockMinimo < 0) {
                put("stockMinimo", "Stock mínimo inválido.")
            }
            if (precio == null || precio < 0) put("precio", "Precio inválido.")
            if (current.tallerId == null) put("tallerId", "Selecciona un taller.")
        }

        if (fieldErrors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = fieldErrors, error = null) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null, fieldErrors = emptyMap()) }

            val result: ApiResult<*> = if (current.refaccionId == null) {
                catalogRepository.createRefaccion(
                    CreateRefaccionRequest(
                        codigo = current.codigo.trim(),
                        nombre = current.nombre.trim(),
                        descripcion = current.descripcion.trim().ifBlank { null },
                        marca = current.marca.trim().ifBlank { null },
                        tipo = current.tipo,
                        stock = stock!!,
                        stockMinimo = stockMinimo!!,
                        precio = precio!!,
                        tallerId = current.tallerId,
                    ),
                )
            } else {
                catalogRepository.updateRefaccion(
                    current.refaccionId,
                    UpdateRefaccionRequest(
                        codigo = current.codigo.trim(),
                        nombre = current.nombre.trim(),
                        descripcion = current.descripcion.trim().ifBlank { null },
                        marca = current.marca.trim().ifBlank { null },
                        tipo = current.tipo,
                        stock = stock,
                        stockMinimo = stockMinimo,
                        precio = precio,
                        tallerId = current.tallerId,
                    ),
                )
            }

            when (result) {
                is ApiResult.Success -> _state.update {
                    it.copy(saving = false, saved = true)
                }
                else -> _state.update {
                    it.copy(saving = false, error = result.adminMessage())
                }
            }
        }
    }
}
