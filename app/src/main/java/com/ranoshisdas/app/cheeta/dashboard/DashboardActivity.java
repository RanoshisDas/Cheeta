package com.ranoshisdas.app.cheeta.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.ranoshisdas.app.cheeta.R;
import com.ranoshisdas.app.cheeta.auth.LoginActivity;
import com.ranoshisdas.app.cheeta.billing.BillHistoryActivity;
import com.ranoshisdas.app.cheeta.billing.CreateBillActivity;
import com.ranoshisdas.app.cheeta.inventory.InventoryActivity;
import com.ranoshisdas.app.cheeta.settings.InvoiceSettingsActivity;
import com.ranoshisdas.app.cheeta.utils.FirebaseUtil;
import com.ranoshisdas.app.cheeta.utils.InvoiceSettings;

public class DashboardActivity extends AppCompatActivity {

    private MaterialCardView createBillCard, inventoryCard, billHistoryCard, settingsCard;
    private Button logoutButton;
    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initializeViews();
        setupListeners();
        displayUserInfo();
        checkFirstRun();
    }

    private void initializeViews() {
        createBillCard = findViewById(R.id.createBillCard);
        inventoryCard = findViewById(R.id.inventoryCard);
        billHistoryCard = findViewById(R.id.billHistoryCard);
        settingsCard = findViewById(R.id.settingsCard); // NEW
        logoutButton = findViewById(R.id.logoutButton);
        welcomeText = findViewById(R.id.welcomeText);
    }

    private void setupListeners() {
        createBillCard.setOnClickListener(v ->
                startActivity(new Intent(this, CreateBillActivity.class)));

        inventoryCard.setOnClickListener(v ->
                startActivity(new Intent(this, InventoryActivity.class)));

        billHistoryCard.setOnClickListener(v ->
                startActivity(new Intent(this, BillHistoryActivity.class)));

        // NEW: Settings card
        settingsCard.setOnClickListener(v ->
                startActivity(new Intent(this, InvoiceSettingsActivity.class)));

        logoutButton.setOnClickListener(v -> showLogoutDialog());
    }

    private void displayUserInfo() {
        String email = FirebaseUtil.auth().getCurrentUser().getEmail();
        welcomeText.setText("Welcome, " + email);
    }

    // NEW: Check if settings are configured on first run
    private void checkFirstRun() {
        if (!InvoiceSettings.isSettingsCompleted(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("Setup Required")
                    .setMessage("Please configure your invoice settings before creating bills.")
                    .setPositiveButton("Configure Now", (dialog, which) -> {
                        startActivity(new Intent(this, InvoiceSettingsActivity.class));
                    })
                    .setNegativeButton("Later", null)
                    .setCancelable(false)
                    .show();
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseUtil.auth().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}