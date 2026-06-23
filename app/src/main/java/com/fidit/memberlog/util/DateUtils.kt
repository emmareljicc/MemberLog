package com.fidit.memberlog.util

import java.time.LocalDate
import java.time.YearMonth

object DateUtils {

    fun todayIso(): String = LocalDate.now().toString()

    fun currentYearMonth(): String = YearMonth.now().toString()

    fun formatIsoDate(iso: String): String = try {
        val d = LocalDate.parse(iso)
        "%02d.%02d.%04d.".format(d.dayOfMonth, d.monthValue, d.year)
    } catch (e: Exception) {
        iso
    }

    fun formatPeriod(period: String): String = try {
        val ym = YearMonth.parse(period)
        "%02d.%04d.".format(ym.monthValue, ym.year)
    } catch (e: Exception) {
        period
    }
}
