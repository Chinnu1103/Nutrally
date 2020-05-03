package com.example.nutrally.ui.Social;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutrally.R;
import com.example.nutrally.TweetAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetcomposer.ComposerActivity;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import io.opencensus.internal.StringUtils;

import static android.content.Context.MODE_PRIVATE;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class SocialFragment extends Fragment {

    private SocialViewModel mSocialViewModel;
    private ArrayList<Tweet> mTweets = new ArrayList<>();
    private TweetAdapter mAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mSocialViewModel = new ViewModelProvider(this).get(SocialViewModel.class);
        final Observer<Integer> likesObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                //Update UI
            }
        };
        mSocialViewModel.getCurrentLikes().observe(getViewLifecycleOwner(), likesObserver);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView rv_social = root.findViewById(R.id.rv_home);
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
//        rv_social.addItemDecoration(decoration);
        rv_social.setLayoutManager(new LinearLayoutManager(getContext()));

        final SocialFragmentArgs args = SocialFragmentArgs.fromBundle(getArguments());

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(View.inflate(getActivity(), R.layout.loading_layout, null))
                .setCancelable(false)
                .show();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final long tweet_id = args.getUserTweetId();
        mAdapter = new TweetAdapter(mTweets, getContext(), args.getMineTweets());
        rv_social.setAdapter(mAdapter);

        final Comparator<Tweet> comparator = new Comparator<Tweet>() {
            @Override
            public int compare(Tweet tweet, Tweet t1) {
                final SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss +0000 yyyy", Locale.getDefault());
                int i = 0;
                try{
                    i = format.parse(t1.createdAt).compareTo(format.parse(tweet.createdAt));
                }catch (Exception e){
                    e.printStackTrace();
                }
                return i;
            }
        };

        mTweets.clear();
        final SharedPreferences sp = getActivity().getSharedPreferences("User", MODE_PRIVATE);
        final CollectionReference db = FirebaseFirestore.getInstance().collection("Tweets");
        db.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                StatusesService statusesService = new TwitterApiClient().getStatusesService();
                if (task.isSuccessful()) {
                    final List<String> tweets = new ArrayList<>();
                    for (final DocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        tweets.add(document.getString("id"));
                    }
                    if (tweet_id != -1){
                        tweets.add(0, String.valueOf(tweet_id));
                        HashMap<String, Object> map = new HashMap<>();
                        List<String> temp = new ArrayList<>();
                        map.put("id", String.valueOf(tweet_id));
                        map.put("claps", temp);
                        db.document(String.valueOf(tweet_id)).set(map);
                    }
                    if (tweets.size() > 0){
                        statusesService.lookup(TextUtils.join(",", tweets), true, false, false).enqueue(new Callback<List<Tweet>>() {
                            @Override
                            public void success(Result<List<Tweet>> result) {
                                mTweets.addAll(result.data);
                                Collections.sort(mTweets, comparator);
                                mAdapter.notifyDataSetChanged();
                                dialog.dismiss();
                            }

                            @Override
                            public void failure(TwitterException exception) {
                                dialog.dismiss();
                                exception.printStackTrace();
                            }
                        });
                    } else dialog.dismiss();
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        if (getArguments() != null) getArguments().clear();
        super.onDestroyView();
    }
}