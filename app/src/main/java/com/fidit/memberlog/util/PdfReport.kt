package com.fidit.memberlog.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import com.fidit.memberlog.R
import java.io.File
import java.time.LocalDate

object PdfReport {

    private const val PW = 595
    private const val PH = 842
    private const val LEFT = 48f
    private const val RIGHT = 547f
    private const val BOTTOM = 788f

    private val INK = "#1C1B20".toColorInt()
    private val MUTED = "#6B6580".toColorInt()
    private val HAIRLINE = "#D9D3E4".toColorInt()
    private val RULE = "#1C1B20".toColorInt()
    private val DEBT = "#A53B30".toColorInt()

    fun write(context: Context, data: ReportData, periodLabel: String? = null): File {
        val grotesk = ResourcesCompat.getFont(context, R.font.space_grotesk) ?: Typeface.DEFAULT_BOLD
        val display = Typeface.create(grotesk, Typeface.BOLD)
        val today = LocalDate.now().toString()

        val totalPages = Pager(null, display, today, periodLabel, 0).run { lay(data); pageNo }
        val doc = PdfDocument()
        Pager(doc, display, today, periodLabel, totalPages).run { lay(data) }

        val dir = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
        val file = File(dir, "izvjestaj.pdf")
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }

    private class Pager(
        val doc: PdfDocument?,
        val display: Typeface,
        val today: String,
        val periodLabel: String?,
        val totalPages: Int
    ) {
        var page: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var y = 0f
        var pageNo = 0

        fun lay(data: ReportData) {
            start()
            summary(data)
            val debtors = data.memberRows.filter { it.owed > 0.0 }.sortedByDescending { it.owed }
            table("Dugovanja", debtors.size, "Nema dužnika.", ::headDebtors) { rowDebtors(debtors[it]) }
            table("Uplate", data.paymentRows.size, "Nema uplata u razdoblju.", ::headPayments) { rowPayments(data.paymentRows[it]) }
            table("Događanja", data.eventRows.size, "Nema događanja.", ::headEvents) { rowEvents(data.eventRows[it]) }
            finish()
        }

        private fun start() {
            pageNo++
            if (doc != null) {
                page = doc.startPage(PdfDocument.PageInfo.Builder(PW, PH, pageNo).create())
                canvas = page!!.canvas
            }
            y = if (pageNo == 1) header() else slimHeader()
        }

        private fun finish() {
            footer()
            if (doc != null && page != null) doc.finishPage(page)
            page = null; canvas = null
        }

        private fun ensure(h: Float): Boolean {
            if (y + h > BOTTOM) { finish(); start(); return true }
            return false
        }

        private fun header(): Float {
            val c = canvas
            c?.drawText("MemberLog izvještaj", LEFT, 70f, text(22f, INK, display))
            val sub = "Datum: $today" + (periodLabel?.let { "      Razdoblje: $it" } ?: "")
            c?.drawText(sub, LEFT, 92f, text(10f, MUTED))
            c?.drawLine(LEFT, 106f, RIGHT, 106f, stroke(RULE, 1.2f))
            return 134f
        }

        private fun slimHeader(): Float {
            val c = canvas
            c?.drawText("MemberLog izvještaj", LEFT, 50f, text(12f, MUTED, display))
            c?.drawLine(LEFT, 60f, RIGHT, 60f, stroke(HAIRLINE, 1f))
            return 86f
        }

        private fun footer() {
            val c = canvas ?: return
            c.drawLine(LEFT, 804f, RIGHT, 804f, stroke(HAIRLINE, 1f))
            c.drawText("Generirano: $today", LEFT, 820f, text(8.5f, MUTED))
            c.drawText("Stranica $pageNo / $totalPages", RIGHT, 820f, text(8.5f, MUTED, align = Paint.Align.RIGHT))
        }

        private fun summary(data: ReportData) {
            val stats = listOf(
                "Članova" to data.totals.members.toString(),
                "Plaćeno (mjesec)" to "${data.totals.paidThisMonth} / ${data.totals.members}",
                "Prikupljeno" to "${money(data.totals.collected)} €",
                "Dugovanja" to "${money(data.totals.outstanding)} €"
            )
            val colW = (RIGHT - LEFT) / 4
            stats.forEachIndexed { i, (label, value) ->
                val x = LEFT + i * colW
                canvas?.drawText(value, x, y + 4f, text(16f, INK, display))
                canvas?.drawText(label.uppercase(), x, y + 20f, text(8f, MUTED))
            }
            y += 44f
            canvas?.drawLine(LEFT, y, RIGHT, y, stroke(HAIRLINE, 1f))
            y += 24f
        }

        private fun table(title: String, rows: Int, emptyText: String, head: () -> Unit, row: (Int) -> Unit) {
            ensure(28f + 20f + 22f)
            canvas?.drawText(title, LEFT, y, text(13f, INK, display))
            y += 10f
            canvas?.drawLine(LEFT, y, RIGHT, y, stroke(RULE, 1f))
            y += 18f
            head()
            y += 6f
            canvas?.drawLine(LEFT, y, RIGHT, y, stroke(HAIRLINE, 1f))
            y += 16f
            if (rows == 0) {
                canvas?.drawText(emptyText, LEFT, y, text(10f, MUTED))
                y += 22f
            } else {
                for (i in 0 until rows) {
                    if (ensure(22f)) { head(); y += 6f; canvas?.drawLine(LEFT, y, RIGHT, y, stroke(HAIRLINE, 1f)); y += 16f }
                    row(i)
                    y += 8f
                    canvas?.drawLine(LEFT, y, RIGHT, y, stroke(HAIRLINE, 0.7f))
                    y += 14f
                }
            }
            y += 22f
        }

        private fun headDebtors() {
            canvas?.drawText("IME", LEFT, y, text(8.5f, MUTED, display))
            canvas?.drawText("DUGOVANJE (EUR)", RIGHT, y, text(8.5f, MUTED, display, Paint.Align.RIGHT))
        }

        private fun rowDebtors(r: MemberRow) {
            canvas?.drawText(clip(r.name, text(10.5f, INK), 380f), LEFT, y, text(10.5f, INK))
            canvas?.drawText(money(r.owed), RIGHT, y, text(10.5f, DEBT, align = Paint.Align.RIGHT))
        }

        private fun headPayments() {
            canvas?.drawText("ČLAN", LEFT, y, text(8.5f, MUTED, display))
            canvas?.drawText("MJESEC", 330f, y, text(8.5f, MUTED, display))
            canvas?.drawText("DATUM", 430f, y, text(8.5f, MUTED, display))
            canvas?.drawText("IZNOS (EUR)", RIGHT, y, text(8.5f, MUTED, display, Paint.Align.RIGHT))
        }

        private fun rowPayments(p: PaymentRow) {
            canvas?.drawText(clip(p.memberName, text(10.5f, INK), 270f), LEFT, y, text(10.5f, INK))
            canvas?.drawText(DateUtils.formatPeriod(p.period), 330f, y, text(10.5f, INK))
            canvas?.drawText(DateUtils.formatIsoDate(p.paidDate), 430f, y, text(10.5f, INK))
            canvas?.drawText(money(p.amount), RIGHT, y, text(10.5f, INK, align = Paint.Align.RIGHT))
        }

        private fun headEvents() {
            canvas?.drawText("NAZIV", LEFT, y, text(8.5f, MUTED, display))
            canvas?.drawText("DATUM", 350f, y, text(8.5f, MUTED, display))
            canvas?.drawText("DOLAZAKA", RIGHT, y, text(8.5f, MUTED, display, Paint.Align.RIGHT))
        }

        private fun rowEvents(e: EventRow) {
            canvas?.drawText(clip(e.title, text(10.5f, INK), 290f), LEFT, y, text(10.5f, INK))
            canvas?.drawText(DateUtils.formatIsoDate(e.date), 350f, y, text(10.5f, INK))
            canvas?.drawText("${e.attendees}", RIGHT, y, text(10.5f, INK, align = Paint.Align.RIGHT))
        }

        private fun stroke(color: Int, w: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color; style = Paint.Style.STROKE; strokeWidth = w
        }

        private fun text(size: Float, color: Int, face: Typeface? = null, align: Paint.Align = Paint.Align.LEFT) =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color; textSize = size; textAlign = align
                if (face != null) typeface = face
            }
    }

    private fun clip(value: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(value) <= maxWidth) return value
        var s = value
        while (s.isNotEmpty() && paint.measureText("$s…") > maxWidth) s = s.dropLast(1)
        return "$s…"
    }

    private fun money(v: Double): String = Format.money(v)
}
