package com.example.foodapp.Domain;

import java.util.List;

public class Order {
    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }

    private String key;
    private String userName;
    private String phone;
    private String location;
    private List<OrderItem> lOrderItem;
    private Status status;
    private String dateTime;
    private double totalPrice;
    private String note;



    public Order(String key, String userName, String phone, String location, List<OrderItem> lOrderItem, Status status, String dateTime, double totalPrice, String note) {
        this.key = key;
        this.userName = userName;
        this.phone = phone;
        this.location = location;
        this.lOrderItem = lOrderItem;
        this.status = status;
        this.dateTime = dateTime;
        this.totalPrice = totalPrice;
        this.note = note;
    }

    public Order() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<OrderItem> getlOrderItem() {
        return lOrderItem;
    }

    public void setlOrderItem(List<OrderItem> lOrderItem) {
        this.lOrderItem = lOrderItem;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

}
