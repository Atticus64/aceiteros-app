package com.example.aceiterosready.presentation.mecanico

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.example.aceiterosready.presentation.common.ScreenScaffold
import com.example.aceiterosready.presentation.common.SelectorField
import com.example.aceiterosready.presentation.common.formatFecha
import com.example.aceiterosready.presentation.common.formatMoney
import com.example.aceiterosready.presentation.common.nombreCompleto

@Composable
fun MecanicoOrdenesScreen(
    onVer: (String) -> Unit,
) {
    val vm: MecanicoOrdenesViewModel = appViewModel {
        MecanicoOrdenesViewModel(it.ordenRepository)
    }
    val state by vm.state.collectAsState()

    ScreenScaffold(
        title = "Mis mantenimientos",
        actions = {
            IconButton(onClick = vm::load) {
                Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            ListState(
                loading = state.loading,
                error = state.error,
                isEmpty = state.ordenes.isEmpty(),
                onRetry = vm::load,
                emptyTitle = "Sin mantenimientos asignados",
                emptyMessage = "Cuando recepción te asigne una orden aparecerá aquí.",
            ) {
                val enTrabajo = state.ordenes.filter {
                    it.estado in setOf(
                        EstadosOrden.DIAGNOSTICO,
                        EstadosOrden.EN_REPARACION,
                        EstadosOrden.CONTROL_CALIDAD,
                    )
                }
                val listos = state.ordenes.filter { it.estado == EstadosOrden.LISTO_ENTREGA }
                val historial = state.ordenes.filter {
                    it.estado == EstadosOrden.ENTREGADO || it.estado == EstadosOrden.CANCELADO
                }
                val recepcion = state.ordenes.filter { it.estado == EstadosOrden.RECEPCION }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (recepcion.isNotEmpty()) {
                        item { GrupoTitulo("Recién recibidas") }
                        items(recepcion, key = { it.id }) { orden ->
                            OrdenResumenCard(orden = orden, onClick = { onVer(orden.id) })
                        }
                    }
                    if (enTrabajo.isNotEmpty()) {
                        item { GrupoTitulo("En trabajo") }
                        items(enTrabajo, key = { it.id }) { orden ->
                            OrdenResumenCard(orden = orden, onClick = { onVer(orden.id) })
                        }
                    }
                    if (listos.isNotEmpty()) {
                        item { GrupoTitulo("Listos para entrega") }
                        items(listos, key = { it.id }) { orden ->
                            OrdenResumenCard(orden = orden, onClick = { onVer(orden.id) })
                        }
                    }
                    if (historial.isNotEmpty()) {
                        item { GrupoTitulo("Historial") }
                        items(historial, key = { it.id }) { orden ->
                            OrdenResumenCard(orden = orden, onClick = { onVer(orden.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GrupoTitulo(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 6.dp),
    )
}

@Composable
fun OrdenTrabajoScreen(
    ordenId: String,
    onBack: () -> Unit,
) {
    val vm: OrdenTrabajoViewModel = appViewModel {
        OrdenTrabajoViewModel(it.ordenRepository, it.catalogRepository)
    }
    val state by vm.state.collectAsState()

    LaunchedEffect(ordenId) { vm.load(ordenId) }

    ScreenScaffold(
        title = state.orden?.folio ?: "Orden de trabajo",
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
            else -> {
                val cerrada = orden.estado == EstadosOrden.ENTREGADO ||
                    orden.estado == EstadosOrden.CANCELADO
                val detallesEditables = orden.estado in EstadosOrden.EDITABLES_DETALLE

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
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
                            InfoRow("Taller", orden.taller?.nombre)
                            InfoRow("Recepción", formatFecha(orden.fechaRecepcion))
                            InfoRow("Total", formatMoney(orden.total))
                        }
                    }

                    Text(
                        text = "Diagnóstico",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    OutlinedTextField(
                        value = state.diagnostico,
                        onValueChange = vm::setDiagnostico,
                        label = { Text("Diagnóstico de la orden") },
                        minLines = 3,
                        enabled = !cerrada,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (!cerrada) {
                        Button(
                            onClick = vm::guardarDiagnostico,
                            enabled = !state.saving,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Guardar diagnóstico")
                        }
                    }

                    Text(
                        text = "Estado del flujo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        EstadosOrden.FLUJO_MECANICO.forEach { estado ->
                            FilterChip(
                                selected = orden.estado == estado,
                                onClick = { vm.cambiarEstado(estado) },
                                enabled = !state.saving && !cerrada,
                                label = { Text(EstadosOrden.label(estado)) },
                            )
                        }
                    }

                    if (cerrada) {
                        InlineMessage(
                            "La orden está cerrada y ya no admite cambios.",
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Operaciones (${state.detalles.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        if (detallesEditables) {
                            TextButton(onClick = vm::abrirNuevoDetalle) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.size(4.dp))
                                Text("Agregar")
                            }
                        }
                    }

                    if (!detallesEditables && !cerrada) {
                        InlineMessage(
                            "Las operaciones ya no se pueden modificar en este estado.",
                        )
                    }

                    if (state.detalles.isEmpty()) {
                        Text(
                            text = "Aún no hay servicios ni refacciones en esta orden.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        state.detalles.forEach { detalle ->
                            DetalleOrdenItem(detalle = detalle) {
                                if (detallesEditables) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                    ) {
                                        TextButton(onClick = { vm.abrirEditarDetalle(detalle) }) {
                                            Text("Editar")
                                        }
                                        TextButton(
                                            onClick = { vm.pedirEliminarDetalle(detalle) },
                                        ) {
                                            Text(
                                                "Eliminar",
                                                color = MaterialTheme.colorScheme.error,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    state.detalleForm?.let { form ->
        AlertDialog(
            onDismissRequest = vm::cerrarDetalle,
            title = {
                Text(if (form.editando) "Editar operación" else "Nueva operación")
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (!form.editando) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = form.tipo == "Servicio",
                                onClick = { vm.setDetalleTipo("Servicio") },
                                label = { Text("Servicio") },
                            )
                            FilterChip(
                                selected = form.tipo == "Refaccion",
                                onClick = { vm.setDetalleTipo("Refaccion") },
                                label = { Text("Refacción") },
                            )
                        }

                        if (form.tipo == "Servicio") {
                            SelectorField(
                                label = "Servicio",
                                options = state.servicios,
                                selected = state.servicios.firstOrNull { it.id == form.servicioId },
                                optionLabel = { "${it.nombre} · ${formatMoney(it.precio)}" },
                                onSelect = { vm.setDetalleServicio(it.id) },
                            )
                        } else {
                            SelectorField(
                                label = "Refacción",
                                options = state.refacciones,
                                selected = state.refacciones.firstOrNull {
                                    it.id == form.refaccionId
                                },
                                optionLabel = {
                                    "${it.codigo} · ${it.nombre} (stock ${it.stock})"
                                },
                                onSelect = { vm.setDetalleRefaccion(it.id) },
                            )
                        }
                    } else {
                        Text(
                            text = "Concepto: " + (
                                state.detalles.firstOrNull { it.id == form.detalleId }?.let {
                                    it.servicio?.nombre ?: it.refaccion?.nombre
                                } ?: "—"
                                ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    OutlinedTextField(
                        value = form.descripcion,
                        onValueChange = vm::setDetalleDescripcion,
                        label = { Text("Descripción (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = form.cantidad,
                        onValueChange = vm::setDetalleCantidad,
                        label = { Text("Cantidad") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = form.precioUnitario,
                        onValueChange = vm::setDetallePrecio,
                        label = { Text("Precio unitario") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    InlineMessage(form.error, isError = true)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = vm::guardarDetalle,
                    enabled = !state.saving,
                ) {
                    if (state.saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Guardar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = vm::cerrarDetalle) { Text("Cancelar") }
            },
        )
    }

    state.detalleAEliminar?.let { detalle ->
        ConfirmDialog(
            title = "Eliminar operación",
            text = "Se eliminará \"${detalle.servicio?.nombre ?: detalle.refaccion?.nombre ?: detalle.descripcion}\". Si es una refacción, su stock se devolverá al inventario.",
            confirmText = "Eliminar",
            onConfirm = vm::eliminarDetalle,
            onDismiss = vm::cancelarEliminarDetalle,
        )
    }
}
