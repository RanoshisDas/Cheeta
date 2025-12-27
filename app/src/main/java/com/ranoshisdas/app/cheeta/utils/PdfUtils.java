package com.ranoshisdas.app.cheeta.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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

    public static File generateBillPdf(Context context, Bill bill) throws IOException {
        // Create PDF document
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        int y = 50;
        int lineHeight = 25;
        int margin = 40;

        // Title
        paint.setTextSize(24);
        paint.setFakeBoldText(true);
        canvas.drawText("BILL / INVOICE", margin, y, paint);
        y += lineHeight * 2;

        // Bill ID
        paint.setTextSize(14);
        paint.setFakeBoldText(false);
        canvas.drawText("Bill ID: " + bill.billId, margin, y, paint);
        y += lineHeight;

        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        canvas.drawText("Date: " + sdf.format(new Date(bill.timestamp)), margin, y, paint);
        y += lineHeight * 2;

        // Customer details
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText("Customer Details:", margin, y, paint);
        y += lineHeight;

        paint.setTextSize(14);
        paint.setFakeBoldText(false);
        canvas.drawText("Name: " + bill.customer.name, margin, y, paint);
        y += lineHeight;
        canvas.drawText("Phone: " + bill.customer.phone, margin, y, paint);
        y += lineHeight;

        if (bill.customer.email != null && !bill.customer.email.isEmpty()) {
            canvas.drawText("Email: " + bill.customer.email, margin, y, paint);
            y += lineHeight;
        }
        y += lineHeight;

        // Items header
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText("Items:", margin, y, paint);
        y += lineHeight;

        // Draw line
        paint.setStrokeWidth(2);
        canvas.drawLine(margin, y, 555, y, paint);
        y += 10;

        // Table headers
        paint.setTextSize(12);
        canvas.drawText("Item", margin, y, paint);
        canvas.drawText("Qty", 280, y, paint);
        canvas.drawText("Price", 340, y, paint);
        canvas.drawText("Subtotal", 450, y, paint);
        y += 5;

        paint.setStrokeWidth(1);
        canvas.drawLine(margin, y, 555, y, paint);
        y += 15;

        // Items
        paint.setFakeBoldText(false);
        for (BillItem item : bill.items) {
            canvas.drawText(item.name, margin, y, paint);
            canvas.drawText(String.valueOf(item.quantity), 280, y, paint);
            canvas.drawText("₹" + String.format("%.2f", item.price), 340, y, paint);
            canvas.drawText("₹" + String.format("%.2f", item.subtotal), 450, y, paint);
            y += lineHeight;
        }

        y += 10;
        paint.setStrokeWidth(2);
        canvas.drawLine(margin, y, 555, y, paint);
        y += lineHeight;

        // Total
        paint.setTextSize(18);
        paint.setFakeBoldText(true);
        canvas.drawText("Total: ₹" + String.format("%.2f", bill.total), 350, y, paint);

        document.finishPage(page);

        // Save PDF to file
        File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Cheeta/Bills");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = "Bill_" + bill.billId + "_" + System.currentTimeMillis() + ".pdf";
        File file = new File(dir, fileName);

        FileOutputStream fos = new FileOutputStream(file);
        document.writeTo(fos);
        document.close();
        fos.close();

        return file;
    }
}