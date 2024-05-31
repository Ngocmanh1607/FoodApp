package com.example.foodapp.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.foodapp.Domain.Foods;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class TinyDB {
    private SharedPreferences preferences; // Đối tượng SharedPreferences để lưu trữ dữ liệu cục bộ
    private String DEFAULT_APP_IMAGEDATA_DIRECTORY; // Thư mục mặc định để lưu trữ hình ảnh
    private String lastImagePath = ""; // Đường dẫn của hình ảnh được lưu lần cuối

    // Constructor để khởi tạo đối tượng SharedPreferences
    public TinyDB(Context appContext) {
        preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
    }

    // Phương thức lấy hình ảnh từ đường dẫn
    public Bitmap getImage(String path) {
        Bitmap bitmapFromPath = null;
        try {
            bitmapFromPath = BitmapFactory.decodeFile(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmapFromPath;
    }

    // Phương thức lấy đường dẫn của hình ảnh được lưu lần cuối
    public String getSavedImagePath() {
        return lastImagePath;
    }

    // Phương thức lưu hình ảnh vào bộ nhớ ngoài
    public String putImage(String theFolder, String theImageName, Bitmap theBitmap) {
        if (theFolder == null || theImageName == null || theBitmap == null)
            return null;

        this.DEFAULT_APP_IMAGEDATA_DIRECTORY = theFolder;
        String mFullPath = setupFullPath(theImageName);

        if (!mFullPath.equals("")) {
            lastImagePath = mFullPath;
            saveBitmap(mFullPath, theBitmap);
        }

        return mFullPath;
    }

    // Phương thức lưu hình ảnh với đường dẫn đầy đủ
    public boolean putImageWithFullPath(String fullPath, Bitmap theBitmap) {
        return !(fullPath == null || theBitmap == null) && saveBitmap(fullPath, theBitmap);
    }

    // Thiết lập đường dẫn đầy đủ cho hình ảnh
    private String setupFullPath(String imageName) {
        File mFolder = new File(Environment.getExternalStorageDirectory(), DEFAULT_APP_IMAGEDATA_DIRECTORY);

        if (isExternalStorageReadable() && isExternalStorageWritable() && !mFolder.exists()) {
            if (!mFolder.mkdirs()) {
                Log.e("ERROR", "Failed to setup folder");
                return "";
            }
        }

        return mFolder.getPath() + '/' + imageName;
    }

    // Lưu hình ảnh dưới dạng bitmap vào đường dẫn đầy đủ
    private boolean saveBitmap(String fullPath, Bitmap bitmap) {
        if (fullPath == null || bitmap == null)
            return false;

        boolean fileCreated = false;
        boolean bitmapCompressed = false;
        boolean streamClosed = false;

        File imageFile = new File(fullPath);

        if (imageFile.exists())
            if (!imageFile.delete())
                return false;

        try {
            fileCreated = imageFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(imageFile);
            bitmapCompressed = bitmap.compress(CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
            bitmapCompressed = false;
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                    streamClosed = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    streamClosed = false;
                }
            }
        }

        return (fileCreated && bitmapCompressed && streamClosed);
    }

    // Phương thức lấy danh sách các đối tượng Foods từ SharedPreferences
    public ArrayList<Foods> getListObject(String key){
        Gson gson = new Gson();
        ArrayList<String> objStrings = getListString(key);
        ArrayList<Foods> playerList =  new ArrayList<Foods>();
        for(String jObjString : objStrings){
            Foods player  = gson.fromJson(jObjString,  Foods.class);
            playerList.add(player);
        }
        return playerList;
    }

    // Phương thức lưu danh sách các đối tượng Foods vào SharedPreferences
    public void putListObject(String key, ArrayList<Foods> playerList){
        checkForNullKey(key);
        Gson gson = new Gson();
        ArrayList<String> objStrings = new ArrayList<String>();
        for(Foods player: playerList){
            objStrings.add(gson.toJson(player));
        }
        putListString(key, objStrings);
    }

    // Phương thức lấy danh sách các chuỗi từ SharedPreferences
    public ArrayList<String> getListString(String key) {
        return new ArrayList<String>(Arrays.asList(TextUtils.split(preferences.getString(key, ""), "‚‗‚")));
    }

    // Phương thức lưu danh sách các chuỗi vào SharedPreferences
    public void putListString(String key, ArrayList<String> stringList) {
        checkForNullKey(key);
        String[] myStringList = stringList.toArray(new String[stringList.size()]);
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myStringList)).apply();
    }

    // Phương thức xóa một mục trong SharedPreferences
    public void remove(String key) {
        preferences.edit().remove(key).apply();
    }

    // Kiểm tra xem bộ nhớ ngoài có thể ghi được không
    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    // Kiểm tra xem bộ nhớ ngoài có thể đọc được không
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    // Kiểm tra khóa có null không để ngăn chặn lỗi
    private void checkForNullKey(String key){
        if (key == null){
            throw new NullPointerException();
        }
    }

    // Kiểm tra giá trị có null không để ngăn chặn lỗi
    private void checkForNullValue(String value){
        if (value == null){
            throw new NullPointerException();
        }
    }
}
