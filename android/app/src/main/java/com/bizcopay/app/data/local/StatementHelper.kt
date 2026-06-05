package com.bizcopay.app.data.local

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.bizcopay.app.data.network.models.TransactionResponse
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StatementHelper {

    private const val PAGE_WIDTH  = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN      = 48f

    fun exportAndShare(
        context: Context,
        transactions: List<TransactionResponse>,
        role: String,
        ownerName: String = "",
        ownerEmail: String = "",
        walletId: String = "",
    ) {
        val pdf = buildPdf(transactions, role, ownerName, ownerEmail, walletId)
        val dir = File(context.getExternalFilesDir("Documents"), "").also { it.mkdirs() }
        val file = File(dir, "bizcopay_statement_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(intent, "Open Statement").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun buildPdf(
        transactions: List<TransactionResponse>,
        role: String,
        ownerName: String,
        ownerEmail: String,
        walletId: String,
    ): PdfDocument {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val bold   = Paint().apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        val normal = Paint().apply { typeface = Typeface.DEFAULT }
        val small  = Paint().apply { typeface = Typeface.DEFAULT }

        var y = MARGIN + 10f

        // ── Header ────────────────────────────────────────────────────────────
        bold.textSize = 22f
        bold.color = Color.rgb(26, 75, 138)
        canvas.drawText("Bizcopay", MARGIN, y, bold)

        bold.textSize = 12f
        bold.color = Color.rgb(90, 106, 133)
        val title = if (role == "MERCHANT") "Sales Statement" else "Transaction Statement"
        canvas.drawText(title, MARGIN, y + 24f, bold)

        normal.textSize = 11f
        normal.color = Color.rgb(13, 27, 54)
        if (ownerName.isNotBlank()) canvas.drawText(ownerName, MARGIN, y + 44f, normal)

        normal.textSize = 10f
        normal.color = Color.rgb(90, 106, 133)
        if (ownerEmail.isNotBlank()) canvas.drawText(ownerEmail, MARGIN, y + 59f, normal)

        if (walletId.isNotBlank()) {
            val maskedId = if (walletId.length > 8)
                "${walletId.take(4)}…${walletId.takeLast(4)}"
            else walletId
            normal.textSize = 10f
            normal.color = Color.rgb(90, 106, 133)
            canvas.drawText("Account ID: $maskedId", MARGIN, y + 74f, normal)
        }

        normal.textSize = 10f
        normal.color = Color.rgb(154, 170, 191)
        canvas.drawText("Generated: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())}", MARGIN, y + 89f, normal)

        val divPaint = Paint().apply { color = Color.rgb(221, 227, 238); strokeWidth = 1f }
        canvas.drawLine(MARGIN, y + 103f, PAGE_WIDTH - MARGIN, y + 103f, divPaint)
        y += 121f

        // ── Summary ───────────────────────────────────────────────────────────
        val approved = transactions.filter { it.status == "APPROVED" }
        val total = approved.sumOf { it.amount.toDouble() }
        bold.textSize = 13f
        bold.color = Color.rgb(13, 27, 54)
        canvas.drawText("Total: RWF ${"%,.0f".format(total)}", MARGIN, y, bold)
        normal.textSize = 10f
        normal.color = Color.rgb(90, 106, 133)
        canvas.drawText("${approved.size} approved  •  ${transactions.size} total", MARGIN, y + 18f, normal)
        y += 42f

        // ── Top 5 Transactions (payer only) ───────────────────────────────────
        if (role == "PAYER") {
            val top5 = approved.sortedByDescending { it.amount.toDouble() }.take(5)
            if (top5.isNotEmpty()) {
                bold.textSize = 10f
                bold.color = Color.rgb(13, 27, 54)
                canvas.drawText("TOP SPENDING", MARGIN, y, bold)
                y += 14f

                val topHeaderBg = Paint().apply { color = Color.rgb(238, 242, 248) }
                canvas.drawRect(MARGIN, y - 12f, PAGE_WIDTH - MARGIN, y + 5f, topHeaderBg)
                bold.textSize = 8f
                bold.color = Color.rgb(13, 27, 54)
                canvas.drawText("Merchant", MARGIN + 4f, y, bold)
                canvas.drawText("Amount (RWF)", 310f, y, bold)
                canvas.drawText("Date", 460f, y, bold)
                y += 14f

                small.textSize = 9f
                val rowAlt = Paint().apply { color = Color.rgb(248, 249, 252) }
                top5.forEachIndexed { idx, tx ->
                    if (idx % 2 == 1) canvas.drawRect(MARGIN, y - 10f, PAGE_WIDTH - MARGIN, y + 4f, rowAlt)
                    small.color = Color.rgb(13, 27, 54)
                    canvas.drawText((tx.merchant?.name ?: "—").take(22), MARGIN + 4f, y, small)
                    small.color = Color.rgb(229, 57, 53)
                    canvas.drawText("%,.0f".format(tx.amount.toDouble()), 310f, y, small)
                    small.color = Color.rgb(90, 106, 133)
                    canvas.drawText(tx.createdAt.take(10), 460f, y, small)
                    y += 16f
                }
                canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, divPaint)
                y += 18f
            }
        }

        // ── Table header ──────────────────────────────────────────────────────
        val col = floatArrayOf(MARGIN, 140f, 290f, 400f, 490f)
        val headers = if (role == "MERCHANT")
            arrayOf("Date", "Customer", "Amount (RWF)", "Status", "Category")
        else
            arrayOf("Date", "Merchant", "Amount (RWF)", "Status", "Category")

        val headerBg = Paint().apply { color = Color.rgb(238, 242, 248) }
        canvas.drawRect(MARGIN, y - 14f, PAGE_WIDTH - MARGIN, y + 6f, headerBg)
        bold.textSize = 9f
        bold.color = Color.rgb(13, 27, 54)
        headers.forEachIndexed { i, h -> canvas.drawText(h, col[i], y, bold) }
        y += 16f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, divPaint)
        y += 10f

        // ── Rows ──────────────────────────────────────────────────────────────
        small.textSize = 9f
        val rowPaintAlt = Paint().apply { color = Color.rgb(248, 249, 252) }

        transactions.forEachIndexed { idx, tx ->
            if (y > PAGE_HEIGHT - MARGIN) return@forEachIndexed

            if (idx % 2 == 1) canvas.drawRect(MARGIN, y - 10f, PAGE_WIDTH - MARGIN, y + 4f, rowPaintAlt)

            val date   = tx.createdAt.take(10)
            val party  = if (role == "MERCHANT") tx.payer?.name ?: "—" else tx.merchant?.name ?: "—"
            val amount = "%,.0f".format(tx.amount.toDouble())
            val status = tx.status
            val cat    = tx.category ?: "—"

            small.color = Color.rgb(13, 27, 54)
            canvas.drawText(date, col[0], y, small)
            canvas.drawText(party.take(18), col[1], y, small)
            canvas.drawText(amount, col[2], y, small)
            small.color = if (status == "APPROVED") Color.rgb(29, 185, 84) else Color.rgb(229, 57, 53)
            canvas.drawText(status, col[3], y, small)
            small.color = Color.rgb(90, 106, 133)
            canvas.drawText(cat, col[4], y, small)
            y += 18f
        }

        doc.finishPage(page)
        return doc
    }
}
