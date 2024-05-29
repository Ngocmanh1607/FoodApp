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
import com.example.foodapp.Activity.DetailResActivity;
import com.example.foodapp.Domain.Foods;
import com.example.foodapp.R;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FoodListResAdapter extends RecyclerView.Adapter<FoodListResAdapter.ViewHolder> {
    private ArrayList<Foods> items;
    private Context context;
    public FoodListResAdapter(ArrayList<Foods> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.viewholder_list_food_res, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Foods food = items.get(position);

        holder.titleTxt.setText(food.getTitle());
        holder.timeTxt.setText(food.getTimeValue() + " min");
        holder.priceTxt.setText("$" + food.getPrice());

        // Load image with Glide and apply transformations
        Glide.with(context)
                .load(food.getImagePath())
                .transform(new CenterCrop(), new RoundedCorners(30))
                .into(holder.pic);

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailResActivity.class);
            intent.putExtra("object", food); // Pass food object to detail activity
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size(); // Return the size of the items list
    }

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
