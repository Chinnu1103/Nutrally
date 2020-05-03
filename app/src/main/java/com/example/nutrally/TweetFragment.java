package com.example.nutrally;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.Session2Command;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Media;
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Provider;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class TweetFragment extends Fragment {
    private EditText et_tweet_text;
    private ImageView iv_tweet_image, iv_cross;
    private Uri mUri = null;
    private View mRoot;
    private static int PHOTO_CODE = 1103;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_tweet, container, false);
        mRoot = root;

        et_tweet_text = root.findViewById(R.id.et_tweet_text);
        Button bt_add_chart = root.findViewById(R.id.bt_add_chart);
        Button bt_add_image = root.findViewById(R.id.bt_add_image);
        iv_cross = root.findViewById(R.id.iv_image_cross);
        iv_tweet_image = root.findViewById(R.id.iv_tweet_image);

        iv_cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iv_tweet_image.setImageResource(0);
                root.findViewById(R.id.ll_tweet_image).setVisibility(View.VISIBLE);
                root.findViewById(R.id.cv_image_cross).setVisibility(View.GONE);
            }
        });

        bt_add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(photoIntent, PHOTO_CODE);
            }
        });

        Button button = getActivity().findViewById(R.id.bt_tweet);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setView(View.inflate(getActivity(), R.layout.loading_layout, null))
                        .setCancelable(false)
                        .show();
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                TwitterApiClient client = TwitterCore.getInstance().getApiClient();
                Call<Media> media = client.getMediaService().upload(RequestBody.create(MediaType.parse("image/jpeg"), getByteArray()), null, null);
                media.enqueue(new Callback<Media>() {
                    @Override
                    public void success(Result<Media> result) {
                        tweet(dialog, result.data.mediaIdString);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        tweet(dialog, null);
                    }
                });
            }
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mUri = data.getData();
            iv_tweet_image.setImageURI(mUri);
            mRoot.findViewById(R.id.cv_image_cross).setVisibility(View.VISIBLE);
            mRoot.findViewById(R.id.ll_tweet_image).setVisibility(View.GONE);
        }
    }

    private byte[] getByteArray() {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mUri);
            bitmap = Bitmap.createScaledBitmap(bitmap, 500, 500*bitmap.getHeight()/bitmap.getWidth(), false);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int rotation = getImageRotation();
            Matrix matrix = new Matrix();
            matrix.preRotate(rotation);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            byte[] buffer = os.toByteArray();
            bitmap.recycle();
            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[1024];
    }

    private int getImageRotation(){
        ExifInterface exif = null;
        int rotation = 0;
        try {
            exif = new ExifInterface(getContext().getContentResolver().openInputStream(mUri));
            rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        }catch (IOException e){
            e.printStackTrace();
        }
        if (rotation == ExifInterface.ORIENTATION_ROTATE_90) return 90;
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_180) return 180;
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_270) return 270;
        else return 0;
    }

    private void tweet(final AlertDialog dialog, String mediaId) {
        TwitterApiClient client = TwitterCore.getInstance().getApiClient();
        StatusesService service = client.getStatusesService();
        String text = et_tweet_text.getText().toString();
        if(!text.contains("#Nutrally")){
            text = text + "\n#Nutrally";
        }
        Call<Tweet> tweet = service.update(text, null, false, null, null, null, false, false, mediaId);
        tweet.enqueue(new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                dialog.dismiss();
                TweetFragmentDirections.ActionTweetFragmentToNavSocial action =
                        TweetFragmentDirections.actionTweetFragmentToNavSocial();
                action.setUserTweetId(result.data.id);
                Navigation.findNavController(getView()).navigate(action);
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(getContext(), "Tweet Failed", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

}
