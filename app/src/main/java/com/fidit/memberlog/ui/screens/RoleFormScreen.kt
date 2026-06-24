package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.ui.components.ScreenHeader
import com.fidit.memberlog.ui.theme.Dimens
import com.fidit.memberlog.util.RolePalette
import com.fidit.memberlog.util.roleColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoleFormScreen(
    title: String,
    existing: Role? = null,
    onBack: () -> Unit,
    onSubmit: (name: String, colorHex: String, grantsAdmin: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var colorHex by remember { mutableStateOf(existing?.colorHex ?: RolePalette.first()) }
    var grantsAdmin by remember { mutableStateOf(existing?.grantsAdmin ?: false) }
    val valid = name.isNotBlank()

    Column(modifier = Modifier.fillMaxSize().padding(Dimens.screenPadding).padding(bottom = NavBarSpace)) {
        ScreenHeader(title = title, onBack = onBack)
        Spacer(Modifier.height(Dimens.gap))
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Naziv uloge") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            Text("Boja", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RolePalette.forEach { hex ->
                    val selected = hex == colorHex
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(roleColor(hex))
                            .border(width = if (selected) 3.dp else 0.dp, color = MaterialTheme.colorScheme.onSurface, shape = CircleShape)
                            .clickable { colorHex = hex }
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Administratorski pristup", fontWeight = FontWeight.SemiBold)
                    Text("Članovi s ovom ulogom upravljaju aplikacijom", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = grantsAdmin, onCheckedChange = { grantsAdmin = it })
            }
            Spacer(Modifier.height(Dimens.gap))
        }
        Spacer(Modifier.height(Dimens.gapSmall))
        Button(
            onClick = { if (valid) onSubmit(name.trim(), colorHex, grantsAdmin) },
            enabled = valid,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.small
        ) { Text("Spremi") }
    }
}
