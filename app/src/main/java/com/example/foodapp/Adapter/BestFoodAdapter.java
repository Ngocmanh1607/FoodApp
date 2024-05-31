package com.example.foodapp.Adapter;

import android.content.Context;
import android.content.Intent;
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
import com.example.foodapp.Activity.DetailActivity;
import com.example.foodapp.Domain.Foods;
import com.example.foodapp.R;

import java.util.ArrayList;

public class BestFoodAdapter extends RecyclerView.Adapter<BestFoodAdapter.viewholder> {

    ArrayList<Foods> items;
    Context context;

    // Constructor để khởi tạo danh sách các món ăn
    public BestFoodAdapter(ArrayList<Foods> items) {
        this.items = items;
    }

    // Phương thức để tạo và gán ViewHolder cho mỗi item trong RecyclerView
    @NonNull
    @Override
    public BestFoodAdapter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext(); // Lấy context từ parent
        // Inflate layout viewholder cho mỗi item
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_best_deal, parent, false);

        return new viewholder(inflate);
    }

    // Phương thức để gán dữ liệu cho mỗi ViewHolder
    @Override
    public void onBindViewHolder(@NonNull BestFoodAdapter.viewholder holder, int position) {
        // Gán các giá trị từ items vào các TextView và ImageView
        holder.titleTxt.setText(items.get(position).getTitle());
        holder.priceTxt.setText("$" + items.get(position).getPrice());
        holder.timeTxt.setText(items.get(position).getTimeValue() + "min");
        holder.starTxt.setText("" + items.get(position).getStar());

        // Sử dụng thư viện Glide để tải và hiển thị hình ảnh với hiệu ứng cắt và bo góc
        Glide.with(context).load(items.get(position).getImagePath()).transform(new CenterCrop(), new RoundedCorners(30)).into(holder.pic);

        // Đặt sự kiện click cho mỗi item để chuyển sang DetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", items.get(position));
            context.startActivity(intent);
        });
    }

    // Phương thức để trả về số lượng item trong danh sách
    @Override
    public int getItemCount() {
        return items.size();
    }

    // Lớp ViewHolder để giữ các view cho mỗi item
    public class viewholder extends RecyclerView.ViewHolder {
        TextView titleTxt, priceTxt, starTxt, timeTxt;
        ImageView pic;

        // Constructor để khởi tạo các view
        public viewholder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            priceTxt = itemView.findViewById(R.id.priceTxt);
            starTxt = itemView.findViewById(R.id.startTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
            pic = itemView.findViewById(R.id.pic);
        }
    }
}
