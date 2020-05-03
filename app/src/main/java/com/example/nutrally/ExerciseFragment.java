package com.example.nutrally;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Process;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static com.twitter.sdk.android.core.Twitter.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseFragment extends Fragment implements AddAdapter.ItemClickListener,
        SearchView.OnQueryTextListener, View.OnFocusChangeListener, NutriUtils.AsyncResponse,
        AddAdapter.ItemLongClickListener{
    private ArrayList<Food> mFoods = new ArrayList<>(), mLoggedFoods = new ArrayList<>();
    private AddAdapter mSearchAdapter;
    private AddAdapter mLogAdapter;
    private NutriUtils utils;
    private SearchView sv_add;
    private View mRoot;
    private String[] exercises;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View root = inflater.inflate(R.layout.fragment_exercise, container, false);
        mRoot = root;
        setHasOptionsMenu(true);

        root.findViewById(R.id.bt_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPreferences();
            }
        });

        RecyclerView rv_add = root.findViewById(R.id.rv_add);
        rv_add.setLayoutManager(new LinearLayoutManager(getContext()));
        mSearchAdapter = new AddAdapter(getContext(), mFoods);
        mSearchAdapter.setClickListener(this);
        DividerItemDecoration decoration = new DividerItemDecoration(rv_add.getContext(), DividerItemDecoration.VERTICAL);
        rv_add.addItemDecoration(decoration);
        rv_add.setAdapter(mSearchAdapter);

        rv_add.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) hideKeyboard();
            }
        });

        exercises = new String[0];

        RecyclerView rv_log = root.findViewById(R.id.rv_log);
        rv_log.setLayoutManager(new LinearLayoutManager(getContext()));
        mLogAdapter = new AddAdapter(getContext(), mLoggedFoods);
        mLogAdapter.setLongClickListener(this);
        rv_log.addItemDecoration(decoration);
        rv_log.setAdapter(mLogAdapter);

        Button bt_add = root.findViewById(R.id.bt_add);
        Button bt_log = root.findViewById(R.id.bt_log);
        final EditText et_add = root.findViewById(R.id.et_add);
        bt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                utils = new NutriUtils(getContext(), ExerciseFragment.this);
                utils.execute("naturalExercise", et_add.getText().toString());
                hideKeyboard();
                et_add.clearFocus();
            }
        });

        bt_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Food[] foods = new Food[mLoggedFoods.size()];
                ExerciseFragmentDirections.ActionExerciseFragmentToNavHome action =
                        ExerciseFragmentDirections.actionExerciseFragmentToNavHome();
                action.setMealName("Exercise");
                action.setMealValues(mLoggedFoods.toArray(foods));
                Navigation.findNavController(root).navigate(action);
            }
        });

        sv_add = getActivity().findViewById(R.id.sv_log);
        sv_add.setQueryHint(Html.fromHtml("<font color = #808080>" + "Search exercise here" + "</font"));
        sv_add.setOnQueryTextListener(this);
        sv_add.setOnQueryTextFocusChangeListener(this);

        return root;
    }

    private int dpToPx() {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 30, getResources().getDisplayMetrics());
    }

    private void setPreferences() {
        final SharedPreferences sp = getActivity().getSharedPreferences("User", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();
        final User user = new User();
        final DocumentReference df = FirebaseFirestore.getInstance().collection("Users").document(sp.getString("Email", ".").replace('.', ','));
        final View view = View.inflate(getActivity(), R.layout.intro_two, null);
        view.setPadding(0, dpToPx(), 0, 0);
        final EditText et_dd = view.findViewById(R.id.et_dd);
        final EditText et_mm = view.findViewById(R.id.et_mm);
        final EditText et_yyyy = view.findViewById(R.id.et_yyyy);
        final EditText et_feet_cm = view.findViewById(R.id.et_feet_cm);
        final EditText et_inches = view.findViewById(R.id.et_inches);
        final EditText et_weight = view.findViewById(R.id.et_weight);
        final MaterialButtonToggleGroup tb_height = view.findViewById(R.id.tb_group_height);
        final MaterialButtonToggleGroup tb_weight = view.findViewById(R.id.tb_group_weight);
        final MaterialButtonToggleGroup tb_gender = view.findViewById(R.id.tb_group_gender);

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
                }else {
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

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(getLayoutInflater().inflate(R.layout.loading_layout, null))
                .setCancelable(false)
                .create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final AlertDialog main = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        dialog.show();

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

                        df.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialog.dismiss();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .create();

        dialog.show();
        df.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc != null && doc.exists()) {
                        User u = doc.toObject(User.class);
                        user.setEmail(u.getEmail());
                        user.setName(u.getName());
                        user.setCalories(u.getCalories());
                        user.setExercise(u.getExercise());
                        if (u.getBday() != null) {
                            Calendar dob = Calendar.getInstance();
                            dob.setTime(u.getBday());
                            et_dd.setText(String.valueOf(dob.get(Calendar.DAY_OF_MONTH)));
                            et_mm.setText(String.valueOf(dob.get(Calendar.MONTH)));
                            et_yyyy.setText(String.valueOf(dob.get(Calendar.YEAR)));
                        }
                        if (u.getHeight() != 0) et_feet_cm.setText(String.valueOf(Math.round(u.getHeight()*100)/100f));
                        if (u.getWeight() != 0) et_weight.setText(String.valueOf(Math.round(u.getWeight()*10)/10f));
                        if (u.getGender() != null) tb_gender.check( (u.getGender().equals("Male")) ? R.id.bt_male : R.id.bt_female );
                    }
                    dialog.dismiss();
                    main.show();
                }else{
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Something is wrong. I can feel it!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (b) {
            getActivity().findViewById(R.id.iv_cross).setVisibility(View.GONE);
            mRoot.findViewById(R.id.ll_config).setVisibility(View.INVISIBLE);
            mRoot.findViewById(R.id.rv_log).setVisibility(View.INVISIBLE);
            mRoot.findViewById(R.id.bt_log).setVisibility(View.INVISIBLE);
            mRoot.findViewById(R.id.tv_ent_items).setVisibility(View.INVISIBLE);
            mRoot.findViewById(R.id.rv_add).setVisibility(View.VISIBLE);
            exercises = getResources().getStringArray(R.array.Exercises);
            for (String e : exercises) {
                mFoods.add(new Food(e, "Exercise"));
            }
            mSearchAdapter.notifyDataSetChanged();
        } else {
            getActivity().findViewById(R.id.iv_cross).setVisibility(View.VISIBLE);
            mRoot.findViewById(R.id.ll_config).setVisibility(View.VISIBLE);
            mRoot.findViewById(R.id.rv_log).setVisibility(View.VISIBLE);
            mRoot.findViewById(R.id.bt_log).setVisibility(View.VISIBLE);
            updateUiVisibility();
            mRoot.findViewById(R.id.rv_add).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        hideKeyboard();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mFoods.clear();
        for (String e : exercises) {
            if (e.toLowerCase().contains(newText.toLowerCase())) mFoods.add(new Food(e, "Exercise"));
        }
        mSearchAdapter.notifyDataSetChanged();
        return true;
    }

    @Override
    public void onItemClick(View view, final int position) {
        sv_add.clearFocus();
        LayoutInflater inflater = getLayoutInflater();
        final View v = inflater.inflate(R.layout.item_details, null);
        v.findViewById(R.id.ll_measure).setVisibility(View.GONE);
        ((TextView) v.findViewById(R.id.tv_qty)).setText(R.string.duration);
        ((TextInputLayout) v.findViewById(R.id.til_qty)).setHintEnabled(true);
        ((EditText) v.findViewById(R.id.et_qty)).setHint("Min");
        new AlertDialog.Builder(getActivity())
                .setTitle("Add Details")
                .setView(v)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String s = ((EditText) v.findViewById(R.id.et_qty)).getText().toString();
                        s += " minutes ";
                        s += mFoods.get(position).getName();
                        utils = new NutriUtils(getContext(), ExerciseFragment.this);
                        utils.execute("naturalExercise", s);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getActivity().getCurrentFocus();
        if (view == null) view = new View(getActivity());
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onItemLongClick(View view, final int position) {
        new AlertDialog.Builder(getActivity())
                .setTitle(mLoggedFoods.get(position).getName())
                .setMessage("Do you want to delete this item?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mLoggedFoods.remove(position);
                        updateUiVisibility();
                        mLogAdapter.updateItems(mLoggedFoods);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void processFinish(List<Food> output, String opt) {
        mLoggedFoods.addAll(output);
        mLogAdapter.notifyDataSetChanged();
        updateUiVisibility();
    }

    private void updateUiVisibility() {
        if (mLogAdapter.getItemCount() != 0) {
            mRoot.findViewById(R.id.tv_del).setVisibility(View.VISIBLE);
            mRoot.findViewById(R.id.tv_ent_items).setVisibility(View.INVISIBLE);
        }
        else{
            mRoot.findViewById(R.id.tv_del).setVisibility(View.INVISIBLE);
            mRoot.findViewById(R.id.tv_ent_items).setVisibility(View.VISIBLE);
        }
    }
}
