package com.example.foodapp.Helper;

import android.content.Context;
import android.widget.Toast;
import com.example.foodapp.Domain.Foods;
import java.util.ArrayList;

public class ManagmentCart {
    private Context context; // Context để hiển thị thông báo Toast
    private TinyDB tinyDB; // Đối tượng TinyDB để thực hiện các thao tác lưu trữ cục bộ

    // Constructor để khởi tạo context và đối tượng TinyDB
    public ManagmentCart(Context context) {
        this.context = context;
        this.tinyDB = new TinyDB(context);
    }

    // Phương thức thêm món ăn vào giỏ hàng
    public void insertFood(Foods item) {
        ArrayList<Foods> listpop = getListCart(); // Lấy danh sách các món ăn hiện tại trong giỏ hàng
        boolean existAlready = false; // Cờ để kiểm tra xem món ăn đã tồn tại chưa
        int n = 0;

        // Vòng lặp kiểm tra xem món ăn đã tồn tại trong giỏ hàng chưa
        for (int i = 0; i < listpop.size(); i++) {
            if (listpop.get(i).getTitle().equals(item.getTitle())) {
                existAlready = true; // Món ăn đã tồn tại trong giỏ hàng
                n = i;
                break;
            }
        }

        if(existAlready){
            listpop.get(n).setNumberInCart(item.getNumberInCart()); // Cập nhật số lượng nếu món ăn đã tồn tại
        } else {
            listpop.add(item); // Thêm món ăn mới vào giỏ hàng nếu chưa tồn tại
        }

        tinyDB.putListObject("CartList", listpop); // Lưu danh sách giỏ hàng vào bộ nhớ cục bộ
        Toast.makeText(context, "Đã thêm vào giỏ hàng của bạn", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo
    }

    // Phương thức lấy danh sách giỏ hàng
    public ArrayList<Foods> getListCart() {
        return tinyDB.getListObject("CartList"); // Lấy danh sách giỏ hàng từ bộ nhớ cục bộ
    }

    // Phương thức tính tổng phí
    public Double getTotalFee() {
        ArrayList<Foods> listItem = getListCart(); // Lấy danh sách các món ăn trong giỏ hàng
        double fee = 0;

        // Vòng lặp tính tổng phí của các món ăn trong giỏ hàng
        for (int i = 0; i < listItem.size(); i++) {
            fee = fee + (listItem.get(i).getPrice() * listItem.get(i).getNumberInCart());
        }

        return fee; // Trả về tổng phí
    }

    // Phương thức giảm số lượng của món ăn trong giỏ hàng
    public void minusNumberItem(ArrayList<Foods> listItem, int position, ChangeNumberItemsListener changeNumberItemsListener) {
        if (listItem.get(position).getNumberInCart() == 1) {
            listItem.remove(position); // Nếu số lượng là 1, xóa món ăn khỏi giỏ hàng
        } else {
            listItem.get(position).setNumberInCart(listItem.get(position).getNumberInCart() - 1); // Giảm số lượng món ăn đi 1
        }

        tinyDB.putListObject("CartList", listItem); // Lưu danh sách giỏ hàng vào bộ nhớ cục bộ
        changeNumberItemsListener.change(); // Gọi phương thức thay đổi số lượng món ăn
    }

    // Phương thức tăng số lượng của món ăn trong giỏ hàng
    public void plusNumberItem(ArrayList<Foods> listItem, int position, ChangeNumberItemsListener changeNumberItemsListener) {
        listItem.get(position).setNumberInCart(listItem.get(position).getNumberInCart() + 1); // Tăng số lượng món ăn thêm 1
        tinyDB.putListObject("CartList", listItem); // Lưu danh sách giỏ hàng vào bộ nhớ cục bộ
        changeNumberItemsListener.change(); // Gọi phương thức thay đổi số lượng món ăn
    }

    // Phương thức xóa toàn bộ giỏ hàng
    public void clearCart() {
        tinyDB.remove("CartList"); // Xóa danh sách giỏ hàng khỏi bộ nhớ cục bộ
        Toast.makeText(context, "Đã xóa giỏ hàng thành công", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo
    }
}
