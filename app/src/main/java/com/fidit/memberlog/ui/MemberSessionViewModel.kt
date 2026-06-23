package com.fidit.memberlog.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fidit.memberlog.data.MemberDatabase
import com.fidit.memberlog.data.MemberRepository
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.util.PasswordHash
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MemberSessionViewModel(app: Application) : AndroidViewModel(app) {

    private val db = MemberDatabase.getInstance(app)
    private val repository = MemberRepository(db.memberDao())

    fun member(id: Int): Flow<Member?> = db.memberDao().getById(id)

    fun updateContact(member: Member) {
        viewModelScope.launch { repository.update(member) }
    }

    fun changePassword(member: Member, newPassword: String) {
        viewModelScope.launch { repository.update(member.copy(passwordHash = PasswordHash.sha256(newPassword))) }
    }
}
