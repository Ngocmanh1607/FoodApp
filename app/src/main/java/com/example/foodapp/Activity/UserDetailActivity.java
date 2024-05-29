package com.example.foodapp.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.foodapp.R;
import com.example.foodapp.databinding.ActivityUserDetailBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class UserDetailActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_GALLERY = 102;
    private static final int LOCATION_REQUEST_CODE = 100;

    private ActivityUserDetailBinding binding;
    private DatabaseReference userRef;
    private boolean isEditing = false;
    private Uri selectedImageUri;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo FusedLocationProviderClient để sử dụng dịch vụ vị trí
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Khởi tạo dữ liệu người dùng từ Firebase
        initializeUserData();

        // Thiết lập sự kiện click cho nút chỉnh sửa/lưu
        binding.EditBtn.setOnClickListener(v -> toggleEditSave());

        // Thiết lập sự kiện click cho văn bản danh sách đơn hàng để hiển thị menu popup
        binding.listOrderTxt.setOnClickListener(this::showPopupMenu);

        // Thiết lập sự kiện click cho nút quay lại để kết thúc activity
        binding.btnBack.setOnClickListener(v -> finish());

        // Thiết lập sự kiện long click cho văn bản địa chỉ để lấy vị trí hiện tại
        binding.AddressTxt.setOnLongClickListener(v -> {
            getLocation();
            return true;
        });
    }

    // Phương thức chuyển đổi giữa trạng thái chỉnh sửa và lưu
    private void toggleEditSave() {
        if (!isEditing) {
            binding.EditBtn.setText("Save");
            binding.imgUser.setOnClickListener(v1 -> openGallery());
            setEditingEnabled(true);
        } else {
            saveUserInfo();
        }
        isEditing = !isEditing;
    }

    // Hiển thị menu popup khi click vào văn bản danh sách đơn hàng
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(this::onMenuItemClick);
        popupMenu.show();
    }

    // Xử lý sự kiện khi một mục trong menu popup được chọn
    private boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.history_order:
                startActivity(new Intent(UserDetailActivity.this, ListUserOrderActivity.class));
                return true;
            case R.id.favorite_food:
                startActivity(new Intent(UserDetailActivity.this, FavouriteActivity.class));
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(UserDetailActivity.this, LoginActivity.class));
                finish();
                return true;
            default:
                return false;
        }
    }

    // Mở thư viện ảnh để chọn ảnh mới
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Sử dụng Glide để tải và hiển thị ảnh người dùng
                Glide.with(UserDetailActivity.this)
                        .load(selectedImageUri)
                        .error(R.drawable.account_staff)
                        .fitCenter()
                        .transform(new CircleCrop())
                        .into(binding.imgUser);
                // Tải ảnh lên Firebase Storage
                uploadImageToFirebaseStorage(selectedImageUri);
            }
        }
    }

    // Tải ảnh lên Firebase Storage và cập nhật URL ảnh trong cơ sở dữ liệu
    private void uploadImageToFirebaseStorage(Uri imageUri) {
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("UserImages/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");
            storageRef.putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        userRef.child("urlImg").setValue(uri.toString(), (error, ref) -> {
                            if (error == null) {
                                Toast.makeText(UserDetailActivity.this, "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(UserDetailActivity.this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).addOnFailureListener(e -> {
                        Toast.makeText(UserDetailActivity.this, "Failed to retrieve image URL", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(UserDetailActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(UserDetailActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }

    // Khởi tạo dữ liệu người dùng từ Firebase
    private void initializeUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference().child("User").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Lấy dữ liệu người dùng từ Firebase và hiển thị trên giao diện
                        String name = dataSnapshot.child("Name").getValue(String.class);
                        String gender = dataSnapshot.child("Gender").getValue(String.class);
                        String phone = dataSnapshot.child("Phone").getValue(String.class);
                        String birthday = dataSnapshot.child("Birthday").getValue(String.class);
                        String location = dataSnapshot.child("Location").getValue(String.class);
                        String url = dataSnapshot.child("urlImg").getValue(String.class);

                        binding.NameTxt.setText(name);
                        setGenderRadioButton(gender);
                        binding.PhoneTxt.setText(phone);
                        binding.BirthUserTxt.setText(birthday);
                        binding.AddressTxt.setText(location);
                        Glide.with(UserDetailActivity.this)
                                .load(url)
                                .error(R.drawable.account_staff)
                                .fitCenter()
                                .transform(new CircleCrop())
                                .into(binding.imgUser);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("UserDetailActivity", "User data load cancelled", error.toException());
                }
            });
        }
    }

    // Thiết lập radio button giới tính
    private void setGenderRadioButton(String gender) {
        if (gender != null) {
            if (gender.equalsIgnoreCase("Male")) {
                binding.genderRadioGroup.check(R.id.maleRadioButton);
            } else if (gender.equalsIgnoreCase("Female")) {
                binding.genderRadioGroup.check(R.id.femaleRadioButton);
            }
        }
    }

    // Lưu thông tin người dùng vào Firebase
    private void saveUserInfo() {
        String newName = binding.NameTxt.getText().toString();
        RadioButton selectedGenderButton = findViewById(binding.genderRadioGroup.getCheckedRadioButtonId());
        String newGender = selectedGenderButton != null ? selectedGenderButton.getText().toString() : "";
        String newPhone = binding.PhoneTxt.getText().toString();
        String newBirthday = binding.BirthUserTxt.getText().toString();
        String newLocation = binding.AddressTxt.getText().toString();

        userRef.child("Name").setValue(newName);
        userRef.child("Gender").setValue(newGender);
        userRef.child("Phone").setValue(newPhone);
        userRef.child("Birthday").setValue(newBirthday);
        userRef.child("Location").setValue(newLocation);

        Toast.makeText(UserDetailActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

        binding.EditBtn.setText(R.string.edit);
        setEditingEnabled(false);
    }

    // Thiết lập các trường có thể chỉnh sửa
    private void setEditingEnabled(boolean enabled) {
        binding.NameTxt.setEnabled(enabled);
        binding.PhoneTxt.setEnabled(enabled);
        binding.BirthUserTxt.setEnabled(enabled);
        binding.AddressTxt.setEnabled(enabled);
        binding.genderRadioGroup.setEnabled(enabled);
        for (int i = 0; i < binding.genderRadioGroup.getChildCount(); i++) {
            binding.genderRadioGroup.getChildAt(i).setEnabled(enabled);
        }
    }

    // Phương thức lấy vị trí hiện tại
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }
        fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Location location = task.getResult();
                getAddressFromLocation(location.getLatitude(), location.getLongitude());
            } else {
                Toast.makeText(UserDetailActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Lấy địa chỉ từ vị trí (vĩ độ và kinh độ)
    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder addressStringBuilder = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressStringBuilder.append(address.getAddressLine(i)).append("\n");
                }
                binding.AddressTxt.setText(addressStringBuilder.toString());
            } else {
                binding.AddressTxt.setText("Address not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            binding.AddressTxt.setText("Unable to get address.");
        }
    }
}
