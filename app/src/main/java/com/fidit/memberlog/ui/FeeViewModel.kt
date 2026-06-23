package com.fidit.memberlog.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fidit.memberlog.data.FeeRepository
import com.fidit.memberlog.data.MemberDatabase
import com.fidit.memberlog.model.FeeConfig
import com.fidit.memberlog.model.FeePayment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FeeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = FeeRepository(MemberDatabase.getInstance(app).feeDao())

    val config: StateFlow<FeeConfig> = repo.config
        .map { it ?: FeeConfig() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeeConfig())

    fun paymentsFor(memberId: Int): Flow<List<FeePayment>> = repo.paymentsForMember(memberId)

    fun recordPayment(memberId: Int, period: String, amount: Double, paidDateIso: String) {
        viewModelScope.launch {
            repo.recordPayment(
                FeePayment(memberId = memberId, periodMonth = period, amount = amount, paidDate = paidDateIso)
            )
        }
    }

    fun deletePayment(payment: FeePayment) {
        viewModelScope.launch { repo.deletePayment(payment) }
    }

    fun setDefaultFee(amount: Double) {
        viewModelScope.launch { repo.setDefaultFee(amount) }
    }
}
