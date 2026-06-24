package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.fidit.memberlog.ui.components.AppCard
import com.fidit.memberlog.ui.components.LoadingSpinner
import com.fidit.memberlog.ui.components.ScreenHeader
import com.fidit.memberlog.ui.theme.Dimens
import com.fidit.memberlog.util.Plurals
import com.fidit.memberlog.util.roleColor

@Composable
fun RolesScreen(
    isAdmin: Boolean,
    onBack: () -> Unit,
    viewModel: RolesViewModel = viewModel()
) {
    val rolesState by viewModel.roles.collectAsState()
    val countsState by viewModel.memberCounts.collectAsState()

    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Role?>(null) }
    var deleting by remember { mutableStateOf<Role?>(null) }

    if (showForm) {
        RoleFormScreen(
            title = if (editing == null) "Nova uloga" else "Uredi ulogu",
            existing = editing,
            onBack = { showForm = false },
            onSubmit = { name, color, grantsAdmin ->
                val current = editing
                if (current == null) viewModel.addRole(name, color, grantsAdmin)
                else viewModel.updateRole(current.copy(name = name, colorHex = color, grantsAdmin = grantsAdmin))
                showForm = false
            }
        )
        return
    }

    val roles = rolesState
    val counts = countsState
    if (roles == null || counts == null) {
        LoadingSpinner(Modifier.fillMaxSize())
        return
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
        ScreenHeader(title = "Uloge", subtitle = "Upravljaj ulogama i njihovim bojama", onBack = onBack)

        Spacer(Modifier.height(Dimens.gap))

        if (isAdmin) {
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
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(roles) { role ->
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = if (isAdmin) ({ editing = role; showForm = true }) else null,
                    contentPadding = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                Plurals.clanovi(counts[role.id] ?: 0) + if (role.grantsAdmin) " • administrator" else "",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isAdmin) {
                            IconButton(onClick = { deleting = role }) {
                                Icon(Icons.Default.Delete, contentDescription = "Obriši", tint = MaterialTheme.colorScheme.error)
                            }
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
                    Text("Ova uloga ima ${Plurals.clanovi(memberCount)}, a nema druge uloge na koju ih premjestiti. Prvo dodaj drugu ulogu.")
                } else {
                    Text("${Plurals.clanovi(memberCount)} ima ovu ulogu. Premjesti ih na:")
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
