package com.example.aceiterosready.di

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.aceiterosready.data.auth.AuthRepository
import com.example.aceiterosready.data.network.ApiClient
import com.example.aceiterosready.data.repository.CatalogRepository
import com.example.aceiterosready.data.repository.OrdenRepository
import com.example.aceiterosready.data.repository.UsuarioRepository
import com.example.aceiterosready.data.session.SessionManager

class AppContainer(context: Context) {
    val sessionManager = SessionManager(context.applicationContext)
    val apiClient = ApiClient(sessionManager)
    val authRepository = AuthRepository(apiClient, sessionManager)
    val usuarioRepository = UsuarioRepository(apiClient)
    val catalogRepository = CatalogRepository(apiClient)
    val ordenRepository = OrdenRepository(apiClient)
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer no fue proporcionado")
}

@Composable
inline fun <reified VM : ViewModel> appViewModel(
    crossinline create: (AppContainer) -> VM,
): VM {
    val container = LocalAppContainer.current
    return viewModel(
        factory = viewModelFactory {
            initializer { create(container) }
        },
    )
}
