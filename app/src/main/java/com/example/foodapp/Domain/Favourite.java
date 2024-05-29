package com.example.foodapp.Domain;

public class Favourite {
    public String foodId;
    public String userId;

    public Favourite() {
        // Default constructor required for calls to DataSnapshot.getValue(Favourite.class)
    }

    public Favourite(String foodId, String userId) {
        this.foodId = foodId;
        this.userId = userId;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}