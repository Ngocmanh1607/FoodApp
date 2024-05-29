package com.example.foodapp.Activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.Adapter.FoodListAdapter;
import com.example.foodapp.Domain.Favourite;
import com.example.foodapp.Domain.Foods;
import com.example.foodapp.R;
import com.example.foodapp.databinding.ActivityFavouriteBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavouriteActivity extends AppCompatActivity {

    private ActivityFavouriteBinding binding;
    private FoodListAdapter adapter;
    private ArrayList<Foods> favouriteFoods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavouriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo danh sách và adapter
        favouriteFoods = new ArrayList<>();
        adapter = new FoodListAdapter(favouriteFoods);

        // Thiết lập RecyclerView với GridLayoutManager
        binding.favourite.setLayoutManager(new GridLayoutManager(this, 2));
        binding.favourite.setAdapter(adapter);

        // Tải danh sách món ăn yêu thích từ Firebase
        loadFavouriteFoods();

        // Đặt sự kiện click cho nút quay lại
        binding.backBtn.setOnClickListener(v -> finish());
    }

    /**
     * Tải danh sách món ăn yêu thích từ Firebase cho người dùng hiện tại.
     */
    private void loadFavouriteFoods() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("Favourites");

            // Truy vấn cơ sở dữ liệu để lấy danh sách món ăn yêu thích của người dùng hiện tại
            favRef.orderByChild("userId").equalTo(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    favouriteFoods.clear();
                    List<String> foodIds = new ArrayList<>();
                    for (DataSnapshot favSnapshot : snapshot.getChildren()) {
                        Favourite favourite = favSnapshot.getValue(Favourite.class);
                        if (favourite != null) {
                            foodIds.add(favourite.getFoodId());
                        }
                    }
                    // Tải chi tiết cho mỗi món ăn yêu thích
                    loadFoodDetails(foodIds);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Ghi log lỗi hoặc thông báo cho người dùng
                }
            });
        }
    }

    /**
     * Tải chi tiết của mỗi món ăn dựa trên ID của nó.
     * @param foodIds Danh sách các ID của món ăn cần tải chi tiết.
     */
    private void loadFoodDetails(List<String> foodIds) {
        DatabaseReference foodRef = FirebaseDatabase.getInstance().getReference("Foods");

        // Với mỗi ID món ăn, truy vấn cơ sở dữ liệu và thêm vào danh sách món ăn yêu thích
        for (String foodId : foodIds) {
            foodRef.child(foodId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Foods food = snapshot.getValue(Foods.class);
                    if (food != null) {
                        favouriteFoods.add(food);
                    }
                    // Thông báo cho adapter rằng dữ liệu đã thay đổi
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Xử lý các lỗi có thể xảy ra
                }
            });
        }
    }
}
