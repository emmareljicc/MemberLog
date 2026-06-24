package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.model.MembershipStatus
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.ui.components.AppCard
import com.fidit.memberlog.ui.components.MemberAvatar
import com.fidit.memberlog.ui.components.LoadingSpinner
import com.fidit.memberlog.ui.components.RoleChip
import com.fidit.memberlog.ui.components.ScreenHeader
import com.fidit.memberlog.ui.components.StatusPill
import com.fidit.memberlog.ui.theme.Dimens
import com.fidit.memberlog.ui.theme.paidColor
import com.fidit.memberlog.ui.theme.unpaidColor
import com.fidit.memberlog.util.roleColor

@Composable
fun MembersListScreen(
    members: List<Member>?,
    owedByMember: Map<Int, Double>?,
    rolesById: Map<Int, Role>,
    onMemberClick: (Member) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf<Int?>(null) }
    var statusFilter by remember { mutableStateOf<MembershipStatus?>(null) }
    var owingOnly by remember { mutableStateOf(false) }

    val roleList = rolesById.values.sortedBy { it.name }
    val filtered = members.orEmpty().filter { m ->
        (query.isBlank() || m.name.foldCro().contains(query.foldCro())) &&
            (roleFilter == null || m.roleId == roleFilter) &&
            (statusFilter == null || m.status == statusFilter!!.name) &&
            (!owingOnly || (owedByMember.orEmpty()[m.id] ?: 0.0) > 0.0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding)
    ) {
        ScreenHeader(title = "Popis članova", subtitle = "Klikni na člana za detaljniji pregled")
        Spacer(Modifier.height(Dimens.gap))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Pretraži članove") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(Dimens.gap))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Dimens.gapSmall)
        ) {
            FilterChip(selected = owingOnly, onClick = { owingOnly = !owingOnly }, label = { Text("Duguje") })
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

        Spacer(Modifier.height(Dimens.gap))

        when {
            members == null || owedByMember == null -> LoadingSpinner()
            filtered.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Nema članova koji odgovaraju filtrima.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(Dimens.gap)) {
                items(filtered) { member ->
                    val role = rolesById[member.roleId]
                    val avatarColor = role?.let { roleColor(it.colorHex) } ?: MaterialTheme.colorScheme.primary
                    AppCard(modifier = Modifier.fillMaxWidth(), onClick = { onMemberClick(member) }, contentPadding = 16.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MemberAvatar(name = member.name, photoPath = member.photoPath, color = avatarColor, size = Dimens.avatarSmall)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    member.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.gapSmall)) {
                                    RoleChip(name = role?.name ?: "Bez uloge", color = avatarColor)
                                    val owed = owedByMember.orEmpty()[member.id] ?: 0.0
                                    if (owed > 0.0) StatusPill("Duguje ${money(owed)}", unpaidColor())
                                    else StatusPill("Podmireno", paidColor())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String.foldCro(): String =
    java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase()

private fun money(v: Double): String =
    (if (v % 1.0 == 0.0) v.toInt().toString() else "%.2f".format(v)) + " €"
