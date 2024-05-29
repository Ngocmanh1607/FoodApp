package com.example.foodapp.Activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.Adapter.OrderListAdapter;
import com.example.foodapp.Domain.Order;
import com.example.foodapp.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ListOrderFragment extends Fragment {
    private RecyclerView recyclerView;
    private OrderListAdapter adapter;

    public ListOrderFragment() {
    }

    public static ListOrderFragment newInstance() {
        return new ListOrderFragment();
    }
    public  Date convertDateTime(String dateTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            return dateFormat.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_order, container, false);

        recyclerView = view.findViewById(R.id.orderListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        initOrderList();

        return view;
    }

    private void initOrderList() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        ArrayList<Order> orderList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date currentDate = calendar.getTime();
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    String firebaseDateTime = dataSnapshot.child("dateTime").getValue(String.class);

                    Date orderDate = convertDateTime(firebaseDateTime);


                    if (order != null && orderDate != null && isSameDate(orderDate, currentDate)) {
                        orderList.add(0, order);
                    }
                }

                if (adapter == null) {
                    adapter = new OrderListAdapter(orderList);
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                    Snackbar.make(recyclerView, "Danh sách đơn hàng đã được cập nhật", Snackbar.LENGTH_SHORT).show();
                }
            }
            //so sanh ngay
            private boolean isSameDate(Date orderDate, Date currentDate) {
                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(currentDate);
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(orderDate);

                    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                            cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}