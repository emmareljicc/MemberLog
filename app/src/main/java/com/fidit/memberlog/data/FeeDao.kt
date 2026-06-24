package com.fidit.memberlog.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fidit.memberlog.model.FeeConfig
import com.fidit.memberlog.model.FeePayment
import com.fidit.memberlog.model.FeeRate
import kotlinx.coroutines.flow.Flow

@Dao
interface FeeDao {

    @Query("SELECT * FROM fee_payments WHERE memberId = :memberId ORDER BY periodMonth")
    fun paymentsForMember(memberId: Int): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments")
    fun allPayments(): Flow<List<FeePayment>>

    @Insert
    suspend fun insertPayment(payment: FeePayment)

    @Query("SELECT * FROM fee_config WHERE id = 1")
    fun getConfig(): Flow<FeeConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConfig(config: FeeConfig)

    @Query("SELECT * FROM fee_rates")
    fun allRates(): Flow<List<FeeRate>>

    @Insert
    suspend fun insertRate(rate: FeeRate)
}
