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
import com.example.foodapp.Activity.DetailResActivity;
import com.example.foodapp.Domain.Foods;
import com.example.foodapp.R;

import java.util.ArrayList;

public class FoodListResAdapter extends RecyclerView.Adapter<FoodListResAdapter.ViewHolder> {
    private ArrayList<Foods> items;
    private Context context;

    // Constructor để khởi tạo adapter với danh sách các món ăn
    public FoodListResAdapter(ArrayList<Foods> items) {
        this.items = items;
    }

    // Phương thức tạo ViewHolder cho RecyclerView
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.viewholder_list_food_res, parent, false);
        return new ViewHolder(inflate);
    }

    // Phương thức gắn dữ liệu vào ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Foods food = items.get(position);

        // Set các giá trị cho các TextView trong ViewHolder
        holder.titleTxt.setText(food.getTitle());
        holder.timeTxt.setText(food.getTimeValue() + " min");
        holder.priceTxt.setText("$" + food.getPrice());

        // Sử dụng Glide để tải và hiển thị hình ảnh từ đường dẫn (imagePath)
        Glide.with(context)
                .load(food.getImagePath())
                .transform(new CenterCrop(), new RoundedCorners(30))
                .into(holder.pic);

        // Thiết lập sự kiện click để mở chi tiết món ăn
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailResActivity.class);
            intent.putExtra("object", food); // Truyền đối tượng food tới activity chi tiết
            context.startActivity(intent);
        });
    }

    // Phương thức trả về số lượng món ăn trong danh sách
    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder để chứa các thành phần của item view
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt, priceTxt, timeTxt;
        ImageView pic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            priceTxt = itemView.findViewById(R.id.priceTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
            pic = itemView.findViewById(R.id.img);
        }
    }
}
