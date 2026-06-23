package com.fidit.memberlog.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "members",
    foreignKeys = [
        ForeignKey(
            entity = Role::class,
            parentColumns = ["id"],
            childColumns = ["roleId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("roleId")]
)
data class Member(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val roleId: Int,
    val joinDate: String,
    val email: String,
    val phone: String,
    val monthlyFeeOverride: Double? = null
)
