package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.fidit.memberlog.util.DateUtils

@Composable
fun RecordPaymentDialog(
    period: String,
    expectedAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, paidDateIso: String) -> Unit
) {
    var amount by remember { mutableStateOf(formatAmount(expectedAmount)) }
    var date by remember { mutableStateOf(DateUtils.todayIso()) }
    val parsedAmount = amount.replace(',', '.').toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { parsedAmount?.let { onConfirm(it, date.ifBlank { DateUtils.todayIso() }) } },
                enabled = parsedAmount != null && parsedAmount > 0.0
            ) { Text("Spremi") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Odustani") } },
        title = { Text("Zabilježi uplatu — ${DateUtils.formatPeriod(period)}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Iznos (EUR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Datum (GGGG-MM-DD)") },
                    singleLine = true
                )
            }
        }
    )
}

private fun formatAmount(v: Double): String =
    if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()
