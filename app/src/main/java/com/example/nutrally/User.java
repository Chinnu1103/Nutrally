package com.example.nutrally;

import java.util.Date;

public class User {
    String mName;
    String mEmail;
    double mHeight;
    double mWeight;
    String mGender;
    Date mBday;
    int mCalories;
    double mExercise;

    public User() {
    }

    public User(String name, String email, double height, double weight, Date bday, String gender, int calories, double exercise) {
        mName = name;
        mEmail = email;
        mHeight = height;
        mWeight = weight;
        mBday = bday;
        mGender = gender;
        mCalories = calories;
        mExercise = exercise;
    }

    public String getGender() {
        return mGender;
    }

    public int getCalories() {
        return mCalories;
    }

    public void setCalories(int calories) {
        mCalories = calories;
    }

    public double getExercise() {
        return mExercise;
    }

    public void setExercise(double exercise) {
        mExercise = exercise;
    }

    public void setGender(String gender) {
        mGender = gender;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public double getHeight() {
        return mHeight;
    }

    public void setHeight(double height) {
        mHeight = height;
    }

    public double getWeight() {
        return mWeight;
    }

    public void setWeight(double weight) {
        mWeight = weight;
    }

    public Date getBday() {
        return mBday;
    }

    public void setBday(Date bday) {
        mBday = bday;
    }
}


