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

    public static ListFoodFragment newInstance() {
        return new ListFoodFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_food, container, false);

        recyclerView = view.findViewById(R.id.foodListView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        initList();

        return view;
    }

    private void initList() {
        DatabaseReference myRef = database.getReference("Foods");
        ArrayList<Foods> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Foods food = dataSnapshot.getValue(Foods.class);
                        if (food != null) {
                            list.add(food);
                        }
                    }

                    // Create and set adapter for RecyclerView
                    adapterListFood = new FoodListResAdapter(list);
                    recyclerView.setAdapter(adapterListFood);

                    // Notify adapter that data has changed
                    adapterListFood.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible error scenario
                // For example, show a toast message or log the error
            }
        });
    }
}
