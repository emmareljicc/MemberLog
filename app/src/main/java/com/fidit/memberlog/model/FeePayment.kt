package com.fidit.memberlog.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fee_payments",
    foreignKeys = [
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("memberId")]
)
data class FeePayment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberId: Int,

    val periodMonth: String,
    val amount: Double,

    val paidDate: String
)
