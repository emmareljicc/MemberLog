package com.fidit.memberlog.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fidit.memberlog.data.MemberDatabase
import com.fidit.memberlog.data.RoleRepository
import com.fidit.memberlog.model.Role
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RolesViewModel(app: Application) : AndroidViewModel(app) {

    private val db = MemberDatabase.getInstance(app)
    private val repo = RoleRepository(db.roleDao())

    val roles: StateFlow<List<Role>?> = repo.allRoles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val memberCounts: StateFlow<Map<Int, Int>?> = combine(
        repo.allRoles,
        db.memberDao().getAll()
    ) { roles, members ->
        roles.associate { role -> role.id to members.count { it.roleId == role.id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun addRole(name: String, colorHex: String, grantsAdmin: Boolean) {
        viewModelScope.launch { repo.insert(Role(name = name, colorHex = colorHex, grantsAdmin = grantsAdmin)) }
    }

    fun updateRole(role: Role) {
        viewModelScope.launch { repo.update(role) }
    }

    fun deleteRole(role: Role, replacementId: Int) {
        viewModelScope.launch { repo.deleteWithReassign(role, replacementId) }
    }
}
