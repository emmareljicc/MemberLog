package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fidit.memberlog.model.Member

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberFormDialog(
    title: String,
    existing: Member? = null,
    confirmLabel: String = "Spremi",
    onDismiss: () -> Unit,
    onSubmit: (name: String, role: String, email: String, phone: String, monthlyFeeOverride: Double?) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var role by remember { mutableStateOf(existing?.role ?: "Član") }
    var email by remember { mutableStateOf(existing?.email ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var feeOverride by remember {
        mutableStateOf(existing?.monthlyFeeOverride?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank()) {
                        val override = feeOverride.replace(',', '.').toDoubleOrNull()
                        onSubmit(name, role, email, phone, override)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Odustani")
            }
        },
        title = { Text(title) },
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
                OutlinedTextField(
                    value = feeOverride,
                    onValueChange = { feeOverride = it },
                    label = { Text("Mjesečna članarina (EUR, opcionalno)") },
                    placeholder = { Text("Zadano klupski iznos") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }
    )
}
