package com.fidit.memberlog.util

object Plurals {
    fun clanovi(n: Int): String {
        val word = when {
            n % 100 in 11..14 -> "članova"
            n % 10 == 1 -> "član"
            n % 10 in 2..4 -> "člana"
            else -> "članova"
        }
        return "$n $word"
    }
}
