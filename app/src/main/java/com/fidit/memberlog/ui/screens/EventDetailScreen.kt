package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.ui.ActivitiesViewModel
import com.fidit.memberlog.ui.components.AppCard
import com.fidit.memberlog.ui.components.LoadingSpinner
import com.fidit.memberlog.ui.components.MemberAvatar
import com.fidit.memberlog.ui.components.ScreenHeader
import com.fidit.memberlog.ui.theme.Dimens
import com.fidit.memberlog.util.DateUtils
import com.fidit.memberlog.util.roleColor

@Composable
fun EventDetailScreen(
    eventId: Int,
    members: List<Member>,
    rolesById: Map<Int, Role>,
    isAdmin: Boolean,
    onBack: () -> Unit,
    viewModel: ActivitiesViewModel = viewModel()
) {
    val events by viewModel.events.collectAsState()
    val event = events.orEmpty().firstOrNull { it.id == eventId }
    val attendeeState by viewModel.attendeeIds(eventId).collectAsState(initial = null)
    val rsvpState by viewModel.rsvpMemberIds(eventId).collectAsState(initial = null)
    var showEdit by remember { mutableStateOf(false) }

    if (event == null) {
        LoadingSpinner()
        return
    }

    if (showEdit) {
        EventFormScreen(
            title = "Uredi događanje",
            existing = event,
            onBack = { showEdit = false },
            onSubmit = { t, d, l, n ->
                viewModel.updateEvent(event.copy(title = t, date = d, location = l, notes = n))
                showEdit = false
            }
        )
        return
    }

    val attendeeIds = attendeeState
    val rsvpIds = rsvpState
    if (attendeeIds == null || rsvpIds == null) {
        LoadingSpinner()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.screenPadding)
    ) {
        ScreenHeader(title = "Detalji događanja", onBack = onBack)
        Spacer(Modifier.height(Dimens.gap))

        AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 20.dp) {
            Text(event.title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Dimens.gap))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(Dimens.gapSmall))
                Text(DateUtils.formatIsoDate(event.date), style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.height(Dimens.gapSmall))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(Dimens.gapSmall))
                Text(event.location, style = MaterialTheme.typography.bodyLarge)
            }
            if (event.notes.isNotBlank()) {
                Spacer(Modifier.height(Dimens.gapSmall))
                Text(event.notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(Dimens.gap))

        if (isAdmin) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.gap)) {
                OutlinedButton(onClick = { showEdit = true }, modifier = Modifier.weight(1f).height(50.dp), shape = MaterialTheme.shapes.small) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(Dimens.gapSmall))
                    Text("Uredi")
                }
                Button(onClick = { viewModel.deleteEvent(event); onBack() }, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), shape = MaterialTheme.shapes.small) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(Dimens.gapSmall))
                    Text("Obriši")
                }
            }
            Spacer(Modifier.height(Dimens.gap))
        }

        Text("Dolasci (${attendeeIds.size}/${members.size})", style = MaterialTheme.typography.titleMedium)
        if (rsvpIds.isNotEmpty()) {
            Text("Najavili dolazak: ${rsvpIds.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(Dimens.gapSmall))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            members.forEach { member ->
                val present = attendeeIds.contains(member.id)
                val accent = rolesById[member.roleId]?.let { roleColor(it.colorHex) } ?: MaterialTheme.colorScheme.primary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .then(if (isAdmin) Modifier.clickable { viewModel.setAttendance(eventId, member.id, !present) } else Modifier)
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MemberAvatar(name = member.name, photoPath = member.photoPath, color = accent, size = 32.dp, fontSize = MaterialTheme.typography.labelMedium.fontSize)
                    Spacer(Modifier.width(Dimens.gap))
                    Text(member.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    if (rsvpIds.contains(member.id)) {
                        Text("najava", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = Dimens.gapSmall))
                    }
                    Checkbox(checked = present, enabled = isAdmin, onCheckedChange = { viewModel.setAttendance(eventId, member.id, it) })
                }
            }
        }

        Spacer(Modifier.height(Dimens.gap))
    }
}
