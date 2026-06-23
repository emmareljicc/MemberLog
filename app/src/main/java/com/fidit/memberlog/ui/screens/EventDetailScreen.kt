package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.ui.ActivitiesViewModel
import com.fidit.memberlog.util.DateUtils
import com.fidit.memberlog.util.roleColor

@Composable
fun EventDetailScreen(
    eventId: Int,
    members: List<Member>,
    rolesById: Map<Int, Role>,
    onBack: () -> Unit,
    viewModel: ActivitiesViewModel = viewModel()
) {
    val events by viewModel.events.collectAsState()
    val event = events.firstOrNull { it.id == eventId }
    val attendeeIds by viewModel.attendeeIds(eventId).collectAsState(initial = emptyList())
    var showEdit by remember { mutableStateOf(false) }

    if (event == null) {
        Box(Modifier.fillMaxSize()) {}
        return
    }

    if (showEdit) {
        EventFormDialog(
            title = "Uredi događaj",
            existing = event,
            onDismiss = { showEdit = false },
            onSubmit = { t, d, l, n ->
                viewModel.updateEvent(event.copy(title = t, date = d, location = l, notes = n))
                showEdit = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(event.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(8.dp))
                    Text(DateUtils.formatIsoDate(event.date), fontSize = 15.sp)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(8.dp))
                    Text(event.location, fontSize = 15.sp)
                }
                if (event.notes.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(event.notes, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { showEdit = true },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Uredi")
            }
            Button(
                onClick = { viewModel.deleteEvent(event); onBack() },
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Obriši")
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "Dolasci (${attendeeIds.size}/${members.size})",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(members) { member ->
                val present = attendeeIds.contains(member.id)
                val accent = rolesById[member.roleId]?.let { roleColor(it.colorHex) } ?: MaterialTheme.colorScheme.primary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.setAttendance(eventId, member.id, !present) }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials(member.name), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(member.name, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    Checkbox(
                        checked = present,
                        onCheckedChange = { viewModel.setAttendance(eventId, member.id, it) }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

private fun initials(name: String): String {
    val parts = name.split(" ")
    return if (parts.size > 1) "${parts[0][0]}${parts[1][0]}" else name.take(1)
}
