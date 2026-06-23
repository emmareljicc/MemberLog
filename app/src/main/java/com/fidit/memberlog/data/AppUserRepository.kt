package com.fidit.memberlog.data

import com.fidit.memberlog.model.AppUser
import com.fidit.memberlog.model.UserRole
import com.fidit.memberlog.util.PasswordHash
import kotlinx.coroutines.flow.Flow

class AppUserRepository(private val dao: AppUserDao) {

    val allUsers: Flow<List<AppUser>> = dao.getAll()

    suspend fun hasAnyUsers(): Boolean = dao.count() > 0

    suspend fun authenticate(username: String, password: String): UserRole? {
        val user = dao.getByUsername(username.trim()) ?: return null
        return if (user.passwordHash == PasswordHash.sha256(password)) {
            runCatching { UserRole.valueOf(user.role) }.getOrNull()
        } else {
            null
        }
    }

    suspend fun register(username: String, password: String, role: UserRole) {
        dao.insert(
            AppUser(
                username = username.trim(),
                passwordHash = PasswordHash.sha256(password),
                role = role.name
            )
        )
    }

    suspend fun usernameExists(username: String): Boolean = dao.getByUsername(username.trim()) != null

    suspend fun adminCount(): Int = dao.countByRole(UserRole.ADMIN.name)

    suspend fun delete(user: AppUser) = dao.delete(user)
}
