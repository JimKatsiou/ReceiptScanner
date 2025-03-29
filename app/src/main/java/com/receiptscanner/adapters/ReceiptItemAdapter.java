package com.receiptscanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.receiptscanner.R;
import com.receiptscanner.models.ReceiptItem;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

public class ReceiptItemAdapter extends RecyclerView.Adapter<ReceiptItemAdapter.ViewHolder> {
    private List<ReceiptItem> items;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());

    public ReceiptItemAdapter(List<ReceiptItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_receipt, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReceiptItem item = items.get(position);
        holder.productName.setText(item.getProductName());
        holder.quantity.setText(String.format("Quantity: %.0f", item.getQuantity()));
        holder.price.setText(currencyFormatter.format(item.getTotalPrice()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName;
        TextView quantity;
        TextView price;

        ViewHolder(View view) {
            super(view);
            productName = view.findViewById(R.id.productName);
            quantity = view.findViewById(R.id.quantity);
            price = view.findViewById(R.id.price);
        }
    }
} 