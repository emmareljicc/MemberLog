package com.fidit.memberlog.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fidit.memberlog.model.AppUser
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUserDao {

    @Query("SELECT * FROM app_users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): AppUser?

    @Query("SELECT COUNT(*) FROM app_users")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM app_users WHERE role = :role")
    suspend fun countByRole(role: String): Int

    @Query("SELECT * FROM app_users ORDER BY username")
    fun getAll(): Flow<List<AppUser>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: AppUser)

    @Delete
    suspend fun delete(user: AppUser)
}
