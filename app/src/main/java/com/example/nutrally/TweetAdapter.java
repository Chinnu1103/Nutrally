package com.example.nutrally;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {
    private List<Tweet> mTweets;
    private Context mContext;
    private boolean mine;
    private HashMap<Long, List<String>> clap_map;

    public TweetAdapter(List<Tweet> tweets, Context context, boolean mine) {
        clap_map = new HashMap<>();
        mTweets = tweets;
        mContext = context;
        this.mine = mine;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.tweets_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Tweet tweet = mTweets.get(position);
        final String usr = mContext.getSharedPreferences("User", MODE_PRIVATE).getString("twitter_user_name", "");
        if (mine && !tweet.user.screenName.equals(usr)){
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return;
        }else{
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        holder.tv_user_text.setText(tweet.text.substring(tweet.displayTextRange.get(0), tweet.displayTextRange.get(1)));
        holder.tv_name.setText(tweet.user.name);
        holder.tv_user_name.setText("@" + tweet.user.screenName);

        if (clap_map.get(tweet.id) != null){
            holder.bt_claps.setText(String.valueOf(clap_map.get(tweet.id).size()));
            if(clap_map.get(tweet.id).contains(usr)) holder.bt_claps.setIconTintResource(R.color.colorPrimary);
            else holder.bt_claps.setIconTintResource(R.color.textColor);
        }else{
            FirebaseFirestore.getInstance().collection("Tweets").document(String.valueOf(tweet.id)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        clap_map.put(tweet.id, (List<String>) task.getResult().get("claps"));
                        holder.bt_claps.setText(String.valueOf(clap_map.get(tweet.id).size()));
                        if(clap_map.get(tweet.id).contains(usr)) holder.bt_claps.setIconTintResource(R.color.colorPrimary);
                        else holder.bt_claps.setIconTintResource(R.color.textColor);
                    }
                }
            });
        }

        holder.bt_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String url = tweet.text.substring(tweet.displayTextRange.get(0), tweet.displayTextRange.get(1));
                String url = String.format(Locale.getDefault(), "https://twitter.com/%s/status/%d", tweet.user.screenName, tweet.id);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, url);
                intent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(intent, null);
                mContext.startActivity(shareIntent);
            }
        });

        holder.bt_claps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TwitterCore.getInstance().getSessionManager().getActiveSession() == null){
                    Toast.makeText(mContext, "Please login to like tweets", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (holder.bt_claps.getIconTint().getDefaultColor() == mContext.getColor(R.color.textColor)){
                    holder.bt_claps.setText(String.valueOf(Integer.parseInt(holder.bt_claps.getText().toString()) + 1));
                    holder.bt_claps.setIconTintResource(R.color.colorPrimary);
                    List<String> claps = clap_map.get(tweet.id);
                    claps.add(usr);
                }else{
                    holder.bt_claps.setText(String.valueOf(Integer.parseInt(holder.bt_claps.getText().toString()) - 1));
                    holder.bt_claps.setIconTintResource(R.color.textColor);
                    List<String> claps = clap_map.get(tweet.id);
                    claps.remove(usr);
                }
                FirebaseFirestore.getInstance().collection("Tweets").document(String.valueOf(tweet.id)).update("claps", clap_map.get(tweet.id));
            }
        });

        final SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss +0000 yyyy", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date  = format.parse(tweet.createdAt);
            if (DateUtils.isToday(date.getTime())){
                SimpleDateFormat today = new SimpleDateFormat("hh:mm aa");
                today.setTimeZone(TimeZone.getDefault());
                holder.tv_post_date.setText(today.format(date));
            }else{
                SimpleDateFormat past = new SimpleDateFormat("dd MMM yy");
                past.setTimeZone(TimeZone.getDefault());
                holder.tv_post_date.setText(past.format(date));
            }
        } catch (ParseException e) {
            holder.tv_post_date.setText(tweet.createdAt.substring(4, 10) + " " + tweet.createdAt.substring(28));
            e.printStackTrace();
        }

        if (tweet.entities.media.size() == 1){
            holder.iv_user_image.setVisibility(View.VISIBLE);
            Glide.with(mContext)
                    .load(tweet.entities.media.get(0).mediaUrlHttps)
                    .into(holder.iv_user_image);
        }
        else holder.iv_user_image.setVisibility(View.GONE);
        Glide.with(mContext)
                .load(tweet.user.profileImageUrlHttps)
                .into(holder.iv_user_dp);

        if (usr.equals(tweet.user.screenName)) holder.iv_more.setVisibility(View.VISIBLE);
        else holder.iv_more.setVisibility(View.INVISIBLE);
        holder.iv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, holder.iv_more);
                popupMenu.inflate(R.menu.tweet_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        TwitterApiClient client = TwitterCore.getInstance().getApiClient();
                        client.getStatusesService().destroy(tweet.id, true).enqueue(new Callback<Tweet>() {
                            @Override
                            public void success(Result<Tweet> result) {
                                mTweets.remove(position);
                                notifyDataSetChanged();
                                FirebaseFirestore.getInstance().collection("Tweets").document(String.valueOf(tweet.id)).delete();
                            }

                            @Override
                            public void failure(TwitterException exception) {
                                exception.printStackTrace();
                                Toast.makeText(mContext, "Cannot delete tweet", Toast.LENGTH_SHORT).show();
                            }
                        });

                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_user_dp, iv_user_image, iv_more;
        TextView tv_user_name, tv_user_text, tv_name, tv_post_date;
        MaterialButton bt_claps, bt_share;

        public ViewHolder(View view) {
            super(view);
            iv_user_dp = view.findViewById(R.id.iv_user_dp);
            iv_user_image = view.findViewById(R.id.iv_user_image);
            tv_user_name = view.findViewById(R.id.tv_user_name);
            tv_user_text = view.findViewById(R.id.tv_user_text);
            tv_name = view.findViewById(R.id.tv_name);
            tv_post_date = view.findViewById(R.id.tv_post_date);
            iv_more = view.findViewById(R.id.iv_more);
            bt_claps = view.findViewById(R.id.bt_claps);
            bt_share = view.findViewById(R.id.bt_share);
        }
    }
}
