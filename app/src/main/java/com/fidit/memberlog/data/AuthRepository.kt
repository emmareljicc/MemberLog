package com.fidit.memberlog.data

import com.fidit.memberlog.model.Member
import com.fidit.memberlog.util.DateUtils
import com.fidit.memberlog.util.PasswordHash

data class LoginResult(val memberId: Int, val isAdmin: Boolean)

class AuthRepository(
    private val memberDao: MemberDao,
    private val roleDao: RoleDao
) {
    suspend fun login(email: String, password: String): LoginResult? {
        val member = memberDao.getByEmail(email) ?: return null
        val hash = member.passwordHash ?: return null
        if (hash != PasswordHash.sha256(password)) return null
        val role = roleDao.getById(member.roleId)
        return LoginResult(memberId = member.id, isAdmin = role?.grantsAdmin == true)
    }

    suspend fun register(name: String, roleId: Int, email: String, phone: String, password: String): LoginResult? {
        if (memberDao.getByEmail(email) != null) return null
        memberDao.insert(
            Member(
                name = name,
                roleId = roleId,
                joinDate = DateUtils.todayIso(),
                email = email,
                phone = phone,
                passwordHash = PasswordHash.sha256(password)
            )
        )
        return login(email, password)
    }
}
