package com.example.foodapp.Activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.foodapp.R;
import com.example.foodapp.databinding.ActivitySignupBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    private FirebaseAuth mAuth;
    private static final String TAG = "SignupActivity";
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Xử lý sự kiện khi nhấn vào trường ngày sinh để chọn ngày
        binding.BirthUserTxt.setOnClickListener(v -> showDatePickerDialog());

        // Xử lý sự kiện khi nhấn vào biểu tượng lấy vị trí
        binding.locationUserTxt.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                getLocation();
                return false;
            }
        });
        // Xử lý sự kiện khi nhấn nút đăng ký
        binding.signupBtn.setOnClickListener(v -> {
            String email = binding.userEdt.getText().toString().trim();
            String password = binding.passEdt.getText().toString().trim();
            String name = binding.NameUserTxt.getText().toString().trim();
            String gender = getSelectedGender();
            String phone = binding.PhoneUserTxt.getText().toString().trim();
            String birthday = binding.BirthUserTxt.getText().toString().trim();
            String location = binding.locationUserTxt.getText().toString().trim();

            // Kiểm tra các trường bắt buộc
            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || gender.isEmpty() || phone.isEmpty() || birthday.isEmpty() || location.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Vui lòng điền đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo người dùng với email và mật khẩu
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Đăng ký thành công, cập nhật giao diện người dùng với thông tin người dùng đã đăng ký
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Gửi email xác thực
                                sendEmailVerification(user);

                                Boolean role = false;
                                saveUserToDatabase(user.getUid(), name, email, gender, phone, birthday, location, role);
                                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                            }
                        } else {
                            // Nếu đăng ký thất bại, hiển thị thông báo lỗi cho người dùng
                            Log.e(TAG, "Đăng ký người dùng thất bại", task.getException());
                            Toast.makeText(SignupActivity.this, "Xác thực thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Xử lý sự kiện khi văn bản đăng nhập được nhấn
        binding.loginTxt.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    // Hàm hiển thị DatePickerDialog để chọn ngày sinh
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(SignupActivity.this, (view, year1, month1, dayOfMonth) -> {
            String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            binding.BirthUserTxt.setText(selectedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    // Hàm lấy giới tính đã chọn
    private String getSelectedGender() {
        int selectedId = binding.genderRadioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            return "";
        } else {
            RadioButton selectedRadioButton = findViewById(selectedId);
            return selectedRadioButton.getText().toString();
        }
    }

    // Hàm lấy vị trí hiện tại
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }
        fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Location location = task.getResult();
                getAddressFromLocation(location.getLatitude(),location.getLongitude());
            } else {
                Toast.makeText(SignupActivity.this, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Hàm lấy địa chỉ thông qua kinh độ và vĩ độ
    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                StringBuilder addressStringBuilder = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressStringBuilder.append(address.getAddressLine(i)).append("\n");
                }
                binding.locationUserTxt.setText(addressStringBuilder.toString());
            } else {
                binding.locationUserTxt.setText("Không tìm thấy địa chỉ.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            binding.locationUserTxt.setText("Không thể lấy địa chỉ.");
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Quyền truy cập vị trí bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Gửi email xác thực đến người dùng
    private void sendEmailVerification(FirebaseUser user) {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Email xác thực đã gửi đến " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Gửi email xác thực thất bại", task.getException());
                            Toast.makeText(SignupActivity.this, "Gửi email xác thực thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Lưu thông tin người dùng vào cơ sở dữ liệu
    private void saveUserToDatabase(String userId, String name, String email, String gender, String phone, String birthday, String location, Boolean role) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = mDatabase.child("User").child(userId);
        usersRef.child("Role").setValue(role);
        usersRef.child("Name").setValue(name);
        usersRef.child("Email").setValue(email);
        usersRef.child("Gender").setValue(gender);
        usersRef.child("Phone").setValue(phone);
        usersRef.child("Birthday").setValue(birthday);
        usersRef.child("Location").setValue(location);
    }
}
