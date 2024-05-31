package com.example.foodapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.Activity.OrderDetailActivity;
import com.example.foodapp.Domain.Order;
import com.example.foodapp.R;

import java.util.ArrayList;

public class HistoryOrderAdapter extends RecyclerView.Adapter<HistoryOrderAdapter.ViewHolder> {
    private ArrayList<Order> items;
    private Context context;
    private String orderKey;

    // Constructor để khởi tạo adapter với danh sách các đơn hàng
    public HistoryOrderAdapter(ArrayList<Order> items) {
        this.items = items;
    }

    // Phương thức tạo ViewHolder cho RecyclerView
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.view_holder_list_user_order, parent, false);
        return new ViewHolder(view);
    }

    // Phương thức gắn dữ liệu vào ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = items.get(position);

        // Set các giá trị cho các TextView trong ViewHolder
        holder.timeTxt.setText(order.getDateTime());
        holder.noteTxt.setText(order.getNote());
        holder.totalTxt.setText("$" + order.getTotalPrice());
        orderKey = order.getKey();
        String orderDate = order.getDateTime();
        String status = "";

        // Xác định trạng thái của đơn hàng và đặt giá trị tương ứng cho status
        switch (order.getStatus()) {
            case PENDING:
                status = "Pending";
                break;
            case ACCEPTED:
                status = "Accepted";
                break;
            case REJECTED:
                status = "Rejected";
                break;
        }

        holder.statusTxt.setText(status);

        // Thiết lập sự kiện click để mở chi tiết đơn hàng
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("orderDate", orderDate);
            context.startActivity(intent);
        });
    }

    // Phương thức trả về số lượng đơn hàng trong danh sách
    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder để chứa các thành phần của item view
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeTxt, noteTxt, totalTxt, statusTxt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTxt = itemView.findViewById(R.id.timeTxt);
            noteTxt = itemView.findViewById(R.id.noteTxt);
            totalTxt = itemView.findViewById(R.id.totalPriceTxt);
            statusTxt = itemView.findViewById(R.id.statusTxt);
        }
    }
}
