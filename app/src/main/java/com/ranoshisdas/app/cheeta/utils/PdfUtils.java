package com.ranoshisdas.app.cheeta.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import com.ranoshisdas.app.cheeta.models.Bill;
import com.ranoshisdas.app.cheeta.models.BillItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfUtils {

    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int MARGIN = 40;

    public static File generateBillPdf(Context context, Bill bill) throws IOException {

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        int y = 50;

        /* =========================
           COMPANY HEADER (LEFT)
           ========================= */
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText("New Priti Press", MARGIN, y, paint);

        paint.setTextSize(11);
        paint.setFakeBoldText(false);
        y += 18;
        canvas.drawText("Prop: Pronay Kumar Sikder", MARGIN, y, paint);
        y += 14;
        canvas.drawText("GSTIN: 19CNHPS3979J1Z1", MARGIN, y, paint);

        /* =========================
           INVOICE HEADER (RIGHT)
           ========================= */
        paint.setTextSize(26);
        paint.setFakeBoldText(true);
        canvas.drawText("INVOICE", PAGE_WIDTH - 180, 50, paint);

        paint.setTextSize(11);
        paint.setFakeBoldText(false);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        canvas.drawText("Date:", PAGE_WIDTH - 180, 90, paint);
        canvas.drawText(sdf.format(new Date(bill.timestamp)), PAGE_WIDTH - 100, 90, paint);

        canvas.drawText("Invoice #:", PAGE_WIDTH - 180, 110, paint);
        canvas.drawText(bill.billId, PAGE_WIDTH - 100, 110, paint);

        canvas.drawText("Customer ID:", PAGE_WIDTH - 180, 130, paint);
        canvas.drawText(bill.customer.phone, PAGE_WIDTH - 100, 130, paint);

        /* =========================
           BILL TO / SHIP TO
           ========================= */
        y = 170;

        drawSectionHeader(canvas, paint, MARGIN, y, "BILL TO:");
        drawSectionHeader(canvas, paint, PAGE_WIDTH / 2 + 10, y, "SHIP TO:");

        paint.setTextSize(11);
        paint.setFakeBoldText(false);

        y += 20;
        canvas.drawText(bill.customer.name, MARGIN, y, paint);
        canvas.drawText(bill.customer.name, PAGE_WIDTH / 2 + 10, y, paint);

        y += 14;
        canvas.drawText(bill.customer.phone, MARGIN, y, paint);
        canvas.drawText(bill.customer.phone, PAGE_WIDTH / 2 + 10, y, paint);

        /* =========================
           ITEMS TABLE HEADER
           ========================= */
        y += 40;
        drawTableLine(canvas, y);
        y += 15;

        paint.setTextSize(11);
        paint.setFakeBoldText(true);

        canvas.drawText("ITEM", MARGIN, y, paint);
        canvas.drawText("DESCRIPTION", 120, y, paint);
        canvas.drawText("QTY", 330, y, paint);
        canvas.drawText("UNIT PRICE", 380, y, paint);
        canvas.drawText("TOTAL", 480, y, paint);

        y += 8;
        drawTableLine(canvas, y);

        /* =========================
           ITEMS ROWS
           ========================= */
        paint.setFakeBoldText(false);
        y += 18;

        for (BillItem item : bill.items) {
            canvas.drawText(item.name, MARGIN, y, paint);
            canvas.drawText(item.name, 120, y, paint);
            canvas.drawText(String.valueOf(item.quantity), 330, y, paint);
            canvas.drawText("₹" + format(item.price), 380, y, paint);
            canvas.drawText("₹" + format(item.subtotal), 480, y, paint);
            y += 18;
        }

        y += 5;
        drawTableLine(canvas, y);

        /* =========================
           TOTAL SUMMARY (RIGHT)
           ========================= */
        y += 30;

        paint.setTextSize(11);
        canvas.drawText("SUBTOTAL:", 360, y, paint);
        canvas.drawText("₹" + format(bill.total), 480, y, paint);

        y += 18;
        canvas.drawText("TAX:", 360, y, paint);
        canvas.drawText("₹0.00", 480, y, paint);

        y += 18;
        paint.setFakeBoldText(true);
        canvas.drawText("TOTAL:", 360, y, paint);
        canvas.drawText("₹" + format(bill.total), 480, y, paint);

        /* =========================
           FOOTER
           ========================= */
        y += 50;
        paint.setTextSize(10);
        paint.setFakeBoldText(false);
        canvas.drawText(
                "* This is a system generated invoice. Signature not required.",
                MARGIN,
                y,
                paint
        );

        y += 20;
        paint.setFakeBoldText(true);
        canvas.drawText("Thank You For Your Business!", PAGE_WIDTH / 2 - 80, y, paint);

        document.finishPage(page);

        /* =========================
           SAVE FILE
           ========================= */
        File dir = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "Cheeta/Bills"
        );

        if (!dir.exists()) dir.mkdirs();

        File file = new File(
                dir,
                "Invoice_" + bill.billId + ".pdf"
        );

        FileOutputStream fos = new FileOutputStream(file);
        document.writeTo(fos);
        document.close();
        fos.close();

        return file;
    }

    /* =========================
       HELPERS
       ========================= */

    private static void drawSectionHeader(Canvas canvas, Paint paint, int x, int y, String title) {
        paint.setTextSize(12);
        paint.setFakeBoldText(true);
        canvas.drawText(title, x, y, paint);
    }

    private static void drawTableLine(Canvas canvas, int y) {
        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(1);
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint);
    }

    private static String format(double value) {
        return String.format(Locale.getDefault(), "%.2f", value);
    }
}
