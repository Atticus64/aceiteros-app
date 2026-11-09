package com.example.aceiterosready.data.repository

import com.example.aceiterosready.data.model.CreateRefaccionRequest
import com.example.aceiterosready.data.model.CreateServicioRequest
import com.example.aceiterosready.data.model.CreateVehiculoRequest
import com.example.aceiterosready.data.model.MovimientoInventarioDto
import com.example.aceiterosready.data.model.RefaccionDto
import com.example.aceiterosready.data.model.RegisterMovementRequest
import com.example.aceiterosready.data.model.ServicioDto
import com.example.aceiterosready.data.model.TallerDto
import com.example.aceiterosready.data.model.UpdateRefaccionRequest
import com.example.aceiterosready.data.model.UpdateServicioRequest
import com.example.aceiterosready.data.model.UpdateVehiculoRequest
import com.example.aceiterosready.data.model.VehiculoDto
import com.example.aceiterosready.data.network.ApiClient
import com.example.aceiterosready.data.network.ApiResult

class CatalogRepository(
    private val apiClient: ApiClient,
) {
    // Servicios

    suspend fun listServicios(
        search: String? = null,
        categoria: String? = null,
        activo: Boolean? = null,
    ): ApiResult<List<ServicioDto>> = apiClient.authenticatedCall { bearer ->
        apiClient.api.listServicios(
            bearer = bearer,
            search = search,
            categoria = categoria,
            activo = activo,
        )
    }

    suspend fun getServicio(id: String): ApiResult<ServicioDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.getServicio(bearer, id) }

    suspend fun createServicio(request: CreateServicioRequest): ApiResult<ServicioDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.createServicio(bearer, request) }

    suspend fun updateServicio(id: String, request: UpdateServicioRequest): ApiResult<ServicioDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.updateServicio(bearer, id, request) }

    suspend fun deactivateServicio(id: String): ApiResult<Unit> =
        apiClient.authenticatedUnitCall { bearer -> apiClient.api.deleteServicio(bearer, id) }

    // Refacciones

    suspend fun listRefacciones(
        search: String? = null,
        tipo: String? = null,
        activo: Boolean? = null,
    ): ApiResult<List<RefaccionDto>> = apiClient.authenticatedCall { bearer ->
        apiClient.api.listRefacciones(
            bearer = bearer,
            search = search,
            tipo = tipo,
            activo = activo,
        )
    }

    suspend fun getRefaccion(id: String): ApiResult<RefaccionDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.getRefaccion(bearer, id) }

    suspend fun createRefaccion(request: CreateRefaccionRequest): ApiResult<RefaccionDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.createRefaccion(bearer, request) }

    suspend fun updateRefaccion(id: String, request: UpdateRefaccionRequest): ApiResult<RefaccionDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.updateRefaccion(bearer, id, request) }

    suspend fun deactivateRefaccion(id: String): ApiResult<Unit> =
        apiClient.authenticatedUnitCall { bearer -> apiClient.api.deleteRefaccion(bearer, id) }

    suspend fun listMovimientos(refaccionId: String): ApiResult<List<MovimientoInventarioDto>> =
        apiClient.authenticatedCall { bearer ->
            apiClient.api.listMovimientos(bearer, refaccionId)
        }

    suspend fun createMovimiento(
        refaccionId: String,
        request: RegisterMovementRequest,
    ): ApiResult<RefaccionDto> = apiClient.authenticatedCall { bearer ->
        apiClient.api.createMovimiento(bearer, refaccionId, request)
    }

    // Vehículos

    suspend fun listVehiculos(
        search: String? = null,
        clienteId: String? = null,
        activo: Boolean? = null,
    ): ApiResult<List<VehiculoDto>> = apiClient.authenticatedCall { bearer ->
        apiClient.api.listVehiculos(
            bearer = bearer,
            search = search,
            clienteId = clienteId,
            activo = activo,
        )
    }

    suspend fun getVehiculo(id: String): ApiResult<VehiculoDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.getVehiculo(bearer, id) }

    suspend fun createVehiculo(request: CreateVehiculoRequest): ApiResult<VehiculoDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.createVehiculo(bearer, request) }

    suspend fun updateVehiculo(id: String, request: UpdateVehiculoRequest): ApiResult<VehiculoDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.updateVehiculo(bearer, id, request) }

    suspend fun deactivateVehiculo(id: String): ApiResult<Unit> =
        apiClient.authenticatedUnitCall { bearer -> apiClient.api.deleteVehiculo(bearer, id) }

    // Talleres

    suspend fun listTalleres(): ApiResult<List<TallerDto>> =
        apiClient.authenticatedCall { bearer -> apiClient.api.listTalleres(bearer) }

    suspend fun getTaller(id: String): ApiResult<TallerDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.getTaller(bearer, id) }
}
