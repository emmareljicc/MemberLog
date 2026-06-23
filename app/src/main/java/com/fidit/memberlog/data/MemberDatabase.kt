package com.fidit.memberlog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fidit.memberlog.model.Member

@Database(entities = [Member::class], version = 1, exportSchema = false)
abstract class MemberDatabase : RoomDatabase() {

    abstract fun memberDao(): MemberDao

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
                    .fallbackToDestructiveMigration(false)
                    .addCallback(SeedCallback)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        /** Populates the database with the initial demo members the first time it is created. */
        private val SeedCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val seed = listOf(
                    "('Ivan Horvat', 'Voditelj', '01.03.2021.', 1, 'ivan.horvat@email.com', '091/123-4567')",
                    "('Marko Marić', 'Tajnik', '15.08.2022.', 1, 'marko.maric@email.com', '092/876-5432')",
                    "('Ana Anić', 'Blagajnik', '10.10.2022.', 0, 'ana.anic@email.com', '095/555-4443')",
                    "('Petra Petrović', 'Član', '05.02.2024.', 1, 'petra.petrovic@email.com', '098/987-6543')",
                    "('Josip Jurić', 'Član', '20.08.2023.', 0, 'josip.juric@email.com', '097/111-2222')"
                )
                seed.forEach { values ->
                    db.execSQL(
                        "INSERT INTO members (name, role, joinDate, isPaid, email, phone) VALUES $values"
                    )
                }
            }
        }
    }
}
