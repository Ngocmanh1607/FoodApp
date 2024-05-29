package com.example.foodapp.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodapp.R;
import com.example.foodapp.databinding.ActivityIntroBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class IntroActivity extends BaseActivity {

    ActivityIntroBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setVariable();
        getWindow().setStatusBarColor(Color.parseColor("#FFE4B5"));
    }

    private void setVariable() {
        binding.loginBtn.setOnClickListener(v -> {
            if(mAuth.getCurrentUser()!=null)
            {
                // Đăng nhập thành công, kiểm tra quyền của người dùng
                checkUserRole(mAuth.getCurrentUser().getUid());
            }
            else{
                startActivity(new Intent(IntroActivity.this,LoginActivity.class));
            }
        });
        binding.signupBtn.setOnClickListener(v -> startActivity(new Intent(IntroActivity.this,SignupActivity.class)));
    }
    private void checkUserRole(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("User").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean role = dataSnapshot.child("Role").getValue(Boolean.class);
                    if (role) {
                        // Người dùng là admin, chuyển hướng đến màn hình hoạt động của admin
                        startActivity(new Intent(IntroActivity.this, MainResActivity.class));
                    } else {
                        // Người dùng không phải là admin, chuyển hướng đến màn hình hoạt động của người dùng thông thường
                        startActivity(new Intent(IntroActivity.this, MainActivity.class));
                    }
                    finish(); // Đóng hoạt động hiện tại sau khi chuyển hướng
                } else {
                    Toast.makeText(IntroActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("LoginActivity", "Firebase Database Error: " + databaseError.getMessage());
                Toast.makeText(IntroActivity.this, "Firebase Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}