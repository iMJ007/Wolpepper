package com.eclectik.wolpepper.activities;

import static com.eclectik.wolpepper.utils.ConstantValues.HeaderKeys.RATE_LIMIT_REMAINING_HEADER_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_DOWNLOAD_API_CALL_LINK_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_HEIGHT_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_HTML_LINK_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_LINKS_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_BIO_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_TOTAL_COLLECTIONS;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_TOTAL_LIKES;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_TOTAL_PHOTOS;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_WIDTH_KEY;

import android.animation.Animator;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.adapters.NewPapersAdapter;
import com.eclectik.wolpepper.dataStructures.Papers;
import com.eclectik.wolpepper.listenerInterfaces.RecyclerViewDataUpdateRequester;
import com.eclectik.wolpepper.utils.ConstantValues;
import com.eclectik.wolpepper.utils.NetworkUtils;
import com.eclectik.wolpepper.utils.UtilityMethods;
import com.eclectik.wolpepper.wolpepper;
import com.tapadoo.alerter.Alerter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class CategoryResultActivity extends AppCompatActivity implements RecyclerViewDataUpdateRequester {

    private String selectedCategory;
    private int pageNumber = 1;
    private String APP_ID;
    private int totalCategoriesPhotos = -1;
    private RecyclerView recyclerView;
    private NewPapersAdapter adapter;
    private ArrayList<Papers> papersArrayList = new ArrayList<>();
    private boolean isRateLimitReached = false;
    private boolean isRequestRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        APP_ID = ((wolpepper) getApplication()).getAPP_ID();

        selectedCategory = getIntent().getStringExtra(ConstantValues.IMAGE_CATEGORY_KEY);

        setTitle(selectedCategory);

        recyclerView = findViewById(R.id.category_result_list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new NewPapersAdapter(this, this);

        recyclerView.setAdapter(adapter);

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
     * Interface method used to call to add new items to adapter data set when scroll reaches last item
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
    private boolean parseJson(String json, boolean isFromHashMap) {
        try {
            if (json.substring(0, 1).equals("{") && new JSONObject(json).has("error")) {
                Toast.makeText(CategoryResultActivity.this, json, Toast.LENGTH_LONG).show();
                return false;
            }

            boolean isPortraitOnly = ((wolpepper) getApplication()).isPortraitOnly();
            JSONObject rootJsonObject = new JSONObject(json);
            totalCategoriesPhotos = rootJsonObject.getInt("total");
            double qualityPercent = getSharedPreferences(getString(R.string.preview_quality_base_pref_key), Context.MODE_PRIVATE).getFloat(getString(R.string.preview_quality_percent_pref_key), 45);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            JSONArray resultJsonArray = rootJsonObject.getJSONArray("results");
            for (int i = 0; i < resultJsonArray.length(); i++) {
                //Current Json Object
                JSONObject currentImageJsonObject = resultJsonArray.getJSONObject(i);
                String imageId = currentImageJsonObject.getString(ConstantValues.IMAGE_ID_KEY);
                String date = currentImageJsonObject.getString(ConstantValues.IMAGE_CREATED_DATE_KEY);
                String color = currentImageJsonObject.getString(ConstantValues.IMAGE_COLOR_KEY);
                int height = currentImageJsonObject.getInt(IMAGE_HEIGHT_KEY);
                int width = currentImageJsonObject.getInt(IMAGE_WIDTH_KEY);

                if (isPortraitOnly && height < width + 500) {
                    continue;
                }

                boolean isLiked = currentImageJsonObject.getBoolean(ConstantValues.IMAGE_LIKED_BY_USER_KEY);

                /* UPDATE LIKE STATUS IN APP CONTEXTS LIST */
                if (isFromHashMap) {
                    isLiked = ((wolpepper) getApplication()).isImageLiked(imageId);
                } else {
                    ((wolpepper) getApplication()).updateIdLikeStatusMap(imageId, isLiked);
                }

                //User Details from json object
                JSONObject userDetails = currentImageJsonObject.getJSONObject(ConstantValues.IMAGE_USER_DETAILS_OBJECT_KEY);
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
                JSONObject imageUrlsObject = currentImageJsonObject.getJSONObject(ConstantValues.IMAGE_URLS_KEY);
                String imageUrl = imageUrlsObject.getString(ConstantValues.IMAGE_REGULAR_IMAGE_URL_KEY);
                String fullImageUrl = imageUrlsObject.getString(ConstantValues.IMAGE_FULL_IMAGE_URL_KEY);
                String rawImageUrl = imageUrlsObject.getString(ConstantValues.IMAGE_RAW_IMAGE_URL_KEY);

                // Image API Download section
                String apiDownloadSpot = currentImageJsonObject.getJSONObject(IMAGE_LINKS_KEY).getString(IMAGE_DOWNLOAD_API_CALL_LINK_KEY);



                /** IMPLEMENT A LOGIC FROM SETTINGS THAT IF SAVE MOBILE DATA IS SELECTED THEN USE LOW RES IMAGE MULTIPLY WIDTHPIXELS BY 0.50 or 0.75 **/
                imageUrl = imageUrl.replace("w=1080", "w=" + Double.toString(displayMetrics.widthPixels * (qualityPercent / 100)));

                JSONObject links = currentImageJsonObject.getJSONObject(ConstantValues.IMAGE_LINKS_KEY);
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
                paper.setInMuzeiList(UtilityMethods.isInMuzeiActiveList(CategoryResultActivity.this, imageId));
                paper.setDownloadImageApiCallUrl(apiDownloadSpot);
                papersArrayList.add(paper);

            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method to add new items to adapter
     */
    private void updateAdapterData() {
        ArrayList<Papers> newPapersDataList = new ArrayList<>();
        if (adapter.getItemCount() == 0 && papersArrayList.size() > 0) {
            if (papersArrayList.size() < 10) {
                newPapersDataList.addAll(papersArrayList);
            } else {
                newPapersDataList.addAll(papersArrayList.subList(0, 10));
            }
            adapter.updateDataSet(newPapersDataList);
            adapter.notifyDataSetChanged();

        } else if (adapter.getItemCount() + 10 <= papersArrayList.size() && !recyclerView.isComputingLayout()) {
            int currentItemCount = adapter.getItemCount();

            if (currentItemCount + 10 < papersArrayList.size()) {                                        //Check if current items in adapter is less than 10 items in papers array list. IF ITS NOT then add the few remaining items to adapter
                newPapersDataList.addAll(papersArrayList.subList(0, currentItemCount + 10));
            } else {
                newPapersDataList.addAll(papersArrayList);
            }

            adapter.updateDataSet(newPapersDataList);
            adapter.notifyDataSetChanged();
        } else if (adapter.getItemCount() < totalCategoriesPhotos) {
            // If condition to prevent duplicate Api calls
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
        if (totalCategoriesPhotos != -1 && totalCategoriesPhotos == 0) { // If user dont have photos then dont request api
            return;
        }
        URL url = NetworkUtils.buildSearchUrl(selectedCategory.toLowerCase(), String.valueOf(pageNumber), APP_ID);
        new LoadCategoryPaperList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
        pageNumber++;
    }

    /**
     * Async Task to Get List of Papers (Wallpapers)
     */
    private class LoadCategoryPaperList extends AsyncTask<URL, Void, URL> {

        @Override
        protected void onPreExecute() {
            if (isFinishing()){
                cancel(true);
                return;
            }

            YoYo.with(Techniques.SlideInUp)
                    .duration(500)
                    .onStart(new YoYo.AnimatorCallback() {
                        @Override
                        public void call(Animator animator) {
                            findViewById(R.id.more_loader).setVisibility(View.VISIBLE);
                        }
                    })
                    .playOn(findViewById(R.id.more_loader));

            isRequestRunning = true;
            isRateLimitReached = false;
            super.onPreExecute();
        }

        @Override
        protected URL doInBackground(URL... params) {
            URL url = params[0];
            try {
                String json = NetworkUtils.getResponseFromHttpUrl(url, CategoryResultActivity.this);

                if (json != null && json.equals(RATE_LIMIT_REMAINING_HEADER_KEY)) {
                    isRateLimitReached = true;
                    return url;
                }

                // Return Null if json is parsed successfully
                if (parseJson(json, false)) {
                    return null;
                }

                return url;
            } catch (IOException e) {
                e.printStackTrace();
                return url;
            }
        }

        @Override
        protected void onPostExecute(final URL url) {
            super.onPostExecute(url);
            if (isFinishing()) {
                return;
            }

            if (isRateLimitReached) {
                UtilityMethods.showRateLimitReachedAlert(CategoryResultActivity.this);
                return;
            }

            YoYo.with(Techniques.SlideOutDown)
                    .duration(500)
                    .onEnd(new YoYo.AnimatorCallback() {
                        @Override
                        public void call(Animator animator) {
                            findViewById(R.id.more_loader).setVisibility(View.GONE);
                        }
                    })
                    .playOn(findViewById(R.id.more_loader));

            YoYo.with(Techniques.ZoomOut)
                    .duration(500)
                    .onEnd(new YoYo.AnimatorCallback() {
                        @Override
                        public void call(Animator animator) {
                            findViewById(R.id.search_square_loading).setVisibility(View.GONE);
                        }
                    })
                    .playOn(findViewById(R.id.search_square_loading));

            if (url != null) {
                final Alerter alerter = Alerter.create(CategoryResultActivity.this);

                alerter.setTitle(getString(R.string.connection_error))
                        .setTextTypeface(ResourcesCompat.getFont(CategoryResultActivity.this, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(CategoryResultActivity.this, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setText(getString(R.string.connection_error_description))
                        .setBackgroundColorRes(R.color.alert_default_error_background)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new LoadCategoryPaperList().executeOnExecutor(THREAD_POOL_EXECUTOR, url);
                                Alerter.hide();
                            }
                        })
                        .enableVibration(true)
                        .enableInfiniteDuration(true)
                        .show();

                return;
            }
            updateAdapterData();

            isRequestRunning = false;
        }
    }
}
