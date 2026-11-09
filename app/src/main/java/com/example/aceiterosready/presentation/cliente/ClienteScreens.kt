package com.example.aceiterosready.presentation.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aceiterosready.data.model.VehiculoDto
import com.example.aceiterosready.di.appViewModel
import com.example.aceiterosready.presentation.common.DetalleOrdenItem
import com.example.aceiterosready.presentation.common.EstadoOrdenChip
import com.example.aceiterosready.presentation.common.EstadosOrden
import com.example.aceiterosready.presentation.common.ErrorState
import com.example.aceiterosready.presentation.common.InfoRow
import com.example.aceiterosready.presentation.common.ListState
import com.example.aceiterosready.presentation.common.LoadingState
import com.example.aceiterosready.presentation.common.OrdenResumenCard
import com.example.aceiterosready.presentation.common.ScreenScaffold
import com.example.aceiterosready.presentation.common.formatFecha
import com.example.aceiterosready.presentation.common.formatMoney
import com.example.aceiterosready.presentation.common.nombreCompleto

@Composable
fun MisVehiculosScreen(
    onVer: (String) -> Unit,
) {
    val vm: MisVehiculosViewModel = appViewModel {
        MisVehiculosViewModel(it.catalogRepository)
    }
    val state by vm.state.collectAsState()

    ScreenScaffold(
        title = "Mis vehículos",
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
                isEmpty = state.vehiculos.isEmpty(),
                onRetry = vm::load,
                emptyTitle = "Sin vehículos registrados",
                emptyMessage = "El taller vinculará tus vehículos cuando registres una orden.",
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.vehiculos, key = { it.id }) { vehiculo ->
                        VehiculoClienteCard(
                            vehiculo = vehiculo,
                            onClick = { onVer(vehiculo.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VehiculoClienteCard(
    vehiculo: VehiculoDto,
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
                text = "${vehiculo.kilometraje} km · ${vehiculo.color ?: "Sin color"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun MiVehiculoDetalleScreen(
    vehiculoId: String,
    onBack: () -> Unit,
    onVerOrden: (String) -> Unit,
) {
    val vm: MiVehiculoDetalleViewModel = appViewModel {
        MiVehiculoDetalleViewModel(it.catalogRepository, it.ordenRepository)
    }
    val state by vm.state.collectAsState()

    LaunchedEffect(vehiculoId) { vm.load(vehiculoId) }

    ScreenScaffold(
        title = state.vehiculo?.placa ?: "Vehículo",
        onBack = onBack,
        actions = {
            IconButton(onClick = vm::refresh) {
                Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
            }
        },
    ) { padding ->
        val vehiculo = state.vehiculo

        when {
            state.loading && vehiculo == null -> LoadingState(Modifier.padding(padding))
            vehiculo == null -> ErrorState(
                message = state.error ?: "No se pudo cargar el vehículo.",
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
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "${vehiculo.marca} ${vehiculo.modelo}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        InfoRow("Placa", vehiculo.placa)
                        InfoRow("VIN", vehiculo.vin)
                        InfoRow("Año", vehiculo.anio.toString())
                        InfoRow("Color", vehiculo.color)
                        InfoRow("Kilometraje", "${vehiculo.kilometraje} km")
                    }
                }

                Text(
                    text = "Historial de órdenes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                if (state.ordenes.isEmpty()) {
                    Text(
                        text = "Este vehículo todavía no tiene órdenes registradas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    state.ordenes.forEach { orden ->
                        OrdenResumenCard(
                            orden = orden,
                            onClick = { onVerOrden(orden.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MisOrdenesScreen(
    onVer: (String) -> Unit,
) {
    val vm: MisOrdenesViewModel = appViewModel {
        MisOrdenesViewModel(it.ordenRepository)
    }
    val state by vm.state.collectAsState()

    ScreenScaffold(
        title = "Mis órdenes",
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
                emptyTitle = "Sin órdenes",
                emptyMessage = "Aquí verás el seguimiento de tus mantenimientos.",
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
fun MiOrdenDetalleScreen(
    ordenId: String,
    onBack: () -> Unit,
) {
    val vm: MiOrdenDetalleViewModel = appViewModel {
        MiOrdenDetalleViewModel(it.ordenRepository)
    }
    val state by vm.state.collectAsState()

    LaunchedEffect(ordenId) { vm.load(ordenId) }

    ScreenScaffold(
        title = state.orden?.folio ?: "Mi orden",
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
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
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
                            "${it.marca} ${it.modelo} · ${it.placa}"
                        })
                        InfoRow("Taller", orden.taller?.nombre)
                        InfoRow("Mecánico", orden.mecanico?.nombreCompleto())
                        InfoRow("Recepción", formatFecha(orden.fechaRecepcion))
                        InfoRow("Entrega", formatFecha(orden.fechaEntrega))
                        InfoRow("Diagnóstico", orden.diagnostico)
                        InfoRow("Total", formatMoney(orden.total))
                    }
                }

                Text(
                    text = "Seguimiento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                TimelineOrden(orden.estado)

                Text(
                    text = "Operaciones realizadas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                if (state.detalles.isEmpty()) {
                    Text(
                        text = "Todavía no se han registrado operaciones en esta orden.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    state.detalles.forEach { detalle ->
                        DetalleOrdenItem(detalle = detalle)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineOrden(estado: String) {
    if (estado == EstadosOrden.CANCELADO) {
        Text(
            text = "La orden fue cancelada.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
        return
    }

    val flujo = listOf(
        EstadosOrden.RECEPCION,
        EstadosOrden.DIAGNOSTICO,
        EstadosOrden.EN_REPARACION,
        EstadosOrden.CONTROL_CALIDAD,
        EstadosOrden.LISTO_ENTREGA,
        EstadosOrden.ENTREGADO,
    )
    val currentIndex = flujo.indexOf(estado)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        flujo.forEachIndexed { index, paso ->
            val alcanzado = currentIndex >= index
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = if (alcanzado) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = CircleShape,
                        ),
                )
                Text(
                    text = EstadosOrden.label(paso),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (paso == estado) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (alcanzado) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}
