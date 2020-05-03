package com.example.nutrally;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nutrally.ui.Home.HomeFragment;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

public class MealViewHolder extends GroupViewHolder {
    private TextView tv_meal;
    private ImageView iv_meal;
    private TextView tv_cal;

    public MealViewHolder(View itemView) {
        super(itemView);
        tv_meal = itemView.findViewById(R.id.tv_meal);
        iv_meal = itemView.findViewById(R.id.iv_meal);
        tv_cal = itemView.findViewById(R.id.tv_cal);
    }

    public void bind(Meal meal){
        tv_meal.setText(meal.getTitle());
        int cal = 0;
        for (Food food : meal.getItems()) {
            if (food.getNutrition() != null) cal += food.getNutrition().calories;
        }
        tv_cal.setText(String.valueOf(cal));
    }

    @Override
    public void expand() {
        iv_meal.animate().rotation(180).start();
        if (tv_meal.getText().toString().equals("Breakfast")) HomeFragment.bf_exp = true;
        else if (tv_meal.getText().toString().equals("Lunch")) HomeFragment.ln_exp = true;
        else if (tv_meal.getText().toString().equals("Snacks")) HomeFragment.sn_exp = true;
        else if (tv_meal.getText().toString().equals("Dinner")) HomeFragment.dn_exp = true;

    }

    @Override
    public void collapse() {
        iv_meal.animate().rotation(0).start();
        if (tv_meal.getText().toString().equals("Breakfast")) HomeFragment.bf_exp = false;
        else if (tv_meal.getText().toString().equals("Lunch")) HomeFragment.ln_exp = false;
        else if (tv_meal.getText().toString().equals("Snacks")) HomeFragment.sn_exp = false;
        else if (tv_meal.getText().toString().equals("Dinner")) HomeFragment.dn_exp = false;
    }

    private void animateExpand() {
        RotateAnimation rotate = new RotateAnimation(360, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        iv_meal.setAnimation(rotate);
    }

    private void animateCollapse() {
        RotateAnimation rotate = new RotateAnimation(180, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        iv_meal.setAnimation(rotate);
    }
}
