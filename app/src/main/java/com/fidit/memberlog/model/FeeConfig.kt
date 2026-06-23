package com.fidit.memberlog.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fee_config")
data class FeeConfig(
    @PrimaryKey val id: Int = 1,
    val defaultMonthlyFee: Double = 10.0
)
