package com.example.aceiterosready.presentation.recepcion

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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.aceiterosready.data.model.ClienteDto
import com.example.aceiterosready.data.model.VehiculoDto
import com.example.aceiterosready.di.appViewModel
import com.example.aceiterosready.presentation.common.ConfirmDialog
import com.example.aceiterosready.presentation.common.DetalleOrdenItem
import com.example.aceiterosready.presentation.common.EstadoOrdenChip
import com.example.aceiterosready.presentation.common.EstadosOrden
import com.example.aceiterosready.presentation.common.ErrorState
import com.example.aceiterosready.presentation.common.InlineMessage
import com.example.aceiterosready.presentation.common.InfoRow
import com.example.aceiterosready.presentation.common.ListState
import com.example.aceiterosready.presentation.common.LoadingState
import com.example.aceiterosready.presentation.common.OrdenResumenCard
import com.example.aceiterosready.presentation.common.PasswordField
import com.example.aceiterosready.presentation.common.ScreenScaffold
import com.example.aceiterosready.presentation.common.SelectorField
import com.example.aceiterosready.presentation.common.formatFecha
import com.example.aceiterosready.presentation.common.formatMoney
import com.example.aceiterosready.presentation.common.nombreCompleto

@Composable
fun ClientesScreen(
    onNuevo: () -> Unit,
    onEditar: (String) -> Unit,
) {
    val vm: ClientesViewModel = appViewModel {
        ClientesViewModel(it.usuarioRepository)
    }
    val state by vm.state.collectAsState()
    var showLink by remember { mutableStateOf(false) }

    LaunchedEffect(state.vinculado) {
        if (state.vinculado) {
            showLink = false
            vm.resetLink()
        }
    }

    ScreenScaffold(
        title = "Clientes",
        actions = {
            IconButton(onClick = { showLink = true }) {
                Icon(Icons.Filled.Link, contentDescription = "Vincular cliente")
            }
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
                    label = { Text("Buscar cliente") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                InlineMessage(state.message)
            }

            ListState(
                loading = state.loading,
                error = state.error,
                isEmpty = state.clientes.isEmpty(),
                onRetry = vm::load,
                emptyTitle = "Sin clientes",
                emptyMessage = "Registra un cliente nuevo o vincula uno existente.",
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.clientes, key = { it.id }) { cliente ->
                        ClienteCard(cliente = cliente, onClick = { onEditar(cliente.id) })
                    }
                }
            }
        }
    }

    if (showLink) {
        AlertDialog(
            onDismissRequest = {
                showLink = false
                vm.resetLink()
            },
            title = { Text("Vincular cliente existente") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Busca por correo o teléfono para vincular una cuenta existente a tu taller.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedTextField(
                        value = state.linkEmail,
                        onValueChange = vm::onLinkEmailChange,
                        label = { Text("Correo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.linkTelefono,
                        onValueChange = vm::onLinkTelefonoChange,
                        label = { Text("Teléfono") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = vm::buscarParaVincular,
                        enabled = !state.linkBuscando,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (state.linkBuscando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Buscar")
                        }
                    }
                    InlineMessage(state.linkError, isError = true)
                    state.linkCliente?.let { cliente ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    cliente.nombreCompleto(),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Text(
                                    cliente.telefono ?: "Sin teléfono",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = vm::vincular,
                                    enabled = !state.vinculando,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("Vincular al taller")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showLink = false
                    vm.resetLink()
                }) { Text("Cerrar") }
            },
        )
    }
}

@Composable
private fun ClienteCard(
    cliente: ClienteDto,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = cliente.nombreCompleto(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = cliente.telefono ?: "Sin teléfono registrado",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun ClienteFormScreen(
    clienteId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val vm: ClienteFormViewModel = appViewModel {
        ClienteFormViewModel(it.usuarioRepository)
    }
    val state by vm.state.collectAsState()

    LaunchedEffect(clienteId) { vm.load(clienteId) }
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    ScreenScaffold(
        title = if (state.editando) "Editar cliente" else "Nuevo cliente",
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
                value = state.telefono,
                onValueChange = vm::setTelefono,
                label = { Text("Teléfono (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (!state.editando) {
                OutlinedTextField(
                    value = state.email,
                    onValueChange = vm::setEmail,
                    label = { Text("Correo") },
                    singleLine = true,
                    isError = state.fieldErrors.containsKey("email"),
                    supportingText = state.fieldErrors["email"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                )
                PasswordField(
                    value = state.password,
                    onValueChange = vm::setPassword,
                    label = "Contraseña inicial",
                    isError = state.fieldErrors.containsKey("password"),
                    supportingText = state.fieldErrors["password"],
                )
            } else {
                Text(
                    text = "Puedes actualizar el nombre y el teléfono. El correo se gestiona desde administración.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

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
fun VehiculosScreen(
    onNuevo: () -> Unit,
    onEditar: (String) -> Unit,
) {
    val vm: VehiculosViewModel = appViewModel {
        VehiculosViewModel(it.catalogRepository, it.usuarioRepository)
    }
    val state by vm.state.collectAsState()

    ScreenScaffold(
        title = "Vehículos",
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
                    label = { Text("Buscar por placa, marca o modelo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                InlineMessage(state.message)
            }

            ListState(
                loading = state.loading,
                error = state.error,
                isEmpty = state.vehiculos.isEmpty(),
                onRetry = vm::load,
                emptyTitle = "Sin vehículos",
                emptyMessage = "Registra el primer vehículo del taller.",
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.vehiculos, key = { it.id }) { vehiculo ->
                        VehiculoCard(
                            vehiculo = vehiculo,
                            cliente = state.clientes.firstOrNull { it.id == vehiculo.clienteId },
                            onClick = { onEditar(vehiculo.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VehiculoCard(
    vehiculo: VehiculoDto,
    cliente: ClienteDto?,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = vehiculo.placa,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = vehiculo.anio.toString(),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Text(
                text = "${vehiculo.marca} ${vehiculo.modelo}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Cliente: ${cliente?.nombreCompleto() ?: "Sin asignar"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${vehiculo.kilometraje} km · ${vehiculo.color ?: "Sin color"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun VehiculoFormScreen(
    vehiculoId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val vm: VehiculoFormViewModel = appViewModel {
        VehiculoFormViewModel(it.catalogRepository, it.usuarioRepository)
    }
    val state by vm.state.collectAsState()

    LaunchedEffect(vehiculoId) { vm.load(vehiculoId) }
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    ScreenScaffold(
        title = if (state.editando) "Editar vehículo" else "Nuevo vehículo",
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
                value = state.placa,
                onValueChange = vm::setPlaca,
                label = { Text("Placa") },
                singleLine = true,
                isError = state.fieldErrors.containsKey("placa"),
                supportingText = state.fieldErrors["placa"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.vin,
                onValueChange = vm::setVin,
                label = { Text("VIN (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.marca,
                    onValueChange = vm::setMarca,
                    label = { Text("Marca") },
                    singleLine = true,
                    isError = state.fieldErrors.containsKey("marca"),
                    supportingText = state.fieldErrors["marca"]?.let { { Text(it) } },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.modelo,
                    onValueChange = vm::setModelo,
                    label = { Text("Modelo") },
                    singleLine = true,
                    isError = state.fieldErrors.containsKey("modelo"),
                    supportingText = state.fieldErrors["modelo"]?.let { { Text(it) } },
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.anio,
                    onValueChange = vm::setAnio,
                    label = { Text("Año") },
                    singleLine = true,
                    isError = state.fieldErrors.containsKey("anio"),
                    supportingText = state.fieldErrors["anio"]?.let { { Text(it) } },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.kilometraje,
                    onValueChange = vm::setKilometraje,
                    label = { Text("Kilometraje") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
            OutlinedTextField(
                value = state.color,
                onValueChange = vm::setColor,
                label = { Text("Color (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            SelectorField(
                label = "Cliente",
                options = state.clientes,
                selected = state.clientes.firstOrNull { it.id == state.clienteId },
                optionLabel = { it.nombreCompleto() },
                onSelect = { vm.setCliente(it.id) },
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
fun OrdenesScreen(
    onNueva: () -> Unit,
    onVer: (String) -> Unit,
) {
    val vm: OrdenesViewModel = appViewModel {
        OrdenesViewModel(it.ordenRepository)
    }
    val state by vm.state.collectAsState()

    ScreenScaffold(
        title = "Órdenes",
        actions = {
            IconButton(onClick = vm::load) {
                Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNueva,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nueva") },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.estado == null,
                    onClick = { vm.onEstadoChange(null) },
                    label = { Text("Todas") },
                )
                EstadosOrden.TODOS.forEach { estado ->
                    FilterChip(
                        selected = state.estado == estado,
                        onClick = { vm.onEstadoChange(estado) },
                        label = { Text(EstadosOrden.label(estado)) },
                    )
                }
            }

            InlineMessage(
                state.message,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            ListState(
                loading = state.loading,
                error = state.error,
                isEmpty = state.ordenes.isEmpty(),
                onRetry = vm::load,
                emptyTitle = "Sin órdenes",
                emptyMessage = "Crea la primera orden de mantenimiento.",
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.ordenes, key = { it.id }) { orden ->
                        OrdenResumenCard(
                            orden = orden,
                            onClick = { onVer(orden.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrdenFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val vm: OrdenFormViewModel = appViewModel {
        OrdenFormViewModel(it.ordenRepository, it.catalogRepository, it.usuarioRepository)
    }
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.load() }
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    ScreenScaffold(
        title = "Nueva orden",
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

            SelectorField(
                label = "Vehículo",
                options = state.vehiculos,
                selected = state.vehiculos.firstOrNull { it.id == state.vehiculoId },
                optionLabel = { "${it.marca} ${it.modelo} · ${it.placa}" },
                onSelect = { vm.setVehiculo(it.id) },
            )
            state.fieldErrors["vehiculoId"]?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            SelectorField(
                label = "Cliente (opcional)",
                options = state.clientes,
                selected = state.clientes.firstOrNull { it.id == state.clienteId },
                optionLabel = { it.nombreCompleto() },
                onSelect = { vm.setCliente(it.id) },
            )

            SelectorField(
                label = "Mecánico",
                options = state.mecanicos,
                selected = state.mecanicos.firstOrNull { it.id == state.mecanicoId },
                optionLabel = { "${it.nombreCompleto()} · ${it.email}" },
                onSelect = { vm.setMecanico(it.id) },
                enabled = state.mecanicos.isNotEmpty(),
            )
            state.fieldErrors["mecanicoId"]?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            if (state.mecanicos.isEmpty()) {
                InlineMessage(
                    "No hay mecánicos activos en tu taller. No se puede crear la orden sin asignar un mecánico.",
                    isError = true,
                )
            }

            OutlinedTextField(
                value = state.diagnostico,
                onValueChange = vm::setDiagnostico,
                label = { Text("Diagnóstico inicial (opcional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = vm::save,
                enabled = !state.saving && state.mecanicos.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Crear orden")
                }
            }
        }
    }
}

@Composable
fun OrdenDetalleScreen(
    ordenId: String,
    onBack: () -> Unit,
) {
    val vm: OrdenDetalleViewModel = appViewModel {
        OrdenDetalleViewModel(it.ordenRepository, it.usuarioRepository)
    }
    val state by vm.state.collectAsState()
    var showConfirmarEntrega by remember { mutableStateOf(false) }

    LaunchedEffect(ordenId) { vm.load(ordenId) }

    ScreenScaffold(
        title = state.orden?.folio ?: "Orden",
        onBack = onBack,
        actions = {
            IconButton(onClick = vm::refresh) {
                Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
            }
        },
    ) { padding ->
        val orden = state.orden

        when {
            state.loading && orden == null -> LoadingState(Modifier.padding(padding))
            orden == null -> ErrorState(
                message = state.error ?: "No se pudo cargar la orden.",
                onRetry = vm::refresh,
                modifier = Modifier.padding(padding),
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                InlineMessage(state.message)

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = orden.folio,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                            )
                            EstadoOrdenChip(orden.estado)
                        }
                        InfoRow("Vehículo", orden.vehiculo?.let {
                            "${it.marca} ${it.modelo} · ${it.placa} (${it.anio})"
                        })
                        InfoRow("Cliente", orden.cliente?.nombreCompleto())
                        InfoRow("Mecánico", orden.mecanico?.nombreCompleto() ?: "Sin asignar")
                        InfoRow("Taller", orden.taller?.nombre)
                        InfoRow("Recepción", formatFecha(orden.fechaRecepcion))
                        InfoRow("Entrega", formatFecha(orden.fechaEntrega))
                        InfoRow("Diagnóstico", orden.diagnostico)
                        InfoRow("Total", formatMoney(orden.total))
                        if (orden.confirmacionCanal != null) {
                            InfoRow(
                                "Confirmación",
                                "${orden.confirmacionCanal} · ${formatFecha(orden.confirmacionClienteEn)}",
                            )
                        }
                    }
                }

                if (
                    orden.estado != EstadosOrden.ENTREGADO &&
                    orden.estado != EstadosOrden.CANCELADO
                ) {
                    SectionTitle("Asignación")
                    SelectorField(
                        label = "Mecánico asignado",
                        options = state.mecanicos,
                        selected = state.mecanicos.firstOrNull { it.id == orden.mecanico?.id },
                        optionLabel = { "${it.nombreCompleto()} · ${it.email}" },
                        onSelect = { vm.asignarMecanico(it.id) },
                        enabled = !state.saving && state.mecanicos.isNotEmpty(),
                    )
                }

                SectionTitle("Operaciones (${state.detalles.size})")
                if (state.detalles.isEmpty()) {
                    Text(
                        "Esta orden todavía no tiene servicios ni refacciones capturadas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    state.detalles.forEach { detalle ->
                        DetalleOrdenItem(detalle = detalle)
                    }
                }

                if (orden.estado == EstadosOrden.LISTO_ENTREGA) {
                    Button(
                        onClick = { showConfirmarEntrega = true },
                        enabled = !state.saving,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Confirmar entrega al cliente")
                    }
                }
            }
        }
    }

    if (showConfirmarEntrega) {
        ConfirmDialog(
            title = "Confirmar entrega",
            text = "La orden pasará a estado Entregado. Esta acción no se puede deshacer.",
            confirmText = "Confirmar",
            onConfirm = {
                vm.confirmarEntrega()
                showConfirmarEntrega = false
            },
            onDismiss = { showConfirmarEntrega = false },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}
