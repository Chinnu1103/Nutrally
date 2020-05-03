package com.example.nutrally;

import java.util.Map;

public class Nutrition {
    int calories;
    int fat;
    int cholestrol;
    int sodium;
    int carbs;
    int protein;
    String qty;

    public Nutrition(int calories, String qty) {
        this.calories = calories;
        this.qty = qty;
    }

    public Nutrition(int calories, int fat, int cholestrol, int sodium, int carbs, int protein, String qty) {
        this.calories = calories;
        this.fat = fat;
        this.cholestrol = cholestrol;
        this.sodium = sodium;
        this.carbs = carbs;
        this.protein = protein;
        this.qty = qty;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public int getFat() {
        return fat;
    }

    public void setFat(int fat) {
        this.fat = fat;
    }

    public int getCholestrol() {
        return cholestrol;
    }

    public void setCholestrol(int cholestrol) {
        this.cholestrol = cholestrol;
    }

    public int getSodium() {
        return sodium;
    }

    public void setSodium(int sodium) {
        this.sodium = sodium;
    }

    public int getCarbs() {
        return carbs;
    }

    public void setCarbs(int carbs) {
        this.carbs = carbs;
    }

    public int getProtein() {
        return protein;
    }

    public void setProtein(int protein) {
        this.protein = protein;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public static Nutrition convertHashMap(Map<String, Object> m) {
        return new Nutrition(Integer.parseInt(m.get("calories").toString()), Integer.parseInt(m.get("fat").toString()), Integer.parseInt(m.get("cholestrol").toString()), Integer.parseInt(m.get("sodium").toString()), Integer.parseInt(m.get("carbs").toString()), Integer.parseInt(m.get("protein").toString()), m.get("qty").toString());
    }
}
