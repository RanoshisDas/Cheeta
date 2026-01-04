package com.ranoshisdas.app.cheeta.billing;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ranoshisdas.app.cheeta.R;
import com.ranoshisdas.app.cheeta.models.Bill;
import com.ranoshisdas.app.cheeta.utils.FirebaseUtil;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BillHistoryActivity extends AppCompatActivity {

    private EditText searchInput;
    private ImageButton filterButton, clearFilterButton;
    private TextView dateRangeText;
    private RecyclerView recyclerView;
    private BillAdapter adapter;
    private ProgressBar progressBar;

    private List<Bill> allBills = new ArrayList<>();
    private List<Bill> filteredBills = new ArrayList<>();

    // Filter state
    private long fromDate = 0;
    private long toDate = 0;
    private String searchQuery = "";

    // Debounce handler
    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_history);

        initializeViews();
        setupRecyclerView();
        setupSearchListener();
        setupFilterListeners();
        loadBills();
    }

    private void initializeViews() {
        searchInput = findViewById(R.id.searchInput);
        filterButton = findViewById(R.id.filterButton);
        clearFilterButton = findViewById(R.id.clearFilterButton);
        dateRangeText = findViewById(R.id.dateRangeText);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        filteredBills = new ArrayList<>();
        adapter = new BillAdapter(filteredBills, this::openBillDetail);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchListener() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();

                // Cancel previous search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Debounce search by 300ms
                searchRunnable = () -> applyFilters();
                searchHandler.postDelayed(searchRunnable, 300);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterListeners() {
        filterButton.setOnClickListener(v -> showDateRangePicker());

        clearFilterButton.setOnClickListener(v -> {
            fromDate = 0;
            toDate = 0;
            searchQuery = "";
            searchInput.setText("");
            dateRangeText.setText("All Bills");
            clearFilterButton.setVisibility(View.GONE);
            applyFilters();
        });
    }

    private void showDateRangePicker() {
        Calendar calendar = Calendar.getInstance();

        // From Date Picker
        DatePickerDialog fromPicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar fromCal = Calendar.getInstance();
                    fromCal.set(year, month, dayOfMonth, 0, 0, 0);
                    fromDate = fromCal.getTimeInMillis();

                    // Show To Date Picker
                    showToDatePicker();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        fromPicker.setTitle("Select From Date");
        fromPicker.show();
    }

    private void showToDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog toPicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar toCal = Calendar.getInstance();
                    toCal.set(year, month, dayOfMonth, 23, 59, 59);
                    toDate = toCal.getTimeInMillis();

                    // Update UI
                    updateDateRangeText();
                    clearFilterButton.setVisibility(View.VISIBLE);
                    applyFilters();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        toPicker.setTitle("Select To Date");
        toPicker.show();
    }

    private void updateDateRangeText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String fromStr = sdf.format(fromDate);
        String toStr = sdf.format(toDate);
        dateRangeText.setText(fromStr + " - " + toStr);
    }

    private void openBillDetail(Bill bill) {
        Intent intent = new Intent(this, BillDetailActivity.class);
        intent.putExtra("bill", (Serializable) bill);
        startActivity(intent);
    }

    private void loadBills() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = FirebaseUtil.auth().getCurrentUser().getUid();

        FirebaseUtil.db().collection("users")
                .document(userId)
                .collection("bills")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allBills.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Bill bill = doc.toObject(Bill.class);
                        bill.billId = doc.getId();
                        allBills.add(bill);
                    }
                    applyFilters();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load bills", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    /**
     * Apply all filters locally without querying Firestore
     */
    private void applyFilters() {
        filteredBills.clear();

        String query = searchQuery.toLowerCase().trim();

        for (Bill bill : allBills) {
            boolean matches = true;

            // Search filter (bill number, customer name, phone)
            if (!query.isEmpty()) {
                boolean matchesBillNumber = bill.billNumber != null &&
                        bill.billNumber.toLowerCase().contains(query);

                boolean matchesCustomerName = bill.customer != null &&
                        bill.customer.name != null &&
                        bill.customer.name.toLowerCase().contains(query);

                boolean matchesPhone = bill.customer != null &&
                        bill.customer.phone != null &&
                        bill.customer.phone.contains(query);

                matches = matchesBillNumber || matchesCustomerName || matchesPhone;
            }

            // Date range filter
            if (matches && fromDate > 0 && bill.timestamp < fromDate) {
                matches = false;
            }

            if (matches && toDate > 0 && bill.timestamp > toDate) {
                matches = false;
            }

            if (matches) {
                filteredBills.add(bill);
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}