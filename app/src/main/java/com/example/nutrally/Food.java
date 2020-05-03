package com.example.nutrally;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

enum Type{
    COMMON, BRANDED
}

public class Food implements Parcelable {
    private static final String TAG = "Food";
    private String mName;
    private String mNix_id;
    private String mImage;
    private Nutrition mNutrition;
    private Type mType;
    private String mMeal;

    public Food(String name, String meal) {
        mName = name;
        mNutrition = null;
        mMeal = meal;
        mImage = null;
    }

    public Food(String name, String id, String image, Type type) {
        mName = name;
        mNix_id = id;
        mType = type;
        mImage = image;
        mNutrition = null;
        setMeal(null);
    }

    public Food(String name, String id, String image, Nutrition nutrition, Type type) {
        mName = name;
        mNix_id = id;
        mType = type;
        mImage = image;
        mNutrition = nutrition;
        setMeal(null);
    }

    public Food(String name, String nix_id, String image, Nutrition nutrition, Type type, String meal) {
        mName = name;
        mNix_id = nix_id;
        mImage = image;
        mNutrition = nutrition;
        mType = type;
        mMeal = meal;
    }

    public String getMeal() {
        return mMeal;
    }

    public void setMeal(String meal) {
        mMeal = meal;
    }

    public String getNix_id() {
        return mNix_id;
    }

    public Type getType() {
        return mType;
    }

    public String getImage() {
        return mImage;
    }

    public void setNutrition(Nutrition nutrition){
        mNutrition = nutrition;
    }

    public Nutrition getNutrition(){
        return mNutrition;
    }

    public String getName() {
        return mName;
    }

    public static Map<String, Food> convertMap(Map<String, Object> m, String meal_type) {
        Map<String, Food> map = new HashMap<>();
        for (Object obj : m.values().toArray()) {
            HashMap food_map = (HashMap) obj;
            Log.d(TAG, "convertMap: " + food_map.values());
            Food food = new Food(food_map.get("name").toString(), food_map.get("nix_id").toString(), (food_map.get("image") != null) ? food_map.get("image").toString() : null, Nutrition.convertHashMap((Map<String, Object>)food_map.get("nutrition")), (food_map.get("type") != null) ? Type.valueOf(food_map.get("type").toString()) : null);
            food.setMeal(meal_type);
            map.put(food.getName(), food);
        }
        return map;
    }

    protected Food(Parcel in) {
        mName = in.readString();
    }

    public static final Creator<Food> CREATOR = new Creator<Food>() {
        @Override
        public Food createFromParcel(Parcel in) {
            return new Food(in);
        }

        @Override
        public Food[] newArray(int size) {
            return new Food[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mName);
    }
}
