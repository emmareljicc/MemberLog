package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.util.roleColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberFormDialog(
    title: String,
    roles: List<Role>,
    existing: Member? = null,
    confirmLabel: String = "Spremi",
    onDismiss: () -> Unit,
    onSubmit: (name: String, roleId: Int, email: String, phone: String, monthlyFeeOverride: Double?) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var email by remember { mutableStateOf(existing?.email ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var feeOverride by remember {
        mutableStateOf(existing?.monthlyFeeOverride?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }
    var roleId by remember { mutableStateOf(existing?.roleId ?: roles.firstOrNull()?.id) }
    var roleMenuExpanded by remember { mutableStateOf(false) }
    val selectedRole = roles.firstOrNull { it.id == roleId }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val rid = roleId
                    if (name.isNotBlank() && email.isNotBlank() && rid != null) {
                        onSubmit(name, rid, email, phone, feeOverride.replace(',', '.').toDoubleOrNull())
                    }
                },
                enabled = name.isNotBlank() && roleId != null
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Odustani") } },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ime i Prezime") },
                    singleLine = true
                )
                ExposedDropdownMenuBox(
                    expanded = roleMenuExpanded,
                    onExpandedChange = { roleMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedRole?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Uloga") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleMenuExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false }
                    ) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(roleColor(role.colorHex))
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(role.name)
                                    }
                                },
                                onClick = { roleId = role.id; roleMenuExpanded = false }
                            )
                        }
                    }
                }
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
