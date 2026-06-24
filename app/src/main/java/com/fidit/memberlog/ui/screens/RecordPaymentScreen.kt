package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fidit.memberlog.ui.components.ScreenHeader
import com.fidit.memberlog.ui.theme.Dimens
import com.fidit.memberlog.util.DateUtils

@Composable
fun RecordPaymentScreen(
    period: String,
    expectedAmount: Double,
    onBack: () -> Unit,
    onConfirm: (amount: Double, paidDateIso: String) -> Unit
) {
    var amount by remember { mutableStateOf(formatAmount(expectedAmount)) }
    var date by remember { mutableStateOf(DateUtils.todayIso()) }
    val parsedAmount = amount.replace(',', '.').toDoubleOrNull()
    val valid = parsedAmount != null && parsedAmount > 0.0

    Column(modifier = Modifier.fillMaxSize().padding(Dimens.screenPadding)) {
        ScreenHeader(title = "Zabilježi uplatu", subtitle = DateUtils.formatPeriod(period), onBack = onBack)
        Spacer(Modifier.height(Dimens.gap))
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Iznos (EUR)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Datum (GGGG-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(Dimens.gap))
        }
        Spacer(Modifier.height(Dimens.gapSmall))
        Button(
            onClick = { parsedAmount?.let { onConfirm(it, date.ifBlank { DateUtils.todayIso() }) } },
            enabled = valid,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.small
        ) { Text("Spremi") }
    }
}

private fun formatAmount(v: Double): String =
    if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()
