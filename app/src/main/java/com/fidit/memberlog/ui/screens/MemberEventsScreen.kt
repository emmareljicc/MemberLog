package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.ui.ActivitiesViewModel
import com.fidit.memberlog.ui.components.AppCard
import com.fidit.memberlog.ui.components.LoadingSpinner
import com.fidit.memberlog.ui.components.ScreenHeader
import com.fidit.memberlog.ui.components.StatusPill
import com.fidit.memberlog.ui.theme.Dimens
import com.fidit.memberlog.ui.theme.paidColor
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
            .padding(Dimens.screenPadding)
    ) {
        ScreenHeader(title = "Događanja", subtitle = "Najavi dolazak na nadolazeća događanja")
        Spacer(Modifier.height(Dimens.gap))

        val eventList = events
        if (eventList == null) LoadingSpinner(Modifier.fillMaxSize())
        else LazyColumn(contentPadding = PaddingValues(bottom = NavBarSpace), verticalArrangement = Arrangement.spacedBy(Dimens.gap)) {
            items(eventList) { event ->
                val upcoming = event.date >= today
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Text(event.title, style = MaterialTheme.typography.titleMedium)
                    Text("${DateUtils.formatIsoDate(event.date)} • ${event.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(Dimens.gapSmall))
                    if (upcoming) {
                        val coming = rsvpIds.contains(event.id)
                        FilterChip(selected = coming, onClick = { activitiesViewModel.setRsvp(event.id, member.id, !coming) }, label = { Text(if (coming) "Dolazim" else "Dolazim?") })
                    } else {
                        val attendedThis = attendedIds.contains(event.id)
                        if (attendedThis) StatusPill("Dolazak zabilježen", paidColor())
                        else StatusPill("Nema dolaska", MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
