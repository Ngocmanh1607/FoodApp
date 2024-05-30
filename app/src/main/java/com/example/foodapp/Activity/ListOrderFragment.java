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

    // Constructor không đối số
    public ListOrderFragment() {
    }

    // Tạo một instance mới của ListOrderFragment
    public static ListOrderFragment newInstance() {
        return new ListOrderFragment();
    }

    // Chuyển đổi chuỗi datetime sang đối tượng Date
    public Date convertDateTime(String dateTime) {
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
        // Inflate layout cho fragment này
        View view = inflater.inflate(R.layout.fragment_list_order, container, false);

        // Thiết lập RecyclerView
        recyclerView = view.findViewById(R.id.orderListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo danh sách đơn hàng
        initOrderList();

        return view;
    }

    // Khởi tạo danh sách đơn hàng và lấy dữ liệu từ Firebase
    private void initOrderList() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        ArrayList<Order> orderList = new ArrayList<>();

        // Lấy ngày hiện tại mà không có phần giờ, phút, giây, millisecond
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date currentDate = calendar.getTime();

        // Lấy dữ liệu từ Firebase
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();

                // Lặp qua tất cả các mục trong snapshot và thêm vào danh sách
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    String firebaseDateTime = dataSnapshot.child("dateTime").getValue(String.class);

                    Date orderDate = convertDateTime(firebaseDateTime);

                    // Nếu đơn hàng không null và ngày đặt hàng giống ngày hiện tại, thêm vào danh sách
                    if (order != null && orderDate != null && isSameDate(orderDate, currentDate)) {
                        orderList.add(0, order);
                    }
                }

                // Thiết lập adapter cho RecyclerView nếu chưa có, nếu có rồi thì thông báo thay đổi
                if (adapter == null) {
                    adapter = new OrderListAdapter(orderList);
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                    Snackbar.make(recyclerView, "Danh sách đơn hàng đã được cập nhật", Snackbar.LENGTH_SHORT).show();
                }
            }

            // So sánh ngày của hai đối tượng Date để kiểm tra xem chúng có cùng ngày hay không
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
                // Xử lý lỗi có thể xảy ra khi truy cập Firebase
            }
        });
    }
}
