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

    private val _rates = mutableStateOf<Map<String, Double>>(emptyMap())
    val rates: State<Map<String, Double>> = _rates

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    init {
        fetchRates()
    }

    fun fetchRates() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = api.getExchangeRates()
                _rates.value = response.rates.filter { it.key in tracked }
            } catch (e: Exception) {
                _errorMessage.value = "Nije moguće dohvatiti tečajnu listu. Provjeri internetsku vezu."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
