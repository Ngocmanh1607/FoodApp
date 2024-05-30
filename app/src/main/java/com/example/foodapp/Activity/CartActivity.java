package com.example.foodapp.Activity;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.Adapter.CartAdapter;
import com.example.foodapp.Domain.Foods;
import com.example.foodapp.Domain.Order;
import com.example.foodapp.Domain.OrderItem;
import com.example.foodapp.Helper.ManagmentCart;
import com.example.foodapp.databinding.ActivityCartBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {
    private ActivityCartBinding binding;
    private RecyclerView.Adapter adapter;
    private ManagmentCart managmentCart;
    private DatabaseReference ordersRef;
    private double tax;
    private double total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managmentCart = new ManagmentCart(this);

        setVariable(); // Thiết lập các biến và sự kiện
        calculateCart(); // Tính toán giỏ hàng
        initList(); // Khởi tạo danh sách giỏ hàng
    }

    private void initList() {
        if (managmentCart.getListCart().isEmpty()) {
            binding.emptyTxt.setVisibility(View.VISIBLE); // Hiển thị thông báo giỏ hàng trống
            binding.scrollviewCart.setVisibility(View.GONE); // Ẩn ScrollView của giỏ hàng
        } else {
            binding.emptyTxt.setVisibility(View.GONE); // Ẩn thông báo giỏ hàng trống
            binding.scrollviewCart.setVisibility(View.VISIBLE); // Hiển thị ScrollView của giỏ hàng
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.cardView.setLayoutManager(linearLayoutManager); // Thiết lập LayoutManager cho RecyclerView
        adapter = new CartAdapter(managmentCart.getListCart(), this, () -> calculateCart()); // Thiết lập adapter cho RecyclerView
        binding.cardView.setAdapter(adapter); // Gán adapter cho RecyclerView
    }

    private void calculateCart() {
        double percenTax = 0.02; // Tỉ lệ thuế
        double delivery = 10; // Phí giao hàng

        tax = Math.round(managmentCart.getTotalFee() * percenTax * 100.0) / 100; // Tính thuế

        total = Math.round((managmentCart.getTotalFee() + tax + delivery) * 100) / 100; // Tính tổng chi phí
        double itemTotal = Math.round(managmentCart.getTotalFee() * 100) / 100; // Tính tổng giá trị các mặt hàng

        binding.totalFeeTxt.setText("$" + itemTotal); // Hiển thị tổng giá trị các mặt hàng
        binding.deliveryTxt.setText("$" + delivery); // Hiển thị phí giao hàng
        binding.totalTxt.setText("$" + total); // Hiển thị tổng chi phí
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> finish()); // Sự kiện khi nhấn nút quay lại

        binding.oderBtn.setOnClickListener(v -> placeOrder()); // Sự kiện khi nhấn nút đặt hàng
    }

    private void placeOrder() {
        if (!managmentCart.getListCart().isEmpty()) { // Kiểm tra giỏ hàng có trống không
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) { // Kiểm tra người dùng đã đăng nhập chưa
                String userId = currentUser.getUid();

                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("User").child(userId);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) { // Kiểm tra dữ liệu người dùng có tồn tại không
                            String userName = dataSnapshot.child("Name").getValue(String.class);
                            String phone = dataSnapshot.child("Phone").getValue(String.class);
                            String location = dataSnapshot.child("Location").getValue(String.class);
                            String key_user = dataSnapshot.getKey();

                            if (userName == null || phone == null || location == null) {
                                Log.e("CartActivity", "User data is incomplete.");
                                Toast.makeText(CartActivity.this, "Thông tin người dùng không đầy đủ. Vui lòng cập nhật hồ sơ!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Order order = new Order();
                            order.setUserName(userName); // Thiết lập tên người dùng
                            order.setPhone(phone); // Thiết lập số điện thoại
                            order.setLocation(location); // Thiết lập địa chỉ
                            order.setStatus(Order.Status.PENDING); // Thiết lập trạng thái đơn hàng

                            String note = binding.noteTxt.getText().toString();
                            order.setNote(note != null ? note : ""); // Thiết lập ghi chú đơn hàng

                            List<Foods> cartItems = managmentCart.getListCart();

                            List<OrderItem> orderItems = new ArrayList<>();
                            for (Foods item : cartItems) {
                                OrderItem orderItem = new OrderItem(item.getTitle(), item.getNumberInCart(), item.getImagePath());
                                orderItems.add(orderItem); // Thêm các mặt hàng vào danh sách đơn hàng
                            }
                            order.setlOrderItem(orderItems);
                            order.setTotalPrice(total); // Thiết lập tổng giá đơn hàng

                            String currentDateTime = getCurrentDateTime();
                            order.setDateTime(currentDateTime); // Thiết lập thời gian đơn hàng

                            DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");
                            String orderId = ordersRef.push().getKey();
                            order.setKey(key_user);

                            if (orderId != null) {
                                ordersRef.child(orderId).setValue(order)
                                        .addOnSuccessListener(aVoid -> {
                                            managmentCart.clearCart(); // Xóa giỏ hàng sau khi đặt hàng thành công
                                            Toast.makeText(CartActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(CartActivity.this, MainActivity.class);
                                            startActivity(intent); // Chuyển hướng về trang chính
                                            finish(); // Kết thúc hoạt động hiện tại
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("CartActivity", "Failed to place order: " + e.getMessage());
                                            Toast.makeText(CartActivity.this, "Đặt hàng thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Log.e("CartActivity", "Failed to generate order ID.");
                                Toast.makeText(CartActivity.this, "Đặt hàng thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("CartActivity", "User data not found in Firebase Realtime Database.");
                            Toast.makeText(CartActivity.this, "Không tìm thấy thông tin người dùng. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("CartActivity", "Firebase Database Error: " + databaseError.getMessage());
                        Toast.makeText(CartActivity.this, "Lỗi cơ sở dữ liệu Firebase. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(CartActivity.this, "Người dùng chưa được xác thực. Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CartActivity.this, LoginActivity.class));
                finish();
            }
        } else {
            Toast.makeText(CartActivity.this, "Giỏ hàng của bạn đang trống!", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date); // Lấy thời gian hiện tại
    }
}
