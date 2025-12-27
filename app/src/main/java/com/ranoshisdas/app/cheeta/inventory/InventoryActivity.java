package com.ranoshisdas.app.cheeta.inventory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ranoshisdas.app.cheeta.R;
import com.ranoshisdas.app.cheeta.models.Item;
import com.ranoshisdas.app.cheeta.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private List<Item> itemList;
    private FloatingActionButton addButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        initializeViews();
        setupRecyclerView();
        loadItems();

        addButton.setOnClickListener(v -> showAddItemDialog());
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        addButton = findViewById(R.id.addButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        itemList = new ArrayList<>();
        adapter = new ItemAdapter(itemList, this::showEditItemDialog, this::deleteItem);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadItems() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = FirebaseUtil.auth().getCurrentUser().getUid();

        FirebaseUtil.db().collection("users")
                .document(userId)
                .collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Item item = doc.toObject(Item.class);
                        item.id = doc.getId();
                        itemList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load items", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void showAddItemDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        EditText nameInput = dialogView.findViewById(R.id.nameInput);
        EditText priceInput = dialogView.findViewById(R.id.priceInput);
        EditText stockInput = dialogView.findViewById(R.id.stockInput);

        new AlertDialog.Builder(this)
                .setTitle("Add Item")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String priceStr = priceInput.getText().toString().trim();
                    String stockStr = stockInput.getText().toString().trim();

                    if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                        Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    addItem(name, Double.parseDouble(priceStr), Integer.parseInt(stockStr));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditItemDialog(Item item) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        EditText nameInput = dialogView.findViewById(R.id.nameInput);
        EditText priceInput = dialogView.findViewById(R.id.priceInput);
        EditText stockInput = dialogView.findViewById(R.id.stockInput);

        nameInput.setText(item.name);
        priceInput.setText(String.valueOf(item.price));
        stockInput.setText(String.valueOf(item.stock));

        new AlertDialog.Builder(this)
                .setTitle("Edit Item")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String priceStr = priceInput.getText().toString().trim();
                    String stockStr = stockInput.getText().toString().trim();

                    if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                        Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateItem(item.id, name, Double.parseDouble(priceStr), Integer.parseInt(stockStr));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addItem(String name, double price, int stock) {
        String userId = FirebaseUtil.auth().getCurrentUser().getUid();

        Map<String, Object> itemData = new HashMap<>();
        itemData.put("name", name);
        itemData.put("price", price);
        itemData.put("stock", stock);

        FirebaseUtil.db().collection("users")
                .document(userId)
                .collection("items")
                .add(itemData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show();
                    loadItems();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show());
    }

    private void updateItem(String itemId, String name, double price, int stock) {
        String userId = FirebaseUtil.auth().getCurrentUser().getUid();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("price", price);
        updates.put("stock", stock);

        FirebaseUtil.db().collection("users")
                .document(userId)
                .collection("items")
                .document(itemId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                    loadItems();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show());
    }

    private void deleteItem(Item item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete " + item.name + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    String userId = FirebaseUtil.auth().getCurrentUser().getUid();

                    FirebaseUtil.db().collection("users")
                            .document(userId)
                            .collection("items")
                            .document(item.id)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                                loadItems();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}