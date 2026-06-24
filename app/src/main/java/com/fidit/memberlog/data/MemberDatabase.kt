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
import com.fidit.memberlog.model.FeeRate
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.util.PasswordHash
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Database(
    entities = [Member::class, FeePayment::class, FeeConfig::class, FeeRate::class, Role::class, Event::class, Attendance::class, EventRsvp::class],
    version = 10,
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
                seedDatabase(db)
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                val empty = db.query("SELECT COUNT(*) FROM roles").use { it.moveToFirst() && it.getInt(0) == 0 }
                if (empty) seedDatabase(db)
            }

            private fun seedDatabase(db: SupportSQLiteDatabase) {
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
                    arrayOf<String?>("Ivan Horvat", "1", "2021-03-01", "ivan.horvat@email.com", "091 123-4567", "ACTIVE", "Ilica 25, Zagreb", "", null),
                    arrayOf<String?>("Marko Marić", "2", "2022-08-15", "marko.maric@email.com", "092 876-5432", "ACTIVE", "Vukovarska 14, Split", "", null),
                    arrayOf<String?>("Ana Anić", "3", "2022-10-10", "ana.anic@email.com", "095 555-4443", "ACTIVE", "Korzo 7, Rijeka", "", null),
                    arrayOf<String?>("Petra Petrović", "4", "2024-02-05", "petra.petrovic@email.com", "098 987-6543", "ACTIVE", "Trg slobode 3, Osijek", "", null),
                    arrayOf<String?>("Josip Jurić", "4", "2023-08-20", "josip.juric@email.com", "097 111-2222", "INACTIVE", "Zrinska 9, Varaždin", "", null),
                    arrayOf<String?>("Marija Kovač", "4", "2021-09-01", "marija.kovac@email.com", "091 222-3344", "ACTIVE", "Maksimirska 110, Zagreb", "", null),
                    arrayOf<String?>("Luka Novak", "4", "2023-01-12", "luka.novak@email.com", "098 333-1122", "ACTIVE", "Riva 5, Zadar", "", null),
                    arrayOf<String?>("Iva Babić", "4", "2020-05-01", "iva.babic@email.com", "095 777-8899", "HONORARY", "Tkalčićeva 18, Zagreb", "", null),
                    arrayOf<String?>("Tomislav Knežević", "4", "2024-09-10", "tomislav.knezevic@email.com", "097 444-5566", "ACTIVE", "Slavonska 2, Đakovo", "", null),
                    arrayOf<String?>("Sara Vuković", "4", "2022-03-03", "sara.vukovic@email.com", "091 888-1212", "ACTIVE", "Branimirova 33, Zagreb", "", null),
                    arrayOf<String?>("Filip Pavlović", "4", "2023-11-05", "filip.pavlovic@email.com", "092 121-3434", "INACTIVE", "Šetalište 8, Pula", "", null),
                    arrayOf<String?>("Dora Matić", "4", "2025-01-15", "dora.matic@email.com", "099 565-7878", "ACTIVE", "Kvaternikov trg 1, Zagreb", "", null),
                    arrayOf<String?>("Nikolina Babić", "4", "2025-09-12", "nikolina.babic@email.com", "098 210-3456", "ACTIVE", "Petrinjska 12, Sisak", "Vodi sekciju fotografije", null),
                    arrayOf<String?>("Ante Tomić", "4", "2019-04-18", "ante.tomic@email.com", "091 640-7788", "HONORARY", "Stradun 4, Dubrovnik", "Počasni član od osnutka", "0.0"),
                    arrayOf<String?>("Lucija Horvat", "2", "2024-06-30", "lucija.horvat@email.com", "095 330-1290", "ACTIVE", "Trg bana Jelačića 9, Zagreb", "Zamjenica tajnika", null),
                    arrayOf<String?>("Matej Kovačević", "4", "2025-12-03", "matej.kovacevic@email.com", "092 455-6677", "ACTIVE", "Kapucinska 5, Varaždin", "Student, snižena članarina", "5.0"),
                    arrayOf<String?>("Ena Jurić", "4", "2026-02-20", "ena.juric@email.com", "097 812-3344", "ACTIVE", "Ulica grada Vukovara 21, Vinkovci", "", null),
                    arrayOf<String?>("Davor Marić", "3", "2020-11-11", "davor.maric@email.com", "099 123-9988", "ACTIVE", "Frankopanska 30, Karlovac", "Pomoćni blagajnik", null),
                    arrayOf<String?>("Petra Novak", "4", "2023-05-22", "petra.novak@email.com", "091 777-2210", "INACTIVE", "Obala 14, Šibenik", "Privremeno na pauzi", null),
                    arrayOf<String?>("Karlo Babić", "4", "2025-07-08", "karlo.babic@email.com", "098 553-4321", "ACTIVE", "Vukovarska 88, Osijek", "Plaća višu članarinu kao podršku", "15.0"),
                    arrayOf<String?>("Mia Vuković", "4", "2026-04-05", "mia.vukovic@email.com", "095 909-1234", "ACTIVE", "Zagrebačka 2, Čakovec", "", null),
                    arrayOf<String?>("Stjepan Knežević", "4", "2018-09-30", "stjepan.knezevic@email.com", "092 334-5567", "HONORARY", "Masarykova 1, Bjelovar", "Počasni član, bivši voditelj", "0.0"),
                    arrayOf<String?>("Lana Pavlović", "4", "2024-10-17", "lana.pavlovic@email.com", "097 220-8765", "ACTIVE", "Adamićeva 6, Rijeka", "Sekcija mladih", null),
                    arrayOf<String?>("Roko Matić", "4", "2025-11-25", "roko.matic@email.com", "091 445-9090", "INACTIVE", "Splitska 19, Split", "Ne plaća redovito", null)
                )
                val photos = mapOf(
                    "ivan.horvat@email.com" to "file:///android_asset/seed_photos/ivan.jpg",
                    "marko.maric@email.com" to "file:///android_asset/seed_photos/marko.jpg",
                    "petra.petrovic@email.com" to "file:///android_asset/seed_photos/petra.png",
                    "josip.juric@email.com" to "file:///android_asset/seed_photos/josip.png",
                    "luka.novak@email.com" to "file:///android_asset/seed_photos/luka.jpg",
                    "ante.tomic@email.com" to "file:///android_asset/seed_photos/ante.jpg",
                    "davor.maric@email.com" to "file:///android_asset/seed_photos/davor.jpg",
                    "stjepan.knezevic@email.com" to "file:///android_asset/seed_photos/stjepan.png",
                    "matej.kovacevic@email.com" to "file:///android_asset/seed_photos/man1.jpg",
                    "sara.vukovic@email.com" to "file:///android_asset/seed_photos/woman1.png",
                    "lucija.horvat@email.com" to "file:///android_asset/seed_photos/woman2.jpg",
                    "ana.anic@email.com" to "file:///android_asset/seed_photos/woman3.png",
                    "nikolina.babic@email.com" to "file:///android_asset/seed_photos/woman4.jpg"
                )
                members.forEach { m ->
                    val feeVal = m[8] ?: "NULL"
                    val photo = photos[m[3]]?.let { "'$it'" } ?: "NULL"
                    db.execSQL(
                        "INSERT INTO members (name, roleId, joinDate, email, phone, monthlyFeeOverride, photoPath, status, address, notes, passwordHash) " +
                            "VALUES ('${m[0]}', ${m[1]}, '${m[2]}', '${m[3]}', '${m[4]}', $feeVal, $photo, '${m[5]}', '${m[6]}', '${m[7]}', '$pw')"
                    )
                }

                db.execSQL("INSERT INTO fee_config (id, defaultMonthlyFee) VALUES (1, 10.0)")

                db.execSQL("INSERT INTO fee_rates (memberId, effectiveFrom, amount) VALUES (NULL, '1970-01', 10.0)")
                val memberRates = listOf(
                    Triple(14, "2019-04", 0.0),
                    Triple(16, "2025-12", 5.0),
                    Triple(20, "2025-07", 15.0),
                    Triple(22, "2018-09", 0.0)
                )
                memberRates.forEach { (mId, from, amount) ->
                    db.execSQL("INSERT INTO fee_rates (memberId, effectiveFrom, amount) VALUES ($mId, '$from', $amount)")
                }

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
                seedPayments(db, 13, YearMonth.of(2025, 9), now) { fromEnd, _ -> if (fromEnd == 0L) null else 10.0 }
                seedPayments(db, 14, YearMonth.of(2019, 4), now) { _, i -> if (i % 4 == 0) 12.0 else null }
                seedPayments(db, 15, YearMonth.of(2024, 6), now) { _, i -> if (i % 5 == 0) 7.5 else 10.0 }
                seedPayments(db, 16, YearMonth.of(2025, 12), now) { fromEnd, _ -> if (fromEnd == 0L) null else 5.0 }
                seedPayments(db, 17, YearMonth.of(2026, 2), now) { _, _ -> 7.5 }
                seedPayments(db, 18, YearMonth.of(2020, 11), now) { _, _ -> 10.0 }
                seedPayments(db, 19, YearMonth.of(2023, 5), now) { _, i -> if (i < 12) 10.0 else null }
                seedPayments(db, 20, YearMonth.of(2025, 7), now) { fromEnd, _ -> if (fromEnd == 0L) null else 15.0 }
                seedPayments(db, 21, YearMonth.of(2026, 4), now) { _, i -> if (i == 0) 10.0 else null }
                seedPayments(db, 22, YearMonth.of(2018, 9), now) { _, i -> if (i % 6 == 0) 20.0 else null }
                seedPayments(db, 23, YearMonth.of(2024, 10), now) { _, i -> if (i % 3 == 1) 2.5 else 10.0 }
                seedPayments(db, 24, YearMonth.of(2025, 11), now) { _, i -> if (i == 0) 10.0 else null }

                val today = LocalDate.now()
                val events = listOf(
                    arrayOf("Godišnji sastanak", today.minusDays(60).toString(), "Dvorana A", "Godišnji sastanak udruge"),
                    arrayOf("Radionica vještina", today.minusDays(20).toString(), "Učionica 1", "Praktična radionica"),
                    arrayOf("Humanitarna akcija", today.minusDays(5).toString(), "Gradski trg", "Prikupljanje pomoći"),
                    arrayOf("Predavanje o zdravlju", today.minusDays(35).toString(), "Knjižnica", "Gostujući predavač"),
                    arrayOf("Mjesečni sastanak", today.plusDays(7).toString(), "Dvorana B", "Redovni mjesečni sastanak"),
                    arrayOf("Radionica za nove članove", today.plusDays(12).toString(), "Učionica 2", "Upoznavanje s radom udruge"),
                    arrayOf("Godišnji izlet", today.plusDays(25).toString(), "Planinarski dom", "Druženje članova"),
                    arrayOf("Tečaj prve pomoći", today.minusDays(120).toString(), "Crveni križ", "Edukacija članova"),
                    arrayOf("Izložba radova", today.minusDays(90).toString(), "Galerija grada", "Prikaz članskih radova"),
                    arrayOf("Dobrotvorni koncert", today.minusDays(75).toString(), "Koncertna dvorana", "Prikupljanje sredstava"),
                    arrayOf("Sastanak upravnog odbora", today.minusDays(15).toString(), "Ured udruge", "Mjesečni sastanak vodstva"),
                    arrayOf("Volonterska akcija čišćenja", today.plusDays(3).toString(), "Gradski park", "Uređenje okoliša"),
                    arrayOf("Predavanje o povijesti", today.plusDays(40).toString(), "Gradski muzej", "Gostujući predavač"),
                    arrayOf("Ljetni piknik", today.plusDays(60).toString(), "Jezero Jarun", "Druženje članova i obitelji")
                )
                events.forEach { e ->
                    db.execSQL(
                        "INSERT INTO events (title, date, location, notes) " +
                            "VALUES ('${e[0]}', '${e[1]}', '${e[2]}', '${e[3]}')"
                    )
                }

                val attendance = listOf(
                    1 to 1, 1 to 2, 1 to 3, 1 to 4, 1 to 6, 1 to 8, 1 to 10, 1 to 14, 1 to 15, 1 to 18,
                    2 to 1, 2 to 3, 2 to 5, 2 to 7, 2 to 9, 2 to 15, 2 to 23,
                    3 to 2, 3 to 4, 3 to 5, 3 to 6, 3 to 10, 3 to 11, 3 to 13, 3 to 20, 3 to 23,
                    4 to 1, 4 to 3, 4 to 8, 4 to 10, 4 to 14, 4 to 18, 4 to 22,
                    8 to 1, 8 to 2, 8 to 3, 8 to 6, 8 to 8, 8 to 14, 8 to 18, 8 to 22,
                    9 to 2, 9 to 4, 9 to 6, 9 to 10, 9 to 15, 9 to 18, 9 to 23,
                    10 to 1, 10 to 3, 10 to 8, 10 to 13, 10 to 14, 10 to 20, 10 to 22,
                    11 to 1, 11 to 2, 11 to 15, 11 to 18, 11 to 19, 11 to 23
                )
                attendance.forEach { (eventId, memberId) ->
                    db.execSQL("INSERT INTO attendance (eventId, memberId) VALUES ($eventId, $memberId)")
                }

                val rsvps = listOf(
                    5 to 1, 5 to 4, 5 to 8, 5 to 10, 5 to 13, 5 to 15, 5 to 18, 5 to 23,
                    6 to 4, 6 to 9, 6 to 12, 6 to 16, 6 to 17, 6 to 21,
                    7 to 1, 7 to 3, 7 to 6, 7 to 14, 7 to 20, 7 to 24,
                    12 to 1, 12 to 3, 12 to 13, 12 to 15, 12 to 18, 12 to 20, 12 to 23,
                    13 to 2, 13 to 4, 13 to 16, 13 to 17, 13 to 21,
                    14 to 1, 14 to 6, 14 to 13, 14 to 14, 14 to 20, 14 to 22, 14 to 23, 14 to 24
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
                        val day = 1 + (i * 7) % 27
                        val paidDate = "%s-%02d".format(period, day)
                        db.execSQL(
                            "INSERT INTO fee_payments (memberId, periodMonth, amount, paidDate) " +
                                "VALUES ($memberId, '$period', $amount, '$paidDate')"
                        )
                    }
                    ym = ym.plusMonths(1)
                    i++
                }
            }
        }
    }
}
