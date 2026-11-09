package com.example.aceiterosready.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.aceiterosready.data.model.RefaccionDto
import com.example.aceiterosready.data.model.UserDto
import com.example.aceiterosready.di.appViewModel
import com.example.aceiterosready.presentation.common.EstadosOrden
import com.example.aceiterosready.presentation.common.InlineMessage
import com.example.aceiterosready.presentation.common.OrdenResumenCard
import com.example.aceiterosready.presentation.common.RolChip
import com.example.aceiterosready.presentation.common.Roles
import com.example.aceiterosready.presentation.common.ScreenScaffold
import com.example.aceiterosready.presentation.common.StatCard
import com.example.aceiterosready.presentation.common.formatMoney
import com.example.aceiterosready.presentation.common.nombreCompleto
import com.example.aceiterosready.presentation.navigation.Routes

@Composable
fun HomeScreen(
    user: UserDto,
    onNavigate: (String) -> Unit,
    onPerfil: () -> Unit,
) {
    val vm: HomeViewModel = appViewModel {
        HomeViewModel(it.ordenRepository, it.catalogRepository, it.usuarioRepository)
    }
    val state by vm.state.collectAsState()

    LaunchedEffect(user.id) { vm.load(user) }

    ScreenScaffold(
        title = "Inicio",
        actions = {
            IconButton(onClick = vm::refresh) {
                Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { GreetingCard(user = user, onPerfil = onPerfil) }

            if (state.error != null) {
                item { InlineMessage(state.error, isError = true) }
            }

            when (user.rol) {
                Roles.ADMINISTRADOR -> adminContent(state, onNavigate)
                Roles.RECEPCIONISTA -> recepcionContent(state, onNavigate)
                Roles.MECANICO -> mecanicoContent(state, onNavigate)
                Roles.ALMACENISTA -> almacenContent(state, onNavigate)
                Roles.CLIENTE -> clienteContent(state, onNavigate)
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun GreetingCard(user: UserDto, onPerfil: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Hola, ${user.nombre}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            RolChip(user.rol)
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
            )
            user.telefono?.takeIf { it.isNotBlank() }?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onPerfil) {
                Text("Ver perfil y cerrar sesión")
            }
        }
    }
}

private fun LazyListScope.adminContent(
    state: HomeState,
    onNavigate: (String) -> Unit,
) {
    item {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "Usuarios",
                value = state.usuarios.size.toString(),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                title = "Servicios",
                value = state.servicios.size.toString(),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                title = "Refacciones",
                value = state.refacciones.size.toString(),
                modifier = Modifier.weight(1f),
            )
        }
    }
    item { SectionTitle("Administración") }
    item {
        DashboardAction(
            title = "Usuarios por rol",
            supporting = "Consulta, crea y desactiva cuentas del personal y clientes.",
            onClick = { onNavigate(Routes.ADMIN_USUARIOS) },
        )
    }
    item {
        DashboardAction(
            title = "Servicios",
            supporting = "Catálogo de servicios que ofrece el taller.",
            onClick = { onNavigate(Routes.ADMIN_SERVICIOS) },
        )
    }
    item {
        DashboardAction(
            title = "Refacciones",
            supporting = "Catálogo de refacciones e insumos por taller.",
            onClick = { onNavigate(Routes.ADMIN_REFACCIONES) },
        )
    }
}

private fun LazyListScope.recepcionContent(
    state: HomeState,
    onNavigate: (String) -> Unit,
) {
    val activas = state.ordenes.filter {
        it.estado != EstadosOrden.ENTREGADO && it.estado != EstadosOrden.CANCELADO
    }
    item {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "Órdenes activas",
                value = activas.size.toString(),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                title = "Vehículos",
                value = state.vehiculos.size.toString(),
                modifier = Modifier.weight(1f),
            )
        }
    }
    item { SectionTitle("Operación de recepción") }
    item {
        DashboardAction(
            title = "Clientes",
            supporting = "Alta de clientes y vinculación al taller.",
            onClick = { onNavigate(Routes.RECEPCION_CLIENTES) },
        )
    }
    item {
        DashboardAction(
            title = "Vehículos",
            supporting = "Registro y edición de vehículos del taller.",
            onClick = { onNavigate(Routes.RECEPCION_VEHICULOS) },
        )
    }
    item {
        DashboardAction(
            title = "Órdenes de mantenimiento",
            supporting = "Crea órdenes, asigna mecánicos y confirma entregas.",
            onClick = { onNavigate(Routes.RECEPCION_ORDENES) },
        )
    }

    if (activas.isNotEmpty()) {
        item { SectionTitle("Órdenes recientes") }
        items(activas.take(3), key = { it.id }) { orden ->
            OrdenResumenCard(
                orden = orden,
                onClick = { onNavigate(Routes.recepcionOrdenDetalle(orden.id)) },
            )
        }
    }
}

