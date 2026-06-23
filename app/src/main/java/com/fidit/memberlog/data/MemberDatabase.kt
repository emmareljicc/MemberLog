package com.fidit.memberlog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fidit.memberlog.model.AppUser
import com.fidit.memberlog.model.Attendance
import com.fidit.memberlog.model.Event
import com.fidit.memberlog.model.FeeConfig
import com.fidit.memberlog.model.FeePayment
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.util.PasswordHash
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Database(
    entities = [Member::class, FeePayment::class, FeeConfig::class, Role::class, Event::class, Attendance::class, AppUser::class],
    version = 5,
    exportSchema = false
)
abstract class MemberDatabase : RoomDatabase() {

    abstract fun memberDao(): MemberDao
    abstract fun feeDao(): FeeDao
    abstract fun roleDao(): RoleDao
    abstract fun activityDao(): ActivityDao
    abstract fun appUserDao(): AppUserDao

    companion object {
        @Volatile
        private var INSTANCE: MemberDatabase? = null

        fun getInstance(context: Context): MemberDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MemberDatabase::class.java,
                    "memberlog.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(SeedCallback)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private val SeedCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                val roles = listOf(
                    "Voditelj" to "#6750A4",
                    "Tajnik" to "#1E88E5",
                    "Blagajnik" to "#2E9E6B",
                    "Član" to "#F59E0B"
                )
                roles.forEach { (name, color) ->
                    db.execSQL("INSERT INTO roles (name, colorHex) VALUES ('$name', '$color')")
                }

                val members = listOf(
                    arrayOf("Ivan Horvat", "1", "2021-03-01", "ivan.horvat@email.com", "091/123-4567"),
                    arrayOf("Marko Marić", "2", "2022-08-15", "marko.maric@email.com", "092/876-5432"),
                    arrayOf("Ana Anić", "3", "2022-10-10", "ana.anic@email.com", "095/555-4443"),
                    arrayOf("Petra Petrović", "4", "2024-02-05", "petra.petrovic@email.com", "098/987-6543"),
                    arrayOf("Josip Jurić", "4", "2023-08-20", "josip.juric@email.com", "097/111-2222")
                )
                members.forEach { m ->
                    db.execSQL(
                        "INSERT INTO members (name, roleId, joinDate, email, phone, monthlyFeeOverride) " +
                            "VALUES ('${m[0]}', ${m[1]}, '${m[2]}', '${m[3]}', '${m[4]}', NULL)"
                    )
                }

                db.execSQL("INSERT INTO fee_config (id, defaultMonthlyFee) VALUES (1, 10.0)")

                val now = YearMonth.now()
                seedPayments(db, 1, YearMonth.of(2021, 3), now) { fromEnd, _ -> if (fromEnd == 0L) null else 10.0 }
                seedPayments(db, 2, YearMonth.of(2022, 8), now) { fromEnd, _ -> if (fromEnd <= 1L) null else 10.0 }
                seedPayments(db, 3, YearMonth.of(2022, 10), now) { fromEnd, i ->
                    when { fromEnd == 0L -> null; i % 3 == 2 -> 5.0; else -> 10.0 }
                }
                seedPayments(db, 4, YearMonth.of(2024, 2), now) { fromEnd, _ -> if (fromEnd <= 2L) null else 10.0 }
                seedPayments(db, 5, YearMonth.of(2023, 8), now) { _, i -> if (i % 3 == 0) 10.0 else null }

                val today = LocalDate.now()
                val events = listOf(
                    arrayOf("Godišnja skupština", today.minusDays(60).toString(), "Dvorana A", "Godišnji sastanak udruge"),
                    arrayOf("Radionica vještina", today.minusDays(20).toString(), "Učionica 1", "Praktična radionica"),
                    arrayOf("Humanitarna akcija", today.minusDays(5).toString(), "Gradski trg", "Prikupljanje pomoći"),
                    arrayOf("Mjesečni sastanak", today.plusDays(7).toString(), "Dvorana B", "Redovni mjesečni sastanak"),
                    arrayOf("Godišnji izlet", today.plusDays(25).toString(), "Planinarski dom", "Druženje članova")
                )
                events.forEach { e ->
                    db.execSQL(
                        "INSERT INTO events (title, date, location, notes) " +
                            "VALUES ('${e[0]}', '${e[1]}', '${e[2]}', '${e[3]}')"
                    )
                }

                val attendance = listOf(
                    1 to 1, 1 to 2, 1 to 3, 1 to 4,
                    2 to 1, 2 to 3, 2 to 5,
                    3 to 2, 3 to 4, 3 to 5
                )
                attendance.forEach { (eventId, memberId) ->
                    db.execSQL("INSERT INTO attendance (eventId, memberId) VALUES ($eventId, $memberId)")
                }

                val accounts = listOf(
                    Triple("admin", "admin", "ADMIN"),
                    Triple("preglednik", "preglednik", "VIEWER")
                )
                accounts.forEach { (username, password, role) ->
                    db.execSQL(
                        "INSERT INTO app_users (username, passwordHash, role) " +
                            "VALUES ('$username', '${PasswordHash.sha256(password)}', '$role')"
                    )
                }
            }

            private fun seedPayments(
                db: SupportSQLiteDatabase,
                memberId: Int,
                join: YearMonth,
                now: YearMonth,
                decide: (monthsFromEnd: Long, index: Int) -> Double?
            ) {
                var ym = join
                var i = 0
                while (!ym.isAfter(now)) {
                    val fromEnd = ChronoUnit.MONTHS.between(ym, now)
                    val amount = decide(fromEnd, i)
                    if (amount != null && amount > 0.0) {
                        val period = "%04d-%02d".format(ym.year, ym.monthValue)
                        db.execSQL(
                            "INSERT INTO fee_payments (memberId, periodMonth, amount, paidDate) " +
                                "VALUES ($memberId, '$period', $amount, '$period-05')"
                        )
                    }
                    ym = ym.plusMonths(1)
                    i++
                }
            }
        }
    }
}
