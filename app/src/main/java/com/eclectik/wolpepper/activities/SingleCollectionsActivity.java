package com.eclectik.wolpepper.activities;

import static com.eclectik.wolpepper.utils.ConstantValues.BASE_UNSPLASH_URL_STRING;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_COVER_PHOTO_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_DATE_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_DESCRIPTION_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_ID_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_IS_CURATED_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_TITLE_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_TOTAL_PHOTOS_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.HeaderKeys.RATE_LIMIT_REMAINING_HEADER_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_DOWNLOAD_API_CALL_LINK_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_HEIGHT_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_HTML_LINK_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_LINKS_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_NAME_OF_USER_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USERNAME_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_BIO_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_PROFILE_PIC_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_TOTAL_COLLECTIONS;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_TOTAL_LIKES;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_TOTAL_PHOTOS;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_WIDTH_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.UNSPLASH_COLLECTIONS_PATH_STRING;
import static com.eclectik.wolpepper.utils.ConstantValues.UNSPLASH_DOWNLOAD_PATH_STRING;

import android.animation.Animator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.eclectik.wolpepper.BuildConfig;
import com.eclectik.wolpepper.GlideApp;
import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.adapters.NewPapersAdapter;
import com.eclectik.wolpepper.dataStructures.PaperCollections;
import com.eclectik.wolpepper.dataStructures.Papers;
import com.eclectik.wolpepper.listenerInterfaces.RecyclerViewDataUpdateRequester;
import com.eclectik.wolpepper.utils.ConstantValues;
import com.eclectik.wolpepper.utils.DownloadWallpaperUtils;
import com.eclectik.wolpepper.utils.NetworkUtils;
import com.eclectik.wolpepper.utils.SetWallpaperService;
import com.eclectik.wolpepper.utils.UtilityMethods;
import com.eclectik.wolpepper.wolpepper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.tapadoo.alerter.Alerter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class SingleCollectionsActivity extends AppCompatActivity implements RecyclerViewDataUpdateRequester {

    //Views
    private ImageView headerImageIv;
    private ImageView downloadButtonIv;
    private ImageView shareButtonIv;
    private FloatingActionButton setRandomWallpaperFromCollectionFab;

    private TextView collectionTitleTv, userNameTv, collectionDescriptonTv;
    private ShapeableImageView profileImageIv;

    private ArrayList<Papers> papersArrayList = new ArrayList<>();

    private PaperCollections currentCollection = new PaperCollections();

    private RecyclerView recyclerView;

    private NewPapersAdapter adapter;

    private String APP_ID;

    private boolean isRateLimitReached = false;

    private boolean isImageAnimationTaskRunning = false;

    private int pageNumber = 1;

    private boolean isRequestRunning = false;

    private AnimateHeaderAsync animateHeaderAsync = new AnimateHeaderAsync();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_collections);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        /*Overriding Up Button as Back*/
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (getIntent().hasExtra(COLLECTION_ID_KEY)) {
            currentCollection.setCollectionId(getIntent().getStringExtra(COLLECTION_ID_KEY));
            currentCollection.setCollectionTitle(getIntent().getStringExtra(COLLECTION_TITLE_KEY));
            currentCollection.setCollectionDate(getIntent().getStringExtra(COLLECTION_DATE_KEY));
            currentCollection.setCollectionDescrip(getIntent().getStringExtra(COLLECTION_DESCRIPTION_KEY));
            currentCollection.setCollectionNameOfUser(getIntent().getStringExtra(IMAGE_NAME_OF_USER_KEY));
            currentCollection.setCollectionUserProfilePicUrl(getIntent().getStringExtra(IMAGE_USER_PROFILE_PIC_KEY));
            currentCollection.setCollectionUserName(getIntent().getStringExtra(IMAGE_USERNAME_KEY));
            currentCollection.setTotalPhotos(getIntent().getIntExtra(COLLECTION_TOTAL_PHOTOS_KEY, 0));

            currentCollection.setCoverPhotoDisplayUrl(getIntent().getStringExtra(COLLECTION_COVER_PHOTO_KEY));
            currentCollection.setCurated(getIntent().getBooleanExtra(COLLECTION_IS_CURATED_KEY, false));
            if (getIntent().hasExtra(UNSPLASH_DOWNLOAD_PATH_STRING)) {
                currentCollection.setCollectionAllImageDownloadUrl(getIntent().getStringExtra(UNSPLASH_DOWNLOAD_PATH_STRING));
            }
        }

        initializeViews();

        updateViews();

        setViewClickListeners();

        adapter = new NewPapersAdapter(this, this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(adapter);

        /**
         * This transparent code is working
         * */
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);


        getWindow().setStatusBarColor(Color.TRANSPARENT);

        loadPapersList();
    }

    /**
     * update list if like/unlike status is changed
     */
    @Override
    public void onResume() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    /**
     * Initialize all layout views
     */
    private void initializeViews() {
        recyclerView = findViewById(R.id.collection_photos_recycler_view);

        downloadButtonIv = findViewById(R.id.download_button);

        shareButtonIv = findViewById(R.id.share_button);

        setRandomWallpaperFromCollectionFab = findViewById(R.id.use_muzei_source_fab);

        headerImageIv = findViewById(R.id.collection_header_iv);

        userNameTv = findViewById(R.id.collection_user_tv);

        collectionDescriptonTv = findViewById(R.id.collection_description_tv);

        collectionTitleTv = findViewById(R.id.collection_title_tv);

        profileImageIv = findViewById(R.id.user_profile_pic);
    }


    /**
     * Update Content of Views
     */
    private void updateViews() {
        userNameTv.setText("By " + currentCollection.getCollectionNameOfUser());
        collectionTitleTv.setText(currentCollection.getCollectionTitle());
        Glide.with(this).load(currentCollection.getCollectionUserProfilePicUrl()).into(profileImageIv);
        if (currentCollection.getCollectionDescrip() != null && !currentCollection.getCollectionDescrip().equals("null")) {
            collectionDescriptonTv.setText(currentCollection.getCollectionDescrip());
        } else {
            collectionDescriptonTv.setVisibility(View.GONE);
        }

    }

    /**
     * Set up Fab Layout Click listeners
     */
    private void setViewClickListeners() {

        shareButtonIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Wolpepper Collection");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, "Collection By " + currentCollection.getCollectionUserName() + "\nShared via Wolpepper!\nView it in Wolpepper or at Unsplash website : \n" + BASE_UNSPLASH_URL_STRING + UNSPLASH_COLLECTIONS_PATH_STRING + "/" + currentCollection.getCollectionId());
                startActivity(Intent.createChooser(intent, "Share Collection with :").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        if (!currentCollection.isCurated()) {
            downloadButtonIv.setImageResource(R.drawable.ic_action_download_disabled);
        }

        downloadButtonIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentCollection.isCurated()) {
                    Alerter.create(SingleCollectionsActivity.this)
                            .setTextTypeface(ResourcesCompat.getFont(SingleCollectionsActivity.this, R.font.spacemono_regular))
                            .setTitleTypeface(ResourcesCompat.getFont(SingleCollectionsActivity.this, R.font.spacemono_bold))
                            .setTextAppearance(R.style.alertBody)
                            .setTitleAppearance(R.style.alertTitle)
                            .setTitle(getString(R.string.collection_cant_download))
                            .setText(getString(R.string.collection_cant_download_desc))
                            .setBackgroundColorRes(R.color.alert_default_error_background)
                            .setDuration(5000)
                            .show();
                    return;
                }

                if (DownloadWallpaperUtils.checkIfFileIsAlreadyDownloading(currentCollection.getCollectionTitle())) {

                    Alerter.create(SingleCollectionsActivity.this)
                            .setTextTypeface(ResourcesCompat.getFont(SingleCollectionsActivity.this, R.font.spacemono_regular))
                            .setTitleTypeface(ResourcesCompat.getFont(SingleCollectionsActivity.this, R.font.spacemono_bold))
                            .setTextAppearance(R.style.alertBody)
                            .setTitleAppearance(R.style.alertTitle)
                            .setTitle(getString(R.string.already_downloading_title))
                            .setText(getString(R.string.collection_already_downloading_desc_1) + currentCollection.getCollectionTitle() + getString(R.string.collection_already_downloading_desc_2))
                            .setBackgroundColorRes(R.color.alert_default_error_background)
                            .show();

                } else {

                    File file = new File(ConstantValues.IMAGE_LOCAL_STORAGE_FULL_PATH + "/Collections/" + currentCollection.getCollectionId() + ".zip");

                    if (file.exists()) {

                        Alerter.create(SingleCollectionsActivity.this)
                                .setTextTypeface(ResourcesCompat.getFont(SingleCollectionsActivity.this, R.font.spacemono_regular))
                                .setTitleTypeface(ResourcesCompat.getFont(SingleCollectionsActivity.this, R.font.spacemono_bold))
                                .setTextAppearance(R.style.alertBody)
                                .setTitleAppearance(R.style.alertTitle)
                                .setTitle(getString(R.string.already_downloading_title))
                                .setText(getString(R.string.collection_already_exist_desc_1) + currentCollection.getCollectionTitle() + getString(R.string.collection_already_exist_desc_2))
                                .setBackgroundColorRes(R.color.alert_default_error_background)
                                .show();

                    } else {

                        DownloadWallpaperUtils.downloadCollection(SingleCollectionsActivity.this, Uri.parse(currentCollection.getCollectionAllImageDownloadUrl()), currentCollection);

                    }

                }
            }
        });
    }

    /**
     * Update Data set of adapter Request LISTENER
     */
    @Override
    public void dataSetUpdateRequested() {

        updateAdapterData();
    }

    /**
     * Parse the retrieved String to JSON and further Objects
     *
     * @param json - The JSON String to be parsed
     */
    private boolean parseJson(String json) {
        try {
            if (json.substring(0, 1).equals("{") && new JSONObject(json).has("error")) {
                Alerter.create(SingleCollectionsActivity.this)
                        .setTextTypeface(ResourcesCompat.getFont(SingleCollectionsActivity.this, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(SingleCollectionsActivity.this, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setTitle("Error")
                        .setText(json)
                        .setBackgroundColorRes(R.color.alert_default_error_background)
                        .show();
                return false;
            }

            double qualityPercent = getSharedPreferences(getString(R.string.preview_quality_base_pref_key), Context.MODE_PRIVATE).getFloat(getString(R.string.preview_quality_percent_pref_key), 45);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                //Current Json Object
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String imageId = jsonObject.getString(ConstantValues.IMAGE_ID_KEY);
                String date = jsonObject.getString(ConstantValues.IMAGE_CREATED_DATE_KEY);
                String color = jsonObject.getString(ConstantValues.IMAGE_COLOR_KEY);
                int height = jsonObject.getInt(IMAGE_HEIGHT_KEY);
                int width = jsonObject.getInt(IMAGE_WIDTH_KEY);
                boolean isLiked = jsonObject.getBoolean(ConstantValues.IMAGE_LIKED_BY_USER_KEY);

                ((wolpepper) (this.getApplication())).updateIdLikeStatusMap(imageId, isLiked);

                //User Details from json object
                JSONObject userDetails = jsonObject.getJSONObject(ConstantValues.IMAGE_USER_DETAILS_OBJECT_KEY);
                String name = userDetails.getString(ConstantValues.IMAGE_NAME_OF_USER_KEY);
                String userName = userDetails.getString(ConstantValues.IMAGE_USERNAME_KEY);
                String userId = userDetails.getString(ConstantValues.IMAGE_USER_ID_KEY);
                String userProfileImage = userDetails.getJSONObject(ConstantValues.IMAGE_USER_PROFILE_PIC_KEY).getString(ConstantValues.IMAGE_USER_PROFILE_PIC_MEDIUM_KEY);
                String userProfileLink = userDetails.getJSONObject(IMAGE_LINKS_KEY).getString(IMAGE_HTML_LINK_KEY);
                String userBio = userDetails.getString(IMAGE_USER_BIO_KEY);
                int totalLikes = userDetails.getInt(IMAGE_USER_TOTAL_LIKES);
                int totalPhotos = userDetails.getInt(IMAGE_USER_TOTAL_PHOTOS);
                int totalCollections = userDetails.getInt(IMAGE_USER_TOTAL_COLLECTIONS);

                //Image Url Json Object
                JSONObject imageUrlsObject = jsonObject.getJSONObject(ConstantValues.IMAGE_URLS_KEY);
                String imageUrl = imageUrlsObject.getString(ConstantValues.IMAGE_REGULAR_IMAGE_URL_KEY);
                String fullImageUrl = imageUrlsObject.getString(ConstantValues.IMAGE_FULL_IMAGE_URL_KEY);
                String rawImageUrl = imageUrlsObject.getString(ConstantValues.IMAGE_RAW_IMAGE_URL_KEY);

                // Image API Download section
                String apiDownloadSpot = jsonObject.getJSONObject(IMAGE_LINKS_KEY).getString(IMAGE_DOWNLOAD_API_CALL_LINK_KEY);


                /** IMPLEMENT A LOGIC FROM SETTINGS THAT IF SAVE MOBILE DATA IS SELECTED THEN USE LOW RES IMAGE MULTIPLY WIDTHPIXELS BY 0.50 or 0.75 **/
                imageUrl = imageUrl.replace("w=1080", "w=" + Double.toString(displayMetrics.widthPixels * (qualityPercent / 100)));

                JSONObject links = jsonObject.getJSONObject(ConstantValues.IMAGE_LINKS_KEY);
                String htmlLink = links.getString(ConstantValues.IMAGE_HTML_LINK_KEY);
                // Adding current wallpaper object to arraylist
                Papers paper = new Papers(imageId, name, date, userProfileImage, imageUrl, htmlLink);
                paper.setRawImageUrl(rawImageUrl);
                paper.setFullImageUrl(fullImageUrl);
                paper.setAuthorUserName(userName);
                paper.setColor(color);
                paper.setHeight(height);
                paper.setWidth(width);
                paper.setLiked(isLiked);
                paper.setProfileUrl(userProfileLink);
                paper.setAuthorTotalLikes(totalLikes);
                paper.setAuthorTotalPhotos(totalPhotos);
                paper.setAuthorTotalCollections(totalCollections);
                paper.setAuthorBio(userBio);
                paper.setInMuzeiList(UtilityMethods.isInMuzeiActiveList(SingleCollectionsActivity.this, imageId));
                paper.setDownloadImageApiCallUrl(apiDownloadSpot);
                papersArrayList.add(paper);

            }
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    /**
     * Method to add new items to adapter
     */
    private void updateAdapterData() {

        if (!isImageAnimationTaskRunning) {
            animateHeaderAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        final ArrayList<Papers> newPapersDataList = new ArrayList<>();
        if (adapter.getItemCount() == 0) {        // Execute for first time when adapter is empty
            if (papersArrayList.size() < 10) {
                newPapersDataList.addAll(papersArrayList);
            } else {
                newPapersDataList.addAll(papersArrayList.subList(0, 10));
            }
            adapter.updateDataSet(newPapersDataList);
            adapter.notifyDataSetChanged();

        } else if (adapter.getItemCount() < papersArrayList.size()) {       // Execute when arraylist have items
            showMoreLoader();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    while (recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE && recyclerView.isComputingLayout()) {

                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isFinishing()) {
                                return;
                            }
                            hideMoreLoader();
                            int currentItemCount = adapter.getItemCount();

                            if (currentItemCount + 10 < papersArrayList.size()) {                                        //Check if current items in adapter is less than 10 items in papers array list. IF ITS NOT then add the few remaining items to adapter
                                newPapersDataList.addAll(papersArrayList.subList(0, currentItemCount + 10));
                            } else {
                                newPapersDataList.addAll(papersArrayList);
                            }

                            adapter.updateDataSet(newPapersDataList);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            };
            thread.start();


        } else if (adapter.getItemCount() < currentCollection.getTotalPhotos()) {           // CHECK IF ALL THE PHOTOS OF COLLECTION ARE LOADED AND IF NOT THEN DOWNLOAD MORE
            if (!isRequestRunning) {
                loadPapersList();
            }
        }

    }

    /**
     * Method to create URL and call asyncTask to load Papers
     */
    private void loadPapersList() {
        // Run This Code if App_ID is already initialized
        if (APP_ID != null) {
            URL url = NetworkUtils.buildCollectionPhotosUrl(APP_ID, currentCollection.getCollectionId(), String.valueOf(pageNumber), "30", currentCollection.isCurated());
            new getCollectionPhotoAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
            pageNumber++;
            return;
        }

        APP_ID = BuildConfig.UNSPLASH_APP_ID;
        URL url = NetworkUtils.buildCollectionPhotosUrl(APP_ID, currentCollection.getCollectionId(), String.valueOf(pageNumber), "30", currentCollection.isCurated());
        new getCollectionPhotoAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
        pageNumber++;
    }

    private void showMoreLoader() {
        YoYo.with(Techniques.SlideInUp)
                .duration(500)
                .onStart(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {

                        findViewById(R.id.more_loader).setVisibility(View.VISIBLE);
                    }
                })
                .playOn(findViewById(R.id.more_loader));
    }

    private void hideMoreLoader() {
        YoYo.with(Techniques.SlideOutDown)
                .duration(500)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        findViewById(R.id.more_loader).setVisibility(View.GONE);
                    }
                })
                .playOn(findViewById(R.id.more_loader));
    }

    /**
     * Async task to get collection details
     */
    private class getCollectionPhotoAsync extends AsyncTask<URL, Void, URL> {

        @Override
        protected void onPreExecute() {
            isRequestRunning = true;
            showMoreLoader();
            super.onPreExecute();
        }

        @Override
        protected URL doInBackground(URL... params) {
            URL url = params[0];
            String json = "";
            try {
                json = NetworkUtils.getResponseFromHttpUrl(url, SingleCollectionsActivity.this);
                if (json != null && json.equals(RATE_LIMIT_REMAINING_HEADER_KEY)) {
                    isRateLimitReached = true;
                    return url;
                }
                if (parseJson(json)) {
                    return null;
                } else {
                    return url;
                }
            } catch (IOException e) {
                // THIS IS CALLED WHEN ANY ERROR IS RETURNED IN JSON LIKE FILE NOT FOUND DUE TO SOME ERROR OR API EXCEEDED / PERFORM RELATED UI UPDATE
                return url;
            } catch (NullPointerException e) {
                return url;
            }
        }

        @Override
        protected void onPostExecute(final URL url) {
            super.onPostExecute(url);
            if (isFinishing()) {
                return;
            }

            hideMoreLoader();

            isRequestRunning = false;
            if (isRateLimitReached) {
                UtilityMethods.showRateLimitReachedAlert(SingleCollectionsActivity.this);
            }
            if (url != null) {
                final Alerter alerter = Alerter.create(SingleCollectionsActivity.this);

                alerter.setTitle("Error")
                        .setTextTypeface(ResourcesCompat.getFont(SingleCollectionsActivity.this, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(SingleCollectionsActivity.this, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setText("Connection to the Server Timed out. Try Again!")
                        .setBackgroundColorRes(R.color.alert_default_error_background)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new getCollectionPhotoAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
                                Alerter.hide();
                            }
                        })
                        .enableVibration(true)
                        .enableInfiniteDuration(true)
                        .show();
                return;
            }
            updateAdapterData();

            setRandomWallpaperFromCollectionFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Papers papers = papersArrayList.get(new Random().nextInt(papersArrayList.size()));
                    if (isMyServiceRunning(SetWallpaperService.class)) {
                        UtilityMethods.fileAlreadyDownloadingAlert(SingleCollectionsActivity.this, papers);
                    } else {
                        Intent intent = new Intent(SingleCollectionsActivity.this, SetWallpaperService.class).putExtra(getString(R.string.set_wallpaper_intent_extra), papers).putExtra(getString(R.string.grey_scale_identifier_intent_extra), false);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent);
                        } else {
                            startService(intent);
                        }
                        Alerter.create(SingleCollectionsActivity.this)
                                .setTextTypeface(ResourcesCompat.getFont(SingleCollectionsActivity.this, R.font.spacemono_regular))
                                .setTitleTypeface(ResourcesCompat.getFont(SingleCollectionsActivity.this, R.font.spacemono_bold))
                                .setTextAppearance(R.style.alertBody)
                                .setTitleAppearance(R.style.alertTitle)
                                .setTitle(getString(R.string.download_start_title))
                                .setText(getString(R.string.set_random_wallpaper_desc))
                                .show();
                    }
                }
            });

        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Header Image Animation Async Task
     */
    private class AnimateHeaderAsync extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {
            isImageAnimationTaskRunning = true;
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (papersArrayList.size() > 1) {
                int previousIndex = 0;
                while (true) {
                    Random random = new Random();
                    int index = random.nextInt(papersArrayList.size());
                    while (index == previousIndex) {
                        index = random.nextInt(papersArrayList.size());
                    }

                    publishProgress(index, previousIndex);
                    previousIndex = index;
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        return null;
                    }
                }
            } else {
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            GlideApp.with(SingleCollectionsActivity.this).load(papersArrayList.get(values[0]).getDisplayImageUrl()).thumbnail(Glide.with(SingleCollectionsActivity.this).load(papersArrayList.get(values[1]).getDisplayImageUrl())).transition(new DrawableTransitionOptions().crossFade(2000)).into(headerImageIv);
            super.onProgressUpdate(values);
        }
    }

    @Override
    public void onBackPressed() {
        isImageAnimationTaskRunning = true;
        if (animateHeaderAsync.getStatus() == AsyncTask.Status.RUNNING) {
            animateHeaderAsync.cancel(true);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (animateHeaderAsync.getStatus() == AsyncTask.Status.RUNNING) {
            animateHeaderAsync.cancel(true);
        }
        super.onDestroy();
    }
}
