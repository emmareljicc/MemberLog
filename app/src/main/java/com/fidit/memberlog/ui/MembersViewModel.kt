package com.fidit.memberlog.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fidit.memberlog.data.MemberDatabase
import com.fidit.memberlog.data.MemberRepository
import com.fidit.memberlog.model.Member
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MembersViewModel(app: Application) : AndroidViewModel(app) {

    private val repository =
        MemberRepository(MemberDatabase.getInstance(app).memberDao())

    val members: StateFlow<List<Member>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addMember(name: String, role: String, isPaid: Boolean, email: String, phone: String) {
        viewModelScope.launch {
            repository.insert(
                Member(
                    name = name,
                    role = role,
                    joinDate = "Danas",
                    isPaid = isPaid,
                    email = email,
                    phone = phone
                )
            )
        }
    }

    fun updateMember(member: Member) {
        viewModelScope.launch { repository.update(member) }
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch { repository.delete(member) }
    }
}
