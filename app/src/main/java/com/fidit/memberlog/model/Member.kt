package com.fidit.memberlog.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String,

    val joinDate: String,
    val email: String,
    val phone: String,

    val monthlyFeeOverride: Double? = null
)
