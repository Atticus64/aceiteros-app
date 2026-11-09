package com.example.aceiterosready.presentation.common

import com.example.aceiterosready.data.model.ClienteDto
import com.example.aceiterosready.data.model.MecanicoDto
import com.example.aceiterosready.data.model.UserDto
import com.example.aceiterosready.data.model.UsuarioResumenDto
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object Roles {
    const val ADMINISTRADOR = "ADMINISTRADOR"
    const val RECEPCIONISTA = "RECEPCIONISTA"
    const val MECANICO = "MECANICO"
    const val ALMACENISTA = "ALMACENISTA"
    const val CLIENTE = "CLIENTE"

    val TODOS = listOf(ADMINISTRADOR, RECEPCIONISTA, MECANICO, ALMACENISTA, CLIENTE)
    val PERSONAL = listOf(ADMINISTRADOR, RECEPCIONISTA, MECANICO, ALMACENISTA)

    fun label(rol: String): String = when (rol) {
        ADMINISTRADOR -> "Administrador"
        RECEPCIONISTA -> "Recepción"
        MECANICO -> "Mecánico"
        ALMACENISTA -> "Almacén"
        CLIENTE -> "Cliente"
        else -> rol
    }

    fun requiereTaller(rol: String): Boolean = rol in setOf(
        RECEPCIONISTA,
        MECANICO,
        ALMACENISTA,
    )
}

object EstadosOrden {
    const val RECEPCION = "Recepcion"
    const val DIAGNOSTICO = "Diagnostico"
    const val EN_REPARACION = "En_Reparacion"
    const val CONTROL_CALIDAD = "Control_Calidad"
    const val LISTO_ENTREGA = "Listo_Entrega"
    const val ENTREGADO = "Entregado"
    const val CANCELADO = "Cancelado"

    val TODOS = listOf(
        RECEPCION,
        DIAGNOSTICO,
        EN_REPARACION,
        CONTROL_CALIDAD,
        LISTO_ENTREGA,
        ENTREGADO,
        CANCELADO,
    )

    val FLUJO_MECANICO = listOf(
        DIAGNOSTICO,
        EN_REPARACION,
        CONTROL_CALIDAD,
        LISTO_ENTREGA,
    )

    val EDITABLES_DETALLE = setOf(
        RECEPCION,
        DIAGNOSTICO,
        EN_REPARACION,
        CONTROL_CALIDAD,
    )

    fun label(estado: String): String = when (estado) {
        RECEPCION -> "Recepción"
        DIAGNOSTICO -> "Diagnóstico"
        EN_REPARACION -> "En reparación"
        CONTROL_CALIDAD -> "Control de calidad"
        LISTO_ENTREGA -> "Listo para entrega"
        ENTREGADO -> "Entregado"
        CANCELADO -> "Cancelado"
        else -> estado
    }
}

fun UserDto.esAdmin(): Boolean = rol == Roles.ADMINISTRADOR
fun UserDto.esRecepcion(): Boolean = rol == Roles.RECEPCIONISTA
fun UserDto.esMecanico(): Boolean = rol == Roles.MECANICO
fun UserDto.esAlmacenista(): Boolean = rol == Roles.ALMACENISTA
fun UserDto.esCliente(): Boolean = rol == Roles.CLIENTE

fun UserDto.nombreCompleto(): String = listOfNotNull(nombre, apellido)
    .filter { it.isNotBlank() }
    .joinToString(" ")

fun ClienteDto.nombreCompleto(): String = listOfNotNull(nombre, apellido)
    .filter { it.isNotBlank() }
    .joinToString(" ")

fun MecanicoDto.nombreCompleto(): String = listOfNotNull(nombre, apellido)
    .filter { it.isNotBlank() }
    .joinToString(" ")

fun UsuarioResumenDto.nombreCompleto(): String = listOfNotNull(nombre, apellido)
    .filter { it.isNotBlank() }
    .joinToString(" ")

private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX"))

fun formatMoney(value: Double): String = runCatching {
    currencyFormat.format(value)
}.getOrDefault("$" + String.format(Locale.US, "%.2f", value))

fun formatFecha(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd",
    )
    for (pattern in patterns) {
        val parsed = runCatching {
            SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(iso)
        }.getOrNull()
        if (parsed != null) {
            return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-MX"))
                .format(parsed)
        }
    }
    return iso
}

fun formatCantidad(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
}
