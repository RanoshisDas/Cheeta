package com.ranoshisdas.app.cheeta.billing;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ranoshisdas.app.cheeta.R;
import com.ranoshisdas.app.cheeta.models.Bill;
import com.ranoshisdas.app.cheeta.utils.ImageUtils;
import com.ranoshisdas.app.cheeta.utils.PdfUtils;
import com.ranoshisdas.app.cheeta.utils.ShareUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BillDetailActivity extends AppCompatActivity {

    private TextView customerNameText, customerPhoneText, customerEmailText;
    private TextView billIdText, billDateText, totalText;
    private RecyclerView itemsRecycler;
    private Button shareImageButton, sharePdfButton;
    private ProgressBar progressBar;

    private Bill bill;
    private BillItemDetailAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail);

        // Get bill from intent
        bill = (Bill) getIntent().getSerializableExtra("bill");
        if (bill == null) {
            Toast.makeText(this, "Error loading bill", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        displayBillDetails();
        setupRecyclerView();
        setupListeners();
    }

    private void initializeViews() {
        customerNameText = findViewById(R.id.customerNameText);
        customerPhoneText = findViewById(R.id.customerPhoneText);
        customerEmailText = findViewById(R.id.customerEmailText);
        billIdText = findViewById(R.id.billIdText);
        billDateText = findViewById(R.id.billDateText);
        totalText = findViewById(R.id.totalText);
        itemsRecycler = findViewById(R.id.itemsRecycler);
        shareImageButton = findViewById(R.id.shareImageButton);
        sharePdfButton = findViewById(R.id.sharePdfButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void displayBillDetails() {
        customerNameText.setText("Name: " + bill.customer.name);
        customerPhoneText.setText("Phone: " + bill.customer.phone);
        customerEmailText.setText("Email: " + (bill.customer.email != null && !bill.customer.email.isEmpty()
                ? bill.customer.email : "N/A"));

        billIdText.setText("Bill ID: " + bill.billId);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        billDateText.setText("Date: " + sdf.format(new Date(bill.timestamp)));

        totalText.setText("Total: ₹" + String.format("%.2f", bill.total));
    }

    private void setupRecyclerView() {
        adapter = new BillItemDetailAdapter(bill.items);
        itemsRecycler.setLayoutManager(new LinearLayoutManager(this));
        itemsRecycler.setAdapter(adapter);
    }

    private void setupListeners() {
        shareImageButton.setOnClickListener(v -> shareAsImage());
        sharePdfButton.setOnClickListener(v -> shareAsPdf());
    }

    private void shareAsImage() {
        progressBar.setVisibility(View.VISIBLE);
        shareImageButton.setEnabled(false);

        new Thread(() -> {
            try {
                File imageFile = ImageUtils.generateBillImage(this, bill);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    shareImageButton.setEnabled(true);
                    ShareUtils.shareFile(this, imageFile, "image/png");
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    shareImageButton.setEnabled(true);
                    Toast.makeText(this, "Failed to generate image: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void shareAsPdf() {
        progressBar.setVisibility(View.VISIBLE);
        sharePdfButton.setEnabled(false);

        new Thread(() -> {
            try {
                File pdfFile = PdfUtils.generateBillPdf(this, bill);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    sharePdfButton.setEnabled(true);
                    ShareUtils.shareFile(this, pdfFile, "application/pdf");
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    sharePdfButton.setEnabled(true);
                    Toast.makeText(this, "Failed to generate PDF: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}