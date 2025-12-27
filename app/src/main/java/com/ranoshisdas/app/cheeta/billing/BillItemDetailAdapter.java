package com.ranoshisdas.app.cheeta.billing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ranoshisdas.app.cheeta.R;
import com.ranoshisdas.app.cheeta.models.BillItem;

import java.util.List;

public class BillItemDetailAdapter extends RecyclerView.Adapter<BillItemDetailAdapter.ViewHolder> {

    private List<BillItem> items;

    public BillItemDetailAdapter(List<BillItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bill_item_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BillItem item = items.get(position);
        holder.nameText.setText(item.name);
        holder.priceText.setText("₹" + String.format("%.2f", item.price));
        holder.quantityText.setText(" × " + item.quantity);
        holder.subtotalText.setText(" = ₹" + String.format("%.2f", item.subtotal));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, priceText, quantityText, subtotalText;

        ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.itemNameText);
            priceText = itemView.findViewById(R.id.itemPriceText);
            quantityText = itemView.findViewById(R.id.quantityText);
            subtotalText = itemView.findViewById(R.id.subtotalText);
        }
    }
}