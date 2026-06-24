package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.ui.ExchangeRateViewModel
import com.fidit.memberlog.ui.components.AppCard
import com.fidit.memberlog.ui.components.LoadingSpinner
import com.fidit.memberlog.ui.components.ScreenHeader
import com.fidit.memberlog.ui.theme.Dimens

@Composable
fun ExchangeRateScreen(
    onBack: () -> Unit,
    memberOwedEur: Double = 0.0,
    viewModel: ExchangeRateViewModel = viewModel()
) {
    val ratesMap = viewModel.rates.value
    val isLoading = viewModel.isLoading.value
    val offline = viewModel.offline.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding)
    ) {
        ScreenHeader(title = "Tečajna lista (EUR)", subtitle = "Trenutni tečajevi valuta", onBack = onBack)
        Spacer(Modifier.height(Dimens.gap))

        if (isLoading) {
            LoadingSpinner(Modifier.fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.gap)
            ) {
                if (memberOwedEur > 0.0) {
                    item {
                        AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
                            Text("Vaš dug", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text("%.2f EUR".format(memberOwedEur), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                if (offline) {
                    item {
                        AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Bez internetske veze", style = MaterialTheme.typography.titleSmall)
                                    Text("Prikazani su zadani tečajevi.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                TextButton(onClick = { viewModel.fetchRates() }) { Text("Osvježi") }
                            }
                        }
                    }
                }
                if (ratesMap.isEmpty()) {
                    item {
                        Text("Nema dostupnih tečajeva.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(ratesMap.toList()) { (currencyCode, rateValue) ->
                        ExchangeRateItem(currencyCode = currencyCode, rateValue = rateValue, memberOwedEur = memberOwedEur)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExchangeRateItem(currencyCode: String, rateValue: Double, memberOwedEur: Double) {
    AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(currencyCode, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    when (currencyCode) {
                        "USD" -> "Američki dolar"
                        "CHF" -> "Švicarski franak"
                        "GBP" -> "Britanska funta"
                        "AUD" -> "Australski dolar"
                        "CAD" -> "Kanadski dolar"
                        else -> "Strana valuta"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(6.dp))
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primary) {
                    Text(
                        "1 EUR = %.2f %s".format(rateValue, currencyCode),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                if (memberOwedEur > 0.0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Dug u %s: %.2f".format(currencyCode, memberOwedEur * rateValue),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
