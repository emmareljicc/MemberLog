package com.fidit.memberlog.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Patterns
import android.widget.Toast
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.model.MembershipStatus
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.ui.components.MemberAvatar
import com.fidit.memberlog.ui.components.ScreenHeader
import com.fidit.memberlog.ui.theme.Dimens
import com.fidit.memberlog.util.roleColor
import java.io.File
import java.util.UUID

enum class MemberFormMode { REGISTER, ADMIN_CREATE, ADMIN_EDIT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberFormScreen(
    mode: MemberFormMode,
    roles: List<Role>,
    existing: Member? = null,
    onBack: () -> Unit,
    onSubmit: (name: String, roleId: Int, email: String, phone: String, monthlyFeeOverride: Double?, status: String, address: String, notes: String, photoPath: String?, password: String?) -> Unit
) {
    val isRegister = mode == MemberFormMode.REGISTER
    val isEdit = mode == MemberFormMode.ADMIN_EDIT
    val context = LocalContext.current

    var name by remember { mutableStateOf(existing?.name ?: "") }
    var email by remember { mutableStateOf(existing?.email ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
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

    val registerRoleId = roles.firstOrNull { it.name.equals("Član", true) }?.id
        ?: roles.firstOrNull { !it.grantsAdmin }?.id
        ?: roles.firstOrNull()?.id

    val selectedRole = roles.firstOrNull { it.id == roleId }
    val accent = selectedRole?.let { roleColor(it.colorHex) } ?: MaterialTheme.colorScheme.primary

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) copyToInternal(context, uri)?.let { photoPath = it }
    }

    val passwordsMatch = password == confirm
    val valid = name.isNotBlank() && email.isNotBlank() && when (mode) {
        MemberFormMode.REGISTER -> registerRoleId != null && password.isNotBlank() && passwordsMatch
        MemberFormMode.ADMIN_CREATE -> roleId != null && password.isNotBlank()
        MemberFormMode.ADMIN_EDIT -> roleId != null
    }

    val title = when (mode) {
        MemberFormMode.REGISTER -> "Registracija"
        MemberFormMode.ADMIN_CREATE -> "Novi član"
        MemberFormMode.ADMIN_EDIT -> "Uredi člana"
    }
    val confirmLabel = if (isRegister) "Registriraj se" else if (isEdit) "Spremi" else "Dodaj"

    fun submit() {
        if (!valid) return

        if (name.trim().length < 2) {
            Toast.makeText(context, "Ime mora sadržavati najmanje 2 znaka", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            Toast.makeText(context, "Unesite valjanu e-mail adresu", Toast.LENGTH_SHORT).show()
            return
        }

        if (phone.trim().isNotEmpty() && !Patterns.PHONE.matcher(phone.trim()).matches()) {
            Toast.makeText(context, "Unesite valjan broj telefona", Toast.LENGTH_SHORT).show()
            return
        }

        val clubFee = feeOverride.replace(',', '.').toDoubleOrNull()
        if (clubFee != null && clubFee < 0.0) {
            Toast.makeText(context, "Iznos članarine ne može biti negativan", Toast.LENGTH_SHORT).show()
            return
        }

        if (isRegister) {
            if (password.length < 4) {
                Toast.makeText(context, "Lozinka mora imati najmanje 4 znaka", Toast.LENGTH_SHORT).show()
                return
            }
            if (!passwordsMatch) {
                Toast.makeText(context, "Lozinke se ne podudaraju", Toast.LENGTH_SHORT).show()
                return
            }
            onSubmit(name.trim(), registerRoleId!!, email.trim(), phone.trim(), null, "ACTIVE", "", "", null, password)
        } else {
            if (password.isNotEmpty() && password.length < 4) {
                Toast.makeText(context, "Lozinka mora imati najmanje 4 znaka", Toast.LENGTH_SHORT).show()
                return
            }
            onSubmit(
                name.trim(), roleId!!, email.trim(), phone.trim(),
                clubFee, status.name, address, notes, photoPath,
                password.ifBlank { null }
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(Dimens.screenPadding).padding(bottom = NavBarSpace)) {
        ScreenHeader(title = title, subtitle = if (isRegister) "Stvori svoj članski račun" else null, onBack = onBack)
        Spacer(Modifier.height(Dimens.gap))

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isRegister) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MemberAvatar(name = name.ifBlank { "?" }, photoPath = photoPath, color = accent, size = 64.dp)
                    Column {
                        TextButton(onClick = {
                            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(if (photoPath == null) "Dodaj sliku" else "Promijeni sliku")
                        }
                        if (photoPath != null) TextButton(onClick = { photoPath = null }) { Text("Ukloni") }
                    }
                }
            }

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Ime i prezime") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (!isRegister) {
                ExposedDropdownMenuBox(expanded = roleMenuExpanded, onExpandedChange = { roleMenuExpanded = it }) {
                    OutlinedTextField(
                        value = selectedRole?.name ?: "", onValueChange = {}, readOnly = true,
                        label = { Text("Uloga") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = roleMenuExpanded, onDismissRequest = { roleMenuExpanded = false }) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(14.dp).clip(CircleShape).background(roleColor(role.colorHex)))
                                        Spacer(Modifier.width(8.dp))
                                        Text(role.name)
                                    }
                                },
                                onClick = { roleId = role.id; roleMenuExpanded = false }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = statusMenuExpanded, onExpandedChange = { statusMenuExpanded = it }) {
                    OutlinedTextField(
                        value = status.label, onValueChange = {}, readOnly = true,
                        label = { Text("Status članstva") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = statusMenuExpanded, onDismissRequest = { statusMenuExpanded = false }) {
                        MembershipStatus.entries.forEach { s ->
                            DropdownMenuItem(text = { Text(s.label) }, onClick = { status = s; statusMenuExpanded = false })
                        }
                    }
                }
            }

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("E-mail adresa") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone, onValueChange = { phone = it },
                label = { Text("Broj mobitela") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            if (!isRegister) {
                OutlinedTextField(
                    value = address, onValueChange = { address = it },
                    label = { Text("Adresa") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Bilješke") }, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = feeOverride, onValueChange = { feeOverride = it },
                    label = { Text("Mjesečna članarina (EUR, opcionalno)") },
                    placeholder = { Text("Zadani klupski iznos") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text(if (isEdit) "Nova lozinka (opcionalno)" else "Lozinka za prijavu") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            if (isRegister) {
                OutlinedTextField(
                    value = confirm, onValueChange = { confirm = it },
                    label = { Text("Ponovi lozinku") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true, isError = confirm.isNotEmpty() && !passwordsMatch,
                    supportingText = { if (confirm.isNotEmpty() && !passwordsMatch) Text("Lozinke se ne podudaraju") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(Dimens.gap))
        }

        Spacer(Modifier.height(Dimens.gapSmall))
        Button(
            onClick = { submit() },
            enabled = valid,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.small
        ) { Text(confirmLabel) }
    }
}

private fun copyToInternal(context: Context, uri: Uri): String? {
    return try {
        val dir = File(context.filesDir, "photos").apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        file.absolutePath
    } catch (_: Exception) {
        null
    }
}
