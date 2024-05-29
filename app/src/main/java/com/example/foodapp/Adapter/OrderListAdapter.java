package com.example.foodapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.Activity.OrderDetailActivity;
import com.example.foodapp.Domain.Order;
import com.example.foodapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.ViewHolder> {
    private ArrayList<Order> items;
    private Context context;
    private String orderKey;
    public OrderListAdapter(ArrayList<Order> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_list_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = items.get(position);
        holder.userNameTxt.setText(order.getUserName());
        holder.phoneTxt.setText(order.getPhone());
        holder.locationTxt.setText(order.getLocation());
        holder.totalPriceTxt.setText("$" + order.getTotalPrice());
        holder.noteTxt.setText(order.getNote());
        switch (order.getStatus()) {
            case ACCEPTED:
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
                holder.rejectBtn.setVisibility(View.GONE);
                holder.acceptBtn.setVisibility(View.GONE);
                break;
            case REJECTED:
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.red));
                holder.acceptBtn.setVisibility(View.GONE);
                holder.rejectBtn.setVisibility(View.GONE);
                break;
            default:
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.grey));
                holder.acceptBtn.setVisibility(View.VISIBLE);
                holder.rejectBtn.setVisibility(View.VISIBLE);
                break;
        }

        holder.acceptBtn.setOnClickListener(v -> updateOrderStatus(holder.getAdapterPosition(), Order.Status.ACCEPTED));
        holder.rejectBtn.setOnClickListener(v -> updateOrderStatus(holder.getAdapterPosition(), Order.Status.REJECTED));
        holder.itemView.setOnClickListener(v -> openOrderDetail(holder.getAdapterPosition()));
    }

    private void updateOrderStatus(int position, Order.Status status) {
        if (position == RecyclerView.NO_POSITION) return;

        Order orderItem = items.get(position);
        orderItem.setStatus(status);
        orderKey = orderItem.getKey();

        Query orderQuery = FirebaseDatabase.getInstance().getReference("Orders").orderByChild("key").equalTo(orderKey);

        orderQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        String orderId = orderSnapshot.getKey();
                        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Orders").child(orderId);

                        orderRef.child("status").setValue(status)
                                .addOnSuccessListener(aVoid -> {
                                    Intent intent = new Intent(context, OrderDetailActivity.class);
                                    intent.putExtra("orderId", orderId);
                                    context.startActivity(intent);
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Failed to update status. Please try again.", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Toast.makeText(context, "Order not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void openOrderDetail(int position) {
        if (position == RecyclerView.NO_POSITION) return;
        Order orderItem = items.get(position);
        orderKey = orderItem.getKey();
        Intent intent = new Intent(context, OrderDetailActivity.class);
        intent.putExtra("orderKey", orderKey);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTxt, phoneTxt, locationTxt, totalPriceTxt, noteTxt;
        Button acceptBtn, rejectBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTxt = itemView.findViewById(R.id.userNameTxt);
            phoneTxt = itemView.findViewById(R.id.phoneTxt);
            locationTxt = itemView.findViewById(R.id.locationTxt);
            totalPriceTxt = itemView.findViewById(R.id.totalPriceTxt);
            noteTxt = itemView.findViewById(R.id.noteTxt);
            acceptBtn = itemView.findViewById(R.id.acceptBtn);
            rejectBtn = itemView.findViewById(R.id.rejectBtn);
        }
    }
}
