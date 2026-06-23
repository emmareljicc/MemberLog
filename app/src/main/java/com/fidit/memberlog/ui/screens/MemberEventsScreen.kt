package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.ui.ActivitiesViewModel
import com.fidit.memberlog.ui.theme.DisplayFont
import com.fidit.memberlog.ui.theme.FeePaid
import com.fidit.memberlog.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberEventsScreen(
    member: Member,
    activitiesViewModel: ActivitiesViewModel
) {
    val events by activitiesViewModel.events.collectAsState()
    val rsvpIds by activitiesViewModel.rsvpEventIds(member.id).collectAsState(initial = emptyList())
    val attended by activitiesViewModel.attendedEvents(member.id).collectAsState(initial = emptyList())
    val today = DateUtils.todayIso()
    val attendedIds = attended.map { it.id }.toSet()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Događaji",
            fontFamily = DisplayFont,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Najavi dolazak na nadolazeće događaje",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(events) { event ->
                val upcoming = event.date >= today
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(event.title, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${DateUtils.formatIsoDate(event.date)} • ${event.location}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        if (upcoming) {
                            val coming = rsvpIds.contains(event.id)
                            FilterChip(
                                selected = coming,
                                onClick = { activitiesViewModel.setRsvp(event.id, member.id, !coming) },
                                label = { Text(if (coming) "Dolazim ✓" else "Dolazim?") }
                            )
                        } else {
                            val attendedThis = attendedIds.contains(event.id)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (attendedThis) FeePaid.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    if (attendedThis) "Bili ste prisutni" else "Niste evidentirani",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (attendedThis) FeePaid else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