private fun LazyListScope.mecanicoContent(
    state: HomeState,
    onNavigate: (String) -> Unit,
) {
    val enTrabajo = state.ordenes.filter {
        it.estado in setOf(
            EstadosOrden.RECEPCION,
            EstadosOrden.DIAGNOSTICO,
            EstadosOrden.EN_REPARACION,
            EstadosOrden.CONTROL_CALIDAD,
        )
    }
    val listos = state.ordenes.count { it.estado == EstadosOrden.LISTO_ENTREGA }

    item {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "En trabajo",
                value = enTrabajo.size.toString(),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                title = "Listos",
                value = listos.toString(),
                modifier = Modifier.weight(1f),
            )
        }
    }
    item { SectionTitle("Mi mantenimiento actual") }
    item {
        DashboardAction(
            title = "Ver todas mis órdenes",
            supporting = "Diagnóstico, estados del flujo y operaciones.",
            onClick = { onNavigate(Routes.MECANICO_ORDENES) },
        )
    }
    if (enTrabajo.isNotEmpty()) {
        items(enTrabajo.take(3), key = { it.id }) { orden ->
            OrdenResumenCard(
                orden = orden,
                onClick = { onNavigate(Routes.mecanicoOrdenTrabajo(orden.id)) },
            )
        }
    } else if (!state.loading) {
        item {
            Text(
                text = "No tienes órdenes activas asignadas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun LazyListScope.almacenContent(
    state: HomeState,
    onNavigate: (String) -> Unit,
) {
    val stockTotal = state.refacciones.sumOf { it.stock }
    val stockBajo = state.refacciones.filter { it.stock <= it.stockMinimo }

    item {
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
                value = stockBajo.size.toString(),
                modifier = Modifier.weight(1f),
            )
        }
    }
    item {
        DashboardAction(
            title = "Consultar inventario",
            supporting = "Stock, mínimos, tipos de refacción e historial.",
            onClick = { onNavigate(Routes.ALMACEN_INVENTARIO) },
        )
    }
    if (stockBajo.isNotEmpty()) {
        item { SectionTitle("Requieren reabastecimiento") }
        items(stockBajo.take(5), key = { it.id }) { refaccion ->
            RefaccionAlertaCard(
                refaccion = refaccion,
                onClick = { onNavigate(Routes.almacenRefaccionDetalle(refaccion.id)) },
            )
        }
    }
}

private fun LazyListScope.clienteContent(
    state: HomeState,
    onNavigate: (String) -> Unit,
) {
    val activas = state.ordenes.filter {
        it.estado != EstadosOrden.ENTREGADO && it.estado != EstadosOrden.CANCELADO
    }

    item {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "Mis vehículos",
                value = state.vehiculos.size.toString(),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                title = "Órdenes activas",
                value = activas.size.toString(),
                modifier = Modifier.weight(1f),
            )
        }
    }
    item {
        DashboardAction(
            title = "Mis vehículos",
            supporting = "Consulta tus vehículos y su historial de órdenes.",
            onClick = { onNavigate(Routes.CLIENTE_VEHICULOS) },
        )
    }
    item {
        DashboardAction(
            title = "Mis órdenes",
            supporting = "Sigue el estado y las operaciones de tus mantenimientos.",
            onClick = { onNavigate(Routes.CLIENTE_ORDENES) },
        )
    }
    if (activas.isNotEmpty()) {
        item { SectionTitle("Seguimiento") }
        items(activas.take(3), key = { it.id }) { orden ->
            OrdenResumenCard(
                orden = orden,
                onClick = { onNavigate(Routes.clienteOrdenDetalle(orden.id)) },
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun DashboardAction(
    title: String,
    supporting: String,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun RefaccionAlertaCard(
    refaccion: RefaccionDto,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = refaccion.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${refaccion.codigo} · Stock ${refaccion.stock} / mínimo ${refaccion.stockMinimo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Text(
                text = formatMoney(refaccion.precio),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
