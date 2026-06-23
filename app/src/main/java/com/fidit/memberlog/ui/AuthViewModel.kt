package com.fidit.memberlog.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fidit.memberlog.data.AuthRepository
import com.fidit.memberlog.data.LoginResult
import com.fidit.memberlog.data.MemberDatabase
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val db = MemberDatabase.getInstance(app)
    private val repo = AuthRepository(db.memberDao(), db.roleDao())

    fun login(email: String, password: String, onResult: (LoginResult?) -> Unit) {
        viewModelScope.launch { onResult(repo.login(email, password)) }
    }
}
