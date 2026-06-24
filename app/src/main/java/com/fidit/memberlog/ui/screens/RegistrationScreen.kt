package com.fidit.memberlog.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.ui.AuthViewModel
import com.fidit.memberlog.ui.RolesViewModel
import com.fidit.memberlog.ui.components.LoadingSpinner

@Composable
fun RegistrationScreen(
    onBack: () -> Unit,
    onRegistered: (memberId: Int, isAdmin: Boolean) -> Unit,
    rolesViewModel: RolesViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val roles by rolesViewModel.roles.collectAsState()
    val context = LocalContext.current

    val roleList = roles
    if (roleList == null) {
        LoadingSpinner(Modifier.fillMaxSize())
        return
    }

    MemberFormScreen(
        mode = MemberFormMode.REGISTER,
        roles = roleList,
        onBack = onBack,
        onSubmit = { name, roleId, email, phone, _, _, _, _, _, password ->
            authViewModel.register(name, roleId, email, phone, password ?: "") { result ->
                if (result != null) onRegistered(result.memberId, result.isAdmin)
                else Toast.makeText(context, "E-mail je već registriran", Toast.LENGTH_SHORT).show()
            }
        }
    )
}
