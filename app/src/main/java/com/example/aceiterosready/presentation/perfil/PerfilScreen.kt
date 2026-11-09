package com.example.aceiterosready.presentation.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aceiterosready.data.model.UserDto
import com.example.aceiterosready.di.appViewModel
import com.example.aceiterosready.presentation.common.ConfirmDialog
import com.example.aceiterosready.presentation.common.InlineMessage
import com.example.aceiterosready.presentation.common.PasswordField
import com.example.aceiterosready.presentation.common.RolChip
import com.example.aceiterosready.presentation.common.ScreenScaffold

@Composable
fun PerfilScreen(
    user: UserDto,
    onBack: () -> Unit,
    onLogout: () -> Unit,
) {
    val vm: PerfilViewModel = appViewModel {
        PerfilViewModel(it.authRepository)
    }
    val state by vm.state.collectAsState()
    var showLogout by remember { mutableStateOf(false) }

    LaunchedEffect(user.id) { vm.init(user) }

    ScreenScaffold(
        title = "Perfil",
        onBack = onBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Datos de la cuenta",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = state.email,
                style = MaterialTheme.typography.bodyLarge,
            )
            RolChip(state.rol)

            HorizontalDivider()

            InlineMessage(state.error, isError = true)
            InlineMessage(state.message)

            OutlinedTextField(
                value = state.nombre,
                onValueChange = vm::setNombre,
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.apellido,
                onValueChange = vm::setApellido,
                label = { Text("Apellido") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.telefono,
                onValueChange = vm::setTelefono,
                label = { Text("Teléfono") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = vm::guardarPerfil,
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Guardar cambios")
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Cambiar contraseña",
                style = MaterialTheme.typography.titleMedium,
            )
            InlineMessage(state.passwordError, isError = true)
            InlineMessage(state.passwordMessage)

            PasswordField(
                value = state.currentPassword,
                onValueChange = vm::setCurrentPassword,
                label = "Contraseña actual",
            )
            PasswordField(
                value = state.newPassword,
                onValueChange = vm::setNewPassword,
                label = "Nueva contraseña",
                supportingText = "Mínimo 8 caracteres.",
            )

            Button(
                onClick = vm::cambiarPassword,
                enabled = !state.changingPassword,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.changingPassword) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Actualizar contraseña")
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showLogout = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Cerrar sesión",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }

    if (showLogout) {
        ConfirmDialog(
            title = "Cerrar sesión",
            text = "¿Deseas salir de tu cuenta?",
            confirmText = "Salir",
            onConfirm = {
                showLogout = false
                onLogout()
            },
            onDismiss = { showLogout = false },
        )
    }
}
