package com.fidit.memberlog.data

import com.fidit.memberlog.model.Role
import kotlinx.coroutines.flow.Flow

class RoleRepository(private val dao: RoleDao) {

    val allRoles: Flow<List<Role>> = dao.getAll()

    suspend fun insert(role: Role) = dao.insert(role)

    suspend fun update(role: Role) = dao.update(role)

    suspend fun memberCount(roleId: Int): Int = dao.countMembersWithRole(roleId)

    suspend fun deleteWithReassign(role: Role, replacementId: Int) {
        if (replacementId != role.id) {
            dao.reassignMembers(role.id, replacementId)
        }
        dao.delete(role)
    }
}
