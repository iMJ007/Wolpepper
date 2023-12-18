package com.eclectik.wolpepper.activities;

import static com.eclectik.wolpepper.utils.ConstantValues.HeaderKeys.RATE_LIMIT_REMAINING_HEADER_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_FULL_IMAGE_URL_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_HTML_LINK_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_LINKS_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_LOCATION_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_NAME_OF_USER_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_REGULAR_IMAGE_URL_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USERNAME_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_BIO_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_FOLLOWERS_COUNT_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_FOLLOWING_COUNT_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_PROFILE_PIC_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_PROFILE_PIC_LARGE_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_TOTAL_COLLECTIONS;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_TOTAL_LIKES;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_TOTAL_PHOTOS;
import static com.eclectik.wolpepper.utils.ConstantValues.ProfileFragTags.COLLECTION_FRAG_TAG;
import static com.eclectik.wolpepper.utils.ConstantValues.ProfileFragTags.LIKE_FRAG_TAG;
import static com.eclectik.wolpepper.utils.ConstantValues.ProfileFragTags.PHOTOS_FRAG_TAG;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eclectik.wolpepper.GlideApp;
import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.adapters.SimpleFragmentPagerAdapter;
import com.eclectik.wolpepper.listenerInterfaces.AsyncDataLoadCompletionListener;
import com.eclectik.wolpepper.utils.NetworkUtils;
import com.eclectik.wolpepper.utils.UtilityMethods;
import com.eclectik.wolpepper.wolpepper;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.tapadoo.alerter.Alerter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class ProfileMainActivity extends AppCompatActivity {

    private TextView followersCountTv, followingCountTv, totalPhotosTv, nameOfUserTv, userLocationTv, userBioTv;

    private LinearLayout profileLinkView;

    private ShapeableImageView profileImage;

    private ImageView headerImage;

    private String nameOfUser, userLocation, bio, profilePicUrl, headerImageUrl, userName;

    private int totalLikes = -1, totalPhotos = -1, totalCollections = -1, followersCount = 0, followingCount = 0;

    private String APP_ID;

    private String profileUrl;

    private boolean isRateLimitReached = false;

    private AsyncDataLoadCompletionListener asyncTotalLikeDataLoadCompletionListener, asyncTotalCollectionDataLoadCompletionListener, asyncTotalPhotosDataLoadCompletionListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_main);

        /* Get Values from intent */
        if (getIntent().hasExtra(IMAGE_USERNAME_KEY)) {
            userName = getIntent().getStringExtra(IMAGE_USERNAME_KEY);
            profilePicUrl = getIntent().getStringExtra(IMAGE_USER_PROFILE_PIC_KEY);
            nameOfUser = getIntent().getStringExtra(IMAGE_NAME_OF_USER_KEY);
            userLocation = getIntent().getStringExtra(IMAGE_LOCATION_KEY);
            bio = getIntent().getStringExtra(IMAGE_USER_BIO_KEY);
            headerImageUrl = getIntent().getStringExtra(IMAGE_REGULAR_IMAGE_URL_KEY);
        }

        APP_ID = ((wolpepper) this.getApplication()).getAPP_ID();

        /* Call to initialize all views */
        initializeViews();
        updateViews();

        new LoadProfileAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        /* View Pager */
        ViewPager viewPager = findViewById(R.id.viewpager);
        SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        /* Tab Layout */
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        /* FUll screen Code */
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }


    /**
     * Listener method to notify respective fragments when profile load is complete
     * @param asyncDataLoadCompletionListener
     * @param tag
     */
    public void setAsyncDataLoadCompletionListener(AsyncDataLoadCompletionListener asyncDataLoadCompletionListener, String tag){
        switch (tag){
            case LIKE_FRAG_TAG:
                this.asyncTotalLikeDataLoadCompletionListener = asyncDataLoadCompletionListener;
                break;
            case PHOTOS_FRAG_TAG:
                this.asyncTotalPhotosDataLoadCompletionListener = asyncDataLoadCompletionListener;
                break;
            case COLLECTION_FRAG_TAG:
                this.asyncTotalCollectionDataLoadCompletionListener = asyncDataLoadCompletionListener;
                break;
        }
    }

    /**
     * Initialize all views of activity
     */
    private void initializeViews() {
        followersCountTv = findViewById(R.id.followers_count_tv);
        followingCountTv = findViewById(R.id.following_count_tv);
        totalPhotosTv = findViewById(R.id.total_photos_count_tv);
        nameOfUserTv = findViewById(R.id.name_of_user_tv);
        userLocationTv = findViewById(R.id.location_tv);
        userBioTv = findViewById(R.id.user_bio_tv);
        profileImage = findViewById(R.id.profile_image_iv);
        headerImage = findViewById(R.id.user_photos_header_iv);
        profileLinkView = findViewById(R.id.profile_link_view);
        profileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    /**
     * Update Content of All views
     */
    private void updateViews() {
        UtilityMethods.updateTextView(followersCountTv, "0");
        UtilityMethods.updateTextView(followingCountTv, "0");
        UtilityMethods.updateTextView(totalPhotosTv, "0");
        UtilityMethods.updateTextView(nameOfUserTv, nameOfUser.toUpperCase());
        UtilityMethods.updateTextView(userBioTv, bio);
        if (UtilityMethods.hideEmptyTextViews(userBioTv)){
            userBioTv.setVisibility(View.INVISIBLE);
        }

        if (userLocation == null || userLocation.equals("null") || TextUtils.isEmpty(userLocation)) {
            userLocationTv.setText(getString(R.string.unknown_value));
        } else {
            userLocationTv.setText(userLocation);
        }

        GlideApp.with(this).load(headerImageUrl).into(headerImage);
        GlideApp.with(this).load(profilePicUrl).into(profileImage);

    }

    /**
     * used to get total likes of the user (Usable for fragments)
     * @return - Total user Likes
     */
    public int getLikeCount(){
        return totalLikes;
    }

    public int getCollectionCount(){
        return  totalCollections;
    }

    public int getTotalPhotos(){
        return totalPhotos;
    }

    /**
     * Load image data from api Link
     * @param link - link to api
     * @return - returns data of request
     */
    private String loadImageDataFromLink(String link) {
        try {
            return NetworkUtils.getExtractedJson(ProfileMainActivity.this, link, "", true);
        } catch (IOException e) {
            return null;
        }

    }

    /**
     * Parse the data received as string to json and get values
     * @param json - The json data received from api as string.
     * @return - returns true if parsec successfully else false
     */
    private boolean parseJson(String json) {
        try {
            if (json.substring(0, 1).equals("{") && new JSONObject(json).has("error")) {
                Toast.makeText(this, json, Toast.LENGTH_LONG).show();
                return false;
            }

            JSONObject rootJsonObject = new JSONObject(json);
            followingCount = rootJsonObject.getInt(IMAGE_USER_FOLLOWING_COUNT_KEY);
            followersCount = rootJsonObject.getInt(IMAGE_USER_FOLLOWERS_COUNT_KEY);
            totalLikes = rootJsonObject.getInt(IMAGE_USER_TOTAL_LIKES);
            totalPhotos = rootJsonObject.getInt(IMAGE_USER_TOTAL_PHOTOS);
            totalCollections = rootJsonObject.getInt(IMAGE_USER_TOTAL_COLLECTIONS);

            profilePicUrl = rootJsonObject.getJSONObject(IMAGE_USER_PROFILE_PIC_KEY).getString(IMAGE_USER_PROFILE_PIC_LARGE_KEY);
            profileUrl = rootJsonObject.getJSONObject(IMAGE_LINKS_KEY).getString(IMAGE_HTML_LINK_KEY);


            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Load profile data Async Task
     */
    private class LoadProfileAsync extends AsyncTask<Void, Integer, URL> {

        @Override
        protected URL doInBackground(Void... params) {
            URL url = NetworkUtils.buildUserProfileUrl(userName, APP_ID);

            String json = null;
            try {
                json = NetworkUtils.getProfileDetailHtml(url.toString());
            } catch (IOException e) {
                e.printStackTrace();
                return url;
            }

            if (json != null && json.equals(RATE_LIMIT_REMAINING_HEADER_KEY)) {
                isRateLimitReached = true;
                return url;
            }
            if (json == null){
                return url;
            }

            if (parseJson(json)) {
                return null;
            }

            return url;

        }

        @Override
        protected void onPostExecute(URL url) {
            super.onPostExecute(url);
            if (isFinishing()){
                return;
            }
            if (isRateLimitReached) {
                UtilityMethods.showRateLimitReachedAlert(ProfileMainActivity.this);
                return;
            }

            if (url != null) {
                final Alerter alerter = Alerter.create(ProfileMainActivity.this);
                alerter.setTitle(R.string.connection_error)
                        .setText(R.string.connection_error_description)
                        .setTextTypeface(ResourcesCompat.getFont(ProfileMainActivity.this, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(ProfileMainActivity.this, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setBackgroundColorRes(R.color.alert_default_error_background)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new LoadProfileAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                Alerter.hide();
                            }
                        })
                        .enableVibration(true)
                        .enableInfiniteDuration(true)
                        .show();
                return;
            }
            if (!isFinishing()) {
                updateProfileImageAndCountTextViews();
                asyncTotalPhotosDataLoadCompletionListener.dataLoadComplete();
                asyncTotalLikeDataLoadCompletionListener.dataLoadComplete();
                asyncTotalCollectionDataLoadCompletionListener.dataLoadComplete();
                profileLinkView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Intent.ACTION_VIEW, NetworkUtils.buildLinkWithUtmParameters(ProfileMainActivity.this, profileUrl)));
                    }
                });
            }
        }
    }

    private void updateHeaderImage(){
        Drawable placeholder = headerImage.getDrawable();
        GlideApp.with(ProfileMainActivity.this).load(getIntent().getStringExtra(IMAGE_FULL_IMAGE_URL_KEY)).placeholder(placeholder).transition(new DrawableTransitionOptions().crossFade()).into(headerImage);
    }

    /**
     * Update Pending Views Data
     */
    private void updateProfileImageAndCountTextViews() {
        ValueAnimator followerAnimator = ValueAnimator.ofInt(0, followersCount);
        followerAnimator.setDuration(1500);
        followerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                followersCountTv.setText(animation.getAnimatedValue().toString());
            }
        });
        followerAnimator.start();

        ValueAnimator followingAnimator = ValueAnimator.ofInt(0, followingCount);
        followingAnimator.setDuration(1500);
        followingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                followingCountTv.setText(animation.getAnimatedValue().toString());
            }
        });
        followingAnimator.start();


        ValueAnimator totalPhotosAnimator = ValueAnimator.ofInt(0, totalPhotos);
        totalPhotosAnimator.setDuration(1500);
        totalPhotosAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                totalPhotosTv.setText(animation.getAnimatedValue().toString());
            }
        });
        totalPhotosAnimator.start();

        Drawable placeholder = profileImage.getDrawable();
        GlideApp.with(ProfileMainActivity.this).load(profilePicUrl).placeholder(placeholder).transition(new DrawableTransitionOptions().crossFade(2000)).into(profileImage);

        //update from low res to full
        if (getIntent().hasExtra(IMAGE_FULL_IMAGE_URL_KEY)){
            updateHeaderImage();
        }
    }
}