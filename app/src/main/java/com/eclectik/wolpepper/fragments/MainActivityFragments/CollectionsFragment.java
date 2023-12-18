package com.eclectik.wolpepper.fragments.MainActivityFragments;


import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTIN_IS_FEATURED_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_COVER_PHOTO_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_DATE_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_DESCRIPTION_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_ID_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_IS_CURATED_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_SHARE_KEY_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_TITLE_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_TOTAL_PHOTOS_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.FilterbyKeys.FILTER_BY_CURATED;
import static com.eclectik.wolpepper.utils.ConstantValues.FilterbyKeys.FILTER_BY_FEATURED;
import static com.eclectik.wolpepper.utils.ConstantValues.HeaderKeys.RATE_LIMIT_REMAINING_HEADER_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_COLOR_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_DOWNLOAD_LINK_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_ID_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_LINKS_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_NAME_OF_USER_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_REGULAR_IMAGE_URL_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_URLS_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USERNAME_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_DETAILS_OBJECT_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_ID_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_PROFILE_PIC_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_PROFILE_PIC_MEDIUM_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.UNSPLASH_COLLECTIONS_PATH_STRING;
import static com.eclectik.wolpepper.utils.ConstantValues.UNSPLASH_CURATED_PATH_STRING;
import static com.eclectik.wolpepper.utils.ConstantValues.UNSPLASH_FEATURED_PATH_STRING;

import android.animation.Animator;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.eclectik.wolpepper.BuildConfig;
import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.adapters.CollectionsAdapter;
import com.eclectik.wolpepper.dataStructures.PaperCollections;
import com.eclectik.wolpepper.listenerInterfaces.RecyclerViewDataUpdateRequester;
import com.eclectik.wolpepper.utils.NetworkUtils;
import com.eclectik.wolpepper.utils.UtilityMethods;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tapadoo.alerter.Alerter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class CollectionsFragment extends Fragment implements RecyclerViewDataUpdateRequester {

    // Root View
    private View rootView;

    // Host Activity Bottom Bar
    private BottomNavigationView bottomBar;

    // Array list of Collections
    private ArrayList<PaperCollections> collectionList = new ArrayList<>();

    // Recycler View To display Collection Adapters Contents
    private RecyclerView recyclerView;

    // Adapter to hold and display collections on recycler view
    private CollectionsAdapter adapter;

    // Current ID
    private String APP_ID;

    // Current page number to retrieve
    private int pageNumber = 1;

    // Filter by String
    private String filterBy = "";

    // To identify if Api rate limit is reached
    private boolean isRateLimitReached = false;

    // To identify if the request is already Running
    private boolean isRequestRunning = false;

    // To prevent repeated api calls on changing "filterBy" save the json in hashmap with respective URLS as keys
    private Map<URL, String> fetchedJsonsListMap = new HashMap<>();

    public CollectionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);

        adapter = new CollectionsAdapter(this, getContext());

        bottomBar = getActivity().findViewById(R.id.bottomBar);

        recyclerView = rootView.findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                if (velocityY > 0) {
                    bottomBar.animate().translationY(bottomBar.getHeight()).start();
                } else {
                    bottomBar.animate().translationY(0).start();
                }
                return false;
            }
        });

        if (UtilityMethods.checkConnection(getContext())){
            loadCollectionsList();
        } else {
            showNoConnectionAlert();
        }

        return rootView;
    }

