package com.fidit.memberlog.data

import com.fidit.memberlog.model.FeeConfig
import com.fidit.memberlog.model.FeePayment
import com.fidit.memberlog.model.FeeRate
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

class FeeRepository(private val dao: FeeDao) {

    val allPayments: Flow<List<FeePayment>> = dao.allPayments()
    val config: Flow<FeeConfig?> = dao.getConfig()
    val allRates: Flow<List<FeeRate>> = dao.allRates()

    fun paymentsForMember(memberId: Int): Flow<List<FeePayment>> = dao.paymentsForMember(memberId)

    suspend fun recordPayment(payment: FeePayment) = dao.insertPayment(payment)

    suspend fun setDefaultFee(amount: Double) {
        dao.upsertConfig(FeeConfig(id = 1, defaultMonthlyFee = amount))
        dao.insertRate(FeeRate(memberId = null, effectiveFrom = nextMonth(), amount = amount))
    }

    suspend fun addMemberRate(memberId: Int, effectiveFrom: String, amount: Double?) {
        dao.insertRate(FeeRate(memberId = memberId, effectiveFrom = effectiveFrom, amount = amount))
    }

    private fun nextMonth(): String {
        val ym = YearMonth.now().plusMonths(1)
        return "%04d-%02d".format(ym.year, ym.monthValue)
    }
}
