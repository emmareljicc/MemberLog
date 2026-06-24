package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.ui.ActivitiesViewModel
import com.fidit.memberlog.ui.components.AppCard
import com.fidit.memberlog.ui.components.LoadingSpinner
import com.fidit.memberlog.ui.components.ScreenHeader
import com.fidit.memberlog.ui.components.StatusPill
import com.fidit.memberlog.ui.theme.Dimens
import com.fidit.memberlog.ui.theme.paidColor
import com.fidit.memberlog.util.DateUtils

@Composable
fun ActivitiesScreen(
    isAdmin: Boolean,
    onEventClick: (Int) -> Unit,
    viewModel: ActivitiesViewModel = viewModel()
) {
    val events by viewModel.events.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    val today = DateUtils.todayIso()

    if (showForm) {
        EventFormScreen(
            title = "Novo događanje",
            onBack = { showForm = false },
            onSubmit = { t, d, l, n -> viewModel.addEvent(t, d, l, n); showForm = false }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding)
    ) {
        ScreenHeader(title = "Aktivnosti", subtitle = "Događanja i evidencija dolazaka")
        Spacer(Modifier.height(Dimens.gap))

        if (isAdmin) {
            Button(onClick = { showForm = true }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = MaterialTheme.shapes.small) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(Dimens.gapSmall))
                Text("Dodaj događanje")
            }
            Spacer(Modifier.height(Dimens.gap))
        }

        val eventList = events
        if (eventList == null) {
            LoadingSpinner(Modifier.fillMaxSize())
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(Dimens.gap)) {
                items(eventList) { event ->
                    val upcoming = event.date >= today
                    AppCard(modifier = Modifier.fillMaxWidth(), onClick = { onEventClick(event.id) }, contentPadding = 16.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(event.title, style = MaterialTheme.typography.titleMedium)
                                Text("${DateUtils.formatIsoDate(event.date)} • ${event.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (upcoming) StatusPill("Nadolazi", paidColor())
                        }
                    }
                }
            }
        }
    }
}
