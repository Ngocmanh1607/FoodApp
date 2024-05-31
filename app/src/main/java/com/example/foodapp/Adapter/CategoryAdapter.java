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
import com.example.foodapp.Activity.ListFoodActivity;
import com.example.foodapp.Domain.Category;
import com.example.foodapp.R;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.viewholder> {

    // Danh sách các danh mục và ngữ cảnh
    ArrayList<Category> items;
    Context context;

    // Khởi tạo Adapter với danh sách các danh mục
    public CategoryAdapter(ArrayList<Category> items) {
        this.items = items;
    }

    // Tạo ViewHolder cho RecyclerView
    @NonNull
    @Override
    public CategoryAdapter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_category, parent, false);
        return new viewholder(inflate);
    }

    // Liên kết dữ liệu với ViewHolder
    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.viewholder holder, int position) {
        // Đặt tên danh mục
        holder.titleTxt.setText(items.get(position).getName());

        // Đặt nền cho mỗi danh mục dựa vào vị trí
        switch (position) {
            case 0:
                holder.pic.setBackgroundResource(R.drawable.cat_0_background);
                break;
            case 1:
                holder.pic.setBackgroundResource(R.drawable.cat_1_background);
                break;
            case 2:
                holder.pic.setBackgroundResource(R.drawable.cat_2_background);
                break;
            case 3:
                holder.pic.setBackgroundResource(R.drawable.cat_3_background);
                break;
            case 4:
                holder.pic.setBackgroundResource(R.drawable.cat_4_background);
                break;
            case 5:
                holder.pic.setBackgroundResource(R.drawable.cat_5_background);
                break;
            case 6:
                holder.pic.setBackgroundResource(R.drawable.cat_6_background);
                break;
            case 7:
                holder.pic.setBackgroundResource(R.drawable.cat_7_background);
                break;
        }

        // Lấy ID của tài nguyên hình ảnh từ tên hình ảnh
        int drawableResourceId = context.getResources().getIdentifier(items.get(position).getImagePath(), "drawable", holder.itemView.getContext().getPackageName());
        // Sử dụng Glide để tải hình ảnh vào ImageView
        Glide.with(context).load(drawableResourceId).into(holder.pic);

        // Xử lý sự kiện nhấp vào một danh mục
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ListFoodActivity.class);
            // Truyền dữ liệu ID và tên danh mục sang ListFoodActivity
            intent.putExtra("CategoryId", items.get(position).getId());
            intent.putExtra("CategoryName", items.get(position).getName());
            context.startActivity(intent);
        });
    }

    // Trả về số lượng mục trong danh sách
    @Override
    public int getItemCount() {
        return items.size();
    }

    // Lớp ViewHolder để quản lý các thành phần giao diện của một mục trong danh sách
    public class viewholder extends RecyclerView.ViewHolder {
        TextView titleTxt;
        ImageView pic;

        // Khởi tạo ViewHolder với các thành phần giao diện
        public viewholder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.catNameTxt);
            pic = itemView.findViewById(R.id.imgCat);
        }
    }
}
