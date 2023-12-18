package com.eclectik.wolpepper.fragments.MainActivityFragments;


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
import static com.eclectik.wolpepper.utils.ConstantValues.OrderByKeys.ORDER_BY_LATEST;
import static com.eclectik.wolpepper.utils.ConstantValues.OrderByKeys.ORDER_BY_OLDEST;
import static com.eclectik.wolpepper.utils.ConstantValues.OrderByKeys.ORDER_BY_POPULAR;
import static com.eclectik.wolpepper.utils.ConstantValues.OrderByKeys.ORDER_BY_RANDOM;
import static com.eclectik.wolpepper.utils.ConstantValues.UNSPLASH_PHOTO_PATH_STRING;

import android.animation.Animator;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.eclectik.wolpepper.adapters.NewPapersAdapter;
import com.eclectik.wolpepper.dataStructures.Papers;
import com.eclectik.wolpepper.executors.AppExecutors;
import com.eclectik.wolpepper.listenerInterfaces.RecyclerViewDataUpdateRequester;
import com.eclectik.wolpepper.utils.ConstantValues;
import com.eclectik.wolpepper.utils.NetworkUtils;
import com.eclectik.wolpepper.utils.UtilityMethods;
import com.eclectik.wolpepper.wolpepper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tapadoo.alerter.Alerter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewPictureFragment extends Fragment implements RecyclerViewDataUpdateRequester {

    private ArrayList<Papers> papersArrayList = new ArrayList<>();

    private BottomNavigationView bottomBar;

    private View rootView;

    private RecyclerView recyclerView;

    private NewPapersAdapter adapter;

    private volatile boolean isRequestRunning = false;

    private int pageNumber = 1;

    private String orderBy = ORDER_BY_LATEST;

    private boolean isRateLimitReached = false;


    // To prevent repeated api calls on changing "filterBy" save the json in hashmap with respective URLS as keys
    private Map<URL, String> fetchedJsonsListMap = new HashMap<>();

    public NewPictureFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        setHasOptionsMenu(true);

        bottomBar = getActivity().findViewById(R.id.bottomBar);

        setUpRecyclerView();
        loadPapersList();

        return rootView;
    }

    private void setUpRecyclerView() {

        recyclerView = rootView.findViewById(R.id.recycler_view);

        adapter = new NewPapersAdapter(this, getActivity());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
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
    }

    /**
     * update list if like/unlike status is changed
     */
    @Override
    public void onResume() {
        if (adapter != null && !isHidden()) {
            adapter.updateMuzeiStatusOfWholeDataSet();
            adapter.notifyDataSetChanged();

        }
        super.onResume();
    }

    public void notifyOrientationFilterChanged() {
        if (isAdded()) {
            papersArrayList.clear();
            pageNumber = 1;
            getActivity().getSupportFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }

    /**
     * Menu Creation
     *
     * @param menu     - menu
     * @param inflater - inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sorting_menu, menu);
    }

    /**
     * Switch list items based on the item selected from the menu list i.e latest/random/oldest/popular
     *
     * @param item - Menu Items
     * @return -
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_latest:
                if (!orderBy.equals(ORDER_BY_LATEST)) {
                    orderBy = ORDER_BY_LATEST;
                    recyclerView.scrollToPosition(0);
                    papersArrayList.clear();
                    adapter.updateDataSet(null);
                    adapter.notifyDataSetChanged();
                    pageNumber = 1;
                    loadPapersList();
                }
                break;
            case R.id.action_oldest:
                if (!orderBy.equals(ORDER_BY_OLDEST)) {
                    orderBy = ORDER_BY_OLDEST;
                    recyclerView.scrollToPosition(0);
                    papersArrayList.clear();
                    adapter.updateDataSet(null);
                    adapter.notifyDataSetChanged();
                    pageNumber = 1;
                    loadPapersList();
                }
                break;
            case R.id.action_random:
                if (!orderBy.equals(ORDER_BY_RANDOM)) {
                    orderBy = ORDER_BY_RANDOM;
                    recyclerView.scrollToPosition(0);
                    papersArrayList.clear();
                    adapter.updateDataSet(null);
                    adapter.notifyDataSetChanged();
                    pageNumber = 1;
                    loadPapersList();
                }
                break;
            case R.id.action_popular:
                if (!orderBy.equals(ORDER_BY_POPULAR)) {
                    orderBy = ORDER_BY_POPULAR;
                    recyclerView.scrollToPosition(0);
                    papersArrayList.clear();
                    adapter.updateDataSet(null);
                    adapter.notifyDataSetChanged();
                    pageNumber = 1;
                    loadPapersList();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Interface method used to call to add new items to adapter data set when scroll reaches last item
     */
    @Override
    public void dataSetUpdateRequested() {
        updateAdapterData();
    }

    public void scrollListToTop() {
        if (adapter != null && adapter.getItemCount() > 0) {
            recyclerView.smoothScrollToPosition(0);
        }
    }

    /**
     * Parse the retrieved String to JSON and further Objects
     *
     * @param json - The JSON String to be parsed
     */
    private boolean parseJson(String json, boolean isFromHashMap) {
        if (getActivity() == null) {
            return false;
        }

        boolean isPortraitOnly = ((wolpepper) getActivity().getApplication()).isPortraitOnly();

        try {
            if (json.substring(0, 1).equals("{") && new JSONObject(json).has("error")) {
                Toast.makeText(getContext(), json, Toast.LENGTH_LONG).show();
                return false;
            }

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
                String color = jsonObject.optString(ConstantValues.IMAGE_COLOR_KEY, "#FFFFFF");
                int height = jsonObject.getInt(IMAGE_HEIGHT_KEY);
                int width = jsonObject.getInt(IMAGE_WIDTH_KEY);

                if (isPortraitOnly && height < width + 500) {
                    continue;
                }

                boolean isLiked = jsonObject.getBoolean(ConstantValues.IMAGE_LIKED_BY_USER_KEY);


                /* UPDATE LIKE STATUS IN APP CONTEXTS LIST */
                if (getActivity() != null) {
                    if (isFromHashMap) {
                        isLiked = ((wolpepper) getActivity().getApplication()).isImageLiked(imageId);
                    } else {
                        ((wolpepper) getActivity().getApplication()).updateIdLikeStatusMap(imageId, isLiked);
                    }
                }

                //User Details from json object
                JSONObject userDetails = jsonObject.getJSONObject(ConstantValues.IMAGE_USER_DETAILS_OBJECT_KEY);
                String name = userDetails.getString(ConstantValues.IMAGE_NAME_OF_USER_KEY);
                String userName = userDetails.getString(ConstantValues.IMAGE_USERNAME_KEY);
                String userId = userDetails.getString(ConstantValues.IMAGE_USER_ID_KEY);
                String userProfileImage = userDetails.getJSONObject(ConstantValues.IMAGE_USER_PROFILE_PIC_KEY).getString(ConstantValues.IMAGE_USER_PROFILE_PIC_MEDIUM_KEY);
                String userProfileLink = userDetails.getJSONObject(IMAGE_LINKS_KEY).getString(IMAGE_HTML_LINK_KEY);
                String userBio = userDetails.optString(IMAGE_USER_BIO_KEY, "No Bio available");
                int totalLikes = userDetails.optInt(IMAGE_USER_TOTAL_LIKES, 0);
                int totalPhotos = userDetails.optInt(IMAGE_USER_TOTAL_PHOTOS, 0);
                int totalCollections = userDetails.optInt(IMAGE_USER_TOTAL_COLLECTIONS, 0);

                //Image Url Json Object
                JSONObject imageUrlsObject = jsonObject.getJSONObject(ConstantValues.IMAGE_URLS_KEY);
                String imageUrl = imageUrlsObject.getString(ConstantValues.IMAGE_REGULAR_IMAGE_URL_KEY);
                String fullImageUrl = imageUrlsObject.getString(ConstantValues.IMAGE_FULL_IMAGE_URL_KEY);
                String rawImageUrl = imageUrlsObject.getString(ConstantValues.IMAGE_RAW_IMAGE_URL_KEY);

                // Image API Download section
                String apiDownloadSpot = jsonObject.getJSONObject(IMAGE_LINKS_KEY).getString(IMAGE_DOWNLOAD_API_CALL_LINK_KEY);

                /* Setting preview Quality */
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
                paper.setInMuzeiList(UtilityMethods.isInMuzeiActiveList(getContext(), imageId));
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
        } else if (adapter.getItemCount() + 10 < papersArrayList.size() && !recyclerView.isComputingLayout()) {
            int currentItemCount = adapter.getItemCount();
            newPapersDataList.addAll(papersArrayList.subList(0, currentItemCount + 10));
            adapter.updateDataSet(newPapersDataList);
            adapter.notifyDataSetChanged();
        } else {
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
        String APP_ID = BuildConfig.UNSPLASH_APP_ID;
        URL url;
        if (orderBy.equals(ORDER_BY_RANDOM)) {
            url = NetworkUtils.buildRandomPhotosListUrl(APP_ID, String.valueOf(pageNumber), "30");
        } else {
            url = NetworkUtils.buildNewPhotosUrl(APP_ID, UNSPLASH_PHOTO_PATH_STRING, String.valueOf(pageNumber), "30", orderBy);
        }
        getJson(url);
        pageNumber++;
    }

    private void getJson(URL url){
        if (getContext() == null) return;
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

        AppExecutors.getInstance().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                String json = "";
                if (fetchedJsonsListMap.containsKey(url)) {
                    parseJson(fetchedJsonsListMap.get(url), true);
                    updateUi(url, false);
                    return;
                }
                try {
                    json = NetworkUtils.getResponseFromHttpUrl(url, getContext());

                    if (json != null && json.equals(RATE_LIMIT_REMAINING_HEADER_KEY)) {
                        isRateLimitReached = true;
                        updateUi(url, false);
                    } else if (parseJson(json, false) && getActivity() != null) {
                        fetchedJsonsListMap.put(url, json);
                        updateUi(url, false);
                    }
                } catch (Exception e) {
                    // THIS IS CALLED WHEN ANY ERROR IS RETURNED IN JSON LIKE FILE NOT FOUND DUE TO SOME ERROR OR API EXCEEDED OR TIMEOUT / PERFORM RELATED UI UPDATE
                    updateUi(url, true);
                }
            }
        });
    }

    private void updateUi(URL url, boolean showRetryAlert){
        if (getContext() == null) {
            return;
        }

        AppExecutors.getInstance().mainThread().execute(new Runnable() {
            @Override
            public void run() {
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

                if (showRetryAlert) {
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
                                    getJson(url);
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
        });
    }

}
