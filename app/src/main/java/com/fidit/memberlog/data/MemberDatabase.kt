package com.fidit.memberlog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fidit.memberlog.model.Attendance
import com.fidit.memberlog.model.Event
import com.fidit.memberlog.model.EventRsvp
import com.fidit.memberlog.model.FeeConfig
import com.fidit.memberlog.model.FeePayment
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.util.PasswordHash
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Database(
    entities = [Member::class, FeePayment::class, FeeConfig::class, Role::class, Event::class, Attendance::class, EventRsvp::class],
    version = 7,
    exportSchema = false
)
abstract class MemberDatabase : RoomDatabase() {

    abstract fun memberDao(): MemberDao
    abstract fun feeDao(): FeeDao
    abstract fun roleDao(): RoleDao
    abstract fun activityDao(): ActivityDao

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
                    Triple("Voditelj", "#6750A4", 1),
                    Triple("Tajnik", "#1E88E5", 1),
                    Triple("Blagajnik", "#2E9E6B", 1),
                    Triple("Član", "#F59E0B", 0)
                )
                roles.forEach { (name, color, grantsAdmin) ->
                    db.execSQL("INSERT INTO roles (name, colorHex, grantsAdmin) VALUES ('$name', '$color', $grantsAdmin)")
                }

                val pw = PasswordHash.sha256("lozinka")

                val members = listOf(
                    arrayOf("Ivan Horvat", "1", "2021-03-01", "ivan.horvat@email.com", "091/123-4567", "ACTIVE", "Ilica 25, Zagreb"),
                    arrayOf("Marko Marić", "2", "2022-08-15", "marko.maric@email.com", "092/876-5432", "ACTIVE", "Vukovarska 14, Split"),
                    arrayOf("Ana Anić", "3", "2022-10-10", "ana.anic@email.com", "095/555-4443", "ACTIVE", "Korzo 7, Rijeka"),
                    arrayOf("Petra Petrović", "4", "2024-02-05", "petra.petrovic@email.com", "098/987-6543", "ACTIVE", "Trg slobode 3, Osijek"),
                    arrayOf("Josip Jurić", "4", "2023-08-20", "josip.juric@email.com", "097/111-2222", "INACTIVE", "Zrinska 9, Varaždin"),
                    arrayOf("Marija Kovač", "4", "2021-09-01", "marija.kovac@email.com", "091/222-3344", "ACTIVE", "Maksimirska 110, Zagreb"),
                    arrayOf("Luka Novak", "4", "2023-01-12", "luka.novak@email.com", "098/333-1122", "ACTIVE", "Riva 5, Zadar"),
                    arrayOf("Iva Babić", "4", "2020-05-01", "iva.babic@email.com", "095/777-8899", "HONORARY", "Tkalčićeva 18, Zagreb"),
                    arrayOf("Tomislav Knežević", "4", "2024-09-10", "tomislav.knezevic@email.com", "097/444-5566", "ACTIVE", "Slavonska 2, Đakovo"),
                    arrayOf("Sara Vuković", "4", "2022-03-03", "sara.vukovic@email.com", "091/888-1212", "ACTIVE", "Branimirova 33, Zagreb"),
                    arrayOf("Filip Pavlović", "4", "2023-11-05", "filip.pavlovic@email.com", "092/121-3434", "INACTIVE", "Šetalište 8, Pula"),
                    arrayOf("Dora Matić", "4", "2025-01-15", "dora.matic@email.com", "099/565-7878", "ACTIVE", "Kvaternikov trg 1, Zagreb")
                )
                members.forEach { m ->
                    db.execSQL(
                        "INSERT INTO members (name, roleId, joinDate, email, phone, monthlyFeeOverride, photoPath, status, address, notes, passwordHash) " +
                            "VALUES ('${m[0]}', ${m[1]}, '${m[2]}', '${m[3]}', '${m[4]}', NULL, NULL, '${m[5]}', '${m[6]}', '', '$pw')"
                    )
                }

                db.execSQL("INSERT INTO fee_config (id, defaultMonthlyFee) VALUES (1, 10.0)")

                val now = YearMonth.now()
                seedPayments(db, 1, YearMonth.of(2021, 3), now) { _, _ -> 10.0 }
                seedPayments(db, 2, YearMonth.of(2022, 8), now) { fromEnd, _ -> if (fromEnd <= 1L) null else 10.0 }
                seedPayments(db, 3, YearMonth.of(2022, 10), now) { fromEnd, i ->
                    when { fromEnd == 0L -> null; i % 3 == 2 -> 5.0; else -> 10.0 }
                }
                seedPayments(db, 4, YearMonth.of(2024, 2), now) { fromEnd, _ -> if (fromEnd <= 2L) null else 10.0 }
                seedPayments(db, 5, YearMonth.of(2023, 8), now) { _, i -> if (i % 3 == 0) 10.0 else null }
                seedPayments(db, 6, YearMonth.of(2021, 9), now) { _, i -> if (i % 2 == 0) 10.0 else null }
                seedPayments(db, 7, YearMonth.of(2023, 1), now) { fromEnd, _ -> if (fromEnd == 0L) null else 5.0 }
                seedPayments(db, 8, YearMonth.of(2020, 5), now) { _, _ -> 10.0 }
                seedPayments(db, 9, YearMonth.of(2024, 9), now) { _, i -> if (i == 0) 10.0 else null }
                seedPayments(db, 10, YearMonth.of(2022, 3), now) { _, _ -> 10.0 }
                seedPayments(db, 11, YearMonth.of(2023, 11), now) { _, i -> if (i < 3) 10.0 else null }
                seedPayments(db, 12, YearMonth.of(2025, 1), now) { _, _ -> 10.0 }

                val today = LocalDate.now()
                val events = listOf(
                    arrayOf("Godišnja skupština", today.minusDays(60).toString(), "Dvorana A", "Godišnji sastanak udruge"),
                    arrayOf("Radionica vještina", today.minusDays(20).toString(), "Učionica 1", "Praktična radionica"),
                    arrayOf("Humanitarna akcija", today.minusDays(5).toString(), "Gradski trg", "Prikupljanje pomoći"),
                    arrayOf("Predavanje o zdravlju", today.minusDays(35).toString(), "Knjižnica", "Gostujući predavač"),
                    arrayOf("Mjesečni sastanak", today.plusDays(7).toString(), "Dvorana B", "Redovni mjesečni sastanak"),
                    arrayOf("Radionica za nove članove", today.plusDays(12).toString(), "Učionica 2", "Upoznavanje s radom udruge"),
                    arrayOf("Godišnji izlet", today.plusDays(25).toString(), "Planinarski dom", "Druženje članova")
                )
                events.forEach { e ->
                    db.execSQL(
                        "INSERT INTO events (title, date, location, notes) " +
                            "VALUES ('${e[0]}', '${e[1]}', '${e[2]}', '${e[3]}')"
                    )
                }

                val attendance = listOf(
                    1 to 1, 1 to 2, 1 to 3, 1 to 4, 1 to 6, 1 to 8, 1 to 10,
                    2 to 1, 2 to 3, 2 to 5, 2 to 7, 2 to 9,
                    3 to 2, 3 to 4, 3 to 5, 3 to 6, 3 to 10, 3 to 11,
                    4 to 1, 4 to 3, 4 to 8, 4 to 10
                )
                attendance.forEach { (eventId, memberId) ->
                    db.execSQL("INSERT INTO attendance (eventId, memberId) VALUES ($eventId, $memberId)")
                }

                val rsvps = listOf(
                    5 to 1, 5 to 4, 5 to 8, 5 to 10,
                    6 to 4, 6 to 9, 6 to 12,
                    7 to 1, 7 to 3, 7 to 6
                )
                rsvps.forEach { (eventId, memberId) ->
                    db.execSQL("INSERT INTO event_rsvp (eventId, memberId) VALUES ($eventId, $memberId)")
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
