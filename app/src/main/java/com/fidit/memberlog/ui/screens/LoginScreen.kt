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
import com.fidit.memberlog.model.UserRole
import com.fidit.memberlog.ui.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (UserRole) -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val hasUsers by authViewModel.hasUsers.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    val firstRun = !hasUsers

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
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (firstRun) "Kreiraj administratora" else "Prijavi se za nastavak",
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
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Korisničko ime") },
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
                if (firstRun) {
                    OutlinedTextField(
                        value = confirm,
                        onValueChange = { confirm = it },
                        label = { Text("Ponovi lozinku") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (firstRun) {
                    when {
                        username.isBlank() || password.isBlank() ->
                            Toast.makeText(context, "Popunite sva polja", Toast.LENGTH_SHORT).show()
                        password.length < 4 ->
                            Toast.makeText(context, "Lozinka mora imati barem 4 znaka", Toast.LENGTH_SHORT).show()
                        password != confirm ->
                            Toast.makeText(context, "Lozinke se ne podudaraju", Toast.LENGTH_SHORT).show()
                        else -> authViewModel.createFirstAdmin(username, password) { ok ->
                            if (ok) onLoginSuccess(UserRole.ADMIN)
                            else Toast.makeText(context, "Korisničko ime već postoji", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else if (username.isNotBlank() && password.isNotBlank()) {
                    authViewModel.login(username, password) { role ->
                        if (role != null) onLoginSuccess(role)
                        else Toast.makeText(context, "Neispravni podaci za prijavu", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Unesite podatke", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(if (firstRun) "KREIRAJ I PRIJAVI SE" else "PRIJAVI SE", fontWeight = FontWeight.Bold)
        }
    }
}
