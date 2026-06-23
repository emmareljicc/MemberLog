package com.fidit.memberlog.data

import retrofit2.http.GET

data class RateResponse(
    val rates: Map<String, Double>
)

interface CurrencyApiService {
    @GET("v6/latest/EUR")
    suspend fun getExchangeRates(): RateResponse
}
