package com.example.aceiterosready.data.model

// ---------- Autenticación ----------

data class LoginRequest(
    val email: String,
    val password: String,
)

data class RegisterRequest(
    val nombre: String,
    val apellido: String? = null,
    val email: String,
    val password: String,
    val telefono: String? = null,
)

data class RefreshTokenRequest(
    val refreshToken: String,
)

data class UpdateProfileRequest(
    val nombre: String? = null,
    val apellido: String? = null,
    val telefono: String? = null,
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
)

data class UserDto(
    val id: String,
    val nombre: String,
    val apellido: String? = null,
    val email: String,
    val telefono: String? = null,
    val rolId: String,
    val rol: String,
    val tallerId: String? = null,
    val activo: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

data class TokensDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val refreshExpiresIn: Int,
)

data class LoginData(
    val user: UserDto,
    val tokens: TokensDto,
)

data class RolDto(
    val id: String,
    val nombre: String,
    val descripcion: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

// ---------- Catálogos ----------

data class TallerDto(
    val id: String,
    val nombre: String,
    val direccion: String? = null,
    val telefono: String? = null,
    val activo: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

data class ServicioDto(
    val id: String,
    val nombre: String,
    val descripcion: String? = null,
    val categoria: String,
    val precio: Double,
    val duracionMin: Int,
    val activo: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

data class RefaccionDto(
    val id: String,
    val codigo: String,
    val nombre: String,
    val descripcion: String? = null,
    val marca: String? = null,
    val tipo: String = "Refaccion",
    val stock: Int = 0,
    val stockMinimo: Int = 0,
    val precio: Double,
    val tallerId: String,
    val activo: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

data class MovimientoInventarioDto(
    val id: String,
    val refaccionId: String,
    val tallerId: String,
    val tipo: String,
    val cantidad: Int,
    val stockAnterior: Int,
    val stockNuevo: Int,
    val motivo: String,
    val ordenId: String? = null,
    val detalleOrdenId: String? = null,
    val usuarioId: String? = null,
    val createdAt: String,
)

data class VehiculoDto(
    val id: String,
    val placa: String,
    val vin: String? = null,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val color: String? = null,
    val kilometraje: Int = 0,
    val clienteId: String? = null,
    val tallerId: String,
    val activo: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

/**
 * Resumen de cliente que devuelve el backend en listados y en órdenes.
 * No incluye email ni estado; para eso se usa [UserDto] desde /usuarios.
 */
data class ClienteDto(
    val id: String,
    val nombre: String,
    val apellido: String? = null,
    val telefono: String? = null,
)

data class MecanicoDto(
    val id: String,
    val nombre: String,
    val apellido: String? = null,
    val email: String,
    val telefono: String? = null,
    val tallerId: String? = null,
    val activo: Boolean = true,
)

// ---------- Órdenes ----------

data class VehiculoResumenDto(
    val id: String,
    val placa: String,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val color: String? = null,
)

data class TallerResumenDto(
    val id: String,
    val nombre: String,
    val direccion: String? = null,
    val telefono: String? = null,
)

data class UsuarioResumenDto(
    val id: String,
    val nombre: String,
    val apellido: String? = null,
)

data class ServicioResumenDto(
    val id: String,
    val nombre: String,
    val categoria: String,
    val precio: Double,
    val duracionMin: Int,
)

data class RefaccionResumenDto(
    val id: String,
    val codigo: String,
    val nombre: String,
    val marca: String? = null,
    val precio: Double,
)

data class OrdenDto(
    val id: String,
    val folio: String,
    val estado: String,
    val vehiculo: VehiculoResumenDto? = null,
    val taller: TallerResumenDto? = null,
    val mecanico: UsuarioResumenDto? = null,
    val cliente: ClienteDto? = null,
    val fechaRecepcion: String,
    val fechaEntrega: String? = null,
    val diagnostico: String? = null,
    val total: Double = 0.0,
    val confirmacionClienteEn: String? = null,
    val confirmacionRegistradaPorId: String? = null,
    val confirmacionCanal: String? = null,
    val activo: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

data class DetalleOrdenDto(
    val id: String,
    val ordenId: String,
    val servicio: ServicioResumenDto? = null,
    val refaccion: RefaccionResumenDto? = null,
    val descripcion: String,
    val cantidad: Double,
    val precioUnitario: Double,
    val subtotal: Double,
    val inventarioDevuelto: Boolean = false,
    val activo: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

// ---------- Requests de catálogos y usuarios ----------

data class CreateUsuarioRequest(
    val nombre: String,
    val apellido: String? = null,
    val email: String,
    val password: String,
    val telefono: String? = null,
    val rol: String,
    val tallerId: String? = null,
)

data class UpdateUsuarioRequest(
    val nombre: String? = null,
    val apellido: String? = null,
    val email: String? = null,
    val telefono: String? = null,
    val rol: String? = null,
    val tallerId: String? = null,
    val activo: Boolean? = null,
)

data class CreateClienteRequest(
    val nombre: String,
    val apellido: String? = null,
    val email: String,
    val password: String,
    val telefono: String? = null,
    val tallerId: String? = null,
)

data class UpdateClienteRequest(
    val nombre: String? = null,
    val apellido: String? = null,
    val email: String? = null,
    val telefono: String? = null,
    val password: String? = null,
    val activo: Boolean? = null,
)

data class VincularClienteRequest(
    val clienteId: String,
    val tallerId: String? = null,
)

data class CreateServicioRequest(
    val nombre: String,
    val descripcion: String? = null,
    val categoria: String,
    val precio: Double,
    val duracionMin: Int,
)

data class UpdateServicioRequest(
    val nombre: String? = null,
    val descripcion: String? = null,
    val categoria: String? = null,
    val precio: Double? = null,
    val duracionMin: Int? = null,
)

data class CreateRefaccionRequest(
    val codigo: String,
    val nombre: String,
    val descripcion: String? = null,
    val marca: String? = null,
    val tipo: String = "Refaccion",
    val stock: Int = 0,
    val stockMinimo: Int = 0,
    val precio: Double,
    val tallerId: String? = null,
)

data class UpdateRefaccionRequest(
    val codigo: String? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val marca: String? = null,
    val tipo: String? = null,
    val stock: Int? = null,
    val stockMinimo: Int? = null,
    val precio: Double? = null,
    val tallerId: String? = null,
)

data class RegisterMovementRequest(
    val tipo: String,
    val cantidad: Int? = null,
    val stockNuevo: Int? = null,
    val motivo: String,
)

data class CreateVehiculoRequest(
    val placa: String,
    val vin: String? = null,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val color: String? = null,
    val kilometraje: Int = 0,
    val clienteId: String? = null,
    val tallerId: String? = null,
)

data class UpdateVehiculoRequest(
    val placa: String? = null,
    val vin: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val anio: Int? = null,
    val color: String? = null,
    val kilometraje: Int? = null,
    val clienteId: String? = null,
    val tallerId: String? = null,
)

data class CreateOrdenRequest(
    val vehiculoId: String,
    val tallerId: String? = null,
    val mecanicoId: String? = null,
    val clienteId: String? = null,
    val fechaRecepcion: String? = null,
    val fechaEntrega: String? = null,
    val diagnostico: String? = null,
    val estado: String? = null,
)

data class UpdateOrdenRequest(
    val vehiculoId: String? = null,
    val tallerId: String? = null,
    val mecanicoId: String? = null,
    val clienteId: String? = null,
    val fechaRecepcion: String? = null,
    val fechaEntrega: String? = null,
    val diagnostico: String? = null,
    val estado: String? = null,
)

data class CreateDetalleRequest(
    val ordenId: String,
    val servicioId: String? = null,
    val refaccionId: String? = null,
    val descripcion: String? = null,
    val cantidad: Int,
    val precioUnitario: Double? = null,
)

data class UpdateDetalleRequest(
    val servicioId: String? = null,
    val refaccionId: String? = null,
    val descripcion: String? = null,
    val cantidad: Int? = null,
    val precioUnitario: Double? = null,
)
