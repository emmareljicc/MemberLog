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
    viewModel: ExchangeRateViewModel = viewModel()
) {
    val ratesMap = viewModel.rates.value
    val isLoading = viewModel.isLoading.value
    val errorMessage = viewModel.errorMessage.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding)
    ) {
        ScreenHeader(title = "Tečajna lista (EUR)", subtitle = "Trenutni tečajevi valuta", onBack = onBack)
        Spacer(Modifier.height(Dimens.gap))

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when {
                isLoading -> LoadingSpinner()
                errorMessage != null -> Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(errorMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(Dimens.gap))
                    Button(onClick = { viewModel.fetchRates() }, shape = MaterialTheme.shapes.small) { Text("Pokušaj ponovno") }
                }
                ratesMap.isEmpty() -> Text("Nema dostupnih tečajeva.", modifier = Modifier.align(Alignment.Center), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(Dimens.gap)) {
                    items(ratesMap.toList()) { (currencyCode, rateValue) ->
                        ExchangeRateItem(currencyCode = currencyCode, rateValue = rateValue)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExchangeRateItem(currencyCode: String, rateValue: Double) {
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
            }
        }
    }
}
