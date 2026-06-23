package com.fidit.memberlog.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fidit.memberlog.model.Role
import kotlinx.coroutines.flow.Flow

@Dao
interface RoleDao {

    @Query("SELECT * FROM roles ORDER BY name")
    fun getAll(): Flow<List<Role>>

    @Insert
    suspend fun insert(role: Role)

    @Update
    suspend fun update(role: Role)

    @Delete
    suspend fun delete(role: Role)

    @Query("SELECT COUNT(*) FROM members WHERE roleId = :roleId")
    suspend fun countMembersWithRole(roleId: Int): Int

    @Query("UPDATE members SET roleId = :toRoleId WHERE roleId = :fromRoleId")
    suspend fun reassignMembers(fromRoleId: Int, toRoleId: Int)
}
