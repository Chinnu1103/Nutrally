package com.example.nutrally;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class IntroActivity extends AppCompatActivity {
    private static final String TAG = "IntroActivity";
    ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        final SharedPreferences sp = getSharedPreferences("User", MODE_PRIVATE);

        Intent intent = getIntent();
        String name = intent.getStringExtra("Name");

        TabLayout tabLayout = findViewById(R.id.tl_intro);
        final TextView tv_heading = findViewById(R.id.tv_heading);
        mPager = findViewById(R.id.vp_intro);
        mPager.setOffscreenPageLimit(3);
        final IntroViewPagerAdapter adapter = new IntroViewPagerAdapter(this, name);
        mPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(mPager);

        final Button bt_next = findViewById(R.id.bt_next);
        Button bt_skip = findViewById(R.id.bt_skip);
        bt_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPager.getCurrentItem() == 2) {
                    AlertDialog dialog = new AlertDialog.Builder(IntroActivity.this)
                            .setView(getLayoutInflater().inflate(R.layout.loading_layout, null))
                            .setCancelable(false)
                            .show();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    User user = adapter.getUser();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("Users").document(sp.getString("Email", ".").replace('.', ',')).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Log.d(TAG, "onComplete: " + task.getException());
                            } else {
                                startActivity(new Intent(IntroActivity.this, HomeActivity.class));
                                finish();
                            }
                        }
                    });
                } else mPager.setCurrentItem((mPager.getCurrentItem() + 1) % 3);
            }
        });

        bt_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dialog = new AlertDialog.Builder(IntroActivity.this)
                        .setView(getLayoutInflater().inflate(R.layout.loading_layout, null))
                        .setCancelable(false)
                        .create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                new AlertDialog.Builder(IntroActivity.this)
                        .setTitle("Confirm Action")
                        .setMessage("Are you sure you want to skip?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog.show();
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                User user = new User();
                                db.collection("Users").document(sp.getString("Email", ".").replace('.', ',')).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            Log.d(TAG, "onComplete: " + task.getException());
                                        } else {
                                            startActivity(new Intent(IntroActivity.this, HomeActivity.class));
                                            finish();
                                            dialog.dismiss();
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (mPager.getCurrentItem()){
                    case 0:
                        tv_heading.setText("Profile");
                        bt_next.setText("Next");
                        break;
                    case 1:
                        tv_heading.setText("Details");
                        bt_next.setText("Next");
                        break;
                    default:
                        tv_heading.setText("Goals");
                        bt_next.setText("Done");
                }
                if (mPager.getCurrentItem() == 2){

                }
                else bt_next.setText("Next");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
}
