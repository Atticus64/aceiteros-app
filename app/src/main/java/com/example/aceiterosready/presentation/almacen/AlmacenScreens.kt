package com.example.aceiterosready.presentation.almacen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.aceiterosready.data.model.MovimientoInventarioDto
import com.example.aceiterosready.data.model.RefaccionDto
import com.example.aceiterosready.di.appViewModel
import com.example.aceiterosready.presentation.common.ErrorState
import com.example.aceiterosready.presentation.common.InfoRow
import com.example.aceiterosready.presentation.common.ListState
import com.example.aceiterosready.presentation.common.LoadingState
import com.example.aceiterosready.presentation.common.ScreenScaffold
import com.example.aceiterosready.presentation.common.StatCard
import com.example.aceiterosready.presentation.common.formatFecha
import com.example.aceiterosready.presentation.common.formatMoney
import com.example.aceiterosready.ui.theme.blue
import com.example.aceiterosready.ui.theme.green
import com.example.aceiterosready.ui.theme.red

@Composable
fun InventarioScreen(
    onVer: (String) -> Unit,
) {
    val vm: InventarioViewModel = appViewModel {
        InventarioViewModel(it.catalogRepository)
    }
    val state by vm.state.collectAsState()

    val stockTotal = state.refacciones.sumOf { it.stock }
    val stockBajo = state.refacciones.count { it.stock <= it.stockMinimo }

    ScreenScaffold(
        title = "Inventario",
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
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        title = "Refacciones",
                        value = state.refacciones.size.toString(),
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        title = "Unidades",
                        value = stockTotal.toString(),
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        title = "Stock bajo",
                        value = stockBajo.toString(),
                        modifier = Modifier.weight(1f),
                    )
                }
                OutlinedSearch(
                    value = state.query,
                    onValueChange = vm::onQueryChange,
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
            }

            ListState(
                loading = state.loading,
                error = state.error,
                isEmpty = state.refacciones.isEmpty(),
                onRetry = vm::load,
                emptyTitle = "Sin inventario",
                emptyMessage = "No hay refacciones registradas en tu taller.",
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.refacciones, key = { it.id }) { refaccion ->
                        InventarioCard(
                            refaccion = refaccion,
                            onClick = { onVer(refaccion.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OutlinedSearch(
    value: String,
    onValueChange: (String) -> Unit,
) {
    androidx.compose.material3.OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Buscar por código o nombre") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun InventarioCard(
    refaccion: RefaccionDto,
    onClick: () -> Unit,
) {
    val stockBajo = refaccion.stock <= refaccion.stockMinimo

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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${refaccion.codigo} · ${refaccion.tipo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (stockBajo) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = if (refaccion.stock == 0) "Sin stock" else "Stock bajo",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Stock: ${refaccion.stock} / mínimo ${refaccion.stockMinimo}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (stockBajo) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatMoney(refaccion.precio),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Text(
                text = refaccion.marca ?: "Sin marca",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun RefaccionDetalleScreen(
    refaccionId: String,
    onBack: () -> Unit,
    onVerMovimientos: () -> Unit,
) {
    val vm: RefaccionDetalleViewModel = appViewModel {
        RefaccionDetalleViewModel(it.catalogRepository)
    }
    val state by vm.state.collectAsState()

    LaunchedEffect(refaccionId) { vm.load(refaccionId) }

    ScreenScaffold(
        title = state.refaccion?.codigo ?: "Refacción",
        onBack = onBack,
        actions = {
            IconButton(onClick = vm::refresh) {
                Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
            }
        },
    ) { padding ->
        val refaccion = state.refaccion

        when {
            state.loading && refaccion == null -> LoadingState(Modifier.padding(padding))
            refaccion == null -> ErrorState(
                message = state.error ?: "No se pudo cargar la refacción.",
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
                            text = refaccion.nombre,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        InfoRow("Código", refaccion.codigo)
                        InfoRow("Marca", refaccion.marca)
                        InfoRow("Tipo", refaccion.tipo)
                        InfoRow("Precio", formatMoney(refaccion.precio))
                        InfoRow("Stock", refaccion.stock.toString())
                        InfoRow("Stock mínimo", refaccion.stockMinimo.toString())
                        InfoRow("Taller", refaccion.tallerId)
                        InfoRow("Descripción", refaccion.descripcion)
                    }
                }

                StockSemaforo(stock = refaccion.stock, minimo = refaccion.stockMinimo)

                Button(
                    onClick = onVerMovimientos,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Ver historial de movimientos")
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Últimos movimientos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (state.movimientos.isEmpty()) {
                    Text(
                        text = "Sin movimientos registrados.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    state.movimientos.take(5).forEach { movimiento ->
                        MovimientoCard(movimiento)
                    }
                    if (state.movimientos.size > 5) {
                        Text(
                            text = "Mostrando los últimos 5 movimientos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StockSemaforo(stock: Int, minimo: Int) {
    val (text, color, onColor) = when {
        stock == 0 || stock < minimo -> Triple(
            "Stock crítico: por debajo del mínimo",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
        )
        stock == minimo -> Triple(
            "Stock en el mínimo: conviene reabastecer",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
        )
        else -> Triple(
            "Stock saludable",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }

    Surface(
        color = color,
        contentColor = onColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
fun MovimientosScreen(
    refaccionId: String,
    onBack: () -> Unit,
) {
    val vm: RefaccionDetalleViewModel = appViewModel {
        RefaccionDetalleViewModel(it.catalogRepository)
    }
    val state by vm.state.collectAsState()

    LaunchedEffect(refaccionId) { vm.load(refaccionId) }

    ScreenScaffold(
        title = "Movimientos",
        onBack = onBack,
        actions = {
            IconButton(onClick = vm::refresh) {
                Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            state.refaccion?.let { refaccion ->
                Text(
                    text = "${refaccion.codigo} · ${refaccion.nombre}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            ListState(
                loading = state.loading,
                error = state.error,
                isEmpty = state.movimientos.isEmpty(),
                onRetry = vm::refresh,
                emptyTitle = "Sin movimientos",
                emptyMessage = "Esta refacción todavía no tiene entradas, salidas ni ajustes.",
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.movimientos, key = { it.id }) { movimiento ->
                        MovimientoCard(movimiento)
                    }
                }
            }
        }
    }
}

@Composable
private fun MovimientoCard(movimiento: MovimientoInventarioDto) {
    val color = when (movimiento.tipo) {
        "Entrada" -> green
        "Salida" -> red
        else -> blue
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = movimiento.tipo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatFecha(movimiento.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "Cantidad: ${movimiento.cantidad} · " +
                    "Stock ${movimiento.stockAnterior} → ${movimiento.stockNuevo}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = movimiento.motivo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            movimiento.usuarioId?.let {
                Text(
                    text = "Registrado por: $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
