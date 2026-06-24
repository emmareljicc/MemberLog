package com.fidit.memberlog.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fidit.memberlog.data.FeeRepository
import com.fidit.memberlog.data.MemberDatabase
import com.fidit.memberlog.data.MemberRepository
import com.fidit.memberlog.model.FeeConfig
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.util.DateUtils
import com.fidit.memberlog.util.FeeCalculator
import com.fidit.memberlog.util.PasswordHash
import java.time.YearMonth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MembersViewModel(app: Application) : AndroidViewModel(app) {

    private val db = MemberDatabase.getInstance(app)
    private val repository = MemberRepository(db.memberDao())
    private val feeRepository = FeeRepository(db.feeDao())

    val members: StateFlow<List<Member>?> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val rolesById: StateFlow<Map<Int, Role>> = db.roleDao().getAll()
        .map { roles -> roles.associateBy { it.id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val owedByMember: StateFlow<Map<Int, Double>?> = combine(
        repository.allMembers,
        feeRepository.allPayments,
        feeRepository.config,
        feeRepository.allRates
    ) { members, payments, config, rates ->
        val cfg = config ?: FeeConfig()
        members.associate { m ->
            val statuses = FeeCalculator.computeStatuses(
                joinIso = m.joinDate,
                feeForPeriod = { p -> FeeCalculator.effectiveFee(m.id, p, rates, cfg.defaultMonthlyFee) },
                payments = payments.filter { it.memberId == m.id }
            )
            m.id to FeeCalculator.totalOwed(statuses)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun addMember(
        name: String,
        roleId: Int,
        email: String,
        phone: String,
        monthlyFeeOverride: Double?,
        status: String,
        address: String,
        notes: String,
        photoPath: String?,
        password: String?
    ) {
        viewModelScope.launch {
            val joinIso = DateUtils.todayIso()
            val id = repository.insert(
                Member(
                    name = name,
                    roleId = roleId,
                    joinDate = joinIso,
                    email = email,
                    phone = phone,
                    monthlyFeeOverride = monthlyFeeOverride,
                    status = status,
                    address = address,
                    notes = notes,
                    photoPath = photoPath,
                    passwordHash = password?.let { PasswordHash.sha256(it) }
                )
            )
            if (monthlyFeeOverride != null) {
                feeRepository.addMemberRate(id.toInt(), joinIso.dropLast(3), monthlyFeeOverride)
            }
        }
    }

    fun updateMember(member: Member) {
        viewModelScope.launch {
            val old = members.value?.find { it.id == member.id }
            repository.update(member)
            if (old == null || old.monthlyFeeOverride != member.monthlyFeeOverride) {
                feeRepository.addMemberRate(member.id, nextMonth(), member.monthlyFeeOverride)
            }
        }
    }

    private fun nextMonth(): String {
        val ym = YearMonth.now().plusMonths(1)
        return "%04d-%02d".format(ym.year, ym.monthValue)
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch { repository.delete(member) }
    }
}
