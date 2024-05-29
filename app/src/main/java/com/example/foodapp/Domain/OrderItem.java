package com.example.foodapp.Domain;

public class OrderItem {
    private String name;

    private int quantity;

    private String ImagePath;

    public OrderItem(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public OrderItem(String imagePath) {
        ImagePath = imagePath;
    }

    public OrderItem() {
    }

    public OrderItem(String name, int quantity, String imagePath) {
        this.name = name;
        this.quantity = quantity;
        ImagePath = imagePath;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
