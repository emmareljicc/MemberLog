package com.fidit.memberlog.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.time.LocalDate

object PdfReport {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val LEFT = 40f
    private const val RIGHT = 555f

    fun write(context: Context, data: ReportData): File {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create())
        val canvas = page.canvas

        val title = Paint().apply { textSize = 22f; isFakeBoldText = true }
        val heading = Paint().apply { textSize = 14f; typeface = Typeface.DEFAULT_BOLD }
        val body = Paint().apply { textSize = 11f }
        val muted = Paint().apply { textSize = 10f; color = 0xFF666666.toInt() }

        var y = 56f
        canvas.drawText("MemberLog — Izvjestaj", LEFT, y, title)
        y += 18f
        canvas.drawText("Datum: ${LocalDate.now()}", LEFT, y, muted)
        y += 28f

        canvas.drawText("Sazetak", LEFT, y, heading); y += 18f
        canvas.drawText("Broj clanova: ${data.totals.members}", LEFT, y, body); y += 16f
        canvas.drawText("Placeno ovaj mjesec: ${data.totals.paidThisMonth} / ${data.totals.members}", LEFT, y, body); y += 16f
        canvas.drawText("Prikupljeno ukupno: ${money(data.totals.collected)} EUR", LEFT, y, body); y += 16f
        canvas.drawText("Dugovanje ukupno: ${money(data.totals.outstanding)} EUR", LEFT, y, body); y += 28f

        canvas.drawText("Duznici", LEFT, y, heading); y += 18f
        val debtors = data.memberRows.filter { it.owed > 0.0 }.sortedByDescending { it.owed }
        if (debtors.isEmpty()) {
            canvas.drawText("Nema duznika.", LEFT, y, body); y += 16f
        } else {
            debtors.take(15).forEach { r ->
                canvas.drawText(r.name, LEFT, y, body)
                canvas.drawText("${money(r.owed)} EUR", RIGHT - 80f, y, body)
                y += 15f
            }
        }
        y += 16f

        canvas.drawText("Dogadaji i dolasci", LEFT, y, heading); y += 18f
        if (data.eventRows.isEmpty()) {
            canvas.drawText("Nema dogadaja.", LEFT, y, body); y += 16f
        } else {
            data.eventRows.take(15).forEach { e ->
                if (y < PAGE_HEIGHT - 40f) {
                    canvas.drawText("${e.title} (${e.date})", LEFT, y, body)
                    canvas.drawText("${e.attendees}", RIGHT - 40f, y, body)
                    y += 15f
                }
            }
        }

        doc.finishPage(page)

        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(dir, "izvjestaj.pdf")
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }

    private fun money(v: Double): String =
        if (v % 1.0 == 0.0) v.toInt().toString() else "%.2f".format(v)
}
