package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fidit.memberlog.ui.components.ScreenHeader
import com.fidit.memberlog.ui.theme.Dimens
import com.fidit.memberlog.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentScreen(
    period: String,
    expectedAmount: Double,
    onBack: () -> Unit,
    onConfirm: (amount: Double, paidDateIso: String) -> Unit
) {
    var amount by remember(expectedAmount) { mutableStateOf(formatAmount(expectedAmount)) }
    var date by remember { mutableStateOf(DateUtils.todayIso()) }

    LaunchedEffect(expectedAmount) {
        amount = formatAmount(expectedAmount)
    }

    val parsedAmount = amount.replace(',', '.').toDoubleOrNull()
    val valid = parsedAmount != null && parsedAmount > 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding)
    ) {
        ScreenHeader(
            title = "Zabilježi uplatu",
            subtitle = "Period: ${DateUtils.formatPeriod(period)}",
            onBack = onBack
        )
        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Očekivani iznos zaduženja",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "€%.2f".format(expectedAmount),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Iznos uplate (EUR)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Brzi unos iznosa",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { amount = formatAmount(expectedAmount) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Puni dug")
                    }
                    Button(
                        onClick = {
                            val current = parsedAmount ?: 0.0
                            amount = formatAmount(current + 10.0)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("+10 €")
                    }
                    Button(
                        onClick = {
                            val current = parsedAmount ?: 0.0
                            amount = formatAmount(current + 20.0)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("+20 €")
                    }
                }
            }

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Datum uplate (GGGG-MM-DD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                parsedAmount?.let { amt ->
                    onConfirm(amt, date.ifBlank { DateUtils.todayIso() })
                }
            },
            enabled = valid,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Spremi uplatu", style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun formatAmount(v: Double): String =
    if (v % 1.0 == 0.0) v.toInt().toString() else "%.2f".format(v).replace(',', '.')