package com.example.foodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.foodapp.Activity.ListFoodFragment;
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

        fragmentManager = getSupportFragmentManager();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        addFood =findViewById(R.id.addFoodBtn);
        addFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainResActivity.this,AddFoodActivity.class);
                startActivity(intent);
            }
        });
        // Set listener for item selection
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.menu_food:
                    selectedFragment = ListFoodFragment.newInstance();
                    break;
                case R.id.menu_listOrder:
                    selectedFragment = ListOrderFragment.newInstance();
                    break;
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
                return true;
            }

            return false;
        });

        // Set default fragment on first launch
        if (savedInstanceState == null) {
            // Show the initial fragment (e.g., ListFoodFragment) on activity creation
            ListFoodFragment defaultFragment = ListFoodFragment.newInstance();
            replaceFragment(defaultFragment);
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}
