package com.example.aceiterosready.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aceiterosready.data.model.UserDto
import com.example.aceiterosready.presentation.admin.AdminUsuariosScreen
import com.example.aceiterosready.presentation.admin.RefaccionFormScreen
import com.example.aceiterosready.presentation.admin.RefaccionesAdminScreen
import com.example.aceiterosready.presentation.admin.ServicioFormScreen
import com.example.aceiterosready.presentation.admin.ServiciosAdminScreen
import com.example.aceiterosready.presentation.admin.UsuarioFormScreen
import com.example.aceiterosready.presentation.almacen.InventarioScreen
import com.example.aceiterosready.presentation.almacen.MovimientosScreen
import com.example.aceiterosready.presentation.almacen.RefaccionDetalleScreen
import com.example.aceiterosready.presentation.cliente.MiOrdenDetalleScreen
import com.example.aceiterosready.presentation.cliente.MiVehiculoDetalleScreen
import com.example.aceiterosready.presentation.cliente.MisOrdenesScreen
import com.example.aceiterosready.presentation.cliente.MisVehiculosScreen
import com.example.aceiterosready.presentation.common.ConfirmDialog
import com.example.aceiterosready.presentation.common.LocalOpenDrawer
import com.example.aceiterosready.presentation.common.Roles
import com.example.aceiterosready.presentation.home.HomeScreen
import com.example.aceiterosready.presentation.mecanico.MecanicoOrdenesScreen
import com.example.aceiterosready.presentation.mecanico.OrdenTrabajoScreen
import com.example.aceiterosready.presentation.perfil.PerfilScreen
import com.example.aceiterosready.presentation.recepcion.ClienteFormScreen
import com.example.aceiterosready.presentation.recepcion.ClientesScreen
import com.example.aceiterosready.presentation.recepcion.OrdenDetalleScreen
import com.example.aceiterosready.presentation.recepcion.OrdenFormScreen
import com.example.aceiterosready.presentation.recepcion.OrdenesScreen
import com.example.aceiterosready.presentation.recepcion.VehiculoFormScreen
import com.example.aceiterosready.presentation.recepcion.VehiculosScreen
import com.example.aceiterosready.presentation.common.nombreCompleto
import kotlinx.coroutines.launch

data class MenuItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private fun menuFor(user: UserDto): List<MenuItem> = when (user.rol) {
    Roles.ADMINISTRADOR -> listOf(
        MenuItem(Routes.ADMIN_USUARIOS, "Usuarios", Icons.Filled.Group),
        MenuItem(Routes.ADMIN_SERVICIOS, "Servicios", Icons.Filled.Build),
        MenuItem(Routes.ADMIN_REFACCIONES, "Refacciones", Icons.Filled.Store),
    )
    Roles.RECEPCIONISTA -> listOf(
        MenuItem(Routes.RECEPCION_CLIENTES, "Clientes", Icons.Filled.Person),
        MenuItem(Routes.RECEPCION_VEHICULOS, "Vehículos", Icons.Filled.DirectionsCar),
        MenuItem(Routes.RECEPCION_ORDENES, "Órdenes", Icons.Filled.Receipt),
    )
    Roles.MECANICO -> listOf(
        MenuItem(Routes.MECANICO_ORDENES, "Mis mantenimientos", Icons.Filled.Build),
    )
    Roles.ALMACENISTA -> listOf(
        MenuItem(Routes.ALMACEN_INVENTARIO, "Inventario", Icons.Filled.Inventory),
    )
    Roles.CLIENTE -> listOf(
        MenuItem(Routes.CLIENTE_VEHICULOS, "Mis vehículos", Icons.Filled.DirectionsCar),
        MenuItem(Routes.CLIENTE_ORDENES, "Mis órdenes", Icons.Filled.Receipt),
    )
    else -> emptyList()
}

