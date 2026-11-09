package com.example.aceiterosready.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.aceiterosready.data.model.DetalleOrdenDto
import com.example.aceiterosready.data.model.OrdenDto

@Composable
fun OrdenResumenCard(
    orden: OrdenDto,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = orden.folio,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                EstadoOrdenChip(orden.estado)
            }

            val vehiculo = orden.vehiculo
            Text(
                text = if (vehiculo != null) {
                    "${vehiculo.marca} ${vehiculo.modelo} · ${vehiculo.placa}"
                } else {
                    "Vehículo no disponible"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            orden.cliente?.nombreCompleto()?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "Cliente: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            orden.mecanico?.nombreCompleto()?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "Mecánico: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatFecha(orden.fechaRecepcion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatMoney(orden.total),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
fun DetalleOrdenItem(
    detalle: DetalleOrdenDto,
    modifier: Modifier = Modifier,
    actions: @Composable (() -> Unit)? = null,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = detalle.servicio?.nombre
                            ?: detalle.refaccion?.nombre
                            ?: detalle.descripcion,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = when {
                            detalle.servicio != null -> "Servicio"
                            detalle.refaccion != null ->
                                "Refacción · ${detalle.refaccion.codigo}"
                            else -> "Concepto"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = formatMoney(detalle.subtotal),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Text(
                text = "Cantidad: ${formatCantidad(detalle.cantidad)} · " +
                    "Precio unitario: ${formatMoney(detalle.precioUnitario)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (
                detalle.descripcion.isNotBlank() &&
                detalle.descripcion != detalle.servicio?.nombre &&
                detalle.descripcion != detalle.refaccion?.nombre
            ) {
                Text(
                    text = detalle.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            actions?.invoke()
        }
    }
}
