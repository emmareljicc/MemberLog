package com.fidit.memberlog.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roles")
data class Role(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorHex: String
)
