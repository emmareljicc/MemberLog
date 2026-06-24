package com.fidit.memberlog.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ExchangeRateViewModel : ViewModel() {

    private val tracked = listOf("USD", "CHF", "GBP", "AUD", "CAD")

    private val offlineRates = mapOf(
        "USD" to 1.07,
        "CHF" to 0.95,
        "GBP" to 0.84,
        "AUD" to 1.61,
        "CAD" to 1.47
    )

    private val _rates = mutableStateOf<Map<String, Double>>(emptyMap())
    val rates: State<Map<String, Double>> = _rates

    init {
        fetchRates()
    }

    fun fetchRates() {
        _rates.value = offlineRates.filter { it.key in tracked }
    }

    fun convert(amountInEur: Double, targetCurrency: String): Double {
        val rate = offlineRates[targetCurrency] ?: 1.0
        return amountInEur * rate
    }
}