package com.example.nutrally;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

import java.util.Locale;

public class FoodViewHolder extends ChildViewHolder implements View.OnLongClickListener{
    private static final String TAG = "FoodViewHolder";
    private TextView tv_name, tv_cal, tv_qty;
    private ImageView iv_info, iv_item;
    private LinearLayout ll_cal;
    private Context mContext;
    private AddAdapter.ItemLongClickListener mLongClickListener;

    FoodViewHolder(View itemView, AddAdapter.ItemLongClickListener listener, Context context) {
        super(itemView);
        mContext = context;
        mLongClickListener = listener;
        tv_name = itemView.findViewById(R.id.tv_name);
        tv_qty = itemView.findViewById(R.id.tv_qty);
        tv_cal = itemView.findViewById(R.id.tv_cal);
        iv_info = itemView.findViewById(R.id.iv_info);
        iv_item = itemView.findViewById(R.id.iv_item);
        ll_cal = itemView.findViewById(R.id.ll_cal);
        itemView.setOnLongClickListener(this);
    }

    void bind(final Food food, final Context context){
        tv_name.setText(food.getName());
        if (food.getNutrition() != null){
            ll_cal.setVisibility(View.VISIBLE);
            if (food.getMeal().equals("Exercise")) {
                iv_info.setVisibility(View.GONE);
            } else {
                iv_info.setVisibility(View.VISIBLE);
            }
            tv_qty.setVisibility(View.VISIBLE);
            tv_qty.setText(food.getNutrition().qty);
            tv_cal.setText(String.valueOf(food.getNutrition().calories));

            iv_info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LayoutInflater inflater = LayoutInflater.from(context);
                    final View v = inflater.inflate(R.layout.item_info, null);
                    Nutrition nutrition = food.getNutrition();
                    ((TextView)v.findViewById(R.id.tv_cal)).setText(String.format(Locale.getDefault(), "%d cal", nutrition.calories));
                    ((TextView)v.findViewById(R.id.tv_fat)).setText(String.format(Locale.getDefault(), "%d g", nutrition.fat));
                    ((TextView)v.findViewById(R.id.tv_chol)).setText(String.format(Locale.getDefault(), "%d mg", nutrition.cholestrol));
                    ((TextView)v.findViewById(R.id.tv_sod)).setText(String.format(Locale.getDefault(), "%d mg", nutrition.sodium));
                    ((TextView)v.findViewById(R.id.tv_carbs)).setText(String.format(Locale.getDefault(), "%d g", nutrition.carbs));
                    ((TextView)v.findViewById(R.id.tv_pro)).setText(String.format(Locale.getDefault(), "%d g", nutrition.protein));
                    new AlertDialog.Builder(context)
                            .setTitle(food.getName())
                            .setView(v)
                            .setMessage("Nutrition Information")
                            .setPositiveButton("Ok", null)
                            .show();
                }
            });
        }

        if (food.getImage() != null){
            ll_cal.setPadding(0, 0, 0, 0);
            Glide.with(context)
                    .load(food.getImage())
                    .into(iv_item);
        } else if (food.getMeal().equals("Exercise")) {
            ll_cal.setPadding(0, 0, dpToPx(), 0);
            Glide.with(context)
                    .load(R.drawable.ic_exercise)
                    .into(iv_item);
        }
    }

    private int dpToPx() {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 8, mContext.getResources().getDisplayMetrics());
    }

    @Override
    public boolean onLongClick(View view) {
        if (mLongClickListener != null) mLongClickListener.onItemLongClick(view, getAdapterPosition());
        return true;
    }
}
