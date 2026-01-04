package com.ranoshisdas.app.cheeta.billing;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ranoshisdas.app.cheeta.R;
import com.ranoshisdas.app.cheeta.models.Bill;
import com.ranoshisdas.app.cheeta.models.BillItem;
import com.ranoshisdas.app.cheeta.models.Customer;
import com.ranoshisdas.app.cheeta.models.Item;
import com.ranoshisdas.app.cheeta.settings.InvoiceSettingsActivity;
import com.ranoshisdas.app.cheeta.utils.FirebaseUtil;
import com.ranoshisdas.app.cheeta.utils.InvoiceSettings;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Transaction;
import com.ranoshisdas.app.cheeta.utils.BillNumberGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateBillActivity extends AppCompatActivity {

    private EditText customerNameInput, customerPhoneInput, customerEmailInput;
    private RecyclerView selectedItemsRecycler;
    private Button addItemButton, saveBillButton;
    private TextView subtotalText, cgstText, sgstText, totalText;
    private ProgressBar progressBar;

    private List<Item> availableItems;
    private List<BillItem> selectedItems;
    private BillItemAdapter adapter;

    private double subtotal = 0;
    private double cgstAmount = 0;
    private double sgstAmount = 0;
    private double total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_bill);

        initializeViews();
        setupRecyclerView();
        loadAvailableItems();
        checkSettings();

        addItemButton.setOnClickListener(v -> showItemSelectionDialog());
        saveBillButton.setOnClickListener(v -> saveBill());
    }

    private void initializeViews() {
        customerNameInput = findViewById(R.id.customerNameInput);
        customerPhoneInput = findViewById(R.id.customerPhoneInput);
        customerEmailInput = findViewById(R.id.customerEmailInput);
        selectedItemsRecycler = findViewById(R.id.selectedItemsRecycler);
        addItemButton = findViewById(R.id.addItemButton);
        saveBillButton = findViewById(R.id.saveBillButton);
        subtotalText = findViewById(R.id.subtotalText);
        cgstText = findViewById(R.id.cgstText);
        sgstText = findViewById(R.id.sgstText);
        totalText = findViewById(R.id.totalText);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        selectedItems = new ArrayList<>();
        adapter = new BillItemAdapter(selectedItems, this::removeItem);
        selectedItemsRecycler.setLayoutManager(new LinearLayoutManager(this));
        selectedItemsRecycler.setAdapter(adapter);
    }

    private void loadAvailableItems() {
        String userId = FirebaseUtil.auth().getCurrentUser().getUid();
        availableItems = new ArrayList<>();

        FirebaseUtil.db().collection("users")
                .document(userId)
                .collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Item item = doc.toObject(Item.class);
                        item.id = doc.getId();
                        availableItems.add(item);
                    }
                });
    }

    private void checkSettings() {
        if (!InvoiceSettings.hasMinimumSettings(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("Invoice Settings")
                    .setMessage("Invoice settings are not configured. Configure now for proper invoices with GST.")
                    .setPositiveButton("Configure", (dialog, which) -> {
                        startActivity(new Intent(this, InvoiceSettingsActivity.class));
                    })
                    .setNegativeButton("Later", null)
                    .show();
        }
    }

    private void showItemSelectionDialog() {
        if (availableItems.isEmpty()) {
            Toast.makeText(this, "No items in inventory", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] itemNames = new String[availableItems.size()];
        for (int i = 0; i < availableItems.size(); i++) {
            itemNames[i] = availableItems.get(i).name + " (₹" + availableItems.get(i).price + ")";
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Item")
                .setItems(itemNames, (dialog, which) -> {
                    Item selected = availableItems.get(which);
                    showQuantityDialog(selected);
                })
                .show();
    }

    private void showQuantityDialog(Item item) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_item_quantity, null);
        TextView itemNameText = dialogView.findViewById(R.id.itemNameText);
        TextView itemPriceText = dialogView.findViewById(R.id.itemPriceText);
        EditText quantityInput = dialogView.findViewById(R.id.quantityInput);
        TextView subtotalText = dialogView.findViewById(R.id.subtotalText);

        itemNameText.setText(item.name);
        itemPriceText.setText("Price: ₹" + String.format("%.2f", item.price));

        quantityInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    int qty = Integer.parseInt(s.toString());
                    double subtotal = item.price * qty;
                    subtotalText.setText("Subtotal: ₹" + String.format("%.2f", subtotal));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        subtotalText.setText("Subtotal: ₹" + String.format("%.2f", item.price));

        new AlertDialog.Builder(this)
                .setTitle("Enter Quantity")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String qtyStr = quantityInput.getText().toString().trim();
                    if (qtyStr.isEmpty() || Integer.parseInt(qtyStr) <= 0) {
                        Toast.makeText(this, "Enter valid quantity", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int quantity = Integer.parseInt(qtyStr);
                    addItemToBill(item, quantity);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addItemToBill(Item item, int quantity) {
        BillItem billItem = new BillItem(item, quantity);
        selectedItems.add(billItem);
        subtotal += billItem.subtotal;
        updateTotals();
        adapter.notifyDataSetChanged();
    }

    private void removeItem(BillItem item) {
        selectedItems.remove(item);
        subtotal -= item.subtotal;
        updateTotals();
        adapter.notifyDataSetChanged();
    }

    private void updateTotals() {
        // Get GST rates from settings
        float cgstRate = InvoiceSettings.getCGSTRate(this);
        float sgstRate = InvoiceSettings.getSGSTRate(this);

        // Calculate GST amounts
        cgstAmount = (subtotal * cgstRate) / 100;
        sgstAmount = (subtotal * sgstRate) / 100;
        total = subtotal + cgstAmount + sgstAmount;

        // Update UI
        subtotalText.setText("Subtotal: ₹" + String.format("%.2f", subtotal));
        cgstText.setText("CGST (" + String.format("%.1f", cgstRate) + "%): ₹" + String.format("%.2f", cgstAmount));
        sgstText.setText("SGST (" + String.format("%.1f", sgstRate) + "%): ₹" + String.format("%.2f", sgstAmount));
        totalText.setText("Total: ₹" + String.format("%.2f", total));
    }

    // REPLACE the saveBill() method in CreateBillActivity.java with this implementation

    private void saveBill() {
        String name = customerNameInput.getText().toString().trim();
        String phone = customerPhoneInput.getText().toString().trim();
        String email = customerEmailInput.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Customer name and phone are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Add at least one item", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        saveBillButton.setEnabled(false);

        String userId = FirebaseUtil.auth().getCurrentUser().getUid();
        String billMonth = BillNumberGenerator.getCurrentBillMonth();

        // Counter document reference
        DocumentReference counterRef = FirebaseUtil.db()
                .collection("users")
                .document(userId)
                .collection("meta")
                .document("bill_counters")
                .collection("counters")
                .document(billMonth);

        // Run transaction to get next sequence and save bill
        FirebaseUtil.db().runTransaction((Transaction transaction) -> {
                    // Read counter
                    DocumentSnapshot counterSnapshot = transaction.get(counterRef);

                    int nextSequence = 1;
                    if (counterSnapshot.exists() && counterSnapshot.contains("lastSequence")) {
                        Long lastSeq = counterSnapshot.getLong("lastSequence");
                        nextSequence = (lastSeq != null ? lastSeq.intValue() : 0) + 1;
                    }

                    // Update counter
                    Map<String, Object> counterData = new HashMap<>();
                    counterData.put("lastSequence", nextSequence);
                    counterData.put("lastUpdated", System.currentTimeMillis());
                    transaction.set(counterRef, counterData);

                    // Generate bill number
                    String billNumber = BillNumberGenerator.formatBillNumber(billMonth, nextSequence);

                    // Create customer object
                    Map<String, Object> customerData = new HashMap<>();
                    customerData.put("name", name);
                    customerData.put("phone", phone);
                    customerData.put("email", email);

                    // Get GST rates
                    float cgstRate = InvoiceSettings.getCGSTRate(CreateBillActivity.this);
                    float sgstRate = InvoiceSettings.getSGSTRate(CreateBillActivity.this);

                    // Business details snapshot
                    Map<String, Object> businessDetails = new HashMap<>();
                    businessDetails.put("name", InvoiceSettings.getBusinessName(CreateBillActivity.this));
                    businessDetails.put("address", InvoiceSettings.getAddress(CreateBillActivity.this));
                    businessDetails.put("phone", InvoiceSettings.getPhone(CreateBillActivity.this));
                    businessDetails.put("email", InvoiceSettings.getEmail(CreateBillActivity.this));
                    businessDetails.put("gstin", InvoiceSettings.getGSTIN(CreateBillActivity.this));

                    // Create bill data
                    Map<String, Object> billData = new HashMap<>();
                    billData.put("billNumber", billNumber);
                    billData.put("billSequence", nextSequence);
                    billData.put("billMonth", billMonth);
                    billData.put("customer", customerData);
                    billData.put("items", selectedItems);
                    billData.put("subtotal", subtotal);
                    billData.put("cgst", cgstAmount);
                    billData.put("sgst", sgstAmount);
                    billData.put("total", total);
                    billData.put("cgstRate", cgstRate);
                    billData.put("sgstRate", sgstRate);
                    billData.put("businessDetails", businessDetails);
                    billData.put("timestamp", System.currentTimeMillis());

                    // Save bill with auto-generated ID
                    DocumentReference billRef = FirebaseUtil.db()
                            .collection("users")
                            .document(userId)
                            .collection("bills")
                            .document(); // Auto-generated ID

                    transaction.set(billRef, billData);

                    return billNumber; // Return bill number for success handler
                })
                .addOnSuccessListener(billNumber -> {
                    progressBar.setVisibility(View.GONE);
                    saveBillButton.setEnabled(true);
                    Toast.makeText(this, "Bill " + billNumber + " saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    saveBillButton.setEnabled(true);
                    Toast.makeText(this, "Failed to save bill: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}