//    /**
//     * Menu Creation
//     * @param menu - menu
//     * @param inflater - inflater
//     */
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.collections_menu, menu);
//    }
//
//    /**
//     * Switch list items based on the item selected from the menu list i.e latest/random/oldest/popular
//     * @param item - Menu Items
//     * @return -
//     */
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_all:
//                if (!TextUtils.isEmpty(filterBy)) {
//                    filterBy = "";
//                    collectionList.clear();
//                    recyclerView.scrollToPosition(0);
//                    adapter.updateDataSet(null);
//                    adapter.notifyDataSetChanged();
//                    pageNumber = 1;
//                    loadCollectionsList();
//                }
//                break;
//            case R.id.action_featured:
//                if (!filterBy.equals(FILTER_BY_FEATURED)) {
//                    filterBy = FILTER_BY_FEATURED;
//                    collectionList.clear();
//                    recyclerView.scrollToPosition(0);
//                    adapter.updateDataSet(null);
//                    adapter.notifyDataSetChanged();
//                    pageNumber = 1;
//                    loadCollectionsList();
//                }
//                break;
////            case R.id.action_curated:
////                if (!filterBy.equals(FILTER_BY_CURATED)) {
////                    filterBy = FILTER_BY_CURATED;
////                    collectionList.clear();
////                    recyclerView.scrollToPosition(0);
////                    adapter.updateDataSet(null);
////                    adapter.notifyDataSetChanged();
////                    pageNumber = 1;
////                    loadCollectionsList();
////                }
////                break;
//            default:
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }


    /**
     * Interface method used to call to add new items to adapter data set when scroll reaches last item
     */
    @Override
    public void dataSetUpdateRequested() {
        updateAdapterData();
    }

    public void scrollListToTop(){
        if (adapter != null && adapter.getItemCount() > 0) {
            recyclerView.smoothScrollToPosition(0);
        }
    }

    /**
     * Parse the retrieved String to JSON and further Objects
     *
     * @param json - The JSON String to be parsed
     */
    private boolean parseJson(String json) {
        if (getActivity() == null){
            return false;
        }

        try {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            if (getActivity() != null) {
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            }

            if (json.substring(0, 1).equals("{") && new JSONObject(json).has("error")) {
                Toast.makeText(getContext(), json, Toast.LENGTH_LONG).show();
                return false;
            }
            double qualityPercent = getActivity().getSharedPreferences(getString(R.string.preview_quality_base_pref_key), Context.MODE_PRIVATE).getFloat(getString(R.string.preview_quality_percent_pref_key), 45);
            JSONArray rootJsonArray = new JSONArray(json);
            for (int i = 0; i < rootJsonArray.length(); i++) {

                JSONObject currentCollectionJsonObject = rootJsonArray.getJSONObject(i);
                String collectionId = currentCollectionJsonObject.getString(COLLECTION_ID_KEY);
                String collectionTitle = currentCollectionJsonObject.optString(COLLECTION_TITLE_KEY);
                String collectionDescription = currentCollectionJsonObject.getString(COLLECTION_DESCRIPTION_KEY);
                String collectionDate = currentCollectionJsonObject.getString(COLLECTION_DATE_KEY);
                int collectionTotalPhotos = currentCollectionJsonObject.getInt(COLLECTION_TOTAL_PHOTOS_KEY);
                if (collectionTotalPhotos == 0) {
                    continue;
                }
                String collectionShareKey = currentCollectionJsonObject.getString(COLLECTION_SHARE_KEY_KEY);

                boolean isCurated = currentCollectionJsonObject.getBoolean(COLLECTION_IS_CURATED_KEY);
                boolean isFeatured = currentCollectionJsonObject.getBoolean(COLLECTIN_IS_FEATURED_KEY);

                // Cover Photo
                JSONObject coverPhotoJsonObject = currentCollectionJsonObject.optJSONObject(COLLECTION_COVER_PHOTO_KEY);
                String coverPhotoId = "";
                String imageDisplayUrl = "";
                String color = "";

                if (coverPhotoJsonObject != null) {
                    coverPhotoId = coverPhotoJsonObject.getString(IMAGE_ID_KEY);
                    color = coverPhotoJsonObject.getString(IMAGE_COLOR_KEY);
                    imageDisplayUrl = coverPhotoJsonObject.getJSONObject(IMAGE_URLS_KEY).getString(IMAGE_REGULAR_IMAGE_URL_KEY).replace("w=1080", "w=" + Double.toString(displayMetrics.widthPixels * (qualityPercent / 100)));
                }

                JSONObject userJsonObject = currentCollectionJsonObject.getJSONObject(IMAGE_USER_DETAILS_OBJECT_KEY);
                String userId = userJsonObject.getString(IMAGE_USER_ID_KEY);
                String userName = userJsonObject.getString(IMAGE_USERNAME_KEY);
                String nameOfUser = userJsonObject.getString(IMAGE_NAME_OF_USER_KEY);
                String profilePicUrl = userJsonObject.getJSONObject(IMAGE_USER_PROFILE_PIC_KEY).getString(IMAGE_USER_PROFILE_PIC_MEDIUM_KEY);

                String downloadLink = "";
                if (isCurated){
                    downloadLink = currentCollectionJsonObject.optJSONObject(IMAGE_LINKS_KEY).optString(IMAGE_DOWNLOAD_LINK_KEY);
                }

                PaperCollections paperCollections = new PaperCollections();
                paperCollections.setCollectionId(collectionId);
                paperCollections.setCollectionDate(collectionDate);
                paperCollections.setCollectionTitle(collectionTitle);
                paperCollections.setCollectionDescrip(collectionDescription);
                paperCollections.setCollectionShareKey(collectionShareKey);
                paperCollections.setTotalPhotos(collectionTotalPhotos);
                paperCollections.setCoverPhotoId(coverPhotoId);
                paperCollections.setCoverPhotoDisplayUrl(imageDisplayUrl);
                paperCollections.setCollectionUserId(userId);
                paperCollections.setCollectionUserName(userName);
                paperCollections.setCollectionNameOfUser(nameOfUser);
                paperCollections.setCollectionUserProfilePicUrl(profilePicUrl);
                paperCollections.setCurated(isCurated);
                paperCollections.setFeatured(isFeatured);
                paperCollections.setCoverImageColor(color);
                if (isCurated){
                    paperCollections.setCollectionAllImageDownloadUrl(downloadLink);
                }

                collectionList.add(paperCollections);

            }
            return true;
        } catch (JSONException e) {
            Log.e("WOAHH", e.getMessage());
            return false;
        }
    }

    /**
     * Method to add new items to adapter
     */
    private void updateAdapterData() {

        if (adapter.getItemCount() < collectionList.size()) {

            ArrayList<PaperCollections> newCollectionsArrayList = new ArrayList<>(collectionList);
            adapter.updateDataSet(newCollectionsArrayList);
            adapter.notifyDataSetChanged();

        } else {
            if (!isRequestRunning) {
                loadCollectionsList();
            }
        }

    }

    /**
     * Method to create URL and call asyncTask to load Papers Collections
     */
    private void loadCollectionsList() {
        // Run This Code if App_ID is already initialized
        if (APP_ID != null) {

            URL url;
            switch (filterBy) {
                case FILTER_BY_FEATURED:
                    url = NetworkUtils.buildCollectionsFilterUrl(APP_ID, UNSPLASH_FEATURED_PATH_STRING, String.valueOf(pageNumber), "30");
                    break;
                case FILTER_BY_CURATED:
                    url = NetworkUtils.buildCollectionsFilterUrl(APP_ID, UNSPLASH_CURATED_PATH_STRING, String.valueOf(pageNumber), "30");
                    break;
                default:
                    url = NetworkUtils.buildCollectionsUrl(APP_ID, UNSPLASH_COLLECTIONS_PATH_STRING, String.valueOf(pageNumber), "30");
                    break;
            }
            new getCollectionsJsonAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);

            pageNumber++;

            return;
        }

        APP_ID = BuildConfig.UNSPLASH_APP_ID;
        URL url = NetworkUtils.buildCollectionsUrl(APP_ID, UNSPLASH_COLLECTIONS_PATH_STRING, String.valueOf(pageNumber), "10");

        new getCollectionsJsonAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);

        pageNumber++;
    }

    private void showNoConnectionAlert(){
        final Alerter alerter = Alerter.create(getActivity());

        alerter.setTitle(getString(R.string.connection_error))
                .setTextTypeface(ResourcesCompat.getFont(getContext(), R.font.spacemono_regular))
                .setTitleTypeface(ResourcesCompat.getFont(getContext(), R.font.spacemono_bold))
                .setTextAppearance(R.style.alertBody)
                .setTitleAppearance(R.style.alertTitle)
                .setText(getString(R.string.connection_error_description))
                .setBackgroundColorRes(R.color.alert_default_error_background)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (UtilityMethods.checkConnection(getContext())) {
                            loadCollectionsList();
                            Alerter.hide();
                        } else {
                            Alerter.hide();
                            showNoConnectionAlert();
                        }
                    }
                })
                .enableVibration(true)
                .enableInfiniteDuration(true)
                .show();
    }

    /**
     * Async Task to Get List of Papers Collections (Wallpapers Collections)
     */
    private class getCollectionsJsonAsync extends AsyncTask<URL, Void, URL> {
        @Override
        protected void onPreExecute() {
            isRequestRunning = true;
            isRateLimitReached = false;
            YoYo.with(Techniques.SlideInUp)
                    .duration(500)
                    .onStart(new YoYo.AnimatorCallback() {
                        @Override
                        public void call(Animator animator) {
                            rootView.findViewById(R.id.more_loader).setVisibility(View.VISIBLE);
                        }
                    })
                    .playOn(rootView.findViewById(R.id.more_loader));
            super.onPreExecute();
        }

        @Override
        protected URL doInBackground(URL... params) {
            URL url = params[0];

//            Log.e("BGTASK", String.valueOf(pageNumber));

            if (fetchedJsonsListMap.containsKey(url)) {
                parseJson(fetchedJsonsListMap.get(url));
//                Log.e("BGTASK MAP", String.valueOf(pageNumber));
                return null;
            }
            try {
                String json = NetworkUtils.getResponseFromHttpUrl(url, getContext());
//                Log.e("BGTASK FROM LINK", String.valueOf(pageNumber));
                if (json != null && json.equals(RATE_LIMIT_REMAINING_HEADER_KEY)) {
                    isRateLimitReached = true;
                    return url;
                }

                if (parseJson(json)) {
                    fetchedJsonsListMap.put(url, json);
                    return null;
                }

                return url;
            } catch (IOException e) {
                return url;
            }
        }

        @Override
        protected void onPostExecute(final URL url) {
            super.onPostExecute(url);
            if (getContext() == null){
                return;
            }
            isRequestRunning = false;
            rootView.findViewById(R.id.square_loading).setVisibility(View.GONE);
            YoYo.with(Techniques.SlideOutDown)
                    .duration(500)
                    .onEnd(new YoYo.AnimatorCallback() {
                        @Override
                        public void call(Animator animator) {
                            rootView.findViewById(R.id.more_loader).setVisibility(View.GONE);
                        }
                    })
                    .playOn(rootView.findViewById(R.id.more_loader));

            if (isRateLimitReached) {
                UtilityMethods.showRateLimitReachedAlert(getActivity());
                return;
            }

            if (url != null) {
                final Alerter alerter = Alerter.create(getActivity());
                alerter.setTitle("Error")
                        .setTextTypeface(ResourcesCompat.getFont(getContext(), R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(getContext(), R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setText("Connection to the Server Timed out. Try Again!")
                        .setBackgroundColorRes(R.color.alert_default_error_background)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new getCollectionsJsonAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
                                Alerter.hide();
                            }
                        })
                        .enableVibration(true)
                        .enableInfiniteDuration(true)
                        .show();
                return;
            }

            updateAdapterData();
        }
    }

    @Override
    public void onDestroyView() {
        fetchedJsonsListMap.clear();
        super.onDestroyView();
    }
}
