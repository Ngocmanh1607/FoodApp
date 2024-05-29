package com.example.foodapp.Activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.foodapp.Domain.Favourite;
import com.example.foodapp.Domain.Foods;
import com.example.foodapp.Helper.ManagmentCart;
import com.example.foodapp.R;
import com.example.foodapp.databinding.ActivityDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailActivity extends BaseActivity {
    ActivityDetailBinding binding;
    private Foods object;  // Đối tượng món ăn hiện tại
    private int num = 1;  // Số lượng món ăn
    private boolean isFavourite = false;  // Trạng thái yêu thích
    private ManagmentCart managmentCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(getResources().getColor(R.color.black));  // Đặt màu thanh trạng thái

        // Nhận dữ liệu món ăn từ Intent
        getIntentExtra();

        // Thiết lập các biến và sự kiện
        setVariable();

        // Kiểm tra xem món ăn có trong danh sách yêu thích không
        checkIfFavourite();
    }

    /**
     * Thiết lập các biến và sự kiện cho giao diện.
     */
    private void setVariable() {
        managmentCart = new ManagmentCart(this);

        // Sự kiện nút quay lại
        binding.backBtn.setOnClickListener(v -> finish());

        // Hiển thị hình ảnh món ăn
        Glide.with(DetailActivity.this).load(object.getImagePath()).into(binding.pic);

        // Thiết lập các thông tin món ăn
        binding.priceTxt.setText("$" + object.getPrice());
        binding.titleTxt.setText(object.getTitle());
        binding.descriptionTxt.setText(object.getDescription());
        binding.raceTxt.setText(object.getStar() + " Rating");
        binding.ratingBar.setRating(Float.valueOf(object.getStar().toString()));
        binding.totalTxt.setText(num * object.getPrice() + "$");

        // Sự kiện nút tăng số lượng món ăn
        binding.plusBtn.setOnClickListener(v -> {
            num = num + 1;
            binding.numTxt.setText(num + " ");
            binding.totalTxt.setText("$" + (num * object.getPrice()));
        });

        // Sự kiện nút giảm số lượng món ăn
        binding.minusBtn.setOnClickListener(v -> {
            if (num > 1) {
                num = num - 1;
                binding.numTxt.setText(num + " ");
                binding.totalTxt.setText("$" + (num * object.getPrice()));
            }
        });

        // Sự kiện nút thêm món ăn vào giỏ hàng
        binding.addBtn.setOnClickListener(v -> {
            object.setNumberInCart(num);
            managmentCart.insertFood(object);
        });

        // Sự kiện nút yêu thích
        binding.favBtn.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                String foodId = String.valueOf(object.getId());
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Favourites");

                if (isFavourite) {
                    deleteFav(foodId, userId, databaseReference);
                } else {
                    addFav(foodId, userId, databaseReference);
                }
            }
        });
    }

    /**
     * Nhận dữ liệu món ăn từ Intent.
     */
    private void getIntentExtra() {
        object = (Foods) getIntent().getSerializableExtra("object");
    }

    /**
     * Kiểm tra xem món ăn có trong danh sách yêu thích của người dùng không.
     */
    private void checkIfFavourite() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String foodId = String.valueOf(object.getId());

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Favourites");
            databaseReference.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Favourite favourite = snapshot.getValue(Favourite.class);
                        if (favourite != null && favourite.foodId.equals(foodId)) {
                            isFavourite = true;
                            binding.favBtn.setBackgroundResource(R.drawable.fav_check); // Đổi icon khi đã yêu thích
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Xử lý lỗi nếu có
                    Toast.makeText(DetailActivity.this, "Lỗi khi kiểm tra yêu thích: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Thêm món ăn vào danh sách yêu thích.
     * @param foodId ID của món ăn
     * @param userId ID của người dùng
     * @param databaseReference Tham chiếu đến cơ sở dữ liệu Firebase
     */
    private void addFav(String foodId, String userId, DatabaseReference databaseReference) {
        Favourite favourite = new Favourite(foodId, userId);
        databaseReference.push().setValue(favourite)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(DetailActivity.this, "Added to favourites", Toast.LENGTH_SHORT).show();
                        isFavourite = true;
                        binding.favBtn.setBackgroundResource(R.drawable.fav_check); // Đổi icon khi đã yêu thích
                    } else {
                        Toast.makeText(DetailActivity.this, "Failed to add to favourites", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Xóa món ăn khỏi danh sách yêu thích.
     * @param foodId ID của món ăn
     * @param userId ID của người dùng
     * @param databaseReference Tham chiếu đến cơ sở dữ liệu Firebase
     */
    private void deleteFav(String foodId, String userId, DatabaseReference databaseReference) {
        databaseReference.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Favourite favourite = snapshot.getValue(Favourite.class);
                            if (favourite != null && favourite.foodId.equals(foodId)) {
                                snapshot.getRef().removeValue()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(DetailActivity.this, "Removed from favourites", Toast.LENGTH_SHORT).show();
                                                isFavourite = false;
                                                binding.favBtn.setBackgroundResource(R.drawable.favorite_border); // Đổi icon khi không yêu thích
                                            } else {
                                                Toast.makeText(DetailActivity.this, "Failed to remove from favourites", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Xử lý lỗi nếu có
                        Toast.makeText(DetailActivity.this, "Lỗi khi xóa yêu thích: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
