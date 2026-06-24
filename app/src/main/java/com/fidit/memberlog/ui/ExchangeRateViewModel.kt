package com.fidit.memberlog.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fidit.memberlog.data.CurrencyApiService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ExchangeRateViewModel : ViewModel() {

    private val api = Retrofit.Builder()
        .baseUrl("https://open.er-api.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CurrencyApiService::class.java)

    private val tracked = listOf("USD", "CHF", "GBP", "AUD", "CAD")

    private val fallbackRates = mapOf(
        "USD" to 1.08,
        "CHF" to 0.94,
        "GBP" to 0.84,
        "AUD" to 1.64,
        "CAD" to 1.47
    )

    private val _rates = mutableStateOf<Map<String, Double>>(emptyMap())
    val rates: State<Map<String, Double>> = _rates

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _offline = mutableStateOf(false)
    val offline: State<Boolean> = _offline

    init {
        fetchRates()
    }

    fun fetchRates() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getExchangeRates()
                _rates.value = response.rates.filter { it.key in tracked }
                _offline.value = false
            } catch (e: Exception) {
                _rates.value = fallbackRates.filter { it.key in tracked }
                _offline.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }
}
