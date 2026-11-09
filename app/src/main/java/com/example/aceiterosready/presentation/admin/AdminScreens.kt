package com.example.aceiterosready.presentation.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.aceiterosready.data.model.RefaccionDto
import com.example.aceiterosready.data.model.ServicioDto
import com.example.aceiterosready.data.model.UserDto
import com.example.aceiterosready.di.appViewModel
import com.example.aceiterosready.presentation.common.ConfirmDialog
import com.example.aceiterosready.presentation.common.InlineMessage
import com.example.aceiterosready.presentation.common.InfoRow
import com.example.aceiterosready.presentation.common.ListState
import com.example.aceiterosready.presentation.common.LoadingState
import com.example.aceiterosready.presentation.common.PasswordField
import com.example.aceiterosready.presentation.common.RolChip
import com.example.aceiterosready.presentation.common.Roles
import com.example.aceiterosready.presentation.common.ScreenScaffold
import com.example.aceiterosready.presentation.common.SelectorField
import com.example.aceiterosready.presentation.common.formatMoney
import com.example.aceiterosready.presentation.common.nombreCompleto

@Composable
fun AdminUsuariosScreen(
    onNuevo: () -> Unit,
    onEditar: (String) -> Unit,
) {
    val vm: AdminUsuariosViewModel = appViewModel {
        AdminUsuariosViewModel(it.usuarioRepository, it.catalogRepository)
    }
    val state by vm.state.collectAsState()
    var usuarioAEliminar by remember { mutableStateOf<UserDto?>(null) }

    ScreenScaffold(
        title = "Usuarios",
        actions = {
            IconButton(onClick = vm::load) {
                Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevo,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuevo") },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = vm::onQueryChange,
                    label = { Text("Buscar por nombre o correo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = state.rol == null,
                        onClick = { vm.onRolChange(null) },
                        label = { Text("Todos") },
                    )
                    Roles.TODOS.forEach { rol ->
                        FilterChip(
                            selected = state.rol == rol,
                            onClick = { vm.onRolChange(rol) },
                            label = { Text(Roles.label(rol)) },
                        )
                    }
                }

                InlineMessage(state.message)
            }

            ListState(
                loading = state.loading,
                error = state.error,
                isEmpty = state.usuarios.isEmpty(),
                onRetry = vm::load,
                emptyTitle = "Sin usuarios",
                emptyMessage = "No hay usuarios para el filtro seleccionado.",
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.usuarios, key = { it.id }) { user ->
                        UsuarioCard(
                            user = user,
                            taller = state.talleres.firstOrNull { it.id == user.tallerId }?.nombre,
                            onClick = { onEditar(user.id) },
                            onDesactivar = { usuarioAEliminar = user },
                        )
                    }
                }
            }
        }
    }

    usuarioAEliminar?.let { user ->
        ConfirmDialog(
            title = "Desactivar usuario",
            text = "¿Deseas desactivar a ${user.nombreCompleto()}? No podrá iniciar sesión.",
            confirmText = "Desactivar",
            onConfirm = {
                vm.desactivar(user.id)
                usuarioAEliminar = null
            },
            onDismiss = { usuarioAEliminar = null },
        )
    }
}