@Composable
fun MainApp(
    user: UserDto,
    onLogout: () -> Unit,
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val openDrawer: () -> Unit = {
        scope.launch { drawerState.open() }
    }

    val closeDrawer: () -> Unit = {
        scope.launch { drawerState.close() }
    }

    val items = remember(user.rol) { menuFor(user) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val navigateFromMenu: (String) -> Unit = { route ->
        scope.launch { drawerState.close() }
        navController.navigate(route) {
            launchSingleTop = true
            popUpTo(Routes.HOME) { saveState = true }
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                ) {
                    Text(
                        text = user.nombreCompleto(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = Roles.label(user.rol),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    selected = currentRoute == Routes.HOME,
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    onClick = {
                        navigateFromMenu(Routes.HOME)
                    },
                )

                items.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        icon = { Icon(item.icon, contentDescription = null) },
                        onClick = { navigateFromMenu(item.route) },
                    )
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { Text("Perfil") },
                    selected = currentRoute == Routes.PERFIL,
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
                    onClick = {
                        closeDrawer()
                        navController.navigate(Routes.PERFIL) { launchSingleTop = true }
                    },
                )
                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        closeDrawer()
                        showLogoutDialog = true
                    },
                )
            }
        },
    ) {
        CompositionLocalProvider(LocalOpenDrawer provides openDrawer) {
            AppNavHost(
                navController = navController,
                user = user,
                onLogout = onLogout,
            )
        }
    }

    if (showLogoutDialog) {
        ConfirmDialog(
            title = "Cerrar sesión",
            text = "¿Deseas salir de tu cuenta?",
            confirmText = "Salir",
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false },
        )
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    user: UserDto,
    onLogout: () -> Unit,
) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                user = user,
                onNavigate = { route -> navController.navigate(route) },
                onPerfil = { navController.navigate(Routes.PERFIL) },
            )
        }

        composable(Routes.PERFIL) {
            PerfilScreen(
                user = user,
                onBack = { navController.popBackStack() },
                onLogout = onLogout,
            )
        }

        // Administración
        composable(Routes.ADMIN_USUARIOS) {
            AdminUsuariosScreen(
                onNuevo = { navController.navigate(Routes.ADMIN_USUARIO_NUEVO) },
                onEditar = { id ->
                    navController.navigate(Routes.adminUsuarioEditar(id))
                },
            )
        }
        composable(Routes.ADMIN_USUARIO_NUEVO) {
            UsuarioFormScreen(
                usuarioId = null,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.ADMIN_USUARIO_EDITAR,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            UsuarioFormScreen(
                usuarioId = entry.arguments?.getString("id"),
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(Routes.ADMIN_SERVICIOS) {
            ServiciosAdminScreen(
                onNuevo = { navController.navigate(Routes.ADMIN_SERVICIO_NUEVO) },
                onEditar = { id ->
                    navController.navigate(Routes.adminServicioEditar(id))
                },
            )
        }
        composable(Routes.ADMIN_SERVICIO_NUEVO) {
            ServicioFormScreen(
                servicioId = null,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.ADMIN_SERVICIO_EDITAR,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            ServicioFormScreen(
                servicioId = entry.arguments?.getString("id"),
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(Routes.ADMIN_REFACCIONES) {
            RefaccionesAdminScreen(
                onNuevo = { navController.navigate(Routes.ADMIN_REFACCION_NUEVO) },
                onEditar = { id ->
                    navController.navigate(Routes.adminRefaccionEditar(id))
                },
            )
        }
        composable(Routes.ADMIN_REFACCION_NUEVO) {
            RefaccionFormScreen(
                refaccionId = null,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.ADMIN_REFACCION_EDITAR,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            RefaccionFormScreen(
                refaccionId = entry.arguments?.getString("id"),
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        // Recepción
        composable(Routes.RECEPCION_CLIENTES) {
            ClientesScreen(
                onNuevo = { navController.navigate(Routes.RECEPCION_CLIENTE_NUEVO) },
                onEditar = { id ->
                    navController.navigate(Routes.recepcionClienteEditar(id))
                },
            )
        }
        composable(Routes.RECEPCION_CLIENTE_NUEVO) {
            ClienteFormScreen(
                clienteId = null,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.RECEPCION_CLIENTE_EDITAR,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            ClienteFormScreen(
                clienteId = entry.arguments?.getString("id"),
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(Routes.RECEPCION_VEHICULOS) {
            VehiculosScreen(
                onNuevo = { navController.navigate(Routes.RECEPCION_VEHICULO_NUEVO) },
                onEditar = { id ->
                    navController.navigate(Routes.recepcionVehiculoEditar(id))
                },
            )
        }
        composable(Routes.RECEPCION_VEHICULO_NUEVO) {
            VehiculoFormScreen(
                vehiculoId = null,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.RECEPCION_VEHICULO_EDITAR,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            VehiculoFormScreen(
                vehiculoId = entry.arguments?.getString("id"),
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(Routes.RECEPCION_ORDENES) {
            OrdenesScreen(
                onNueva = { navController.navigate(Routes.RECEPCION_ORDEN_NUEVA) },
                onVer = { id ->
                    navController.navigate(Routes.recepcionOrdenDetalle(id))
                },
            )
        }
        composable(Routes.RECEPCION_ORDEN_NUEVA) {
            OrdenFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.RECEPCION_ORDEN_DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            OrdenDetalleScreen(
                ordenId = entry.arguments?.getString("id").orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }

        // Mecánico
        composable(Routes.MECANICO_ORDENES) {
            MecanicoOrdenesScreen(
                onVer = { id ->
                    navController.navigate(Routes.mecanicoOrdenTrabajo(id))
                },
            )
        }
        composable(
            route = Routes.MECANICO_ORDEN_TRABAJO,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            OrdenTrabajoScreen(
                ordenId = entry.arguments?.getString("id").orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }

        // Cliente
        composable(Routes.CLIENTE_VEHICULOS) {
            MisVehiculosScreen(
                onVer = { id ->
                    navController.navigate(Routes.clienteVehiculoDetalle(id))
                },
            )
        }
        composable(
            route = Routes.CLIENTE_VEHICULO_DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            MiVehiculoDetalleScreen(
                vehiculoId = entry.arguments?.getString("id").orEmpty(),
                onBack = { navController.popBackStack() },
                onVerOrden = { ordenId ->
                    navController.navigate(Routes.clienteOrdenDetalle(ordenId))
                },
            )
        }
        composable(Routes.CLIENTE_ORDENES) {
            MisOrdenesScreen(
                onVer = { id ->
                    navController.navigate(Routes.clienteOrdenDetalle(id))
                },
            )
        }
        composable(
            route = Routes.CLIENTE_ORDEN_DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            MiOrdenDetalleScreen(
                ordenId = entry.arguments?.getString("id").orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }

        // Almacén
        composable(Routes.ALMACEN_INVENTARIO) {
            InventarioScreen(
                onVer = { id ->
                    navController.navigate(Routes.almacenRefaccionDetalle(id))
                },
            )
        }
        composable(
            route = Routes.ALMACEN_REFACCION_DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            RefaccionDetalleScreen(
                refaccionId = id,
                onBack = { navController.popBackStack() },
                onVerMovimientos = {
                    navController.navigate(Routes.almacenMovimientos(id))
                },
            )
        }
        composable(
            route = Routes.ALMACEN_MOVIMIENTOS,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            MovimientosScreen(
                refaccionId = entry.arguments?.getString("id").orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }
    }
}
