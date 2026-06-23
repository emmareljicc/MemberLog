package com.fidit.memberlog.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.model.MembershipStatus
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.ui.components.MemberAvatar
import com.fidit.memberlog.util.roleColor
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberFormDialog(
    title: String,
    roles: List<Role>,
    existing: Member? = null,
    confirmLabel: String = "Spremi",
    onDismiss: () -> Unit,
    onSubmit: (name: String, roleId: Int, email: String, phone: String, monthlyFeeOverride: Double?, status: String, address: String, notes: String, photoPath: String?) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var email by remember { mutableStateOf(existing?.email ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var address by remember { mutableStateOf(existing?.address ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var photoPath by remember { mutableStateOf(existing?.photoPath) }
    var feeOverride by remember {
        mutableStateOf(existing?.monthlyFeeOverride?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }
    var roleId by remember { mutableStateOf(existing?.roleId ?: roles.firstOrNull()?.id) }
    var roleMenuExpanded by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf(MembershipStatus.from(existing?.status ?: "ACTIVE")) }
    var statusMenuExpanded by remember { mutableStateOf(false) }
    val selectedRole = roles.firstOrNull { it.id == roleId }
    val accent = selectedRole?.let { roleColor(it.colorHex) } ?: MaterialTheme.colorScheme.primary

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) copyToInternal(context, uri)?.let { photoPath = it }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val rid = roleId
                    if (name.isNotBlank() && email.isNotBlank() && rid != null) {
                        onSubmit(name, rid, email, phone, feeOverride.replace(',', '.').toDoubleOrNull(), status.name, address, notes, photoPath)
                    }
                },
                enabled = name.isNotBlank() && roleId != null
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Odustani") } },
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MemberAvatar(
                        name = name.ifBlank { "?" },
                        photoPath = photoPath,
                        color = accent,
                        size = 64.dp
                    )
                    Column {
                        TextButton(onClick = {
                            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(if (photoPath == null) "Dodaj sliku" else "Promijeni sliku")
                        }
                        if (photoPath != null) {
                            TextButton(onClick = { photoPath = null }) { Text("Ukloni") }
                        }
                    }
                }
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
                ExposedDropdownMenuBox(
                    expanded = statusMenuExpanded,
                    onExpandedChange = { statusMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = status.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status članstva") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenuExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = statusMenuExpanded,
                        onDismissRequest = { statusMenuExpanded = false }
                    ) {
                        MembershipStatus.entries.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.label) },
                                onClick = { status = s; statusMenuExpanded = false }
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
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Adresa") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Bilješke") }
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

private fun copyToInternal(context: Context, uri: Uri): String? {
    return try {
        val dir = File(context.filesDir, "photos").apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}
