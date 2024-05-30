package com.example.foodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.foodapp.R;
import com.example.foodapp.databinding.ActivityMainResBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainResActivity extends BaseActivity {

    ActivityMainResBinding binding;
    private FragmentManager fragmentManager;
    private TextView addFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainResBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo FragmentManager để quản lý các fragment
        fragmentManager = getSupportFragmentManager();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        addFood = findViewById(R.id.addFoodBtn);

        // Xử lý sự kiện khi nhấn nút "Thêm món ăn"
        addFood.setOnClickListener(v -> {
            Intent intent = new Intent(MainResActivity.this, AddFoodActivity.class);
            startActivity(intent);
        });

        // Đặt listener cho việc chọn mục trên thanh điều hướng dưới cùng
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            // Xác định fragment nào sẽ được hiển thị dựa trên mục được chọn
            switch (item.getItemId()) {
                case R.id.menu_food:
                    selectedFragment = ListFoodFragment.newInstance();
                    break;
                case R.id.menu_listOrder:
                    selectedFragment = ListOrderFragment.newInstance();
                    break;
            }

            // Nếu fragment được chọn không null, thay thế fragment hiện tại bằng fragment được chọn
            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
                return true;
            }

            return false;
        });

        // Đặt fragment mặc định khi ứng dụng được mở lần đầu
        if (savedInstanceState == null) {
            ListFoodFragment defaultFragment = ListFoodFragment.newInstance();
            replaceFragment(defaultFragment);
        }
    }

    // Phương thức thay thế fragment hiện tại bằng fragment mới
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}
