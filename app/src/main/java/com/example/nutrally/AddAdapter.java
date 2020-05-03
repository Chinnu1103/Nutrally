package com.example.nutrally;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class AddAdapter extends RecyclerView.Adapter<AddAdapter.ViewHolder> {
    private List<Food> mFoods;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ItemLongClickListener mLongClickListener;

    public AddAdapter(Context context, List<Food> foods) {
        mInflater = LayoutInflater.from(context);
        mFoods = foods;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.search_item_no_border, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.tv_name.setText(mFoods.get(position).getName());
        if (mFoods.get(position).getNutrition() != null){
            holder.ll_nut.setVisibility(View.VISIBLE);
            if (mFoods.get(position).getMeal() == null || !mFoods.get(position).getMeal().equals("Exercise")) {
                holder.iv_info.setVisibility(View.VISIBLE);
            }
            holder.tv_qty.setVisibility(View.VISIBLE);
            holder.tv_qty.setText(mFoods.get(position).getNutrition().qty);
            holder.tv_cal.setText(String.valueOf(mFoods.get(position).getNutrition().calories));

            holder.iv_info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LayoutInflater inflater = LayoutInflater.from(mInflater.getContext());
                    final View v = inflater.inflate(R.layout.item_info, null);
                    Nutrition nutrition = mFoods.get(position).getNutrition();
                    ((TextView)v.findViewById(R.id.tv_cal)).setText(nutrition.calories + " cal");
                    ((TextView)v.findViewById(R.id.tv_fat)).setText(nutrition.fat + "g");
                    ((TextView)v.findViewById(R.id.tv_chol)).setText(nutrition.cholestrol + "mg");
                    ((TextView)v.findViewById(R.id.tv_sod)).setText(nutrition.sodium + "mg");
                    ((TextView)v.findViewById(R.id.tv_carbs)).setText(nutrition.carbs + "g");
                    ((TextView)v.findViewById(R.id.tv_pro)).setText(nutrition.protein + "g");
                    new AlertDialog.Builder(mInflater.getContext())
                            .setTitle(mFoods.get(position).getName())
                            .setView(v)
                            .setMessage("Nutrition Information")
                            .setPositiveButton("Ok", null)
                            .show();
                }
            });
        }
        if (mFoods.get(position).getImage() != null){
            Glide.with(mInflater.getContext())
                    .load(mFoods.get(position).getImage())
                    .into(holder.iv_item);
        }  else if (mFoods.get(position).getMeal().equals("Exercise")) {
            Glide.with(mInflater.getContext())
                    .load(R.drawable.ic_exercise)
                    .into(holder.iv_item);
        }
    }

    public void updateItems(List<Food> foods){
        mFoods = foods;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mFoods.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView tv_name, tv_cal, tv_qty, tv_cal_text;
        ImageView iv_info, iv_item;
        LinearLayout ll_nut;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_qty = itemView.findViewById(R.id.tv_qty);
            tv_cal = itemView.findViewById(R.id.tv_cal);
            tv_cal_text = itemView.findViewById(R.id.tv_cal_text);
            iv_info = itemView.findViewById(R.id.iv_info);
            iv_item = itemView.findViewById(R.id.iv_item);
            ll_nut = itemView.findViewById(R.id.ll_cal);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            if (mLongClickListener != null) mLongClickListener.onItemLongClick(view, getAdapterPosition());
            return true;
        }
    }

    Food getItem(int id) {
        return mFoods.get(id);
    }

    void setClickListener(ItemClickListener listener) {
        mClickListener = listener;
    }

    void setLongClickListener(ItemLongClickListener listener) {
        mLongClickListener = listener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface ItemLongClickListener {
        void onItemLongClick(View view, int position);
    }
}
