package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onAddMember: (String, String, Boolean, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Član") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isPaid by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank()) {
                        onAddMember(name, role, isPaid, email, phone)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Dodaj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Odustani")
            }
        },
        title = { Text("Dodaj novog člana") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ime i Prezime") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Uloga (npr. Tajnik, Član)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail adresa") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Broj mobitela") },
                    singleLine = true
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Članarina plaćena")
                    Switch(checked = isPaid, onCheckedChange = { isPaid = it })
                }
            }
        }
    )
}
