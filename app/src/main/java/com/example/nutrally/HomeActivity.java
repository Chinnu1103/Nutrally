package com.example.nutrally;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.nutrally.ui.Home.HomeFragmentDirections;
import com.example.nutrally.ui.Social.SocialFragmentDirections;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    TwitterAuthClient mAuthClient;
    NavController mNavController;
    boolean mine;
    public static Calendar mDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig("SKTXpOKBEb0Cr3qYWXgkQgq5R", "fqwJCMcTss0sonLv4nUdQ64WfLwmcVcbtIan28rDqP0MtKPJ98"))
                .debug(true)
                .build();
        Twitter.initialize(config);
        setContentView(R.layout.activity_home);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, mNavController);

        final TextView tw_unm = findViewById(R.id.tv_tw_name);
        final TextView tw_id = findViewById(R.id.tv_tw_unm);
        final MaterialCardView cv_user = findViewById(R.id.cv_twitter_user);

        final FloatingActionMenu fab = findViewById(R.id.fab);
        fab.setClosedOnTouchOutside(true);
        final FloatingActionButton fab_meal = findViewById(R.id.fab_meal);
        final FloatingActionButton fab_exs = findViewById(R.id.fab_exs);
        fab_meal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNavController.navigate(R.id.action_nav_home_to_addFragment);
            }
        });
        fab_exs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNavController.navigate(R.id.action_nav_home_to_exerciseFragment);
            }
        });

        getTwitterUser(cv_user, tw_unm, tw_id);
        mine = false;
        final LinearLayout ll_date = findViewById(R.id.ll_date), ll_profile = findViewById(R.id.ll_profile), ll_search = findViewById(R.id.ll_search);
        cv_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TwitterCore.getInstance().getSessionManager().getActiveSession() == null) {
                    mAuthClient = new TwitterAuthClient();
                    mAuthClient.authorize(HomeActivity.this, new Callback<TwitterSession>() {
                        @Override
                        public void success(Result<TwitterSession> result) {
                            mNavController.navigate(R.id.action_nav_social_self);
                            Toast.makeText(HomeActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            getTwitterUser(cv_user, tw_unm, tw_id);
                            mAuthClient = null;
                        }

                        @Override
                        public void failure(TwitterException exception) {
                            exception.printStackTrace();
                        }
                    });
                } else {
                    View v = getLayoutInflater().inflate(R.layout.twitter_options, null);
                    final AlertDialog dialog = new AlertDialog.Builder(HomeActivity.this)
                            .setView(v)
                            .show();
                    if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    final Button bt_tweets = v.findViewById(R.id.bt_tweets);
                    Button bt_log_out = v.findViewById(R.id.bt_log_out);
                    bt_log_out.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            TwitterCore.getInstance().getSessionManager().clearActiveSession();
                            cv_user.getChildAt(1).setVisibility(View.VISIBLE);
                            ImageView iv_user = (ImageView) cv_user.getChildAt(0);
                            iv_user.setImageResource(R.drawable.twitter);
                            tw_unm.setText("");
                            tw_id.setText("");
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Successfully Logged Out", Toast.LENGTH_SHORT).show();
                            SharedPreferences preferences = getSharedPreferences("User", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.remove("twitter_name");
                            editor.remove("twitter_user_name");
                            editor.remove("twitter_dp");
                            editor.apply();
                            mNavController.navigate(R.id.action_nav_social_self);
                        }
                    });
                    bt_tweets.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mine = true;
                            findViewById(R.id.bt_tweet).setVisibility(View.GONE);
                            SocialFragmentDirections.ActionNavSocialSelf action = SocialFragmentDirections.actionNavSocialSelf();
                            action.setMineTweets(true);
                            mNavController.navigate(action);
                            findViewById(R.id.sv_log).setVisibility(View.INVISIBLE);
                            ll_search.setVisibility(View.VISIBLE);
                            ll_profile.setVisibility(View.INVISIBLE);
                            fab.hideMenuButton(true);
                            dialog.dismiss();
                        }
                    });
                }
            }
        });

        mNavController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if (destination.getId() == R.id.addFragment || destination.getId() == R.id.exerciseFragment){
                    fadeOut(ll_date);
                    fadeIn(ll_search);
                    fab.hideMenuButton(false);
                }else if (destination.getId() == R.id.nav_home) {
                    fadeOut(ll_profile);
                    fadeOut(ll_search);
                    fadeIn(ll_date);
                    findViewById(R.id.bt_tweet).setVisibility(View.GONE);
                    findViewById(R.id.sv_log).setVisibility(View.VISIBLE);
                    fab.hideMenuButton(true);
                    fab.showMenuButton(true);
                    fab.setOnMenuButtonClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (fab.isOpened()) fab.close(false);
                            else fab.open(true);
                        }
                    });
                } else if (destination.getId() == R.id.nav_social){
                    fab.hideMenuButton(true);
                    fab.showMenuButton(true);
                    if (!mine){
                        findViewById(R.id.sv_log).setVisibility(View.INVISIBLE);
                        findViewById(R.id.bt_tweet).setVisibility(View.VISIBLE);
                        ll_search.setVisibility(View.INVISIBLE);
                        fadeOut(ll_date);
                        fadeIn(ll_profile);
                        fab.setOnMenuButtonClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (TwitterCore.getInstance().getSessionManager().getActiveSession() == null){
                                    Toast.makeText(getApplicationContext(), "Login to twitter to post tweets", Toast.LENGTH_SHORT).show();
                                }else mNavController.navigate(R.id.action_nav_social_to_tweetFragment);
                            }
                        });
                    }
                } else if (destination.getId() == R.id.tweetFragment) {
                    fab.hideMenuButton(false);
                    fadeOut(ll_profile);
                    fadeIn(ll_search);
                } else {
                    fadeOut(ll_search);
                    fadeIn(ll_date);
                    fadeOut(ll_profile);
                    fab.hideMenuButton(true);
                }
            }
        });

        final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM", Locale.getDefault());

        ImageView iv_left = findViewById(R.id.iv_left);
        ImageView iv_right = findViewById(R.id.iv_right);
        final TextView tv_date = findViewById(R.id.tv_date);
        tv_date.setText(String.format("Today,%s", format.format(mDate.getTime()).split(",")[1]));
        iv_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDate.add(Calendar.DATE, -1);
                if (DateUtils.isToday(mDate.getTimeInMillis())) tv_date.setText(String.format("Today,%s", format.format(mDate.getTime()).split(",")[1]));
                else tv_date.setText(format.format(mDate.getTime()));
                HomeFragmentDirections.ActionNavHomeSelf action = HomeFragmentDirections.actionNavHomeSelf();
                mNavController.navigate(action);
            }
        });

        iv_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDate.add(Calendar.DATE, 1);
                if (DateUtils.isToday(mDate.getTimeInMillis())) tv_date.setText(String.format("Today,%s", format.format(mDate.getTime()).split(",")[1]));
                else tv_date.setText(format.format(mDate.getTime()));
                HomeFragmentDirections.ActionNavHomeSelf action = HomeFragmentDirections.actionNavHomeSelf();
                mNavController.navigate(action);
            }
        });

        findViewById(R.id.iv_cross).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mine = false;
                mNavController.navigateUp();
            }
        });
    }

    void fadeIn(final View view){
        if (view.getVisibility() == View.VISIBLE) return;
        final Animation fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fade_in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setAlpha(0.0f);
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setAlpha(1.0f);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(fade_in);
    }

    void fadeOut(final View view){
        if (view.getVisibility() != View.VISIBLE) return;
        final Animation fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fade_in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(fade_in);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(mAuthClient != null) mAuthClient.onActivityResult(requestCode, resultCode, data);
    }

    void getTwitterUser(final MaterialCardView iv_dp, final TextView tw_unm, final TextView tw_id) {
        final SharedPreferences sp = getSharedPreferences("User", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();
        TwitterApiClient client = TwitterCore.getInstance().getApiClient();
        Call<User> user = client.getAccountService().verifyCredentials(true, true, true);
        user.enqueue(new Callback<User>() {
            @Override
            public void success(Result<User> result) {
                tw_unm.setText(result.data.name);
                tw_id.setText(String.format("@%s", result.data.screenName));
                Glide.with(HomeActivity.this)
                        .asBitmap()
                        .load(result.data.profileImageUrlHttps)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                iv_dp.getChildAt(1).setVisibility(View.GONE);
                                ImageView iv_user = (ImageView) iv_dp.getChildAt(0);
                                iv_user.setImageBitmap(resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
                editor.putString("twitter_name", result.data.name);
                editor.putString("twitter_user_name", result.data.screenName);
                editor.putString("twitter_dp", result.data.profileImageUrlHttps);
                editor.apply();
            }

            @Override
            public void failure(TwitterException exception) {
                exception.printStackTrace();
            }
        });
    }

}
