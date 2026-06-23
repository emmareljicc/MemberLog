package com.fidit.memberlog.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.ui.theme.DisplayFont
import com.fidit.memberlog.model.UserRole
import com.fidit.memberlog.ui.AuthViewModel

@Composable
fun AccountsScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val users by authViewModel.users.collectAsState()
    val context = LocalContext.current
    var showAdd by remember { mutableStateOf(false) }

    if (showAdd) {
        AccountFormDialog(
            onDismiss = { showAdd = false },
            onSubmit = { username, password, role ->
                authViewModel.addUser(username, password, role) { ok ->
                    if (!ok) Toast.makeText(context, "Korisničko ime već postoji", Toast.LENGTH_SHORT).show()
                }
                showAdd = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onBack() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Natrag", tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Natrag", color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))
        Text("Korisnici", fontFamily = DisplayFont, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Računi i razine pristupa", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { showAdd = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Dodaj korisnika")
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(users) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.username, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (user.role == UserRole.ADMIN.name) "Administrator" else "Preglednik",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = {
                            authViewModel.deleteUser(user) { ok ->
                                if (!ok) Toast.makeText(context, "Mora postojati barem jedan administrator", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Obriši", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountFormDialog(
    onDismiss: () -> Unit,
    onSubmit: (username: String, password: String, role: UserRole) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.VIEWER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { if (username.isNotBlank() && password.length >= 4) onSubmit(username, password, role) },
                enabled = username.isNotBlank() && password.length >= 4
            ) { Text("Spremi") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Odustani") } },
        title = { Text("Novi korisnik") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Korisničko ime") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Lozinka (min. 4 znaka)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                Text("Razina pristupa", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserRole.entries.forEach { r ->
                        Row(
                            modifier = Modifier
                                .selectable(selected = role == r, onClick = { role = r })
                                .padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = role == r, onClick = { role = r })
                            Text(if (r == UserRole.ADMIN) "Admin" else "Preglednik")
                        }
                    }
                }
            }
        }
    )
}
