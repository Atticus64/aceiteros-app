package com.example.aceiterosready.presentation.navigation

object Routes {
    const val HOME = "home"
    const val PERFIL = "perfil"

    const val ADMIN_USUARIOS = "admin/usuarios"
    const val ADMIN_USUARIO_NUEVO = "admin/usuarios/nuevo"
    const val ADMIN_USUARIO_EDITAR = "admin/usuarios/{id}/editar"
    fun adminUsuarioEditar(id: String) = "admin/usuarios/$id/editar"

    const val ADMIN_SERVICIOS = "admin/servicios"
    const val ADMIN_SERVICIO_NUEVO = "admin/servicios/nuevo"
    const val ADMIN_SERVICIO_EDITAR = "admin/servicios/{id}/editar"
    fun adminServicioEditar(id: String) = "admin/servicios/$id/editar"

    const val ADMIN_REFACCIONES = "admin/refacciones"
    const val ADMIN_REFACCION_NUEVO = "admin/refacciones/nuevo"
    const val ADMIN_REFACCION_EDITAR = "admin/refacciones/{id}/editar"
    fun adminRefaccionEditar(id: String) = "admin/refacciones/$id/editar"

    const val RECEPCION_CLIENTES = "recepcion/clientes"
    const val RECEPCION_CLIENTE_NUEVO = "recepcion/clientes/nuevo"
    const val RECEPCION_CLIENTE_EDITAR = "recepcion/clientes/{id}/editar"
    fun recepcionClienteEditar(id: String) = "recepcion/clientes/$id/editar"

    const val RECEPCION_VEHICULOS = "recepcion/vehiculos"
    const val RECEPCION_VEHICULO_NUEVO = "recepcion/vehiculos/nuevo"
    const val RECEPCION_VEHICULO_EDITAR = "recepcion/vehiculos/{id}/editar"
    fun recepcionVehiculoEditar(id: String) = "recepcion/vehiculos/$id/editar"

    const val RECEPCION_ORDENES = "recepcion/ordenes"
    const val RECEPCION_ORDEN_NUEVA = "recepcion/ordenes/nuevo"
    const val RECEPCION_ORDEN_DETALLE = "recepcion/ordenes/{id}"
    fun recepcionOrdenDetalle(id: String) = "recepcion/ordenes/$id"

    const val MECANICO_ORDENES = "mecanico/ordenes"
    const val MECANICO_ORDEN_TRABAJO = "mecanico/ordenes/{id}"
    fun mecanicoOrdenTrabajo(id: String) = "mecanico/ordenes/$id"

    const val CLIENTE_VEHICULOS = "cliente/vehiculos"
    const val CLIENTE_VEHICULO_DETALLE = "cliente/vehiculos/{id}"
    fun clienteVehiculoDetalle(id: String) = "cliente/vehiculos/$id"

    const val CLIENTE_ORDENES = "cliente/ordenes"
    const val CLIENTE_ORDEN_DETALLE = "cliente/ordenes/{id}"
    fun clienteOrdenDetalle(id: String) = "cliente/ordenes/$id"

    const val ALMACEN_INVENTARIO = "almacen/inventario"
    const val ALMACEN_REFACCION_DETALLE = "almacen/inventario/{id}"
    fun almacenRefaccionDetalle(id: String) = "almacen/inventario/$id"

    const val ALMACEN_MOVIMIENTOS = "almacen/inventario/{id}/movimientos"
    fun almacenMovimientos(id: String) = "almacen/inventario/$id/movimientos"
}
