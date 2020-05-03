package com.example.nutrally;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class Meal extends ExpandableGroup<Food> {
    public Meal(String title, List<Food> items) {
        super(title, items);
    }
}
