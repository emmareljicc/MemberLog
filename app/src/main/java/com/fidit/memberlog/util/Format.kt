package com.fidit.memberlog.util

object Format {
    fun money(v: Double): String =
        if (v % 1.0 == 0.0) v.toInt().toString() else "%.2f".format(v)

    fun eur(v: Double): String = money(v) + " €"
}
