package com.example.foodapp.Activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.Adapter.FoodListResAdapter;
import com.example.foodapp.Domain.Foods;
import com.example.foodapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListFoodFragment extends Fragment {
    private RecyclerView recyclerView;
    private FoodListResAdapter adapterListFood;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    public ListFoodFragment() {
        // Required empty public constructor
    }

    // Tạo một instance mới của ListFoodFragment
    public static ListFoodFragment newInstance() {
        return new ListFoodFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout cho fragment này
        View view = inflater.inflate(R.layout.fragment_list_food, container, false);

        // Thiết lập RecyclerView
        recyclerView = view.findViewById(R.id.foodListView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Khởi tạo danh sách thực phẩm
        initList();

        return view;
    }

    // Khởi tạo danh sách thực phẩm và lấy dữ liệu từ Firebase
    private void initList() {
        DatabaseReference myRef = database.getReference("Foods");
        ArrayList<Foods> list = new ArrayList<>();

        // Lấy dữ liệu từ Firebase
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Lặp qua tất cả các mục trong snapshot và thêm vào danh sách
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Foods food = dataSnapshot.getValue(Foods.class);
                        if (food != null) {
                            list.add(food);
                        }
                    }

                    // Tạo và thiết lập adapter cho RecyclerView
                    adapterListFood = new FoodListResAdapter(list);
                    recyclerView.setAdapter(adapterListFood);

                    // Thông báo cho adapter rằng dữ liệu đã thay đổi
                    adapterListFood.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý kịch bản lỗi có thể xảy ra
                // Ví dụ, hiển thị thông báo toast hoặc ghi log lỗi
            }
        });
    }
}
