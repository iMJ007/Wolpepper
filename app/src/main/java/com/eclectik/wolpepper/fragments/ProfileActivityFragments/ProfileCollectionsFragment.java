package com.eclectik.wolpepper.fragments.ProfileActivityFragments;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.activities.ProfileMainActivity;
import com.eclectik.wolpepper.adapters.CollectionsAdapter;
import com.eclectik.wolpepper.dataStructures.PaperCollections;
import com.eclectik.wolpepper.listenerInterfaces.AsyncDataLoadCompletionListener;
import com.eclectik.wolpepper.listenerInterfaces.RecyclerViewDataUpdateRequester;
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

import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTIN_IS_FEATURED_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_COVER_PHOTO_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_DATE_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_DESCRIPTION_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_ID_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_IS_CURATED_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_SHARE_KEY_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_TITLE_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.CollectionKeys.COLLECTION_TOTAL_PHOTOS_KEY;
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
import static com.eclectik.wolpepper.utils.ConstantValues.ProfileFragTags.COLLECTION_FRAG_TAG;
import static com.eclectik.wolpepper.utils.ConstantValues.UNSPLASH_COLLECTIONS_PATH_STRING;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileCollectionsFragment extends Fragment implements RecyclerViewDataUpdateRequester, AsyncDataLoadCompletionListener {

    View rootView;

    // Array list of Collections
    private ArrayList<PaperCollections> collectionList = new ArrayList<>();

    // Recycler View To display Collection Adapters Contents
    private RecyclerView recyclerView;

    // Adapter to hold and display collections on recycler view
    private CollectionsAdapter adapter;

    // Current ID
    private String APP_ID;

    private String userID;

    // Current page number to retrieve
    private int pageNumber = 1;

    // To identify if Api rate limit is reached
    private boolean isRateLimitReached = false;

    // To identify if the request is already Running
    private boolean isRequestRunning = false;

    private int totalUserCollections = -1;

    public ProfileCollectionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        rootView =  inflater.inflate(R.layout.fragment_profile, container, false);

        APP_ID = ((wolpepper) getActivity().getApplication()).getAPP_ID();

        if (getActivity().getIntent().hasExtra(IMAGE_USERNAME_KEY)) {
            userID = getActivity().getIntent().getStringExtra(IMAGE_USERNAME_KEY);
        }

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        adapter = new CollectionsAdapter(this, getContext());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(adapter);

        ((ProfileMainActivity)getActivity()).setAsyncDataLoadCompletionListener(this, COLLECTION_FRAG_TAG);

        return rootView;
    }

    /**
     * Show Toast if user have no collection only when fragment is visible.
     * @param isVisibleToUser-
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (totalUserCollections == 0 && isVisibleToUser){ // if user dont have any collection
            Alerter.create(getActivity())
                    .setTextTypeface(ResourcesCompat.getFont(getContext(), R.font.spacemono_regular))
                    .setTitleTypeface(ResourcesCompat.getFont(getContext(), R.font.spacemono_bold))
                    .setTextAppearance(R.style.alertBody)
                    .setTitleAppearance(R.style.alertTitle)
                    .setTitle(R.string.something_went_wrong_error)
                    .setText(R.string.no_collection_desc)
                    .show();
            return;
        }
        super.setUserVisibleHint(isVisibleToUser);
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
    private boolean parseJson(String json) {
        if (getActivity() == null){
            return false;
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (getActivity() != null) {
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        }

        try {
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
                    downloadLink = currentCollectionJsonObject.getJSONObject(IMAGE_LINKS_KEY).getString(IMAGE_DOWNLOAD_LINK_KEY);
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
            Log.e("JSONERROR", e.getMessage());
            return false;
        }
    }

    /**
     * Method to add new items to adapter
     */
    private void updateAdapterData() {
        ArrayList<PaperCollections> newCollectionsArrayList = new ArrayList<>();

        if (adapter.getItemCount() == 0) {

            if (collectionList.size() < 10){
                newCollectionsArrayList.addAll(collectionList);
            } else {
                newCollectionsArrayList.addAll(collectionList.subList(0, 10));
            }

            adapter.updateDataSet(newCollectionsArrayList);
            adapter.notifyDataSetChanged();

        } else if (adapter.getItemCount() + 10 < collectionList.size() && !recyclerView.isComputingLayout()) {

            int currentItemCount = adapter.getItemCount();
            newCollectionsArrayList.addAll(collectionList.subList(0, currentItemCount + 10));
            adapter.updateDataSet(newCollectionsArrayList);
            adapter.notifyDataSetChanged();

        } else if (adapter.getItemCount() < totalUserCollections){
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
            if (totalUserCollections == 0){ // if user dont have any collection
                return;
            }
            URL url = NetworkUtils.buildUserContentUrl(userID, UNSPLASH_COLLECTIONS_PATH_STRING, String.valueOf(pageNumber), "30", APP_ID);
            new getCollectionsJsonAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
            pageNumber++;

        }
    }

    /**
     * Listener method triggered when host profile Activity finish loading profile data and total collections
     */
    @Override
    public void dataLoadComplete() {
        loadCollectionsList();
    }

    /**
     * Async Task to Get List of Papers Collections (Wallpapers Collections)
     */
    private class getCollectionsJsonAsync extends AsyncTask<URL, Void, URL> {
        @Override
        protected void onPreExecute() {
            if (getContext() == null || getActivity() == null) {
                cancel(true);
            }
            isRateLimitReached = false;
            isRequestRunning = true;
            super.onPreExecute();
        }

        @Override
        protected URL doInBackground(URL... params) {
            URL url = params[0];
            if (getContext() == null){
                return null;
            }
            totalUserCollections = ((ProfileMainActivity) getActivity()).getCollectionCount();

            if (totalUserCollections == 0){
                return null;
            }
            try {
                String json = NetworkUtils.getResponseFromHttpUrl(url, getContext());
                if (json != null && json.equals(RATE_LIMIT_REMAINING_HEADER_KEY)) {
                    isRateLimitReached = true;
                    return url;
                }

                if (parseJson(json)) {
                    return null;
                }

                return url;
            } catch (IOException e) {
                return url;
            } catch (NullPointerException e){
                return url;
            }
        }

        @Override
        protected void onPostExecute(final URL url) {
            super.onPostExecute(url);
            if (getActivity() == null){
                return;
            }
            isRequestRunning = false;

            if (totalUserCollections == 0 && getUserVisibleHint()){
                Alerter.create(getActivity())
                        .setTextTypeface(ResourcesCompat.getFont(getContext(), R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(getContext(), R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setTitle(R.string.something_went_wrong_error)
                        .setText(R.string.no_collection_desc)
                        .show();
                return;
            }

            if (isRateLimitReached) {
                UtilityMethods.showRateLimitReachedAlert(getActivity());
                return;
            }

            if (url != null) {
                final Alerter alerter = Alerter.create(getActivity());
                alerter.setTitle(R.string.connection_error)
                        .setTextTypeface(ResourcesCompat.getFont(getContext(), R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(getContext(), R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setText(R.string.connection_error_description)
                        .setBackgroundColorRes(R.color.alert_default_error_background)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new getCollectionsJsonAsync().execute(url);
                                alerter.hide();
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
}
