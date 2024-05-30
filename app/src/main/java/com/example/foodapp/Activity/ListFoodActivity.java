package com.example.foodapp.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.Adapter.FoodListAdapter;
import com.example.foodapp.Domain.Foods;
import com.example.foodapp.R;
import com.example.foodapp.databinding.ActivityListFoodBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListFoodActivity extends BaseActivity {
    ActivityListFoodBinding binding;
    private RecyclerView.Adapter adapterListFood;
    private int categoryId;
    private String categoryName;
    private String searchText;
    private Boolean isSearch;
    private boolean listFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListFoodBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getIntentExtra(); // Lấy dữ liệu từ Intent
        initList(); // Khởi tạo danh sách thực phẩm
    }

    // Khởi tạo danh sách thực phẩm và lấy dữ liệu từ Firebase
    private void initList() {
        DatabaseReference myRef = database.getReference("Foods");
        binding.progressBar.setVisibility(View.VISIBLE); // Hiển thị thanh tiến trình
        ArrayList<Foods> list = new ArrayList<>();
        Query query;

        // Tạo truy vấn Firebase dựa trên điều kiện tìm kiếm hoặc danh mục
        if (isSearch) {
            query = myRef.orderByChild("Title").startAt(searchText).endAt(searchText + '\uf8ff');
        } else if (listFood) {
            binding.titleTxt.setText("List Food");
            query = myRef;
        } else {
            query = myRef.orderByChild("CategoryId").equalTo(categoryId);
        }

        // Thêm lắng nghe cho truy vấn
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(Foods.class));
                    }
                    if (list.size() > 0) {
                        // Thiết lập RecyclerView với GridLayoutManager và adapter
                        binding.foodListView.setLayoutManager(new GridLayoutManager(ListFoodActivity.this, 2));
                        adapterListFood = new FoodListAdapter(list);
                        binding.foodListView.setAdapter(adapterListFood);
                    }
                    binding.progressBar.setVisibility(View.GONE); // Ẩn thanh tiến trình
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi truy vấn Firebase
            }
        });
    }

    // Lấy dữ liệu từ Intent và thiết lập các biến
    private void getIntentExtra() {
        categoryId = getIntent().getIntExtra("CategoryId", 0);
        categoryName = getIntent().getStringExtra("CategoryName");
        searchText = getIntent().getStringExtra("text");
        isSearch = getIntent().getBooleanExtra("isSearch", false);
        listFood = getIntent().getBooleanExtra("listFood", false);
        binding.titleTxt.setText(categoryName); // Hiển thị tên danh mục
        binding.backBtn.setOnClickListener(v -> finish()); // Sự kiện khi nhấn nút quay lại
    }
}
