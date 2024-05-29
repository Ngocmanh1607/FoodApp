package com.example.foodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.Adapter.HistoryOrderAdapter;
import com.example.foodapp.Domain.Order;
import com.example.foodapp.R;
import com.example.foodapp.databinding.ActivityListUserOrderBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListUserOrderActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private HistoryOrderAdapter adapter;
    private List<Order> userOrders;
    private ActivityListUserOrderBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sử dụng View Binding để thiết lập giao diện
        binding = ActivityListUserOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo danh sách đơn hàng và adapter
        userOrders = new ArrayList<>();
        recyclerView = binding.hisOrder;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryOrderAdapter((ArrayList<Order>) userOrders);
        recyclerView.setAdapter(adapter);

        // Khởi tạo danh sách đơn hàng của người dùng
        initList();

        // Thiết lập sự kiện click cho nút quay lại
        binding.backBtn.setOnClickListener(v -> {
            startActivity(new Intent(ListUserOrderActivity.this, UserDetailActivity.class));
            finish();});
    }

    /**
     * Khởi tạo danh sách đơn hàng của người dùng hiện tại từ Firebase.
     */
    private void initList() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("User").child(userId);

            // Lấy thông tin người dùng từ Firebase
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String key = dataSnapshot.getKey();
                        getUserOrders(key);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Ghi log lỗi khi đọc thông tin người dùng
                    Log.e("UserRef", "Lỗi khi đọc thông tin người dùng từ Firebase Realtime Database", databaseError.toException());
                }
            });
        }
    }

    /**
     * Lấy danh sách đơn hàng của người dùng từ Firebase bằng key người dùng.
     * @param key Key của người dùng để truy vấn đơn hàng.
     */
    private void getUserOrders(String key) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");

        // Lấy thông tin đơn hàng từ Firebase
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userOrders.clear();

                // Duyệt qua các đơn hàng và thêm vào danh sách nếu khớp key người dùng
                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    String orderUser = orderSnapshot.child("key").getValue(String.class);

                    if (orderUser != null && orderUser.equals(key)) {
                        Order order = orderSnapshot.getValue(Order.class);
                        if (order != null) {
                            userOrders.add(order);
                        }
                    }
                }
                // Cập nhật adapter để hiển thị danh sách đơn hàng
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Ghi log lỗi khi đọc danh sách đơn hàng
                Log.e("OrdersRef", "Lỗi khi đọc danh sách đơn đặt hàng từ Firebase Realtime Database", databaseError.toException());
            }
        });
    }
}
