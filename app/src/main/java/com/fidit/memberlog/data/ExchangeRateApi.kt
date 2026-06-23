package com.fidit.memberlog.data

import retrofit2.Call
import retrofit2.http.GET

data class RateResponse(
    val base_code: String,
    val rates: Map<String, Double>
)

interface CurrencyApiService {
    @GET("v6/latest/EUR")
    fun getExchangeRates(): Call<RateResponse>
}