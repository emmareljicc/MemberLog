package com.fidit.memberlog.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.ui.AuthViewModel
import com.fidit.memberlog.ui.theme.DisplayFont

@Composable
fun LoginScreen(
    onLoginSuccess: (memberId: Int, isAdmin: Boolean) -> Unit,
    onRegister: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .navigationBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "MemberLog",
            fontFamily = DisplayFont,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Prijavi se za nastavak",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Lozinka") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    authViewModel.login(email, password) { result ->
                        if (result != null) onLoginSuccess(result.memberId, result.isAdmin)
                        else Toast.makeText(context, "Neispravni podaci za prijavu", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Unesi e-mail i lozinku", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("PRIJAVI SE", fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = onRegister, modifier = Modifier.padding(top = 4.dp)) {
            Text("Nemaš račun? Registriraj se")
        }

        Spacer(modifier = Modifier.height(8.dp))
        DevLoginButtons(authViewModel = authViewModel, onLoginSuccess = onLoginSuccess)
    }
}