@Composable
private fun UsuarioCard(
    user: UserDto,
    taller: String?,
    onClick: () -> Unit,
    onDesactivar: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.nombreCompleto(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                RolChip(user.rol)
            }
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Taller: ${taller ?: "Sin asignar"} · ${if (user.activo) "Activo" else "Inactivo"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (user.activo) {
                TextButton(
                    onClick = onDesactivar,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Desactivar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun UsuarioFormScreen(
    usuarioId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val vm: UsuarioFormViewModel = appViewModel {
        UsuarioFormViewModel(it.usuarioRepository, it.catalogRepository)
    }
    val state by vm.state.collectAsState()

    LaunchedEffect(usuarioId) { vm.load(usuarioId) }
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    ScreenScaffold(
        title = if (state.editando) "Editar usuario" else "Nuevo usuario",
        onBack = onBack,
    ) { padding ->
        if (state.loading) {
            LoadingState(modifier = Modifier.padding(padding))
            return@ScreenScaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InlineMessage(state.error, isError = true)

            OutlinedTextField(
                value = state.nombre,
                onValueChange = vm::setNombre,
                label = { Text("Nombre") },
                singleLine = true,
                isError = state.fieldErrors.containsKey("nombre"),
                supportingText = state.fieldErrors["nombre"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.apellido,
                onValueChange = vm::setApellido,
                label = { Text("Apellido (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.email,
                onValueChange = vm::setEmail,
                label = { Text("Correo") },
                singleLine = true,
                isError = state.fieldErrors.containsKey("email"),
                supportingText = state.fieldErrors["email"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.telefono,
                onValueChange = vm::setTelefono,
                label = { Text("Teléfono (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (!state.editando) {
                PasswordField(
                    value = state.password,
                    onValueChange = vm::setPassword,
                    label = "Contraseña",
                    isError = state.fieldErrors.containsKey("password"),
                    supportingText = state.fieldErrors["password"],
                )
            }

            SelectorField(
                label = "Rol",
                options = state.roles,
                selected = state.rol,
                optionLabel = { Roles.label(it) },
                onSelect = vm::setRol,
            )

            SelectorField(
                label = if (state.requiereTaller) "Taller (obligatorio)" else "Taller (opcional)",
                options = state.talleres,
                selected = state.talleres.firstOrNull { it.id == state.tallerId },
                optionLabel = { it.nombre },
                onSelect = { vm.setTaller(it.id) },
            )
            state.fieldErrors["tallerId"]?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            if (state.editando) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Activo", modifier = Modifier.weight(1f))
                    Switch(checked = state.activo, onCheckedChange = vm::setActivo)
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = vm::save,
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Guardar")
                }
            }
        }
    }
}

@Composable
fun ServiciosAdminScreen(
    onNuevo: () -> Unit,
    onEditar: (String) -> Unit,
) {
    val vm: ServiciosAdminViewModel = appViewModel {
        ServiciosAdminViewModel(it.catalogRepository)
    }
    val state by vm.state.collectAsState()
    var servicioAEliminar by remember { mutableStateOf<ServicioDto?>(null) }

    ScreenScaffold(
        title = "Servicios",
        actions = {
            IconButton(onClick = vm::load) {
                Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevo,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuevo") },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = vm::onQueryChange,
                    label = { Text("Buscar servicio") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                InlineMessage(state.message)
            }

            ListState(
                loading = state.loading,
                error = state.error,
                isEmpty = state.servicios.isEmpty(),
                onRetry = vm::load,
                emptyTitle = "Sin servicios",
                emptyMessage = "Agrega el primer servicio del catálogo.",
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.servicios, key = { it.id }) { servicio ->
                        ServicioCard(
                            servicio = servicio,
                            onClick = { onEditar(servicio.id) },
                            onDesactivar = { servicioAEliminar = servicio },
                        )
                    }
                }
            }
        }
    }

    servicioAEliminar?.let { servicio ->
        ConfirmDialog(
            title = "Desactivar servicio",
            text = "¿Deseas desactivar \"${servicio.nombre}\"?",
            confirmText = "Desactivar",
            onConfirm = {
                vm.desactivar(servicio.id)
                servicioAEliminar = null
            },
            onDismiss = { servicioAEliminar = null },
        )
    }
}

@Composable
private fun ServicioCard(
    servicio: ServicioDto,
    onClick: () -> Unit,
    onDesactivar: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = servicio.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = if (servicio.activo) "Activo" else "Inactivo",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (servicio.activo) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
            Text(
                text = "${servicio.categoria} · ${servicio.duracionMin} min",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatMoney(servicio.precio),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            servicio.descripcion?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (servicio.activo) {
                TextButton(
                    onClick = onDesactivar,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Desactivar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun ServicioFormScreen(
    servicioId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val vm: ServicioFormViewModel = appViewModel {
        ServicioFormViewModel(it.catalogRepository)
    }
    val state by vm.state.collectAsState()

    LaunchedEffect(servicioId) { vm.load(servicioId) }
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    ScreenScaffold(
        title = if (state.editando) "Editar servicio" else "Nuevo servicio",
        onBack = onBack,
    ) { padding ->
        if (state.loading) {
            LoadingState(modifier = Modifier.padding(padding))
            return@ScreenScaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InlineMessage(state.error, isError = true)

            OutlinedTextField(
                value = state.nombre,
                onValueChange = vm::setNombre,
                label = { Text("Nombre") },
                singleLine = true,
                isError = state.fieldErrors.containsKey("nombre"),
                supportingText = state.fieldErrors["nombre"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.categoria,
                onValueChange = vm::setCategoria,
                label = { Text("Categoría") },
                singleLine = true,
                isError = state.fieldErrors.containsKey("categoria"),
                supportingText = state.fieldErrors["categoria"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.descripcion,
                onValueChange = vm::setDescripcion,
                label = { Text("Descripción (opcional)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.precio,
                onValueChange = vm::setPrecio,
                label = { Text("Precio") },
                singleLine = true,
                isError = state.fieldErrors.containsKey("precio"),
                supportingText = state.fieldErrors["precio"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.duracionMin,
                onValueChange = vm::setDuracion,
                label = { Text("Duración (minutos)") },
                singleLine = true,
                isError = state.fieldErrors.containsKey("duracionMin"),
                supportingText = state.fieldErrors["duracionMin"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = vm::save,
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Guardar")
                }
            }
        }
    }
}

@Composable
fun RefaccionesAdminScreen(
    onNuevo: () -> Unit,
    onEditar: (String) -> Unit,
) {
    val vm: RefaccionesAdminViewModel = appViewModel {
        RefaccionesAdminViewModel(it.catalogRepository)
    }
    val state by vm.state.collectAsState()
    var refaccionAEliminar by remember { mutableStateOf<RefaccionDto?>(null) }

    ScreenScaffold(
        title = "Refacciones",
        actions = {
            IconButton(onClick = vm::load) {
                Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevo,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuevo") },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = vm::onQueryChange,
                    label = { Text("Buscar por código o nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.tipo == null,
                        onClick = { vm.onTipoChange(null) },
                        label = { Text("Todas") },
                    )
                    FilterChip(
                        selected = state.tipo == "Refaccion",
                        onClick = { vm.onTipoChange("Refaccion") },
                        label = { Text("Refacciones") },
                    )
                    FilterChip(
                        selected = state.tipo == "Consumible",
                        onClick = { vm.onTipoChange("Consumible") },
                        label = { Text("Consumibles") },
                    )
                }
                InlineMessage(state.message)
            }

            ListState(
                loading = state.loading,
                error = state.error,
                isEmpty = state.refacciones.isEmpty(),
                onRetry = vm::load,
                emptyTitle = "Sin refacciones",
                emptyMessage = "Agrega la primera refacción del catálogo.",
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.refacciones, key = { it.id }) { refaccion ->
                        RefaccionAdminCard(
                            refaccion = refaccion,
                            taller = state.talleres.firstOrNull { it.id == refaccion.tallerId }?.nombre,
                            onClick = { onEditar(refaccion.id) },
                            onDesactivar = { refaccionAEliminar = refaccion },
                        )
                    }
                }
            }
        }
    }

    refaccionAEliminar?.let { refaccion ->
        ConfirmDialog(
            title = "Desactivar refacción",
            text = "¿Deseas desactivar \"${refaccion.nombre}\"?",
            confirmText = "Desactivar",
            onConfirm = {
                vm.desactivar(refaccion.id)
                refaccionAEliminar = null
            },
            onDismiss = { refaccionAEliminar = null },
        )
    }
}

@Composable
private fun RefaccionAdminCard(
    refaccion: RefaccionDto,
    taller: String?,
    onClick: () -> Unit,
    onDesactivar: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = refaccion.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = refaccion.codigo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = refaccion.tipo,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = "Stock: ${refaccion.stock} · Mínimo: ${refaccion.stockMinimo}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (refaccion.stock <= refaccion.stockMinimo) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            Text(
                text = formatMoney(refaccion.precio),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = (refaccion.marca ?: "Sin marca") + " · " + (taller ?: "Sin taller"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (refaccion.activo) {
                TextButton(
                    onClick = onDesactivar,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Desactivar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun RefaccionFormScreen(
    refaccionId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val vm: RefaccionFormViewModel = appViewModel {
        RefaccionFormViewModel(it.catalogRepository)
    }
    val state by vm.state.collectAsState()

    LaunchedEffect(refaccionId) { vm.load(refaccionId) }
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    ScreenScaffold(
        title = if (state.editando) "Editar refacción" else "Nueva refacción",
        onBack = onBack,
    ) { padding ->
        if (state.loading) {
            LoadingState(modifier = Modifier.padding(padding))
            return@ScreenScaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InlineMessage(state.error, isError = true)

            OutlinedTextField(
                value = state.codigo,
                onValueChange = vm::setCodigo,
                label = { Text("Código") },
                singleLine = true,
                isError = state.fieldErrors.containsKey("codigo"),
                supportingText = state.fieldErrors["codigo"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.nombre,
                onValueChange = vm::setNombre,
                label = { Text("Nombre") },
                singleLine = true,
                isError = state.fieldErrors.containsKey("nombre"),
                supportingText = state.fieldErrors["nombre"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.descripcion,
                onValueChange = vm::setDescripcion,
                label = { Text("Descripción (opcional)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.marca,
                onValueChange = vm::setMarca,
                label = { Text("Marca (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            SelectorField(
                label = "Tipo",
                options = listOf("Refaccion", "Consumible"),
                selected = state.tipo,
                optionLabel = { if (it == "Refaccion") "Refacción" else "Consumible" },
                onSelect = vm::setTipo,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.stock,
                    onValueChange = vm::setStock,
                    label = { Text("Stock") },
                    singleLine = true,
                    isError = state.fieldErrors.containsKey("stock"),
                    supportingText = state.fieldErrors["stock"]?.let { { Text(it) } },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.stockMinimo,
                    onValueChange = vm::setStockMinimo,
                    label = { Text("Stock mínimo") },
                    singleLine = true,
                    isError = state.fieldErrors.containsKey("stockMinimo"),
                    supportingText = state.fieldErrors["stockMinimo"]?.let { { Text(it) } },
                    modifier = Modifier.weight(1f),
                )
            }
            OutlinedTextField(
                value = state.precio,
                onValueChange = vm::setPrecio,
                label = { Text("Precio") },
                singleLine = true,
                isError = state.fieldErrors.containsKey("precio"),
                supportingText = state.fieldErrors["precio"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
            SelectorField(
                label = "Taller",
                options = state.talleres,
                selected = state.talleres.firstOrNull { it.id == state.tallerId },
                optionLabel = { it.nombre },
                onSelect = { vm.setTaller(it.id) },
            )

            Button(
                onClick = vm::save,
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Guardar")
                }
            }
        }
    }
}
