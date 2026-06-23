package com.fidit.memberlog.data

import com.fidit.memberlog.model.FeeConfig
import com.fidit.memberlog.model.FeePayment
import kotlinx.coroutines.flow.Flow

class FeeRepository(private val dao: FeeDao) {

    val allPayments: Flow<List<FeePayment>> = dao.allPayments()
    val config: Flow<FeeConfig?> = dao.getConfig()

    fun paymentsForMember(memberId: Int): Flow<List<FeePayment>> = dao.paymentsForMember(memberId)

    suspend fun recordPayment(payment: FeePayment) = dao.insertPayment(payment)

    suspend fun deletePayment(payment: FeePayment) = dao.deletePayment(payment)

    suspend fun setDefaultFee(amount: Double) = dao.upsertConfig(FeeConfig(id = 1, defaultMonthlyFee = amount))
}
