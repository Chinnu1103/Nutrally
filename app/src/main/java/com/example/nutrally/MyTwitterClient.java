package com.example.nutrally;

import android.os.Parcel;
import android.os.Parcelable;

import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Card;
import com.twitter.sdk.android.core.models.Coordinates;
import com.twitter.sdk.android.core.models.Place;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetEntities;
import com.twitter.sdk.android.core.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class MyTwitterClient extends TwitterApiClient {
    public MyTwitterClient(TwitterSession session){
        super(session);
    }
    /**
     * @return {@link CustomService} to access TwitterApi
     */
    public CustomService getCustomService() {
        return getService(CustomService.class);
    }

    interface CustomService {

        /**
         * @param id tweet id
         * @param count no. of tweets
         */
        @GET("/1.1/statuses/retweets/{ids}.json")
        Call<List<Tweet>> retweeters(@Path (value = "ids") Long ids, @Query("id") Long id, @Query("count") Integer count);
    }
}
