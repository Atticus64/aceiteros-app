package com.example.aceiterosready.data.repository

import com.example.aceiterosready.data.model.ClienteDto
import com.example.aceiterosready.data.model.CreateClienteRequest
import com.example.aceiterosready.data.model.CreateUsuarioRequest
import com.example.aceiterosready.data.model.RolDto
import com.example.aceiterosready.data.model.UpdateClienteRequest
import com.example.aceiterosready.data.model.UpdateUsuarioRequest
import com.example.aceiterosready.data.model.UserDto
import com.example.aceiterosready.data.model.VincularClienteRequest
import com.example.aceiterosready.data.network.ApiClient
import com.example.aceiterosready.data.network.ApiResult

class UsuarioRepository(
    private val apiClient: ApiClient,
) {
    suspend fun listUsuarios(
        rol: String? = null,
        search: String? = null,
    ): ApiResult<List<UserDto>> = apiClient.authenticatedCall { bearer ->
        apiClient.api.listUsuarios(bearer = bearer, rol = rol, search = search)
    }

    suspend fun getUsuario(id: String): ApiResult<UserDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.getUsuario(bearer, id) }

    suspend fun createUsuario(request: CreateUsuarioRequest): ApiResult<UserDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.createUsuario(bearer, request) }

    suspend fun updateUsuario(id: String, request: UpdateUsuarioRequest): ApiResult<UserDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.updateUsuario(bearer, id, request) }

    suspend fun deactivateUsuario(id: String): ApiResult<Unit> =
        apiClient.authenticatedUnitCall { bearer -> apiClient.api.deleteUsuario(bearer, id) }

    suspend fun listMecanicos(search: String? = null): ApiResult<List<UserDto>> =
        apiClient.authenticatedCall { bearer ->
            apiClient.api.listMecanicos(bearer = bearer, search = search)
        }

    suspend fun listClientes(search: String? = null): ApiResult<List<ClienteDto>> =
        apiClient.authenticatedCall { bearer ->
            apiClient.api.listClientes(bearer = bearer, search = search)
        }

    suspend fun getCliente(id: String): ApiResult<ClienteDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.getCliente(bearer, id) }

    suspend fun buscarCliente(
        email: String? = null,
        telefono: String? = null,
    ): ApiResult<ClienteDto> = apiClient.authenticatedCall { bearer ->
        apiClient.api.buscarCliente(bearer, email = email, telefono = telefono)
    }

    suspend fun createCliente(request: CreateClienteRequest): ApiResult<ClienteDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.createCliente(bearer, request) }

    suspend fun updateCliente(id: String, request: UpdateClienteRequest): ApiResult<ClienteDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.updateCliente(bearer, id, request) }

    suspend fun vincularCliente(request: VincularClienteRequest): ApiResult<ClienteDto> =
        apiClient.authenticatedCall { bearer -> apiClient.api.vincularCliente(bearer, request) }

    suspend fun listRoles(): ApiResult<List<RolDto>> =
        apiClient.authenticatedCall { bearer -> apiClient.api.listRoles(bearer) }
}
