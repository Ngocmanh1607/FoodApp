package com.example.foodapp.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.Adapter.FoodListAdapter;
import com.example.foodapp.Adapter.OrderDetailAdapter;
import com.example.foodapp.Adapter.OrderListAdapter;
import com.example.foodapp.Domain.Order;
import com.example.foodapp.Domain.OrderItem;
import com.example.foodapp.R;
import com.example.foodapp.databinding.ActivityOrderDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OrderDetailActivity extends AppCompatActivity {
    ActivityOrderDetailBinding binding;

    private RecyclerView.Adapter recyclerView;
    private OrderDetailAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initList();
        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void initList() {
        // Lấy thông tin đơn hàng từ intent
        String orderKey = getIntent().getStringExtra("orderKey");
        if (orderKey != null) {
            Query orderQuery = FirebaseDatabase.getInstance().getReference("Orders").orderByChild("key").equalTo(orderKey);
            orderQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        DataSnapshot orderSnapshot = snapshot.getChildren().iterator().next();

                        // Lấy danh sách các món ăn từ đơn hàng
                        ArrayList<OrderItem> orderItemList = new ArrayList<>();
                        for (DataSnapshot itemSnapshot : orderSnapshot.child("lOrderItem").getChildren()) {
                            // Lấy thông tin của mỗi món ăn từ Snapshot
                            String itemName = itemSnapshot.child("name").getValue(String.class);
                            Integer quantity = itemSnapshot.child("quantity").getValue(Integer.class);
                            String imagePath = itemSnapshot.child("imagePath").getValue(String.class);

                            // Kiểm tra các giá trị không null trước khi thêm vào danh sách
                            if (itemName != null && quantity != null && imagePath != null) {
                                // Tạo đối tượng OrderItem từ thông tin lấy được
                                OrderItem orderItem = new OrderItem(itemName, quantity, imagePath);
                                orderItemList.add(orderItem);
                            } else {
                                Log.e("OrderDetailActivity", "Invalid order item data: " + itemSnapshot.toString());
                            }
                        }

                        // Đổ danh sách các món ăn vào RecyclerView
                        adapter = new OrderDetailAdapter(orderItemList);
                        binding.orderDetail.setLayoutManager(new LinearLayoutManager(OrderDetailActivity.this));
                        binding.orderDetail.setAdapter(adapter);
                    } else {
                        Log.e("OrderDetailActivity", "Order not found with key: " + orderKey);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Xử lý lỗi nếu cần
                    Log.e("FirebaseError", "Database error: " + error.getMessage());
                }
            });
        } else {
            Log.e("OrderDetailActivity", "Order key is null");
        }
    }
}