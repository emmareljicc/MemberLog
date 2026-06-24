package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fidit.memberlog.model.Event
import com.fidit.memberlog.ui.components.ScreenHeader
import com.fidit.memberlog.ui.theme.Dimens
import com.fidit.memberlog.util.DateUtils

@Composable
fun EventFormScreen(
    title: String,
    existing: Event? = null,
    onBack: () -> Unit,
    onSubmit: (title: String, date: String, location: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(existing?.title ?: "") }
    var date by remember { mutableStateOf(existing?.date ?: DateUtils.todayIso()) }
    var location by remember { mutableStateOf(existing?.location ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    val valid = name.isNotBlank() && date.isNotBlank()

    Column(modifier = Modifier.fillMaxSize().padding(Dimens.screenPadding)) {
        ScreenHeader(title = title, onBack = onBack)
        Spacer(Modifier.height(Dimens.gap))
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Naziv događanja") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Datum (GGGG-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Mjesto") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Bilješke") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(Dimens.gap))
        }
        Spacer(Modifier.height(Dimens.gapSmall))
        Button(
            onClick = { if (valid) onSubmit(name.trim(), date.trim(), location.trim(), notes) },
            enabled = valid,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.small
        ) { Text("Spremi") }
    }
}
