package com.example.nutrally.ui.Home;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutrally.AddAdapter;
import com.example.nutrally.Food;
import com.example.nutrally.FoodAdapter;
import com.example.nutrally.HomeActivity;
import com.example.nutrally.Meal;
import com.example.nutrally.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment implements AddAdapter.ItemLongClickListener {
    private static final String TAG = "HomeFragment";
    private final Map<String, Food> map = new HashMap<>();
    private static ArrayList<Food> breakFast_items, lunch_items, snack_items, dinner_items, exercise_items;
    private FoodAdapter mAdapter;
    private CollectionReference ref;
    public static boolean bf_exp = false, ln_exp = false, sn_exp = false, dn_exp = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final SharedPreferences sp = Objects.requireNonNull(getActivity()).getSharedPreferences("User", MODE_PRIVATE);

        final View root = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView rv_home = root.findViewById(R.id.rv_home);
        rv_home.setLayoutManager(new LinearLayoutManager(getContext()));

        final HomeFragmentArgs args = HomeFragmentArgs.fromBundle(getArguments());

        ArrayList<Meal> meals = new ArrayList<>();
        breakFast_items = new ArrayList<>();
        lunch_items = new ArrayList<>();
        snack_items = new ArrayList<>();
        dinner_items = new ArrayList<>();
        exercise_items = new ArrayList<>();

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(View.inflate(getActivity(), R.layout.loading_layout, null))
                .setCancelable(false)
                .show();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ref = db.collection("Users").document(sp.getString("Email", "").replace('.', ',')).collection(getDate());

        meals.add(new Meal("Breakfast", breakFast_items));
        meals.add(new Meal("Lunch", lunch_items));
        meals.add(new Meal("Snacks", snack_items));
        meals.add(new Meal("Dinner", dinner_items));
        meals.add(new Meal("Exercise", exercise_items));

        mAdapter = new FoodAdapter(meals);
        mAdapter.setLongClickListener(this);
        rv_home.setAdapter(mAdapter);

        ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        map.clear();
                        map.putAll(Food.convertMap(document.getData(), document.getId()));
                        if (document.getId().equals("Breakfast")) breakFast_items.addAll(map.values());
                        else if (document.getId().equals("Lunch")) lunch_items.addAll(map.values());
                        else if (document.getId().equals("Snacks")) snack_items.addAll(map.values());
                        else if (document.getId().equals("Dinner")) dinner_items.addAll(map.values());
                        else exercise_items.addAll(map.values());
                        map.clear();
                    }
                }

                if (args.getMealName() != null && args.getMealValues() != null && args.getMealValues().length > 0) {
                    switch (args.getMealName()) {
                        case "Breakfast":
                            breakFast_items.addAll(Arrays.asList(args.getMealValues()));
                            for (Food food : breakFast_items) {
                                map.put(food.getName(), food);
                            }
                            break;
                        case "Lunch":
                            lunch_items.addAll(Arrays.asList(args.getMealValues()));
                            for (Food food : lunch_items) {
                                map.put(food.getName(), food);
                            }
                            break;
                        case "Snacks":
                            snack_items.addAll(Arrays.asList(args.getMealValues()));
                            for (Food food : snack_items) {
                                map.put(food.getName(), food);
                            }
                            break;
                        case "Dinner":
                            dinner_items.addAll(Arrays.asList(args.getMealValues()));
                            for (Food food : dinner_items) {
                                map.put(food.getName(), food);
                            }
                            break;
                        default:
                            exercise_items.addAll(Arrays.asList(args.getMealValues()));
                            for (Food food : exercise_items) {
                                map.put(food.getName(), food);
                            }
                            break;
                    }
                    ref.document(args.getMealName()).set(map);
                }

                mAdapter.notifyDataSetChanged();

                dialog.dismiss();
            }
        });

        return root;
    }

    @Override
    public void onItemLongClick(View view, final int position) {
        new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setTitle(((TextView) view.findViewById(R.id.tv_name)).getText().toString())
                .setMessage("Do you want to delete this item?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        map.clear();
                        int items = 0;
                        if (bf_exp) items += breakFast_items.size();
                        if (position <= items) {
                            breakFast_items.remove(position - items + breakFast_items.size() - 1);
                            for (Food food : breakFast_items) {
                                map.put(food.getName(), food);
                            }
                            ref.document("Breakfast").set(map);
                        } else {
                            items += 1;
                            if (ln_exp) items += lunch_items.size();
                            if (position <= items) {
                                lunch_items.remove(position - items + lunch_items.size() - 1);
                                for (Food food : lunch_items) {
                                    map.put(food.getName(), food);
                                }
                                ref.document("Lunch").set(map);
                            } else {
                                items += 1;
                                if (sn_exp) items += snack_items.size();
                                if (position <= items) {
                                    snack_items.remove(position - items + snack_items.size() - 1);
                                    for (Food food : snack_items) {
                                        map.put(food.getName(), food);
                                    }
                                    ref.document("Snacks").set(map);
                                } else {
                                    items += 1;
                                    if (dn_exp) items += dinner_items.size();
                                    if (position <= items) {
                                        dinner_items.remove(position - items + dinner_items.size() - 1);
                                        for (Food food : dinner_items) {
                                            map.put(food.getName(), food);
                                        }
                                        ref.document("Dinner").set(map);
                                    } else {
                                        exercise_items.remove(position - items - 2);
                                        for (Food food : exercise_items) {
                                            map.put(food.getName(), food);
                                        }
                                        ref.document("Exercise").set(map);
                                    }
                                }
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private String getDate() {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return format.format(HomeActivity.mDate.getTime());
    }

    @Override
    public void onDestroyView() {
        if (getArguments() != null) getArguments().clear();
        super.onDestroyView();
    }
}