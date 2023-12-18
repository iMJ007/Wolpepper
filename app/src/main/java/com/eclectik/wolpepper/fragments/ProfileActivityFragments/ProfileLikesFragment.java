package com.eclectik.wolpepper.fragments.ProfileActivityFragments;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.activities.ProfileMainActivity;
import com.eclectik.wolpepper.adapters.NewPapersAdapter;
import com.eclectik.wolpepper.dataStructures.Papers;
import com.eclectik.wolpepper.listenerInterfaces.AsyncDataLoadCompletionListener;
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

import static com.eclectik.wolpepper.utils.ConstantValues.HeaderKeys.RATE_LIMIT_REMAINING_HEADER_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_DOWNLOAD_API_CALL_LINK_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_LINKS_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USERNAME_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_WIDTH_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.ProfileFragTags.LIKE_FRAG_TAG;
import static com.eclectik.wolpepper.utils.ConstantValues.UNSPLASH_USER_LIKES_PATH_STRING;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileLikesFragment extends Fragment implements RecyclerViewDataUpdateRequester, AsyncDataLoadCompletionListener {

    View rootView;

    private ArrayList<Papers> papersArrayList = new ArrayList<>();

    private RecyclerView recyclerView;

    private NewPapersAdapter adapter;

    private String APP_ID;

    private String userID;

    private boolean isRequestRunning = false;

    private int pageNumber = 1;

    private boolean isRateLimitReached = false;

    private int totalLikedPhotos = -1;

    public ProfileLikesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        APP_ID = ((wolpepper) getActivity().getApplication()).getAPP_ID();

        if (getActivity().getIntent().hasExtra(IMAGE_USERNAME_KEY)) {
            userID = getActivity().getIntent().getStringExtra(IMAGE_USERNAME_KEY);
        }

        recyclerView = rootView.findViewById(R.id.recycler_view);

        adapter = new NewPapersAdapter(this, getActivity());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(adapter);

        // Set Listener to get Total likes on completion
        ((ProfileMainActivity)getActivity()).setAsyncDataLoadCompletionListener(this, LIKE_FRAG_TAG);

        return rootView;
    }

    /**
     * update list if like/unlike status is changed
     */
    @Override
    public void onResume() {
        if (adapter != null && !isHidden()) {
            adapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    /**
     * Show Toast if user have no collection only when fragment is visible.
     *
     * @param isVisibleToUser - is this fragment on the screen or scrolled
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (totalLikedPhotos == 0 && isVisibleToUser) { // if user dont have any collection
            Alerter.create(getActivity())
                    .setTextTypeface(ResourcesCompat.getFont(getContext(), R.font.spacemono_regular))
                    .setTitleTypeface(ResourcesCompat.getFont(getContext(), R.font.spacemono_bold))
                    .setTextAppearance(R.style.alertBody)
                    .setTitleAppearance(R.style.alertTitle)
                    .setTitle(R.string.something_went_wrong_error)
                    .setText(R.string.no_liked_images_desc)
                    .show();
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
        try {
            if (json == null || TextUtils.isEmpty(json)) {
                return false;
            }
            if (json.substring(0, 1).equals("{") && new JSONObject(json).has("error")) {
                Toast.makeText(getContext(), json, Toast.LENGTH_LONG).show();
                return false;
            }

            boolean isPortraitOnly = ((wolpepper) getActivity().getApplication()).isPortraitOnly();

            double qualityPercent = getActivity().getSharedPreferences(getString(R.string.preview_quality_base_pref_key), Context.MODE_PRIVATE).getFloat(getString(R.string.preview_quality_percent_pref_key), 45);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            if (getActivity() != null) {
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            }

            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                //Current Json Object
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String imageId = jsonObject.getString(ConstantValues.IMAGE_ID_KEY);
                String date = jsonObject.getString(ConstantValues.IMAGE_CREATED_DATE_KEY);
                String color = jsonObject.getString(ConstantValues.IMAGE_COLOR_KEY);
                int height = jsonObject.getInt(ConstantValues.IMAGE_HEIGHT_KEY);
                int width = jsonObject.getInt(IMAGE_WIDTH_KEY);

                if (isPortraitOnly && height < width + 500) {
                    continue;
                }

                boolean isLiked = jsonObject.getBoolean(ConstantValues.IMAGE_LIKED_BY_USER_KEY);

                /* UPDATE LIKE STATUS IN APP CONTEXTS LIST */
                if (getActivity() != null) {
                    ((wolpepper) getActivity().getApplication()).updateIdLikeStatusMap(imageId, isLiked);
                }

                //User Details from json object
                JSONObject userDetails = jsonObject.getJSONObject(ConstantValues.IMAGE_USER_DETAILS_OBJECT_KEY);
                String name = userDetails.getString(ConstantValues.IMAGE_NAME_OF_USER_KEY);
                String userName = userDetails.getString(ConstantValues.IMAGE_USERNAME_KEY);
                String userId = userDetails.getString(ConstantValues.IMAGE_USER_ID_KEY);
                String userProfileImage = userDetails.getJSONObject(ConstantValues.IMAGE_USER_PROFILE_PIC_KEY).getString(ConstantValues.IMAGE_USER_PROFILE_PIC_MEDIUM_KEY);

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
                paper.setInMuzeiList(UtilityMethods.isInMuzeiActiveList(getContext(), imageId));
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
        ArrayList<Papers> newPapersDataList = new ArrayList<>();

        if (adapter.getItemCount() == 0) {        // Execute for first time when adapter is empty
            if (papersArrayList.size() < 10) {
                newPapersDataList.addAll(papersArrayList);
            } else {
                newPapersDataList.addAll(papersArrayList.subList(0, 10));
            }
            adapter.updateDataSet(newPapersDataList);
            adapter.notifyDataSetChanged();

        } else if (adapter.getItemCount() < papersArrayList.size() && !recyclerView.isComputingLayout()) {       // Execute when arraylist have items
            int currentItemCount = adapter.getItemCount();

            if (currentItemCount + 10 < papersArrayList.size()) {                                        //Check if current items in adapter is less than 10 items in papers array list. IF ITS NOT then add the few remaining items to adapter
                newPapersDataList.addAll(papersArrayList.subList(0, currentItemCount + 10));
            } else {
                newPapersDataList.addAll(papersArrayList);
            }

            adapter.updateDataSet(newPapersDataList);
            adapter.notifyDataSetChanged();

        } else if (adapter.getItemCount() < totalLikedPhotos) {
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
            if (totalLikedPhotos == 0) { // If user dont have photos then dont request api
                return;
            }
            URL url = NetworkUtils.buildUserContentUrl(userID, UNSPLASH_USER_LIKES_PATH_STRING, String.valueOf(pageNumber), "30", APP_ID);
            new GetLikedPhotoAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
            Log.e(getActivity().getClass().getSimpleName(), pageNumber + "");
            pageNumber++;
        }
    }

    /**
     * Listener method triggered when host profile Activity finish loading profile data and total Likes
     */
    @Override
    public void dataLoadComplete() {
        loadPapersList();
    }

    /**
     * Async Task to Get List of Papers (Wallpapers)
     */
    private class GetLikedPhotoAsync extends AsyncTask<URL, Void, URL> {

        @Override
        protected void onPreExecute() {
            if (getActivity() == null){
                cancel(true);
            }
            isRateLimitReached = false;
            isRequestRunning = true;
            super.onPreExecute();
        }

        @Override
        protected URL doInBackground(URL... params) {
            if (getContext() == null){
                return null;
            }
            totalLikedPhotos = ((ProfileMainActivity) getActivity()).getLikeCount();

            if (totalLikedPhotos == 0) {
                return null;
            }

            URL url = params[0];

            try {
                String json = "";
                if (getContext() != null && !getActivity().isFinishing()) {
                    json = NetworkUtils.getResponseFromHttpUrl(url, getContext());
                }
                if (json != null && json.equals(RATE_LIMIT_REMAINING_HEADER_KEY)) {
                    isRateLimitReached = true;
                    return url;
                }
                // Return Null if json is parsed successfully
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
            if (getActivity() == null) {
                return;
            }
            isRequestRunning = false;


            if (totalLikedPhotos == 0 && getUserVisibleHint()) {
                Alerter.create(getActivity())
                        .setTextTypeface(ResourcesCompat.getFont(getContext(), R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(getContext(), R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setTitle(getString(R.string.something_went_wrong_error))
                        .setText(getString(R.string.no_liked_images_desc))
                        .show();
            }

            if (isRateLimitReached) {
                UtilityMethods.showRateLimitReachedAlert(getActivity());
                return;
            }

            if (url != null) {
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
                                new GetLikedPhotoAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
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
}
