package com.example.aceiterosready

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aceiterosready.di.LocalAppContainer
import com.example.aceiterosready.di.appViewModel
import com.example.aceiterosready.presentation.auth.AuthViewModel
import com.example.aceiterosready.presentation.auth.LoginScreen
import com.example.aceiterosready.presentation.auth.RegisterScreen
import com.example.aceiterosready.presentation.auth.SessionState
import com.example.aceiterosready.presentation.navigation.MainApp
import com.example.aceiterosready.ui.theme.AceiterosReadyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as AceiterosApp).container

        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                AceiterosReadyTheme {
                    val authViewModel: AuthViewModel = appViewModel {
                        AuthViewModel(it.authRepository)
                    }
                    val uiState by authViewModel.uiState.collectAsState()

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        when (val session = uiState.session) {
                            SessionState.Restoring -> RestoringScreen()
                            SessionState.Unauthenticated -> AuthNavHost(authViewModel)
                            is SessionState.Authenticated -> MainApp(
                                user = session.user,
                                onLogout = authViewModel::logout,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthNavHost(viewModel: AuthViewModel) {
    val navController = rememberNavController()
    var loginEmail by remember { mutableStateOf("") }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                initialEmail = loginEmail,
                onNavigateToRegister = { navController.navigate("register") },
            )
        }
        composable("register") {
            RegisterScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onRegistered = { email ->
                    loginEmail = email
                    navController.popBackStack()
                },
            )
        }
    }
}

@Composable
private fun RestoringScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
