package com.ranoshisdas.app.cheeta.billing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ranoshisdas.app.cheeta.R;
import com.ranoshisdas.app.cheeta.models.Bill;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.ViewHolder> {

    private List<Bill> bills;

    public BillAdapter(List<Bill> bills) {
        this.bills = bills;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bill bill = bills.get(position);
        holder.customerText.setText("Customer: " + bill.customer.name);
        holder.phoneText.setText("Phone: " + bill.customer.phone);
        holder.totalText.setText("Total: ₹" + String.format("%.2f", bill.total));
        holder.dateText.setText(formatDate(bill.timestamp));
        holder.itemCountText.setText("Items: " + bill.items.size());
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    @Override
    public int getItemCount() {
        return bills.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView customerText, phoneText, totalText, dateText, itemCountText;

        ViewHolder(View itemView) {
            super(itemView);
            customerText = itemView.findViewById(R.id.customerText);
            phoneText = itemView.findViewById(R.id.phoneText);
            totalText = itemView.findViewById(R.id.totalText);
            dateText = itemView.findViewById(R.id.dateText);
            itemCountText = itemView.findViewById(R.id.itemCountText);
        }
    }
}