package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.ExchangeRateViewModel

@Composable
fun ExchangeRateScreen(
    onBack: () -> Unit,
    viewModel: ExchangeRateViewModel = viewModel()
) {
    val ratesMap = viewModel.rates.value
    val isLoading = viewModel.isLoading.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F4F8))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Natrag",
                        tint = Color(0xFF5E4E9D)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tečajna lista (EUR)",
                    color = Color(0xFF5E4E9D),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Usporedba valuta za klupska dugovanja",
                color = Color(0xFF7A7A7A),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 52.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (!isLoading) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ratesMap.toList()) { (currencyCode, rateValue) ->
                        ExchangeRateItem(currencyCode = currencyCode, rateValue = rateValue)
                    }
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF5E4E9D)
            )
        }
    }
}

@Composable
fun ExchangeRateItem(currencyCode: String, rateValue: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAE6F3))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1E88E5), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currencyCode,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = when(currencyCode) {
                        "USD" -> "Američki dolar"
                        "CHF" -> "Švicarski franak"
                        "GBP" -> "Britanska funta"
                        "AUD" -> "Australski dolar"
                        "CAD" -> "Kanadski dolar"
                        else -> "Strana valuta"
                    },
                    color = Color(0xFF212121),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF5E4E9D)
                ) {
                    Text(
                        text = "1 EUR = %.2f %s".format(rateValue, currencyCode),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}