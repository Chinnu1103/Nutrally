package com.example.nutrally;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class IntroViewPagerAdapter extends PagerAdapter {
    private Context mContext;
    private String mName;

    private EditText et_email;
    private EditText et_name;
    private EditText et_inches;
    private EditText et_feet_cm;
    private EditText et_weight;
    private EditText et_calories;
    private EditText et_exs;
    private EditText et_dd;
    private EditText et_mm;
    private EditText et_yyyy;

    private MaterialButtonToggleGroup tb_weight;
    private MaterialButtonToggleGroup tb_height;
    private MaterialButtonToggleGroup tb_gender;

    IntroViewPagerAdapter(Context context, String name) {
        mContext = context;
        mName = name;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        final SharedPreferences sp = mContext.getSharedPreferences("User", MODE_PRIVATE);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) return new View(mContext);
        final View view;
        if (position == 0) {
            view = inflater.inflate(R.layout.intro_one, null);
            et_email = view.findViewById(R.id.et_email);
            et_email.setText(sp.getString("Email", ""));
            et_name = view.findViewById(R.id.et_name);
            et_name.setText(mName);
        } else if (position == 1) {
            view = inflater.inflate(R.layout.intro_two, null);
            et_dd = view.findViewById(R.id.et_dd);
            et_mm = view.findViewById(R.id.et_mm);
            et_yyyy = view.findViewById(R.id.et_yyyy);
            et_feet_cm = view.findViewById(R.id.et_feet_cm);
            et_inches = view.findViewById(R.id.et_inches);
            et_weight = view.findViewById(R.id.et_weight);
            tb_height = view.findViewById(R.id.tb_group_height);
            tb_weight = view.findViewById(R.id.tb_group_weight);
            tb_gender = view.findViewById(R.id.tb_group_gender);

            tb_height.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
                @Override
                public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                    if (!isChecked) return;
                    if (R.id.bt_met_ht == checkedId) {
                        ((TextInputLayout) view.findViewById(R.id.til_feet_cm)).setHint("CM");
                        view.findViewById(R.id.til_inches).setVisibility(View.GONE);
                        float cm = 0;
                        if (!et_inches.getText().toString().equals("")) cm += Float.parseFloat(et_inches.getText().toString())*2.54;
                        if (!et_feet_cm.getText().toString().equals("")) cm += Float.parseFloat(et_feet_cm.getText().toString())*30.48;
                        if (cm != 0) et_feet_cm.setText(String.valueOf(Math.round(cm*100)/100f));
                    } else {
                        ((TextInputLayout) view.findViewById(R.id.til_feet_cm)).setHint("Feet");
                        view.findViewById(R.id.til_inches).setVisibility(View.VISIBLE);
                        float inches = 0;
                        int feet = 0;
                        if (!et_feet_cm.getText().toString().equals("")) {
                            inches = Float.parseFloat(et_feet_cm.getText().toString()) * 0.3937f;
                            feet = (int) inches / 12;
                            inches = Math.round((inches - feet*12)*100)/100f;
                        }
                        if (inches !=0 && feet != 0) {
                            et_feet_cm.setText(String.valueOf(feet));
                            et_inches.setText(String.valueOf(inches));
                        }
                    }
                }
            });

            tb_weight.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
                @Override
                public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                    if (!isChecked) return;
                    if (R.id.bt_met_wt == checkedId) {
                        ((TextInputLayout) view.findViewById(R.id.til_weight)).setHint("KG");
                        if (!et_weight.getText().toString().equals("")) et_weight.setText(String.valueOf(Math.round(Float.parseFloat(et_weight.getText().toString()) * 4.54f)/10f));
                    }
                    else {
                        ((TextInputLayout) view.findViewById(R.id.til_weight)).setHint("Lbs");
                        if (!et_weight.getText().toString().equals("")) et_weight.setText(String.valueOf(Math.round(Float.parseFloat(et_weight.getText().toString()) / 0.454f * 10f)/10f));
                    }
                }
            });
        } else {
            view = inflater.inflate(R.layout.intro_three, null);
            et_calories = view.findViewById(R.id.et_cal);
            et_exs = view.findViewById(R.id.et_exs);
        }

        container.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        return 3;
    }

    User getUser(){
        SharedPreferences.Editor editor = mContext.getSharedPreferences("User", MODE_PRIVATE).edit();
        User user = new User();
        user.setEmail(et_email.getText().toString());
        user.setName(et_name.getText().toString());

        if (!et_calories.getText().toString().equals("")) {
            user.setCalories(Integer.parseInt(et_calories.getText().toString()));
            editor.putInt("Calories", user.getCalories());
        }

        if (!et_exs.getText().toString().equals("")) {
            user.setExercise(Integer.parseInt(et_exs.getText().toString()));
            editor.putFloat("Exercise", (float) user.getExercise());
        }

        String gender = (tb_gender.getCheckedButtonId() == R.id.bt_male) ? "Male" : "Female";
        user.setGender(gender);
        editor.putString("Gender", gender);

        if (!et_weight.getText().toString().equals("")) {
            if (tb_weight.getCheckedButtonId() == R.id.bt_imp_wt) user.setWeight(Double.parseDouble(et_weight.getText().toString())*0.454);
            else user.setWeight(Double.parseDouble(et_weight.getText().toString()));
            editor.putFloat("Weight", (float) user.getWeight());
        }

        if (tb_height.getCheckedButtonId() == R.id.bt_met_ht && !et_feet_cm.getText().toString().equals("")) {
            user.setHeight(Double.parseDouble(et_feet_cm.getText().toString()));
            editor.putFloat("Height", (float) user.getHeight());
        }
        else if (tb_height.getCheckedButtonId() == R.id.bt_imp_ht && (!et_feet_cm.getText().toString().equals("") || !et_inches.getText().toString().equals(""))) {
            user.setHeight(Double.parseDouble(et_feet_cm.getText().toString())*30.48 + Double.parseDouble(et_inches.getText().toString())*2.54);
            editor.putFloat("Height", (float) user.getHeight());
        }

        try {
            Calendar dob = Calendar.getInstance();
            dob.set(Calendar.DAY_OF_MONTH, Integer.parseInt(et_dd.getText().toString()));
            dob.set(Calendar.MONTH, Integer.parseInt(et_mm.getText().toString()));
            dob.set(Calendar.YEAR, Integer.parseInt(et_yyyy.getText().toString()));
            user.setBday(dob.getTime());
            int year = Calendar.getInstance().get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (dob.get(Calendar.DAY_OF_YEAR) > Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) year -= 1;
            editor.putInt("Age", year);
        } catch (Exception e) {
            user.setBday(null);
        }

        editor.apply();
        return user;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
