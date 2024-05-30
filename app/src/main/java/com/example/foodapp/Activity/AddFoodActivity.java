package com.example.foodapp.Activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodapp.Domain.Category;
import com.example.foodapp.Domain.Foods;
import com.example.foodapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddFoodActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private Spinner spinnerCategory;
    private ImageView ivFoodImage;
    private Uri selectedImageUri;
    private DatabaseReference database;
    private StorageReference storageReference;
    private List<Category> categoryList = new ArrayList<>();
    private int nextFoodId = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);
        EditText etTitle = findViewById(R.id.etTitle);
        EditText etDescription = findViewById(R.id.etDescription);
        EditText etPrice = findViewById(R.id.etPrice);
        EditText etTimeValue = findViewById(R.id.etTimeValue);
        Button btnAddFood = findViewById(R.id.btnAddFood);
        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        CheckBox bestFood = findViewById(R.id.checkBox);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        ivFoodImage = findViewById(R.id.ivFoodImage);

        database = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        loadCategories();
        fetchNextFoodId();
        btnSelectImage.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        btnAddFood.setOnClickListener(view -> {
            if (validateInput(etTitle, etDescription, etPrice, etTimeValue, bestFood)) {
                String title = etTitle.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                double price = Double.parseDouble(etPrice.getText().toString().trim());
                int timeValue = Integer.parseInt(etTimeValue.getText().toString().trim());
                int selectedCategoryPosition = spinnerCategory.getSelectedItemPosition();
                Category category = categoryList.get(selectedCategoryPosition);
                boolean isChecked = bestFood.isChecked();

                if (selectedImageUri != null) {
                    uploadImageAndSaveFood(title, description, price, timeValue, category, isChecked);
                } else {
                    Toast.makeText(AddFoodActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    // Hàm kiểm tra dữ liệu đầu vào
    private boolean validateInput(EditText etTitle, EditText etDescription, EditText etPrice, EditText etTimeValue, CheckBox bestFood) {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String timeValueStr = etTimeValue.getText().toString().trim();
        int selectedCategoryPosition = spinnerCategory.getSelectedItemPosition();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            Toast.makeText(AddFoodActivity.this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            Toast.makeText(AddFoodActivity.this, "Please enter a description", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (priceStr.isEmpty()) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            Toast.makeText(AddFoodActivity.this, "Please enter a price", Toast.LENGTH_SHORT).show();
            return false;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price");
            etPrice.requestFocus();
            Toast.makeText(AddFoodActivity.this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (timeValueStr.isEmpty()) {
            etTimeValue.setError("Preparation time is required");
            etTimeValue.requestFocus();
            Toast.makeText(AddFoodActivity.this, "Please enter the preparation time", Toast.LENGTH_SHORT).show();
            return false;
        }

        int timeValue;
        try {
            timeValue = Integer.parseInt(timeValueStr);
        } catch (NumberFormatException e) {
            etTimeValue.setError("Invalid preparation time");
            etTimeValue.requestFocus();
            Toast.makeText(AddFoodActivity.this, "Please enter a valid preparation time", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedCategoryPosition == Spinner.INVALID_POSITION) {
            Toast.makeText(AddFoodActivity.this, "Please select a category", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
    // Tải ảnh và lưu món ăn vào cơ sở dữ liệu
    private void uploadImageAndSaveFood(String title, String description, double price, int timeValue, Category category,boolean bestfood) {
        StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(selectedImageUri));

        fileReference.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
            String imageUrl = uri.toString();
            saveFoodToDatabase(title, description, price, timeValue, category, imageUrl,bestfood);
        })).addOnFailureListener(e -> Toast.makeText(AddFoodActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    //lưu món ăn vào cơ sở dữ liệu
    private void saveFoodToDatabase(String title, String description, double price, int timeValue, Category category, String imageUrl,boolean bestfood) {
        Double star=5.0;
        int priceId = (price < 10) ? 0 : (price < 30) ? 1 : 2;
        int timeId = (timeValue < 10) ? 0 : (timeValue < 30) ? 1 : 2;
        Foods food = new Foods(
                nextFoodId,
                title,
                description,
                imageUrl,
                price,
                priceId,
                star,
                timeValue,
                timeId,
                category.getId(),
                bestfood
        );

        Map<String, Object> capitalizedFoodMap = capitalizeFieldNames(food);

        database.child("Foods").child(String.valueOf(nextFoodId)).setValue(capitalizedFoodMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AddFoodActivity.this, "Food added successfully", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity
            } else {
                Toast.makeText(AddFoodActivity.this, "Failed to add food", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Chuyển đổi tên trường để lưu trên firebase
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
    // Lấy ID món ăn tiếp theo
    private void fetchNextFoodId() {
        database.child("Foods").orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot foodSnapshot : snapshot.getChildren()) {
                        String lastId = foodSnapshot.getKey();
                        if (lastId != null) {
                            nextFoodId = Integer.parseInt(lastId) + 1;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddFoodActivity.this, "Failed to fetch next food ID", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String getFileExtension(Uri uri) {
        return getContentResolver().getType(uri).split("/")[1];
    }
    // Tải danh sách loại món ăn
    private void loadCategories() {
        database.child("Category").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                List<String> categoryNames = new ArrayList<>();
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    Category category = categorySnapshot.getValue(Category.class);
                    if (category != null) {
                        categoryList.add(category);
                        categoryNames.add(category.getName());
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddFoodActivity.this, android.R.layout.simple_spinner_item, categoryNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddFoodActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ivFoodImage.setImageURI(selectedImageUri);
        }
    }
}