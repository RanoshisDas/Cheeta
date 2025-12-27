package com.ranoshisdas.app.cheeta.billing;

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
import com.ranoshisdas.app.cheeta.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateBillActivity extends AppCompatActivity {

    private EditText customerNameInput, customerPhoneInput, customerEmailInput;
    private RecyclerView selectedItemsRecycler;
    private Button addItemButton, saveBillButton;
    private TextView totalText;
    private ProgressBar progressBar;

    private List<Item> availableItems;
    private List<BillItem> selectedItems;
    private BillItemAdapter adapter;
    private double total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_bill);

        initializeViews();
        setupRecyclerView();
        loadAvailableItems();

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

        // Update subtotal as quantity changes
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

        // Initial subtotal
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
        total += billItem.subtotal;
        updateTotal();
        adapter.notifyDataSetChanged();
    }

    private void removeItem(BillItem item) {
        selectedItems.remove(item);
        total -= item.subtotal;
        updateTotal();
        adapter.notifyDataSetChanged();
    }

    private void updateTotal() {
        totalText.setText("Total: ₹" + String.format("%.2f", total));
    }

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

        Customer customer = new Customer();
        customer.name = name;
        customer.phone = phone;
        customer.email = email;

        Map<String, Object> billData = new HashMap<>();
        billData.put("customer", customer);
        billData.put("total", total);
        billData.put("timestamp", System.currentTimeMillis());
        billData.put("items", selectedItems);

        FirebaseUtil.db().collection("users")
                .document(userId)
                .collection("bills")
                .add(billData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Bill saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    saveBillButton.setEnabled(true);
                    Toast.makeText(this, "Failed to save bill", Toast.LENGTH_SHORT).show();
                });
    }
}