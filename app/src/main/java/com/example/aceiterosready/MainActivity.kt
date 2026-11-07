package com.example.aceiterosready

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aceiterosready.presentation.ServiceViewModel
import com.example.aceiterosready.presentation.auth.LoginScreen
import com.example.aceiterosready.presentation.auth.RegisterScreen
import com.example.aceiterosready.ui.theme.AceiterosReadyTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ServiceViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    LoginScreen(viewModel)
                }
                composable("register") {
                    RegisterScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun WelcomeCard(modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        Column {
            AutoImage()
        }
    }
}

@Composable
fun AutoImage() {
    Image (
        painter = painterResource(id = (R.drawable.carrazo)),
        contentDescription = null
    )
}
