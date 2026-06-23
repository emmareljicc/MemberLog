package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.ui.RolesViewModel
import com.fidit.memberlog.util.roleColor

@Composable
fun RolesScreen(
    onBack: () -> Unit,
    viewModel: RolesViewModel = viewModel()
) {
    val roles by viewModel.roles.collectAsState()
    val counts by viewModel.memberCounts.collectAsState()

    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Role?>(null) }
    var deleting by remember { mutableStateOf<Role?>(null) }

    if (showForm) {
        RoleFormDialog(
            title = if (editing == null) "Nova uloga" else "Uredi ulogu",
            existing = editing,
            onDismiss = { showForm = false },
            onSubmit = { name, color ->
                val current = editing
                if (current == null) viewModel.addRole(name, color)
                else viewModel.updateRole(current.copy(name = name, colorHex = color))
                showForm = false
            }
        )
    }

    deleting?.let { role ->
        DeleteRoleDialog(
            role = role,
            memberCount = counts[role.id] ?: 0,
            otherRoles = roles.filter { it.id != role.id },
            onDismiss = { deleting = null },
            onConfirm = { replacementId ->
                viewModel.deleteRole(role, replacementId)
                deleting = null
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

        Text("Uloge", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(
            "Upravljaj ulogama i njihovim bojama",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { editing = null; showForm = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Dodaj ulogu")
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(roles) { role ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { editing = role; showForm = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(roleColor(role.colorHex))
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(role.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${counts[role.id] ?: 0} članova",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { deleting = role }) {
                            Icon(Icons.Default.Delete, contentDescription = "Obriši", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteRoleDialog(
    role: Role,
    memberCount: Int,
    otherRoles: List<Role>,
    onDismiss: () -> Unit,
    onConfirm: (replacementId: Int) -> Unit
) {
    var replacementId by remember { mutableStateOf(otherRoles.firstOrNull()?.id) }
    val blocked = memberCount > 0 && otherRoles.isEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onConfirm(replacementId ?: role.id) },
                enabled = !blocked && (memberCount == 0 || replacementId != null)
            ) { Text("Obriši") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Odustani") } },
        title = { Text("Obriši ulogu \"${role.name}\"") },
        text = {
            Column {
                if (memberCount == 0) {
                    Text("Nijedan član nema ovu ulogu. Sigurno obrisati?")
                } else if (blocked) {
                    Text("Ova uloga ima $memberCount člana, a nema druge uloge na koju ih premjestiti. Prvo dodajte drugu ulogu.")
                } else {
                    Text("$memberCount član(ova) ima ovu ulogu. Premjesti ih na:")
                    Spacer(Modifier.height(8.dp))
                    otherRoles.forEach { r ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = replacementId == r.id, onClick = { replacementId = r.id })
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = replacementId == r.id, onClick = { replacementId = r.id })
                            Spacer(Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(roleColor(r.colorHex))
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(r.name)
                        }
                    }
                }
            }
        }
    )
}
