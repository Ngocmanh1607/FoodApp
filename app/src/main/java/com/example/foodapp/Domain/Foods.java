package com.example.foodapp.Domain;

import java.io.Serializable;

public class Foods implements Serializable {
    private int CategoryId;
    private String Description;
    private boolean BestFood;
    private int Id;
    private int LocationId;
    private double Price;
    private String ImagePath;
    private int PriceId;
    private Double Star;
    private int TimeId;
    private int TimeValue;
    private String Title;
    private int NumberInCart;

    public Foods(){

    }

    public Foods(int categoryId, String description, boolean bestFood, int id, int locationId, double price, String imagePath, int priceId, Double star, int timeId, int timeValue, String title, int numberInCart) {
        CategoryId = categoryId;
        Description = description;
        BestFood = bestFood;
        Id = id;
        LocationId = locationId;
        Price = price;
        ImagePath = imagePath;
        PriceId = priceId;
        Star = star;
        TimeId = timeId;
        TimeValue = timeValue;
        Title = title;
    }

    public Foods(int foodId, String title, String description, String imageUrl, double price, int priceId, Double star, int timeValue, int timeId, int id, boolean bestfood) {
        Id=foodId;
        Title=title;
        Description=description;
        ImagePath=imageUrl;
        Price=price;
        PriceId=priceId;
        Star=star;
        TimeValue=timeValue;
        TimeId=timeId;
        CategoryId=id;
        BestFood=bestfood;

    }

    @Override
    public String toString() {
        return  Title ;
    }

    public int getCategoryId() {
        return CategoryId;
    }

    public void setCategoryId(int categoryId) {
        CategoryId = categoryId;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public boolean isBestFood() {
        return BestFood;
    }

    public void setBestFood(boolean bestFood) {
        BestFood = bestFood;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getLocationId() {
        return LocationId;
    }

    public void setLocationId(int locationId) {
        LocationId = locationId;
    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(double price) {
        Price = price;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }

    public int getPriceId() {
        return PriceId;
    }

    public void setPriceId(int priceId) {
        PriceId = priceId;
    }

    public Double getStar() {
        return Star;
    }

    public void setStar(Double star) {
        Star = star;
    }

    public int getTimeId() {
        return TimeId;
    }

    public void setTimeId(int timeId) {
        TimeId = timeId;
    }

    public int getTimeValue() {
        return TimeValue;
    }

    public void setTimeValue(int timeValue) {
        TimeValue = timeValue;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public int getNumberInCart() {
        return NumberInCart;
    }

    public void setNumberInCart(int numberInCart) {
        NumberInCart = numberInCart;
    }
}
