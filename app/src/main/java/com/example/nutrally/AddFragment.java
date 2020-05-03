package com.example.nutrally;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AddFragment extends Fragment implements AddAdapter.ItemClickListener,
        SearchView.OnQueryTextListener, View.OnFocusChangeListener, NutriUtils.AsyncResponse,
        AddAdapter.ItemLongClickListener {

    private static final String TAG = "AddFragment";

    private ArrayList<Food> mFoods, mLoggedFoods;
    private AddAdapter mSearchAdapter;
    private AddAdapter mLogAdapter;
    private NutriUtils utils;
    private SearchView sv_add;
    private View mRoot;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_add, container, false);
        mRoot = root;

        mFoods = new ArrayList<>();
        mLoggedFoods = new ArrayList<>();

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

        RecyclerView rv_log = root.findViewById(R.id.rv_log);
        rv_log.setLayoutManager(new LinearLayoutManager(getContext()));
        mLogAdapter = new AddAdapter(getContext(), mLoggedFoods);
        mLogAdapter.setLongClickListener(this);
        rv_log.addItemDecoration(decoration);
        rv_log.setAdapter(mLogAdapter);

        Button bt_add = root.findViewById(R.id.bt_add);
        Button bt_log = root.findViewById(R.id.bt_log);
        final Spinner sp_add = root.findViewById(R.id.sp_add);
        final EditText et_add = root.findViewById(R.id.et_add);
        bt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                utils = new NutriUtils(getContext(), AddFragment.this);
                utils.execute("naturalQuery", et_add.getText().toString());
                hideKeyboard();
                et_add.clearFocus();
            }
        });

        bt_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i=0; i<mLoggedFoods.size(); ++i){
                    mLoggedFoods.get(i).setMeal(sp_add.getSelectedItem().toString());
                }
                Food[] foods = new Food[mLoggedFoods.size()];
                AddFragmentDirections.ActionAddFragmentToNavHome action =
                        AddFragmentDirections.actionAddFragmentToNavHome();
                action.setMealName(sp_add.getSelectedItem().toString());
                action.setMealValues(mLoggedFoods.toArray(foods));
                Navigation.findNavController(view).navigate(action);
            }
        });

        sp_add.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                utils = new NutriUtils(getContext(), AddFragment.this);
                switch (sp_add.getSelectedItem().toString()) {
                    case "Breakfast":
                        utils.execute("instantQuery", "Eggs");
                        break;
                    case "Lunch":
                        utils.execute("instantQuery", "Rice");
                        break;
                    case "Snacks":
                        utils.execute("instantQuery", "Tea");
                        break;
                    default:
                        utils.execute("instantQuery", "Chicken");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        sv_add = getActivity().findViewById(R.id.sv_log);
        sv_add.setQueryHint(Html.fromHtml("<font color = #808080>" + "Search food here" + "</font"));
        sv_add.setOnQueryTextListener(this);
        sv_add.setOnQueryTextFocusChangeListener(this);

        return root;
    }

    @Override
    public void onItemClick(View view, final int position) {
        sv_add.clearFocus();
        LayoutInflater inflater = getLayoutInflater();
        final View v = inflater.inflate(R.layout.item_details, null);
        if (mFoods.get(position).getType() == Type.BRANDED) {
            v.findViewById(R.id.ll_measure).setVisibility(View.GONE);
            ((EditText) v.findViewById(R.id.et_qty)).setHint("1, 2, etc");
        } else {
            ((EditText) v.findViewById(R.id.et_qty)).setHint("1, 0.5, etc");
        }
        new AlertDialog.Builder(getActivity())
                .setTitle("Add Details")
                .setView(v)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mFoods.get(position).getType() == Type.COMMON){
                            String s = ((EditText) v.findViewById(R.id.et_qty)).getText().toString();
                            s += " ";
                            s += ((EditText) v.findViewById(R.id.et_measure)).getText().toString();
                            s += " ";
                            s += mFoods.get(position).getName();
                            utils = new NutriUtils(getContext(), AddFragment.this);
                            utils.execute("naturalQuery", s);
                        } else {
                            utils = new NutriUtils(getContext(), AddFragment.this);
                            utils.execute("searchItem", mFoods.get(position).getNix_id(), ((EditText) v.findViewById(R.id.et_qty)).getText().toString());
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        hideKeyboard();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        utils.cancel(true);
        utils = new NutriUtils(getContext(), this);
        utils.execute("instantQuery", newText);
        return true;
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
    public void processFinish(List<Food> output, String opt) {
        if (opt.equals("instantQuery")) {
            mFoods.clear();
            mFoods.addAll(output);
            mSearchAdapter.updateItems(output);
        } else {
            mLoggedFoods.addAll(output);
            updateUiVisibility();
            mLogAdapter.updateItems(mLoggedFoods);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getActivity().getCurrentFocus();
        if (view == null) view = new View(getActivity());
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
}
