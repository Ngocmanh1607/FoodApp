package com.example.foodapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.foodapp.Domain.OrderItem;
import com.example.foodapp.R;

import java.util.ArrayList;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {
    private ArrayList<OrderItem> items;
    private Context context;

    // Constructor để khởi tạo adapter với danh sách chi tiết đơn hàng
    public OrderDetailAdapter(ArrayList<OrderItem> items) {
        this.items = items;
    }

    // Phương thức cập nhật dữ liệu và thông báo cho adapter thay đổi
    public void updateData(ArrayList<OrderItem> newItems) {
        items = newItems;
        notifyDataSetChanged();
    }

    // Phương thức tạo ViewHolder cho RecyclerView
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_order, parent, false);
        return new ViewHolder(view);
    }

    // Phương thức gắn dữ liệu vào ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem orderItem = items.get(position);

        holder.titleTxt.setText(orderItem.getName());
        holder.numberTxt.setText(String.valueOf(orderItem.getQuantity()));

        // Sử dụng Glide để tải và hiển thị hình ảnh từ đường dẫn (imagePath)
        Glide.with(context)
                .load(orderItem.getImagePath())
                .transform(new CenterCrop(), new RoundedCorners(30))
                .into(holder.pic);
    }

    // Phương thức trả về số lượng chi tiết đơn hàng
    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder để chứa các thành phần của item view
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt, numberTxt;
        ImageView pic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            numberTxt = itemView.findViewById(R.id.numberItemTxt);
            pic = itemView.findViewById(R.id.pic);
        }
    }
}
