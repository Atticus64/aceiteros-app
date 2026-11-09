package com.example.aceiterosready.data.api

import com.example.aceiterosready.data.model.ChangePasswordRequest
import com.example.aceiterosready.data.model.ClienteDto
import com.example.aceiterosready.data.model.CreateClienteRequest
import com.example.aceiterosready.data.model.CreateDetalleRequest
import com.example.aceiterosready.data.model.CreateOrdenRequest
import com.example.aceiterosready.data.model.CreateRefaccionRequest
import com.example.aceiterosready.data.model.CreateServicioRequest
import com.example.aceiterosready.data.model.CreateUsuarioRequest
import com.example.aceiterosready.data.model.CreateVehiculoRequest
import com.example.aceiterosready.data.model.DetalleOrdenDto
import com.example.aceiterosready.data.model.LoginData
import com.example.aceiterosready.data.model.LoginRequest
import com.example.aceiterosready.data.model.MovimientoInventarioDto
import com.example.aceiterosready.data.model.OrdenDto
import com.example.aceiterosready.data.model.RefaccionDto
import com.example.aceiterosready.data.model.RefreshTokenRequest
import com.example.aceiterosready.data.model.RegisterMovementRequest
import com.example.aceiterosready.data.model.RegisterRequest
import com.example.aceiterosready.data.model.RolDto
import com.example.aceiterosready.data.model.ServicioDto
import com.example.aceiterosready.data.model.TallerDto
import com.example.aceiterosready.data.model.TokensDto
import com.example.aceiterosready.data.model.UpdateClienteRequest
import com.example.aceiterosready.data.model.UpdateDetalleRequest
import com.example.aceiterosready.data.model.UpdateOrdenRequest
import com.example.aceiterosready.data.model.UpdateProfileRequest
import com.example.aceiterosready.data.model.UpdateRefaccionRequest
import com.example.aceiterosready.data.model.UpdateServicioRequest
import com.example.aceiterosready.data.model.UpdateUsuarioRequest
import com.example.aceiterosready.data.model.UpdateVehiculoRequest
import com.example.aceiterosready.data.model.UserDto
import com.example.aceiterosready.data.model.VehiculoDto
import com.example.aceiterosready.data.model.VincularClienteRequest
import com.example.aceiterosready.data.network.ApiEnvelope
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {


    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<ApiEnvelope<LoginData>>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<ApiEnvelope<UserDto>>

    @POST("auth/refresh-token")
    suspend fun refreshToken(
        @Body body: RefreshTokenRequest,
    ): Response<ApiEnvelope<TokensDto>>

    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") bearer: String,
        @Body body: RefreshTokenRequest,
    ): Response<Unit>

    @GET("auth/me")
    suspend fun me(
        @Header("Authorization") bearer: String,
    ): Response<ApiEnvelope<UserDto>>

    @PATCH("auth/me")
    suspend fun updateMe(
        @Header("Authorization") bearer: String,
        @Body body: UpdateProfileRequest,
    ): Response<ApiEnvelope<UserDto>>

    @PATCH("auth/password")
    suspend fun changePassword(
        @Header("Authorization") bearer: String,
        @Body body: ChangePasswordRequest,
    ): Response<Unit>

    // ---------- Usuarios ----------

    @GET("usuarios")
    suspend fun listUsuarios(
        @Header("Authorization") bearer: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("rol") rol: String? = null,
        @Query("search") search: String? = null,
        @Query("activo") activo: Boolean? = null,
    ): Response<ApiEnvelope<List<UserDto>>>

    @GET("usuarios/{id}")
    suspend fun getUsuario(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
    ): Response<ApiEnvelope<UserDto>>

    @POST("usuarios")
    suspend fun createUsuario(
        @Header("Authorization") bearer: String,
        @Body body: CreateUsuarioRequest,
    ): Response<ApiEnvelope<UserDto>>

    @PATCH("usuarios/{id}")
    suspend fun updateUsuario(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: UpdateUsuarioRequest,
    ): Response<ApiEnvelope<UserDto>>

    @DELETE("usuarios/{id}")
    suspend fun deleteUsuario(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
    ): Response<Unit>

    @GET("usuarios/mecanicos")
    suspend fun listMecanicos(
        @Header("Authorization") bearer: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("search") search: String? = null,
        @Query("activo") activo: Boolean? = null,
    ): Response<ApiEnvelope<List<UserDto>>>

    @GET("usuarios/clientes")
    suspend fun listClientes(
        @Header("Authorization") bearer: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("search") search: String? = null,
    ): Response<ApiEnvelope<List<ClienteDto>>>

    @GET("usuarios/clientes/{id}")
    suspend fun getCliente(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
    ): Response<ApiEnvelope<ClienteDto>>

    @GET("usuarios/clientes/buscar")
    suspend fun buscarCliente(
        @Header("Authorization") bearer: String,
        @Query("email") email: String? = null,
        @Query("telefono") telefono: String? = null,
    ): Response<ApiEnvelope<ClienteDto>>

    @POST("usuarios/clientes")
    suspend fun createCliente(
        @Header("Authorization") bearer: String,
        @Body body: CreateClienteRequest,
    ): Response<ApiEnvelope<ClienteDto>>

    @PATCH("usuarios/clientes/{id}")
    suspend fun updateCliente(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: UpdateClienteRequest,
    ): Response<ApiEnvelope<ClienteDto>>

    @POST("usuarios/clientes/vincular")
    suspend fun vincularCliente(
        @Header("Authorization") bearer: String,
        @Body body: VincularClienteRequest,
    ): Response<ApiEnvelope<ClienteDto>>

    @GET("roles")
    suspend fun listRoles(
        @Header("Authorization") bearer: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): Response<ApiEnvelope<List<RolDto>>>

    // ---------- Talleres ----------

    @GET("talleres")
    suspend fun listTalleres(
        @Header("Authorization") bearer: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("search") search: String? = null,
        @Query("activo") activo: Boolean? = null,
    ): Response<ApiEnvelope<List<TallerDto>>>

    @GET("talleres/{id}")
    suspend fun getTaller(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
    ): Response<ApiEnvelope<TallerDto>>

    // ---------- Servicios ----------

    @GET("servicios")
    suspend fun listServicios(
        @Header("Authorization") bearer: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("search") search: String? = null,
        @Query("categoria") categoria: String? = null,
        @Query("activo") activo: Boolean? = null,
    ): Response<ApiEnvelope<List<ServicioDto>>>

    @GET("servicios/{id}")
    suspend fun getServicio(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
    ): Response<ApiEnvelope<ServicioDto>>

    @POST("servicios")
    suspend fun createServicio(
        @Header("Authorization") bearer: String,
        @Body body: CreateServicioRequest,
    ): Response<ApiEnvelope<ServicioDto>>

    @PATCH("servicios/{id}")
    suspend fun updateServicio(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: UpdateServicioRequest,
    ): Response<ApiEnvelope<ServicioDto>>

    @DELETE("servicios/{id}")
    suspend fun deleteServicio(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
    ): Response<Unit>

    // ---------- Refacciones ----------

    @GET("refacciones")
    suspend fun listRefacciones(
        @Header("Authorization") bearer: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("search") search: String? = null,
        @Query("tipo") tipo: String? = null,
        @Query("activo") activo: Boolean? = null,
        @Query("tallerId") tallerId: String? = null,
    ): Response<ApiEnvelope<List<RefaccionDto>>>

    @GET("refacciones/{id}")
    suspend fun getRefaccion(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
    ): Response<ApiEnvelope<RefaccionDto>>

    @POST("refacciones")
    suspend fun createRefaccion(
        @Header("Authorization") bearer: String,
        @Body body: CreateRefaccionRequest,
    ): Response<ApiEnvelope<RefaccionDto>>

    @PATCH("refacciones/{id}")
    suspend fun updateRefaccion(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: UpdateRefaccionRequest,
    ): Response<ApiEnvelope<RefaccionDto>>

    @DELETE("refacciones/{id}")
    suspend fun deleteRefaccion(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
    ): Response<Unit>

    @GET("refacciones/{id}/movimientos")
    suspend fun listMovimientos(
        @Header("Authorization") bearer: String,
        @Path("id") refaccionId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): Response<ApiEnvelope<List<MovimientoInventarioDto>>>

    @POST("refacciones/{id}/movimientos")
    suspend fun createMovimiento(
        @Header("Authorization") bearer: String,
        @Path("id") refaccionId: String,
        @Body body: RegisterMovementRequest,
    ): Response<ApiEnvelope<RefaccionDto>>

    // ---------- Vehículos ----------

    @GET("vehiculos")
    suspend fun listVehiculos(
        @Header("Authorization") bearer: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("search") search: String? = null,
        @Query("clienteId") clienteId: String? = null,
        @Query("tallerId") tallerId: String? = null,
        @Query("activo") activo: Boolean? = null,
    ): Response<ApiEnvelope<List<VehiculoDto>>>

    @GET("vehiculos/{id}")
    suspend fun getVehiculo(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
    ): Response<ApiEnvelope<VehiculoDto>>

    @POST("vehiculos")
    suspend fun createVehiculo(
        @Header("Authorization") bearer: String,
        @Body body: CreateVehiculoRequest,
    ): Response<ApiEnvelope<VehiculoDto>>

    @PATCH("vehiculos/{id}")
    suspend fun updateVehiculo(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: UpdateVehiculoRequest,
    ): Response<ApiEnvelope<VehiculoDto>>

    @DELETE("vehiculos/{id}")
    suspend fun deleteVehiculo(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
    ): Response<Unit>

    // ---------- Órdenes ----------

    @GET("ordenes")
    suspend fun listOrdenes(
        @Header("Authorization") bearer: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("estado") estado: String? = null,
        @Query("mecanicoId") mecanicoId: String? = null,
        @Query("clienteId") clienteId: String? = null,
        @Query("activo") activo: Boolean? = null,
    ): Response<ApiEnvelope<List<OrdenDto>>>

    @GET("ordenes/{id}")
    suspend fun getOrden(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
    ): Response<ApiEnvelope<OrdenDto>>

    @POST("ordenes")
    suspend fun createOrden(
        @Header("Authorization") bearer: String,
        @Body body: CreateOrdenRequest,
    ): Response<ApiEnvelope<OrdenDto>>

    @PATCH("ordenes/{id}")
    suspend fun updateOrden(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: UpdateOrdenRequest,
    ): Response<ApiEnvelope<OrdenDto>>

    @POST("ordenes/{id}/confirmar-entrega")
    suspend fun confirmarEntrega(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
    ): Response<ApiEnvelope<OrdenDto>>

    // ---------- Detalles de orden ----------

    @GET("detalleordenes")
    suspend fun listDetalles(
        @Header("Authorization") bearer: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("ordenId") ordenId: String,
        @Query("activo") activo: Boolean? = null,
    ): Response<ApiEnvelope<List<DetalleOrdenDto>>>

    @POST("detalleordenes")
    suspend fun createDetalle(
        @Header("Authorization") bearer: String,
        @Body body: CreateDetalleRequest,
    ): Response<ApiEnvelope<DetalleOrdenDto>>

    @PATCH("detalleordenes/{id}")
    suspend fun updateDetalle(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: UpdateDetalleRequest,
    ): Response<ApiEnvelope<DetalleOrdenDto>>

    @DELETE("detalleordenes/{id}")
    suspend fun deleteDetalle(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
    ): Response<Unit>
}
