package com.example.aceiterosready.data.repository

import com.example.aceiterosready.data.model.CreateDetalleRequest
import com.example.aceiterosready.data.model.CreateOrdenRequest
import com.example.aceiterosready.data.model.DetalleOrdenDto
import com.example.aceiterosready.data.model.OrdenDto
import com.example.aceiterosready.data.model.UpdateDetalleRequest
import com.example.aceiterosready.data.model.UpdateOrdenRequest
import com.example.aceiterosready.data.network.ApiClient
import com.example.aceiterosready.data.network.ApiResult

class OrdenRepository(
    private val apiClient: ApiClient,
) {
    suspend fun listOrdenes(
        estado: String? = null,
        mecanicoId: String? = null,
        clienteId: String? = null,
        activo: Boolean? = null,
    ): ApiResult<List<OrdenDto>> = apiClient.authenticatedCall { bearer ->
        apiClient.api.listOrdenes(
            bearer = bearer,
            estado = estado,
            mecanicoId = mecanicoId,
            clienteId = clienteId,
            activo = activo,
        )
    }

    suspend fun getOrden(id: String): ApiResult<OrdenDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.getOrden(bearer, id) }

    suspend fun createOrden(request: CreateOrdenRequest): ApiResult<OrdenDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.createOrden(bearer, request) }

    suspend fun updateOrden(id: String, request: UpdateOrdenRequest): ApiResult<OrdenDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.updateOrden(bearer, id, request) }

    suspend fun confirmarEntrega(id: String): ApiResult<OrdenDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.confirmarEntrega(bearer, id) }

    suspend fun listDetalles(ordenId: String): ApiResult<List<DetalleOrdenDto>> =
        apiClient.authenticatedCall { bearer ->
            apiClient.api.listDetalles(bearer, ordenId = ordenId)
        }

    suspend fun createDetalle(request: CreateDetalleRequest): ApiResult<DetalleOrdenDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.createDetalle(bearer, request) }

    suspend fun updateDetalle(
        id: String,
        request: UpdateDetalleRequest,
    ): ApiResult<DetalleOrdenDto> = apiClient.authenticatedCall { bearer ->
        apiClient.api.updateDetalle(bearer, id, request)
    }

    suspend fun deleteDetalle(id: String): ApiResult<Unit> =
        apiClient.authenticatedUnitCall { bearer -> apiClient.api.deleteDetalle(bearer, id) }
}
