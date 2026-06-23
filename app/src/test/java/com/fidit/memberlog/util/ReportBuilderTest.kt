package com.fidit.memberlog.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportBuilderTest {

    private val data = ReportData(
        memberRows = listOf(
            MemberRow("Ivan Horvat", "Voditelj", "2021-03-01", "ivan@e.com", "091", 0.0),
            MemberRow("Anić, Ana", "Član", "2022-10-10", "ana@e.com", "095", 12.5)
        ),
        paymentRows = listOf(
            PaymentRow("Ivan Horvat", "2026-06", 10.0, "2026-06-05")
        ),
        eventRows = emptyList(),
        totals = ReportTotals(2, 100.0, 12.5, 1)
    )

    @Test
    fun membersCsv_hasHeaderAndRows() {
        val csv = ReportBuilder.membersCsv(data)
        val lines = csv.trim().split("\n")
        assertTrue(lines[0].startsWith("Ime,Uloga,"))
        assertEquals(3, lines.size)
        assertTrue(lines[1].startsWith("Ivan Horvat,Voditelj,2021-03-01"))
    }

    @Test
    fun membersCsv_escapesCommaWithQuotes() {
        val csv = ReportBuilder.membersCsv(data)
        assertTrue(csv.contains("\"Anić, Ana\""))
    }

    @Test
    fun membersCsv_formatsWholeAndDecimalAmounts() {
        val csv = ReportBuilder.membersCsv(data)
        assertTrue(csv.contains(",0\n"))
        assertTrue(csv.contains(",12.50\n"))
    }

    @Test
    fun paymentsCsv_hasHeaderAndRow() {
        val csv = ReportBuilder.paymentsCsv(data)
        val lines = csv.trim().split("\n")
        assertEquals("Clan,Mjesec,Iznos (EUR),Datum", lines[0])
        assertEquals("Ivan Horvat,2026-06,10,2026-06-05", lines[1])
    }
}
