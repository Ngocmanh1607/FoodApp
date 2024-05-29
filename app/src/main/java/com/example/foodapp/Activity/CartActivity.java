package com.example.foodapp.Activity;

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
        binding= ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managmentCart=new ManagmentCart(this);

        setVariable();
        calculateCart();
        initList();
    }

    private void initList() {
        if(managmentCart.getListCart().isEmpty()){
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollviewCart.setVisibility(View.GONE);
        }
        else{

            binding.emptyTxt.setVisibility(View.GONE);
            binding.scrollviewCart.setVisibility(View.VISIBLE);
        }

        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        binding.cardView.setLayoutManager(linearLayoutManager);
        adapter= new CartAdapter(managmentCart.getListCart(), this, () -> calculateCart());
        binding.cardView.setAdapter(adapter);
    }

    private void calculateCart() {
        double percenTax=0.02;
        double delivery=10;

        tax=Math.round(managmentCart.getTotalFee()*percenTax*100.0)/100;

        total = Math.round((managmentCart.getTotalFee()+tax+delivery)*100)/100;
        double itemTotal=Math.round(managmentCart.getTotalFee()*100)/100;

        binding.totalFeeTxt.setText("$"+itemTotal);
        binding.deliveryTxt.setText("$"+delivery);
        binding.totalTxt.setText("$"+total);
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v->finish());

        binding.oderBtn.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        if (!managmentCart.getListCart().isEmpty()) {
            // Kiểm tra xem người dùng đã đăng nhập hay chưa
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid(); // Lấy ID của người dùng đã đăng nhập

                // Lấy thông tin người dùng từ Firebase Realtime Database
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("User").child(userId);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String userName = dataSnapshot.child("Name").getValue(String.class);
                            String phone = dataSnapshot.child("Phone").getValue(String.class);
                            String location = dataSnapshot.child("Location").getValue(String.class);
                            String key_user = dataSnapshot.getKey();

                            if (userName == null || phone == null || location == null) {
                                Log.e("CartActivity", "User data is incomplete.");
                                Toast.makeText(CartActivity.this, "User data is incomplete. Please update your profile!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Order order = new Order();
                            order.setUserName(userName);
                            order.setPhone(phone);
                            order.setLocation(location);
                            order.setStatus(Order.Status.PENDING);

                            String note = binding.noteTxt.getText().toString();
                            order.setNote(note != null ? note : ""); // Ensure note is not null

                            // Lấy danh sách món ăn từ giỏ hàng
                            List<Foods> cartItems = managmentCart.getListCart();

                            // Tạo danh sách mới chứa thông tin đơn hàng
                            List<OrderItem> orderItems = new ArrayList<>();
                            for (Foods item : cartItems) {
                                // Tạo đối tượng OrderItem với thông tin tên, số lượng và ngày giờ đặt hàng
                                OrderItem orderItem = new OrderItem(item.getTitle(), item.getNumberInCart(), item.getImagePath());
                                orderItems.add(orderItem);
                            }
                            order.setlOrderItem(orderItems);
                            order.setTotalPrice(total); // Ensure 'total' is properly calculated and non-null

                            String currentDateTime = getCurrentDateTime();
                            order.setDateTime(currentDateTime);

                            // Tham chiếu đến Firebase Realtime Database và lưu đơn hàng
                            DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");
                            String orderId = ordersRef.push().getKey();
                            order.setKey(key_user);

                            if (orderId != null) {
                                ordersRef.child(orderId).setValue(order)
                                        .addOnSuccessListener(aVoid -> {
                                            managmentCart.clearCart(); // Ensure clearCart is thread-safe
                                            Toast.makeText(CartActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(CartActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("CartActivity", "Failed to place order: " + e.getMessage());
                                            Toast.makeText(CartActivity.this, "Failed to place order. Please try again!", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Log.e("CartActivity", "Failed to generate order ID.");
                                Toast.makeText(CartActivity.this, "Failed to place order. Please try again!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("CartActivity", "User data not found in Firebase Realtime Database.");
                            Toast.makeText(CartActivity.this, "User data not found. Please try again!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("CartActivity", "Firebase Database Error: " + databaseError.getMessage());
                        Toast.makeText(CartActivity.this, "Firebase Database Error. Please try again!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(CartActivity.this, "User not authenticated. Please login again!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CartActivity.this, LoginActivity.class));
                finish();
            }
        } else {
            Toast.makeText(CartActivity.this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
        }

    }
    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}