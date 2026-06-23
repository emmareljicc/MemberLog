package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.util.RolePalette
import com.fidit.memberlog.util.roleColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoleFormDialog(
    title: String,
    existing: Role? = null,
    onDismiss: () -> Unit,
    onSubmit: (name: String, colorHex: String, grantsAdmin: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var colorHex by remember { mutableStateOf(existing?.colorHex ?: RolePalette.first()) }
    var grantsAdmin by remember { mutableStateOf(existing?.grantsAdmin ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSubmit(name.trim(), colorHex, grantsAdmin) },
                enabled = name.isNotBlank()
            ) { Text("Spremi") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Odustani") } },
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Naziv uloge") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RolePalette.forEach { hex ->
                        val selected = hex == colorHex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(roleColor(hex))
                                .border(
                                    width = if (selected) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { colorHex = hex }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Administratorski pristup", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Članovi s ovom ulogom upravljaju aplikacijom",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = grantsAdmin, onCheckedChange = { grantsAdmin = it })
                }
            }
        }
    )
}
