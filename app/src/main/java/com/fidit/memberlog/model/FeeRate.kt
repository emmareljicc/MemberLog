package com.fidit.memberlog.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "fee_rates", indices = [Index("memberId")])
data class FeeRate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberId: Int?,
    val effectiveFrom: String,
    val amount: Double?
)
