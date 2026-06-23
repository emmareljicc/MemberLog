package com.fidit.memberlog.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fidit.memberlog.data.AppUserRepository
import com.fidit.memberlog.data.MemberDatabase
import com.fidit.memberlog.model.AppUser
import com.fidit.memberlog.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppUserRepository(MemberDatabase.getInstance(app).appUserDao())

    private val _hasUsers = MutableStateFlow(true)
    val hasUsers: StateFlow<Boolean> = _hasUsers.asStateFlow()

    val users: StateFlow<List<AppUser>> = repo.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { _hasUsers.value = repo.hasAnyUsers() }
    }

    fun login(username: String, password: String, onResult: (UserRole?) -> Unit) {
        viewModelScope.launch { onResult(repo.authenticate(username, password)) }
    }

    fun createFirstAdmin(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (repo.usernameExists(username)) {
                onResult(false)
            } else {
                repo.register(username, password, UserRole.ADMIN)
                _hasUsers.value = true
                onResult(true)
            }
        }
    }

    fun addUser(username: String, password: String, role: UserRole, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (repo.usernameExists(username)) onResult(false)
            else {
                repo.register(username, password, role)
                onResult(true)
            }
        }
    }

    fun deleteUser(user: AppUser, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (user.role == UserRole.ADMIN.name && repo.adminCount() <= 1) {
                onResult(false)
            } else {
                repo.delete(user)
                onResult(true)
            }
        }
    }
}
