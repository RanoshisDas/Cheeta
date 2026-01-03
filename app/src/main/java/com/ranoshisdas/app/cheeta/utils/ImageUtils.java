package com.ranoshisdas.app.cheeta.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;

import com.ranoshisdas.app.cheeta.models.Bill;
import com.ranoshisdas.app.cheeta.models.BillItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtils {

    // Same dimensions as PdfUtils for consistent layout
    private static final int PAGE_WIDTH = 595;
    private static final int MARGIN = 40;

    public static File generateBillImage(Context context, Bill bill) throws IOException {

        // 1. Check Settings (Mirroring PdfUtils logic)
        if (!InvoiceSettings.hasMinimumSettings(context)) {
            throw new IOException("Invoice settings not configured. Please set up business details first.");
        }

        // 2. Calculate Dynamic Height
        // Base headers/footers take approx 500px. Each item takes ~20px.
        // We add a buffer to ensure no cutoff.
        int baseHeight = 600;
        int itemRowHeight = 25;
        int totalHeight = baseHeight + (bill.items.size() * itemRowHeight);

        // 3. Setup Bitmap and Canvas
        Bitmap bitmap = Bitmap.createBitmap(PAGE_WIDTH, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw White Background
        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);

        int y = 50;

        /* =========================
           COMPANY HEADER (LEFT)
           ========================= */
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText(InvoiceSettings.getBusinessName(context), MARGIN, y, paint);

        paint.setTextSize(11);
        paint.setFakeBoldText(false);

        // Address
        String address = InvoiceSettings.getAddress(context);
        if (!address.isEmpty()) {
            y += 18;
            canvas.drawText(address, MARGIN, y, paint);
        }

        // Phone
        String phone = InvoiceSettings.getPhone(context);
        if (!phone.isEmpty()) {
            y += 14;
            canvas.drawText("Phone: " + phone, MARGIN, y, paint);
        }

        // Email
        String email = InvoiceSettings.getEmail(context);
        if (!email.isEmpty()) {
            y += 14;
            canvas.drawText("Email: " + email, MARGIN, y, paint);
        }

        // GSTIN
        String gstin = InvoiceSettings.getGSTIN(context);
        if (!gstin.isEmpty()) {
            y += 14;
            canvas.drawText("GSTIN: " + gstin, MARGIN, y, paint);
        }

        /* =========================
           INVOICE HEADER (RIGHT)
           ========================= */
        // Resetting Y for right side elements to align with top
        int rightHeaderY = 50;

        paint.setTextSize(26);
        paint.setFakeBoldText(true);
        canvas.drawText("INVOICE", PAGE_WIDTH - 180, rightHeaderY, paint);

        paint.setTextSize(11);
        paint.setFakeBoldText(false);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        rightHeaderY += 40; // Approx jump to Date line
        canvas.drawText("Date:", PAGE_WIDTH - 180, rightHeaderY, paint);
        canvas.drawText(sdf.format(new Date(bill.timestamp)), PAGE_WIDTH - 100, rightHeaderY, paint);

        rightHeaderY += 20;
        canvas.drawText("Invoice #:", PAGE_WIDTH - 180, rightHeaderY, paint);
        canvas.drawText(bill.billId, PAGE_WIDTH - 100, rightHeaderY, paint);

        rightHeaderY += 20;
        canvas.drawText("Customer ID:", PAGE_WIDTH - 180, rightHeaderY, paint);
        canvas.drawText(bill.customer.phone, PAGE_WIDTH - 100, rightHeaderY, paint);

        /* =========================
           BILL TO / SHIP TO
           ========================= */
        // Ensure Y is below the lowest header element
        y = Math.max(y, rightHeaderY) + 40;
        // Force specific Y from PdfUtils if we want exact match, usually ~170
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

        double subtotalAmount = 0;
        for (BillItem item : bill.items) {
            // Text truncation for image safety, similar to Pdf logic implicitly
            String itemName = item.name.length() > 25 ? item.name.substring(0, 25) + "..." : item.name;

            canvas.drawText(itemName, MARGIN, y, paint);
            canvas.drawText(itemName, 120, y, paint);
            canvas.drawText(String.valueOf(item.quantity), 330, y, paint);
            canvas.drawText("₹" + format(item.price), 380, y, paint);
            canvas.drawText("₹" + format(item.subtotal), 480, y, paint);

            subtotalAmount += item.subtotal;
            y += 18;
        }

        y += 5;
        drawTableLine(canvas, y);

        /* =========================
           TOTAL SUMMARY (RIGHT) WITH GST
           ========================= */
        y += 30;

        float cgstRate = InvoiceSettings.getCGSTRate(context);
        float sgstRate = InvoiceSettings.getSGSTRate(context);

        double cgstAmount = (subtotalAmount * cgstRate) / 100;
        double sgstAmount = (subtotalAmount * sgstRate) / 100;
        double totalWithGst = subtotalAmount + cgstAmount + sgstAmount;

        paint.setTextSize(11);
        paint.setFakeBoldText(false);

        // Subtotal
        canvas.drawText("SUBTOTAL:", 360, y, paint);
        canvas.drawText("₹" + format(subtotalAmount), 480, y, paint);

        // CGST
        y += 18;
        canvas.drawText("CGST (" + format(cgstRate) + "%):", 360, y, paint);
        canvas.drawText("₹" + format(cgstAmount), 480, y, paint);

        // SGST
        y += 18;
        canvas.drawText("SGST (" + format(sgstRate) + "%):", 360, y, paint);
        canvas.drawText("₹" + format(sgstAmount), 480, y, paint);

        // Total
        y += 18;
        paint.setFakeBoldText(true);
        paint.setTextSize(12);
        canvas.drawText("TOTAL:", 360, y, paint);
        canvas.drawText("₹" + format(totalWithGst), 480, y, paint);

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

        /* =========================
           SAVE FILE
           ========================= */
        File dir = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "Cheeta/Bills"
        );

        if (!dir.exists()) dir.mkdirs();

        // Unique filename
        String fileName = "Invoice_" + bill.billId + "_" + System.currentTimeMillis() + ".png";
        File file = new File(dir, fileName);

        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
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
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(1);
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint);
    }

    private static String format(double value) {
        return String.format(Locale.getDefault(), "%.2f", value);
    }

    private static String format(float value) {
        return String.format(Locale.getDefault(), "%.2f", value);
    }
}