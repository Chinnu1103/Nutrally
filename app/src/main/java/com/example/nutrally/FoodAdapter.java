package com.example.nutrally;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

import java.util.List;

public class FoodAdapter extends ExpandableRecyclerViewAdapter<MealViewHolder, FoodViewHolder> {
    private Context mContext;
    private AddAdapter.ItemLongClickListener mLongClickListener;

    public FoodAdapter(List<? extends  ExpandableGroup> groups) {
        super(groups);
    }

    @Override
    public MealViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.expandable_meal, parent, false);
        return new MealViewHolder(v);
    }

    @Override
    public FoodViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
        return new FoodViewHolder(v, mLongClickListener, mContext);
    }

    @Override
    public void onBindChildViewHolder(FoodViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        final Food food = (Food) group.getItems().get(childIndex);
        holder.bind(food, mContext);
    }

    @Override
    public void onBindGroupViewHolder(MealViewHolder holder, int flatPosition, ExpandableGroup group) {
        final Meal meal = (Meal) group;
        holder.bind(meal);
    }

    public void setLongClickListener(AddAdapter.ItemLongClickListener listener) {
        mLongClickListener = listener;
    }


}
