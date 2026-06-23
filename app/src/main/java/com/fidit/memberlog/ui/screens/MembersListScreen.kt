package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
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
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.model.MembershipStatus
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.ui.components.MemberAvatar
import com.fidit.memberlog.ui.theme.DisplayFont
import com.fidit.memberlog.ui.theme.GreenSuccess
import com.fidit.memberlog.ui.theme.RedAlert
import com.fidit.memberlog.util.roleColor

@Composable
fun MembersListScreen(
    members: List<Member>,
    owedByMember: Map<Int, Double>,
    rolesById: Map<Int, Role>,
    onMemberClick: (Member) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf<Int?>(null) }
    var statusFilter by remember { mutableStateOf<MembershipStatus?>(null) }
    var owingOnly by remember { mutableStateOf(false) }

    val roleList = rolesById.values.sortedBy { it.name }
    val filtered = members.filter { m ->
        (query.isBlank() || m.name.foldCro().contains(query.foldCro())) &&
            (roleFilter == null || m.roleId == roleFilter) &&
            (statusFilter == null || m.status == statusFilter!!.name) &&
            (!owingOnly || (owedByMember[m.id] ?: 0.0) > 0.0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Popis članova",
            fontFamily = DisplayFont,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Klikni na člana za detaljniji pregled",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Pretraži članove") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = owingOnly,
                onClick = { owingOnly = !owingOnly },
                label = { Text("Duguje") }
            )
            MembershipStatus.entries.forEach { s ->
                FilterChip(
                    selected = statusFilter == s,
                    onClick = { statusFilter = if (statusFilter == s) null else s },
                    label = { Text(s.label) }
                )
            }
            roleList.forEach { role ->
                FilterChip(
                    selected = roleFilter == role.id,
                    onClick = { roleFilter = if (roleFilter == role.id) null else role.id },
                    label = { Text(role.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Nema članova koji odgovaraju filtrima.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered) { member ->
                val role = rolesById[member.roleId]
                val avatarColor = role?.let { roleColor(it.colorHex) } ?: MaterialTheme.colorScheme.primary
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMemberClick(member) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MemberAvatar(
                            name = member.name,
                            photoPath = member.photoPath,
                            color = avatarColor,
                            size = 48.dp
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = member.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RoleChip(name = role?.name ?: "—", color = avatarColor)
                                val owed = owedByMember[member.id] ?: 0.0
                                val owing = owed > 0.0
                                Badge(
                                    containerColor = if (owing) RedAlert else GreenSuccess
                                ) {
                                    Text(
                                        if (owing) "Duguje ${money(owed)}" else "Podmireno",
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleChip(name: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun String.foldCro(): String =
    java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase()

private fun money(v: Double): String =
    (if (v % 1.0 == 0.0) v.toInt().toString() else "%.2f".format(v)) + " €"
