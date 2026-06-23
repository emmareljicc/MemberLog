package com.fidit.memberlog

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.fidit.memberlog.data.CurrencyApiService
import com.fidit.memberlog.data.RateResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ExchangeRateViewModel : ViewModel() {

    var rates = mutableStateOf<Map<String, Double>>(emptyMap())
        private set

    var isLoading = mutableStateOf(false)
        private set

    init {
        fetchRates()
    }

    fun fetchRates() {
        isLoading.value = true

        val retrofit = Retrofit.Builder()
            .baseUrl("https://open.er-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(CurrencyApiService::class.java)

        apiService.getExchangeRates().enqueue(object : Callback<RateResponse> {
            override fun onResponse(call: Call<RateResponse>, response: Response<RateResponse>) {
                isLoading.value = false
                if (response.isSuccessful) {
                    val allRates = response.body()?.rates ?: emptyMap()
                    rates.value = allRates.filter { it.key in listOf("USD", "CHF", "GBP", "AUD", "CAD") }
                }
            }

            override fun onFailure(call: Call<RateResponse>, t: Throwable) {
                isLoading.value = false
            }
        })
    }
}