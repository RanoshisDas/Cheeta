package com.ranoshisdas.app.cheeta.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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

    public static File generateBillImage(Context context, Bill bill) throws IOException {

        // Validate that bill has required data
        if (bill.businessDetails == null) {
            throw new IOException("Bill missing business details. This bill may have been created with an older version.");
        }

        // Calculate image height based on items
        int baseHeight = 750;
        int itemHeight = 40;
        int totalHeight = baseHeight + (bill.items.size() * itemHeight);

        // Create bitmap
        Bitmap bitmap = Bitmap.createBitmap(800, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Background
        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int y = 40;
        int lineHeight = 35;
        int margin = 30;

        /* =========================
           BUSINESS HEADER FROM FIREBASE
           ========================= */
        paint.setTextSize(36);
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(true);
        canvas.drawText(bill.businessDetails.name, margin, y, paint);
        y += lineHeight;

        // Business Details
        paint.setTextSize(20);
        paint.setFakeBoldText(false);

        if (bill.businessDetails.address != null && !bill.businessDetails.address.isEmpty()) {
            canvas.drawText(bill.businessDetails.address, margin, y, paint);
            y += lineHeight - 10;
        }

        if (bill.businessDetails.phone != null && !bill.businessDetails.phone.isEmpty()) {
            canvas.drawText("Tel: " + bill.businessDetails.phone, margin, y, paint);
            y += lineHeight - 10;
        }

        if (bill.businessDetails.gstin != null && !bill.businessDetails.gstin.isEmpty()) {
            canvas.drawText("GSTIN: " + bill.businessDetails.gstin, margin, y, paint);
            y += lineHeight - 10;
        }

        /* =========================
            INVOICE HEADER - REPLACE THIS SECTION
            ========================= */
        y += 20;

        // Invoice Header
        paint.setTextSize(32);
        paint.setFakeBoldText(true);
        canvas.drawText("INVOICE", margin, y, paint);
        y += lineHeight;

        // Bill Number (UPDATED - use billNumber instead of billId)
        paint.setTextSize(22);
        paint.setFakeBoldText(false);
        String invoiceNumber = (bill.billNumber != null && !bill.billNumber.isEmpty())
                ? bill.billNumber
                : bill.billId.substring(0, 8);
        canvas.drawText("Bill #" + invoiceNumber, margin, y, paint);
        y += lineHeight - 5;

        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        canvas.drawText("Date: " + sdf.format(new Date(bill.timestamp)), margin, y, paint);
        y += lineHeight + 10;

        /* =========================
           CUSTOMER DETAILS BOX
           ========================= */
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        Rect customerBox = new Rect(margin, y, 770, y + 150);
        canvas.drawRect(customerBox, paint);

        paint.setStyle(Paint.Style.FILL);
        y += 35;

        paint.setTextSize(26);
        paint.setFakeBoldText(true);
        canvas.drawText("Customer Details", margin + 15, y, paint);
        y += lineHeight;

        paint.setTextSize(22);
        paint.setFakeBoldText(false);
        canvas.drawText("Name: " + bill.customer.name, margin + 15, y, paint);
        y += lineHeight - 5;
        canvas.drawText("Phone: " + bill.customer.phone, margin + 15, y, paint);
        y += lineHeight - 5;

        if (bill.customer.email != null && !bill.customer.email.isEmpty()) {
            canvas.drawText("Email: " + bill.customer.email, margin + 15, y, paint);
        }

        y = customerBox.bottom + lineHeight * 2;

        /* =========================
           ITEMS SECTION
           ========================= */
        paint.setTextSize(26);
        paint.setFakeBoldText(true);
        canvas.drawText("Items:", margin, y, paint);
        y += lineHeight;

        // Draw line
        paint.setStrokeWidth(3);
        canvas.drawLine(margin, y, 770, y, paint);
        y += 15;

        // Table headers
        paint.setTextSize(20);
        canvas.drawText("Item", margin, y, paint);
        canvas.drawText("Qty", 420, y, paint);
        canvas.drawText("Price", 520, y, paint);
        canvas.drawText("Subtotal", 640, y, paint);
        y += 10;

        paint.setStrokeWidth(2);
        canvas.drawLine(margin, y, 770, y, paint);
        y += 25;

        // Items
        paint.setFakeBoldText(false);
        paint.setTextSize(18);
        for (BillItem item : bill.items) {
            String itemName = item.name.length() > 25 ? item.name.substring(0, 25) + "..." : item.name;
            canvas.drawText(itemName, margin, y, paint);
            canvas.drawText(String.valueOf(item.quantity), 420, y, paint);
            canvas.drawText("₹" + String.format("%.2f", item.price), 520, y, paint);
            canvas.drawText("₹" + String.format("%.2f", item.subtotal), 640, y, paint);
            y += itemHeight;
        }

        y += 15;
        paint.setStrokeWidth(3);
        canvas.drawLine(margin, y, 770, y, paint);
        y += lineHeight;

        /* =========================
           GST SUMMARY FROM FIREBASE
           ========================= */
        paint.setTextSize(20);
        paint.setFakeBoldText(false);

        // Subtotal
        canvas.drawText("Subtotal:", 480, y, paint);
        canvas.drawText("₹" + String.format("%.2f", bill.subtotal), 650, y, paint);
        y += lineHeight - 5;

        // CGST (using stored rate from Firebase)
        canvas.drawText("CGST (" + String.format("%.1f", bill.cgstRate) + "%):", 480, y, paint);
        canvas.drawText("₹" + String.format("%.2f", bill.cgst), 650, y, paint);
        y += lineHeight - 5;

        // SGST (using stored rate from Firebase)
        canvas.drawText("SGST (" + String.format("%.1f", bill.sgstRate) + "%):", 480, y, paint);
        canvas.drawText("₹" + String.format("%.2f", bill.sgst), 650, y, paint);
        y += lineHeight;

        /* =========================
           TOTAL BOX
           ========================= */
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#4CAF50"));
        Rect totalBox = new Rect(margin, y - 25, 770, y + 30);
        canvas.drawRect(totalBox, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(28);
        paint.setFakeBoldText(true);
        canvas.drawText("Total:", margin + 20, y + 5, paint);
        canvas.drawText("₹" + String.format("%.2f", bill.total), 650, y + 5, paint);

        /* =========================
           SAVE FILE
           ========================= */
        File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Cheeta/Bills");
        if (!dir.exists()) {
            dir.mkdirs();
        }

// Use bill number in filename (UPDATED)
        String fileName = (bill.billNumber != null && !bill.billNumber.isEmpty())
                ? "Bill_" + bill.billNumber + ".png"
                : "Bill_" + bill.billId + "_" + System.currentTimeMillis() + ".png";

        File file = new File(dir, fileName);

        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.close();

        return file;
    }
}