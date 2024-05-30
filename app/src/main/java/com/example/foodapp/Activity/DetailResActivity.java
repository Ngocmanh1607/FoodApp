package com.example.foodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.foodapp.Domain.Foods;
import com.example.foodapp.R;
import com.example.foodapp.databinding.ActivityDetailResBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class DetailResActivity extends BaseActivity {
    ActivityDetailResBinding binding;
    private Foods object; // Đối tượng thực phẩm hiện tại
    private boolean edit = false; // Biến kiểm soát trạng thái chỉnh sửa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailResBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        getIntentExtra(); // Lấy dữ liệu từ Intent
        setVariable(); // Thiết lập các biến và sự kiện
    }

    // Thiết lập các biến và sự kiện cho các thành phần giao diện
    private void setVariable() {

        // Sự kiện khi nhấn nút quay lại
        binding.backBtn.setOnClickListener(v -> finish());

        // Hiển thị hình ảnh của thực phẩm
        Glide.with(DetailResActivity.this).load(object.getImagePath()).into(binding.pic);

        // Hiển thị thông tin chi tiết của thực phẩm
        binding.priceTxt.setText("$" + object.getPrice());
        binding.titleTxt.setText(object.getTitle());
        binding.descriptionTxt.setText(object.getDescription());
        binding.raceTxt.setText(object.getStar() + " Rating");
        binding.ratingBar.setRating(Float.valueOf(object.getStar().toString()));

        // Sự kiện khi nhấn nút xóa thực phẩm
        binding.deletefood.setOnClickListener(v -> deleteFoodItem());

        // Sự kiện khi nhấn nút chỉnh sửa thực phẩm
        binding.editfood.setOnClickListener(v -> {
            if (!edit) {
                editFoodItem();
            } else {
                saveFoodItem();
            }
        });

        setEnable(false); // Vô hiệu hóa các trường thông tin khi chưa ở chế độ chỉnh sửa
    }

    // Lấy đối tượng thực phẩm từ Intent
    private void getIntentExtra() {
        object = (Foods) getIntent().getSerializableExtra("object");
    }

    // Xóa mục thực phẩm khỏi cơ sở dữ liệu
    private void deleteFoodItem() {
        if (object != null) {
            String foodNameToDelete = object.getTitle();

            DatabaseReference foodsRef = FirebaseDatabase.getInstance().getReference("Foods");
            foodsRef.orderByChild("Title").equalTo(foodNameToDelete).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            snapshot.getRef().removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getApplicationContext(), "Food item deleted successfully", Toast.LENGTH_SHORT).show();
                                        finish(); // Đóng activity sau khi xóa thành công
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getApplicationContext(), "Failed to delete food item", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Food item not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), "Database error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Kích hoạt chế độ chỉnh sửa các trường thông tin của thực phẩm
    private void editFoodItem() {
        setEnable(true);
        binding.deletefood.setEnabled(false); // Vô hiệu hóa nút xóa khi đang chỉnh sửa
        binding.editfood.setText("Save");
        edit = true; // Đặt trạng thái chỉnh sửa
    }

    // Lưu thông tin thực phẩm sau khi chỉnh sửa
    private void saveFoodItem() {
        String title = binding.titleTxt.getText().toString();
        String price = binding.priceTxt.getText().toString().replace("$", "");
        String time = binding.timeTxt.getText().toString().replace(" min", "");
        String description = binding.descriptionTxt.getText().toString();

        // Kiểm tra các trường có trống không
        if (title.isEmpty() || price.isEmpty() || time.isEmpty() || description.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double parsedPrice = Double.parseDouble(price);
            int parsedTime = Integer.parseInt(time);

            // Cập nhật thông tin đối tượng thực phẩm
            object.setTitle(title);
            object.setPrice(parsedPrice);
            object.setTimeValue(parsedTime);

            if (parsedTime < 10) {
                object.setTimeId(0);
            } else if (parsedTime < 30) {
                object.setTimeId(2);
            } else {
                object.setTimeId(3);
            }

            object.setDescription(description);
            DatabaseReference foodsRef = FirebaseDatabase.getInstance().getReference("Foods");
            String foodId = String.valueOf(object.getId());

            // Tạo bản đồ các trường thông tin của thực phẩm
            Map<String, Object> capitalizedFoodMap = capitalizeFieldNames(object);

            // Lưu thông tin thực phẩm vào cơ sở dữ liệu
            foodsRef.child(foodId).setValue(capitalizedFoodMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getApplicationContext(), "Food item updated successfully", Toast.LENGTH_SHORT).show();
                        restartActivity(); // Khởi động lại activity sau khi lưu thành công
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), "Failed to update food item", Toast.LENGTH_SHORT).show();
                    });

            edit = false; // Đặt lại trạng thái chỉnh sửa sau khi lưu
        } catch (NumberFormatException e) {
            Toast.makeText(getApplicationContext(), "Invalid input for price or time", Toast.LENGTH_SHORT).show();
        }
    }

    // Chuyển đổi các trường thông tin thành bản đồ với tên trường viết hoa chữ cái đầu
    private Map<String, Object> capitalizeFieldNames(Foods food) {
        Map<String, Object> foodMap = new HashMap<>();
        foodMap.put("Id", food.getId());
        foodMap.put("Title", food.getTitle());
        foodMap.put("Description", food.getDescription());
        foodMap.put("ImagePath", food.getImagePath());
        foodMap.put("Price", food.getPrice());
        foodMap.put("PriceId", food.getPriceId());
        foodMap.put("Star", food.getStar());
        foodMap.put("TimeValue", food.getTimeValue());
        foodMap.put("TimeId", food.getTimeId());
        foodMap.put("CategoryId", food.getCategoryId());
        foodMap.put("BestFood", food.isBestFood());
        return foodMap;
    }

    // Bật hoặc vô hiệu hóa chỉnh sửa các trường thông tin
    private void setEnable(boolean enabled) {
        binding.titleTxt.setEnabled(enabled);
        binding.priceTxt.setEnabled(enabled);
        binding.timeTxt.setEnabled(enabled);
        binding.descriptionTxt.setEnabled(enabled);
    }

    // Khởi động lại activity hiện tại
    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
