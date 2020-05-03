package com.example.nutrally;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1103;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mFirebaseAuth;
    private EditText et_eid;
    private EditText et_pwd;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_login);
            initViews(savedInstanceState);
            updateViews();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mFirebaseAuth = FirebaseAuth.getInstance();
                if (mFirebaseAuth.getCurrentUser() != null) {
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                } else {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        setContentView(R.layout.activity_login);
                        initViews(savedInstanceState);
                        final Guideline gl = findViewById(R.id.gl);
                        gl.setGuidelinePercent(1.0f);
                        ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 0.75f).setDuration(300);
                        animator.setInterpolator(new AccelerateDecelerateInterpolator());
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                gl.setGuidelinePercent((float) valueAnimator.getAnimatedValue());
                                if (valueAnimator.getAnimatedFraction() == 1.0f) {
                                    updateViews();
                                }
                            }
                        });
                        animator.start();
                    }

                    findViewById(R.id.bt_su).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(et_eid.getText().toString().isEmpty() || et_pwd.getText().toString().isEmpty()){
                                Toast.makeText(getApplicationContext(), "Please enter all your credentials", Toast.LENGTH_SHORT).show();
                            }else{
                                mFirebaseAuth.createUserWithEmailAndPassword(et_eid.getText().toString(), et_pwd.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful())
                                                    checkNewUser(((EditText) findViewById(R.id.et_eid)).getText().toString(), "");
                                                else
                                                    Toast.makeText(LoginActivity.this, "Sign Up Failed", Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        }
                    });

                    findViewById(R.id.bt_si).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(et_eid.getText().toString().isEmpty() || et_pwd.getText().toString().isEmpty()){
                                Toast.makeText(getApplicationContext(), "Please enter all your credentials", Toast.LENGTH_SHORT).show();
                            }else{
                                mFirebaseAuth.signInWithEmailAndPassword(et_eid.getText().toString(), et_pwd.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful())
                                                    checkNewUser(et_eid.getText().toString(), "");
                                                else
                                                    Toast.makeText(LoginActivity.this, "Sign In Failed", Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        }
                    });

                    findViewById(R.id.bt_gs).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(getString(R.string.default_web_client_id))
                                    .requestEmail()
                                    .build();
                            mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);
                            Intent intent = mGoogleSignInClient.getSignInIntent();
                            startActivityForResult(intent, RC_SIGN_IN);
                        }
                    });
                }
            }
        }, 500);

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        String email = et_eid.getText().toString();
        String password = et_pwd.getText().toString();
        outState.putString("Email", email);
        outState.putString("Password", password);
    }

    void initViews(Bundle inState) {
        et_eid = findViewById(R.id.et_eid);
        et_pwd = findViewById(R.id.et_pwd);
        if (inState != null) {
            et_eid.setText(inState.getString("Email"));
            et_pwd.setText(inState.getString("Password"));
        }
    }

    void updateViews() {
        findViewById(R.id.til_eid).setVisibility(View.VISIBLE);
        findViewById(R.id.til_pwd).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_buttons).setVisibility(View.VISIBLE);
        findViewById(R.id.bt_gs).setVisibility(View.VISIBLE);
        findViewById(R.id.v_left).setVisibility(View.VISIBLE);
        findViewById(R.id.v_right).setVisibility(View.VISIBLE);
        findViewById(R.id.tv_or).setVisibility(View.VISIBLE);
        findViewById(R.id.tv_f_pwd).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) fireBaseAuthWithGoogle(account);
                else Toast.makeText(LoginActivity.this, "Sign In Failed", Toast.LENGTH_LONG).show();
            } catch (ApiException e) {
                Toast.makeText(LoginActivity.this, "Sign In Failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void fireBaseAuthWithGoogle(final GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) checkNewUser(acct.getEmail(), acct.getDisplayName());
                        else Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkNewUser(String email, final String name) {
        AlertDialog dialog = new AlertDialog.Builder(LoginActivity.this)
                .setView(R.layout.loading_layout)
                .setCancelable(false)
                .show();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        SharedPreferences.Editor editor = getSharedPreferences("User", MODE_PRIVATE).edit();
        editor.putString("Email", email);
        editor.putString("Name", name);
        editor.apply();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(email.replace('.', ',')).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                } else {
                    final Intent intent = new Intent(LoginActivity.this, IntroActivity.class);
                    intent.putExtra("Name", name);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

}
