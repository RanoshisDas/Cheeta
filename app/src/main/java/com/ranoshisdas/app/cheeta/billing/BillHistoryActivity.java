package com.ranoshisdas.app.cheeta.billing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ranoshisdas.app.cheeta.R;
import com.ranoshisdas.app.cheeta.models.Bill;
import com.ranoshisdas.app.cheeta.utils.FirebaseUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BillHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BillAdapter adapter;
    private List<Bill> billList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_history);

        initializeViews();
        setupRecyclerView();
        loadBills();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        billList = new ArrayList<>();
        adapter = new BillAdapter(billList, this::openBillDetail);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
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
                    billList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Bill bill = doc.toObject(Bill.class);
                        bill.billId = doc.getId();
                        billList.add(bill);
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load bills", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }
}