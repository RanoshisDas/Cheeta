package com.ranoshisdas.app.cheeta.settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.ranoshisdas.app.cheeta.R;
import com.ranoshisdas.app.cheeta.utils.InvoiceSettings;

public class InvoiceSettingsActivity extends AppCompatActivity {

    private TextInputEditText businessNameInput, addressInput, phoneInput, emailInput, gstinInput;
    private TextInputEditText cgstRateInput, sgstRateInput;
    private TextView totalGstText;
    private Button saveButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_settings);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Invoice Settings");
        }

        initializeViews();
        loadExistingSettings();
        setupListeners();
    }

    private void initializeViews() {
        businessNameInput = findViewById(R.id.businessNameInput);
        addressInput = findViewById(R.id.addressInput);
        phoneInput = findViewById(R.id.phoneInput);
        emailInput = findViewById(R.id.emailInput);
        gstinInput = findViewById(R.id.gstinInput);
        cgstRateInput = findViewById(R.id.cgstRateInput);
        sgstRateInput = findViewById(R.id.sgstRateInput);
        totalGstText = findViewById(R.id.totalGstText);
        saveButton = findViewById(R.id.saveButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadExistingSettings() {
        businessNameInput.setText(InvoiceSettings.getBusinessName(this));
        addressInput.setText(InvoiceSettings.getAddress(this));
        phoneInput.setText(InvoiceSettings.getPhone(this));
        emailInput.setText(InvoiceSettings.getEmail(this));
        gstinInput.setText(InvoiceSettings.getGSTIN(this));
        cgstRateInput.setText(String.valueOf(InvoiceSettings.getCGSTRate(this)));
        sgstRateInput.setText(String.valueOf(InvoiceSettings.getSGSTRate(this)));

        updateTotalGST();
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> validateAndSave());

        // Real-time GST total calculation
        TextWatcher gstWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTotalGST();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        cgstRateInput.addTextChangedListener(gstWatcher);
        sgstRateInput.addTextChangedListener(gstWatcher);
    }

    private void updateTotalGST() {
        try {
            float cgst = Float.parseFloat(cgstRateInput.getText().toString().trim());
            float sgst = Float.parseFloat(sgstRateInput.getText().toString().trim());
            float total = cgst + sgst;
            totalGstText.setText("Total GST: " + String.format("%.1f%%", total));
        } catch (NumberFormatException e) {
            totalGstText.setText("Total GST: --");
        }
    }

    private void validateAndSave() {
        // Clear previous errors
        businessNameInput.setError(null);
        phoneInput.setError(null);
        gstinInput.setError(null);
        cgstRateInput.setError(null);
        sgstRateInput.setError(null);

        // Get values
        String businessName = businessNameInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String gstin = gstinInput.getText().toString().trim();

        boolean hasErrors = false;

        // Validate business name
        String businessNameError = InvoiceSettings.validateBusinessName(businessName);
        if (businessNameError != null) {
            businessNameInput.setError(businessNameError);
            hasErrors = true;
        }

        // Validate phone
        String phoneError = InvoiceSettings.validatePhone(phone);
        if (phoneError != null) {
            phoneInput.setError(phoneError);
            hasErrors = true;
        }

        // Validate GSTIN (optional but must be valid if provided)
        String gstinError = InvoiceSettings.validateGSTIN(gstin);
        if (gstinError != null) {
            gstinInput.setError(gstinError);
            hasErrors = true;
        }

        // Validate GST rates
        float cgstRate, sgstRate;
        try {
            cgstRate = Float.parseFloat(cgstRateInput.getText().toString().trim());
            String cgstError = InvoiceSettings.validateGSTRate(cgstRate);
            if (cgstError != null) {
                cgstRateInput.setError(cgstError);
                hasErrors = true;
            }
        } catch (NumberFormatException e) {
            cgstRateInput.setError("Invalid CGST rate");
            hasErrors = true;
            return;
        }

        try {
            sgstRate = Float.parseFloat(sgstRateInput.getText().toString().trim());
            String sgstError = InvoiceSettings.validateGSTRate(sgstRate);
            if (sgstError != null) {
                sgstRateInput.setError(sgstError);
                hasErrors = true;
            }
        } catch (NumberFormatException e) {
            sgstRateInput.setError("Invalid SGST rate");
            hasErrors = true;
            return;
        }

        if (hasErrors) {
            Toast.makeText(this, "Please fix the errors", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save settings
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        // Simulate async save (in case you want to add Firestore later)
        new Thread(() -> {
            InvoiceSettings.saveAllSettings(
                    this,
                    businessName,
                    address,
                    phone,
                    email,
                    gstin,
                    cgstRate,
                    sgstRate
            );

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}