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
        // Calculate image height based on items
        int baseHeight = 600;
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

        // Title
        paint.setTextSize(40);
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(true);
        canvas.drawText("BILL / INVOICE", margin, y, paint);
        y += lineHeight * 2;

        // Bill ID
        paint.setTextSize(24);
        paint.setFakeBoldText(false);
        canvas.drawText("Bill ID: " + bill.billId, margin, y, paint);
        y += lineHeight;

        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        canvas.drawText("Date: " + sdf.format(new Date(bill.timestamp)), margin, y, paint);
        y += lineHeight * 2;

        // Customer details box
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        Rect customerBox = new Rect(margin, y, 770, y + 150);
        canvas.drawRect(customerBox, paint);

        paint.setStyle(Paint.Style.FILL);
        y += 35;

        paint.setTextSize(28);
        paint.setFakeBoldText(true);
        canvas.drawText("Customer Details", margin + 15, y, paint);
        y += lineHeight;

        paint.setTextSize(24);
        paint.setFakeBoldText(false);
        canvas.drawText("Name: " + bill.customer.name, margin + 15, y, paint);
        y += lineHeight;
        canvas.drawText("Phone: " + bill.customer.phone, margin + 15, y, paint);
        y += lineHeight;

        if (bill.customer.email != null && !bill.customer.email.isEmpty()) {
            canvas.drawText("Email: " + bill.customer.email, margin + 15, y, paint);
        }

        y = customerBox.bottom + lineHeight * 2;

        // Items header
        paint.setTextSize(28);
        paint.setFakeBoldText(true);
        canvas.drawText("Items:", margin, y, paint);
        y += lineHeight;

        // Draw line
        paint.setStrokeWidth(3);
        canvas.drawLine(margin, y, 770, y, paint);
        y += 15;

        // Table headers
        paint.setTextSize(22);
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
        paint.setTextSize(20);
        for (BillItem item : bill.items) {
            // Truncate long item names
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
        y += lineHeight + 10;

        // Total box
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#4CAF50"));
        Rect totalBox = new Rect(margin, y - 30, 770, y + 25);
        canvas.drawRect(totalBox, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(32);
        paint.setFakeBoldText(true);
        canvas.drawText("Total: ₹" + String.format("%.2f", bill.total), margin + 20, y, paint);

        // Save image to file
        File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Cheeta/Bills");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = "Bill_" + bill.billId + "_" + System.currentTimeMillis() + ".png";
        File file = new File(dir, fileName);

        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.close();

        return file;
    }
}