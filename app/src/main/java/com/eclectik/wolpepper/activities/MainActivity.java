package com.eclectik.wolpepper.activities;

import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_HTML_LINK_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_LINKS_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_LOCATION_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_NAME_OF_USER_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USERNAME_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_BIO_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_PROFILE_PIC_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_PROFILE_PIC_LARGE_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_TOTAL_COLLECTIONS;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_TOTAL_LIKES;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_USER_TOTAL_PHOTOS;
import static com.eclectik.wolpepper.utils.ConstantValues.PreferencesKeys.IMAGE_CACHE_PREF_DATE_BASE_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.PreferencesKeys.IMAGE_CACHE_PREF_DATE_KEY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;
import com.diegodobelo.expandingview.ExpandingItem;
import com.diegodobelo.expandingview.ExpandingList;
import com.eclectik.wolpepper.BuildConfig;
import com.eclectik.wolpepper.GlideApp;
import com.eclectik.wolpepper.MuzeiSettingsActivity;
import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.activities.settingsActivities.About;
import com.eclectik.wolpepper.activities.settingsActivities.PreviewQualityActivity;
import com.eclectik.wolpepper.activities.settingsActivities.SettingsActivity;
import com.eclectik.wolpepper.activities.settingsActivities.StorageSettingsActivity;
import com.eclectik.wolpepper.dataStructures.UnsplashUser;
import com.eclectik.wolpepper.fragments.MainActivityFragments.CategoriesFragment;
import com.eclectik.wolpepper.fragments.MainActivityFragments.CollectionsFragment;
import com.eclectik.wolpepper.fragments.MainActivityFragments.NewPictureFragment;
import com.eclectik.wolpepper.utils.ConstantValues;
import com.eclectik.wolpepper.utils.NetworkUtils;
import com.eclectik.wolpepper.utils.UtilityMethods;
import com.eclectik.wolpepper.utils.receivers.GeneralReceiver;
import com.eclectik.wolpepper.wolpepper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.tapadoo.alerter.Alerter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

@SuppressLint("ApplySharedPref")
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private UnsplashUser user = new UnsplashUser();

    NewPictureFragment newPictureFragment = new NewPictureFragment();
    CollectionsFragment collectionsFragment = new CollectionsFragment();
//    CuratedFragment curatedFragment = new CuratedFragment();
    CategoriesFragment categoriesFragment = new CategoriesFragment();

    private String APP_ID, APP_SECRET;

    private final String NEW_PICTURE_FRAG_TAG = "newPictureFragment";
    private final String COLLECTION_FRAG_TAG = "collectionsFragment";
    private final String CURATED_FRAG_TAG = "curatedFragment";
    private final String CATEGORIES_FRAGMENT_TAG = "categoriesFragment";

    private BottomNavigationView bottomBar;

    /* Arbitrary integer for activity request code */
    private int SETTINGS_REQUEST_CODE = 1001;

    private DrawerLayout drawer;
    private View headerLayout;

    private ActionBarDrawerToggle toggle;

    // Used to remove frags on save instance and prevent duplicate frags
    private FragmentManager manager;

    private Alerter persistentPermissionNeededAlert;

    private ExpandingList expandingList;

    /* objects related to current default download format */
    private String currentDefault;
    private ArrayList<String> listOfImageTypes = new ArrayList<>();
    private SharedPreferences preferences;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        MobileAds.setAppVolume(0);
//        MobileAds.setAppMuted(true);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setUpReceiver();

        // REQUEST FOR PERMISSION
        requestPermission();

        checkDateAndClearPreviousPrefs();

        /* FUll screen Code */
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }


        /* Get App Id and store it in App context */
        getAppId();

        /* Drawer Layout stuff */
        drawer = findViewById(R.id.drawer_layout);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);

        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);

        headerLayout = navigationView.getHeaderView(0);

        /* expanding List object */
        expandingList = headerLayout.findViewById(R.id.expanding_list_main);

        // If user is already logged in load saved data else set login intent
        if (UtilityMethods.isUserLoggedIn(this)) {
            loadUserDetails();
            setLoggedInStateClicks();
        } else {
            setLoginClick();
        }

        manager = getSupportFragmentManager();

        // If activity is resuming then load frags from saved instance
        if (savedInstanceState != null) {
            getFragmentsFromSavedInstance();
        }

        bottomBar = findViewById(R.id.bottomBar);

        setBottomBarSelectedItemListener();

        setBottomBarReselectedItemListener();

        setUpSideNavExpandLayouts();
        bottomBar.setSelectedItemId(R.id.navigation_collections);
        bottomBar.setSelectedItemId(R.id.navigation_new);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] == PackageManager.PERMISSION_DENIED) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!shouldShowRequestPermissionRationale(permission)) {
                        requestPermission();
                    } else {
                        loadPersistentPermissionAlert();
                    }
                }
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (SETTINGS_REQUEST_CODE == requestCode) {
            requestPermission();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getData() != null) { //use toString() and use contains("error")
            if (intent.getData().toString().contains("error")) {
                Alerter.create(MainActivity.this)
                        .setTextTypeface(ResourcesCompat.getFont(MainActivity.this, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(MainActivity.this, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setTitle("Error!!!")
                        .setText("Access Denied!")
                        .setBackgroundColorRes(R.color.alert_default_error_background)
                        .show();
            } else {
                String code = intent.getData().getQueryParameter("code");
                new LoginAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, code);
            }

        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_solid_colors:
                startActivity(new Intent(MainActivity.this, SolidColors.class));
                break;
            case R.id.nav_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.nav_about:
                startActivity(new Intent(MainActivity.this, About.class));
                break;
            case R.id.nav_upgrade:
                startActivity(new Intent(MainActivity.this, UpgradeAppActivity.class));
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        drawer.removeDrawerListener(toggle);
        super.onDestroy();
    }

    SearchView searchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        searchView.setIconified(false);
        return super.onOptionsItemSelected(item);
    }

    private void setUpReceiver(){
        ComponentName receiver = new ComponentName(getApplicationContext(), GeneralReceiver.class);
        PackageManager pm = getPackageManager();
        if (pm.getComponentEnabledSetting(receiver) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED){
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    /**
     * get App Id From Server
     */
    private void getAppId() {
        APP_ID = BuildConfig.UNSPLASH_APP_ID;
        APP_SECRET = BuildConfig.UNSPLASH_APP_SECRET;
        ((wolpepper) getApplication()).setAPP_ID(APP_ID);
        ((wolpepper) getApplication()).setAPP_SECRET(APP_SECRET);
    }

    /**
     * Set these click listener if user is already Signed In
     */
    private void setLoggedInStateClicks() {
        /* Go to profile Listener */
        headerLayout.findViewById(R.id.signed_in_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileMainActivity.class);
                intent.putExtra(IMAGE_NAME_OF_USER_KEY, user.getNameOfUser());
                intent.putExtra(ConstantValues.IMAGE_USER_PROFILE_PIC_KEY, user.getProfilePicUrl());
                intent.putExtra(ConstantValues.IMAGE_USERNAME_KEY, user.getUserName());
                intent.putExtra(ConstantValues.IMAGE_USER_TOTAL_PHOTOS, user.getTotalPhotos());
                intent.putExtra(IMAGE_USER_TOTAL_LIKES, user.getTotalLikes());
                intent.putExtra(ConstantValues.IMAGE_USER_TOTAL_COLLECTIONS, user.getTotalCollections());
                intent.putExtra(ConstantValues.IMAGE_USER_PROFILE_LINK_KEY, user.getProfileUrl());
                intent.putExtra(ConstantValues.IMAGE_USER_BIO_KEY, user.getBio());
                intent.putExtra(ConstantValues.IMAGE_REGULAR_IMAGE_URL_KEY, user.getHeaderImageUrl());
                intent.putExtra(ConstantValues.IMAGE_LOCATION_KEY, user.getUserLocation());
                startActivity(intent);
            }
        });

        /* Log out click listener */
        ImageView logOutButton = headerLayout.findViewById(R.id.log_out_button);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    /**
     * Setup Login Click if user is not Signed in
     */
    public void setLoginClick() {

        LinearLayout login_intent = headerLayout.findViewById(R.id.login_Button);


        login_intent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this)
                        .customView(R.layout.dialog_login, false)
                        .show();

                dialog.findViewById(R.id.dialog_login_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (APP_ID != null) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(UtilityMethods.getLoginUrl(MainActivity.this, APP_ID))));
                            dialog.dismiss();
                            drawer.closeDrawers();
                        }
                    }
                });
            }
        });


    }

    /**
     * Load saved User Data
     */
    private void loadUserDetails() {
        if (isFinishing()){
            return;
        }

        headerLayout.findViewById(R.id.login_Button).setVisibility(View.GONE);
        String userDetails = UtilityMethods.getLoggedInUserDetailsJson(this);

        if (TextUtils.isEmpty(userDetails)){
            Alerter.create(this)
                    .setTextTypeface(ResourcesCompat.getFont(MainActivity.this, R.font.spacemono_regular))
                    .setTitleTypeface(ResourcesCompat.getFont(MainActivity.this, R.font.spacemono_bold))
                    .setTextAppearance(R.style.alertBody)
                    .setTitleAppearance(R.style.alertTitle)
                    .setTitle("Alert")
                    .setText("Corrupt login Data. clear app data and retry!")
                    .setBackgroundColorRes(R.color.alert_default_error_background)
                    .show();
            headerLayout.findViewById(R.id.signed_in_layout).setVisibility(View.VISIBLE);
            return;
        }

        try {

            JSONObject jsonObject = new JSONObject(userDetails);
            user.setNameOfUser(jsonObject.getString(IMAGE_NAME_OF_USER_KEY));
            user.setProfilePicUrl(jsonObject.getJSONObject(IMAGE_USER_PROFILE_PIC_KEY).getString(IMAGE_USER_PROFILE_PIC_LARGE_KEY));
            user.setUserName(jsonObject.getString(IMAGE_USERNAME_KEY));
            user.setTotalPhotos(jsonObject.getInt(IMAGE_USER_TOTAL_PHOTOS));
            user.setTotalLikes(jsonObject.getInt(IMAGE_USER_TOTAL_LIKES));
            user.setTotalCollections(jsonObject.getInt(IMAGE_USER_TOTAL_COLLECTIONS));
            user.setProfileUrl(jsonObject.getJSONObject(IMAGE_LINKS_KEY).getString(IMAGE_HTML_LINK_KEY));
            user.setBio(jsonObject.getString(IMAGE_USER_BIO_KEY));
            user.setUserLocation(jsonObject.getString(IMAGE_LOCATION_KEY));
            user.setHeaderImageUrl(null);

            ((TextView) headerLayout.findViewById(R.id.name_of_user_tv)).setText(jsonObject.getString("name"));
            ((TextView) headerLayout.findViewById(R.id.user_email_tv)).setText(jsonObject.getString("email"));
            GlideApp.with(this).load(jsonObject.getJSONObject("profile_image").getString("medium")).into((ImageView) headerLayout.findViewById(R.id.user_profile_image));

        } catch (JSONException e) {
            e.printStackTrace();
            Alerter.create(this)
                    .setTextTypeface(ResourcesCompat.getFont(MainActivity.this, R.font.spacemono_regular))
                    .setTitleTypeface(ResourcesCompat.getFont(MainActivity.this, R.font.spacemono_bold))
                    .setTextAppearance(R.style.alertBody)
                    .setTitleAppearance(R.style.alertTitle)
                    .setTitle("Alert")
                    .setText("Corrupt login Data. clear app data and retry!")
                    .setBackgroundColorRes(R.color.alert_default_error_background)
                    .show();
        }

        headerLayout.findViewById(R.id.signed_in_layout).setVisibility(View.VISIBLE);
    }

    private void setBottomBarSelectedItemListener() {
        bottomBar.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_new:
                    FragmentTransaction newPicFragmentTransaction = getSupportFragmentManager().beginTransaction();
                    newPicFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                    if (newPictureFragment.isAdded()) {
                        newPicFragmentTransaction.show(newPictureFragment);
                    } else {
                        newPicFragmentTransaction.add(R.id.frag_frame, newPictureFragment, NEW_PICTURE_FRAG_TAG);
                    }

                    if (collectionsFragment.isAdded()) {
                        hideCollectionsFragment(collectionsFragment, newPicFragmentTransaction);
                    }

                    if (categoriesFragment.isAdded()) {
                        hideCategoriesFragment(categoriesFragment, newPicFragmentTransaction);
                    }

                    newPicFragmentTransaction.commit();

                    break;

                case R.id.navigation_collections:
                    FragmentTransaction collectionsFragmentTransaction = getSupportFragmentManager().beginTransaction();
                    collectionsFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                    if (collectionsFragment.isAdded()) {
                        collectionsFragmentTransaction.show(collectionsFragment);
                    } else {
                        collectionsFragmentTransaction.add(R.id.frag_frame, collectionsFragment, COLLECTION_FRAG_TAG);
                    }

                    if (newPictureFragment.isAdded()) {
                        hideNewPictureFragment(newPictureFragment, collectionsFragmentTransaction);
                    }

                    if (categoriesFragment.isAdded()) {
                        hideCategoriesFragment(categoriesFragment, collectionsFragmentTransaction);
                    }

                    collectionsFragmentTransaction.commit();

                    break;

                case R.id.navigation_categories:
                    FragmentTransaction categoriesFragmentTransaction = getSupportFragmentManager().beginTransaction();
                    categoriesFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                    if (categoriesFragment.isAdded()) {
                        categoriesFragmentTransaction.show(categoriesFragment);
                    } else {
                        categoriesFragmentTransaction.add(R.id.frag_frame, categoriesFragment, CATEGORIES_FRAGMENT_TAG);
                    }

                    if (newPictureFragment.isAdded()) {
                        hideNewPictureFragment(newPictureFragment, categoriesFragmentTransaction);
                    }

                    if (collectionsFragment.isAdded()) {
                        hideCollectionsFragment(collectionsFragment, categoriesFragmentTransaction);
                    }

                    categoriesFragmentTransaction.commit();

                    break;

                default:
                    break;
            }
            return true;
        });
    }

    private void setBottomBarReselectedItemListener(){
        bottomBar.setOnItemReselectedListener(item -> {
            switch (item.getItemId()){
                case R.id.navigation_new:
                    if (newPictureFragment.isVisible()) {
                        newPictureFragment.scrollListToTop();
                    } else {
                        onNavigationItemSelected(item);
                    }
                    break;
                case R.id.navigation_collections:
                    collectionsFragment.scrollListToTop();
                    break;

                case R.id.navigation_categories:
                    categoriesFragment.scrollListToTop();
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * Hides NewPictureFragment
     *
     * @param fragment            - NewPictureFragment fragment to be hidden
     * @param fragmentTransaction - Frag Transaction
     */
    private void hideNewPictureFragment(NewPictureFragment fragment, FragmentTransaction fragmentTransaction) {
        fragmentTransaction.hide(fragment);
    }

    /**
     * Hides collections Fragment
     *
     * @param collectionsFragment - Fragment to be hidden
     * @param fragmentTransaction - Frag Transaction
     */
    private void hideCollectionsFragment(CollectionsFragment collectionsFragment, FragmentTransaction fragmentTransaction) {
        fragmentTransaction.hide(collectionsFragment);
    }

    /**
     * Hides Categories Fragment
     *
     * @param categoriesFragment  - Fragment to be hidden
     * @param fragmentTransaction - Frag Transaction
     */
    private void hideCategoriesFragment(CategoriesFragment categoriesFragment, FragmentTransaction fragmentTransaction) {
        fragmentTransaction.hide(categoriesFragment);
    }

    /**
     * Gets fragments from saved Instance to prevent fragment duplication on Activity resume
     */
    private void getFragmentsFromSavedInstance() {
        newPictureFragment = (NewPictureFragment) manager.findFragmentByTag(NEW_PICTURE_FRAG_TAG);
        if (newPictureFragment == null) {
            newPictureFragment = new NewPictureFragment();
        }

        collectionsFragment = (CollectionsFragment) manager.findFragmentByTag(COLLECTION_FRAG_TAG);
        if (collectionsFragment == null) {
            collectionsFragment = new CollectionsFragment();
        }

        categoriesFragment = (CategoriesFragment) manager.findFragmentByTag(CATEGORIES_FRAGMENT_TAG);
        if (categoriesFragment == null) {
            categoriesFragment = new CategoriesFragment();
        }
    }

    /**
     * Sign out method.
     */
    private void signOut() {
        getSharedPreferences(getString(R.string.user_base_preference_key), MODE_PRIVATE).edit().clear().commit();
        ((wolpepper) getApplication()).clearID_LIKE_STATUS_MAP();
        recreate();
    }

    /**
     * Check if date is changed and clear image data cache based on that
     */
    private void checkDateAndClearPreviousPrefs() {
        // <-- Getting Date Prefs --> //
        SharedPreferences sharedPreferences = getSharedPreferences(IMAGE_CACHE_PREF_DATE_BASE_KEY, Context.MODE_PRIVATE);

        Calendar c = Calendar.getInstance();
        int thisDay = c.get(Calendar.DAY_OF_YEAR); // GET THE CURRENT DAY OF THE YEAR
        int lastDay = sharedPreferences.getInt(IMAGE_CACHE_PREF_DATE_KEY, 0); //If we don't have a saved value, use 0.
        if (lastDay != thisDay) {  // Day changed so execute clear prefs
            UtilityMethods.clearCachePreferences(MainActivity.this);
            sharedPreferences.edit().putInt(IMAGE_CACHE_PREF_DATE_KEY, thisDay).apply();
        }
    }

    /**
     * Method which requests for permission and handles denied permission and never ask again check box too
     */
    private void requestPermission() {
        SharedPreferences firstRunPermissionPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (!firstRunPermissionPref.getBoolean("isPermissionAskedFirstTimePref", false)){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            firstRunPermissionPref.edit().putBoolean("isPermissionAskedFirstTimePref", true).apply();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            loadPersistentPermissionAlert();
        } else if (persistentPermissionNeededAlert != null) {
            Alerter.hide();
        }
    }

    private void loadPersistentPermissionAlert() {
        persistentPermissionNeededAlert = Alerter.create(MainActivity.this);
        persistentPermissionNeededAlert
                .setTextTypeface(ResourcesCompat.getFont(MainActivity.this, R.font.spacemono_regular))
                .setTitleTypeface(ResourcesCompat.getFont(MainActivity.this, R.font.spacemono_bold))
                .setTextAppearance(R.style.alertBody)
                .setTitleAppearance(R.style.alertTitle)
                .setTitle("Permission Needed!")
                .setText("We need storage permission to download and store wallpapers on your storage. Kindly provide storage permission from settings")
                .setPositiveActionText("Settings")
                .setOnPositiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, SETTINGS_REQUEST_CODE);
            }
        }).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, SETTINGS_REQUEST_CODE);
            }
        }).enableInfiniteDuration(true).disableOutsideTouch().show();
    }

    /**
     * User Login Async Task
     */
    private class LoginAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.login_data_loading_placeholder).setVisibility(View.VISIBLE);
            findViewById(R.id.login_data_loading_placeholder).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Alerter.create(MainActivity.this)
                            .setTextTypeface(ResourcesCompat.getFont(MainActivity.this, R.font.spacemono_regular))
                            .setTitleTypeface(ResourcesCompat.getFont(MainActivity.this, R.font.spacemono_bold))
                            .setTextAppearance(R.style.alertBody)
                            .setTitleAppearance(R.style.alertTitle)
                            .setText("Please Wait!");
                }
            });
            headerLayout.findViewById(R.id.login_Button).setVisibility(View.GONE);

        }

        @Override
        protected String doInBackground(String... params) {
            String code = params[0];
            URL url = NetworkUtils.buildAuthUrl(APP_ID, APP_SECRET, "wolpepper://unsplash-auth-callback", code);
            try {
                String s = NetworkUtils.getAuthorisationResponse(url);

                SharedPreferences preferences = getSharedPreferences(getString(R.string.user_base_preference_key), MODE_PRIVATE);
                String token = new JSONObject(s).getString("access_token");
                preferences.edit().putString(getString(R.string.access_token), token).commit();

                String json = NetworkUtils.getCurrentUserProfile(NetworkUtils.buildSelfProfileUrl("me", APP_ID), token);

                if (TextUtils.isEmpty(json) || json.contains("\"errors\"")) {
                    preferences.edit().putBoolean(getString(R.string.is_user_logged_in), false).commit();
                    return null;
                }

                preferences.edit().putBoolean(getString(R.string.is_user_logged_in), true).commit();
                preferences.edit().putString(getString(R.string.user_json), json).commit();
                return json;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (s == null) {
                findViewById(R.id.login_data_loading_placeholder).setVisibility(View.GONE);
                findViewById(R.id.login_data_loading_placeholder).setOnClickListener(null);
                headerLayout.findViewById(R.id.login_Button).setVisibility(View.VISIBLE);
                Alerter.create(MainActivity.this)
                        .setTextTypeface(ResourcesCompat.getFont(MainActivity.this, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(MainActivity.this, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setTitle(R.string.login_failed_title)
                        .setText(R.string.login_failed_desc)
                        .setBackgroundColorRes(R.color.alert_default_error_background)
                        .setDuration(5000)
                        .show();
            }
            if (s != null) {
                recreate();
            }
            super.onPostExecute(s);
        }
    }

    private void notifyFragsOrientationFilterChanged(){
        //  TODO : notify fragments that orientation filter has changed. kindly refresh content and empty all adapter and array list except (HASH MAP)
//        Fragment newPictureFrag, curatedFrag;
//        newPictureFrag = getSupportFragmentManager().findFragmentByTag(NEW_PICTURE_FRAG_TAG);
//        curatedFrag = getSupportFragmentManager().findFragmentByTag(CURATED_FRAG_TAG);
//        getSupportFragmentManager().beginTransaction().detach(newPictureFrag).detach(curatedFrag).attach(newPictureFrag).attach(curatedFrag).commit();
        newPictureFragment.notifyOrientationFilterChanged();
    }

    private void setUpSideNavExpandLayouts(){

        ExpandingItem solidColorsExpandingItem = expandingList.createNewItem(R.layout.nav_expanding_layout);

        ExpandingItem orientationExpandingItem = expandingList.createNewItem(R.layout.nav_expanding_orientation_layout);

        ExpandingItem upgradeExpandingItem = expandingList.createNewItem(R.layout.nav_expanding_layout);

        ExpandingItem settingsExpandingItem = expandingList.createNewItem(R.layout.nav_expanding_layout);

        ExpandingItem miscExpandingItem = expandingList.createNewItem(R.layout.nav_expanding_layout);

        setUpSideNavSolidColorsExpandingItem(solidColorsExpandingItem);

        setUpSideNavOrientationExpandingItem(orientationExpandingItem);

        setUpExpandingUpgradeItem(upgradeExpandingItem);

        setUpSideNavSettingsExpandingItem(settingsExpandingItem);

        setUpSideNavMiscExpandingItem(miscExpandingItem);

    }

    private void setUpExpandingUpgradeItem(ExpandingItem item){
        ((TextView) item.findViewById(R.id.title)).setText(getString(R.string.upgrade_app_title));
        ((TextView) item.findViewById(R.id.sub_title)).setText(R.string.upgrade_app_desc);

        // indicator color & icon
        item.setIndicatorColorRes(R.color.subTextColor);
        item.setIndicatorIconRes(R.drawable.ic_keyboard_arrow_down_black_24dp);
        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UpgradeAppActivity.class));
            }
        });

        item.findViewById(R.id.title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UpgradeAppActivity.class));
            }
        });
        item.findViewById(R.id.sub_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UpgradeAppActivity.class));
            }
        });
    }

    private void setUpSideNavSolidColorsExpandingItem(ExpandingItem item){
        item.createSubItems(2);

        ((TextView) item.findViewById(R.id.title)).setText(getString(R.string.other_wallpapers_title));
        ((TextView) item.findViewById(R.id.sub_title)).setText(getString(R.string.other_wallpapers_desc));


        item.setIndicatorIconRes(R.drawable.ic_keyboard_arrow_down_black_24dp);
        item.setIndicatorColorRes(R.color.subTextColor);

        View solidColorsItem = item.getSubItemView(0);
        setSubItemTitleText(solidColorsItem, getString(R.string.solid_color_title));
        solidColorsItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SolidColors.class));
            }
        });

        View gradientColorsItem = item.getSubItemView(1);
        setSubItemTitleText(gradientColorsItem, getString(R.string.gradient_color_title));
        gradientColorsItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GradientPaperActivity.class));
            }
        });
    }

    private void setUpSideNavMiscExpandingItem(ExpandingItem item){
        ((TextView) item.findViewById(R.id.title)).setText(getString(R.string.miscellaneous_title));
        ((TextView) item.findViewById(R.id.sub_title)).setText(getString(R.string.miscellaneous_desc));

        item.createSubItems(5);

        View sendFeedBackItem = item.getSubItemView(0);
        setSubItemTitleText(sendFeedBackItem, getString(R.string.send_feedback));
        sendFeedBackItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent send = new Intent(Intent.ACTION_SENDTO);
                String uriText = "mailto:" + Uri.encode("eclectik.devs@gmail.com") +
                        "?subject=" + Uri.encode("Wol:Pepper Feedback " + BuildConfig.VERSION_NAME) +
                        "&body=" + Uri.encode("");
                Uri uri = Uri.parse(uriText);

                send.setData(uri);
                startActivity(Intent.createChooser(send, "Send mail..."));
            }
        });

        View joinCommunityItem = item.getSubItemView(1);
        setSubItemTitleText(joinCommunityItem, getString(R.string.join_eclectik_community));
        joinCommunityItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://t.me/Wolpepper")));
            }
        });

        View shareWolpepperItem = item.getSubItemView(2);
        setSubItemTitleText(shareWolpepperItem, getString(R.string.share_wolpepper));
        shareWolpepperItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, "Wolpepper");
                    String sAux = getString(R.string.let_me_recommend_app);
                    sAux = sAux + "http://play.google.com/store/apps/details?id=" + getPackageName();
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "Choose one"));
                } catch (Exception e) {
                    //e.toString();
                }
            }
        });

        View otherAppsItem = item.getSubItemView(3);
        setSubItemTitleText(otherAppsItem, getString(R.string.other_apps));
        otherAppsItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO : check this issue of if playstore app is installed or not
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=6428398715878309895&hl=en")));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=6428398715878309895&hl=en")));
                }
            }
        });

        View aboutAppItem = item.getSubItemView(4);
        setSubItemTitleText(aboutAppItem, getString(R.string.about_activity_title));
        aboutAppItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, About.class));
            }
        });

        // indicator color & icon
        item.setIndicatorColorRes(R.color.subTextColor);
        item.setIndicatorIconRes(R.drawable.ic_keyboard_arrow_down_black_24dp);

    }

    private void setUpSideNavSettingsExpandingItem(ExpandingItem item){
        ((TextView) item.findViewById(R.id.title)).setText(getString(R.string.action_settings));
        ((TextView) item.findViewById(R.id.sub_title)).setText(getString(R.string.side_nav_settings_subtitle));
        // indicator color & icon
        item.setIndicatorColorRes(R.color.subTextColor);
        item.setIndicatorIconRes(R.drawable.ic_keyboard_arrow_down_black_24dp);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            item.createSubItems(5);
        } else {
            item.createSubItems(4);
        }

        //get a sub item View
        View muzeiItem = item.getSubItemView(0);
        ((TextView) muzeiItem.findViewById(R.id.sub_item_title)).setText(getString(R.string.muzei_setting));
        muzeiItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MuzeiSettingsActivity.class));
            }
        });

        View previewQualityItem = item.getSubItemView(1);
        ((TextView) previewQualityItem.findViewById(R.id.sub_item_title)).setText(getString(R.string.preview_quality));
        previewQualityItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PreviewQualityActivity.class));
            }
        });

        View defaultDownloadFormatItem = item.getSubItemView(2);
        ((TextView) defaultDownloadFormatItem.findViewById(R.id.sub_item_title)).setText(getString(R.string.download_image_format));
        defaultDownloadFormatItem.findViewById(R.id.sub_item_title_desc).setVisibility(View.VISIBLE);

        final TextView currentDefaultTv = defaultDownloadFormatItem.findViewById(R.id.sub_item_title_desc);
        currentDefaultTv.setText(getString(R.string.current_default_string));
        setUpDefaultImageType(currentDefaultTv);
        defaultDownloadFormatItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawers();
                showDefaultImageFormatDialog(currentDefaultTv);
            }
        });

        View quickSetWallpaperSettingItem = item.getSubItemView(3);
        quickSetWallpaperSettingItem.findViewById(R.id.side_arrow_icon).setVisibility(View.GONE);
        ((TextView) quickSetWallpaperSettingItem.findViewById(R.id.sub_item_title)).setText(getString(R.string.quick_set_wallpaper_nav_drawer_title));
        CheckBox quickSetCheckBox = quickSetWallpaperSettingItem.findViewById(R.id.expanding_sub_item_checkbox);
        quickSetCheckBox.setVisibility(View.VISIBLE);

        boolean isQuickSetEnabled = getSharedPreferences(getString(R.string.quick_set_wallpaper_base_pref), MODE_PRIVATE).getBoolean(getString(R.string.quick_set_wallpaper_pref), false);
        quickSetCheckBox.setChecked(isQuickSetEnabled);
        quickSetCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getSharedPreferences(getString(R.string.quick_set_wallpaper_base_pref), MODE_PRIVATE).edit().putBoolean(getString(R.string.quick_set_wallpaper_pref), isChecked).commit();
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            View storageSettingsItem = item.getSubItemView(4);
            ((TextView) storageSettingsItem.findViewById(R.id.sub_item_title)).setText(getString(R.string.storage_setting_nav_drawer_title));
            storageSettingsItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, StorageSettingsActivity.class));
                }
            });
        }
    }

    /**
     * Method to setup Orientation Filter side nav expanding item
     * @param orientationExpandingItem -
     */
    private void setUpSideNavOrientationExpandingItem(ExpandingItem orientationExpandingItem){
        ((TextView) orientationExpandingItem.findViewById(R.id.title)).setText(getString(R.string.side_nave_orientation_filter_title));
        ((TextView) orientationExpandingItem.findViewById(R.id.sub_title)).setText(getString(R.string.side_nave_orientation_filter_subtitle));

        orientationExpandingItem.createSubItems(1);

        //get a sub item View
        View subItemZero = orientationExpandingItem.getSubItemView(0);
        ((TextView) subItemZero.findViewById(R.id.sub_title)).setText(getString(R.string.portrait_only));
        ((CheckBox) subItemZero.findViewById(R.id.orientation_check_box)).setChecked(false);
        ((CheckBox) subItemZero.findViewById(R.id.orientation_check_box)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((wolpepper)getApplication()).setPortraitOnly(isChecked);
                notifyFragsOrientationFilterChanged();
            }
        });

        // Indicator Color and icon
        orientationExpandingItem.setIndicatorColorRes(R.color.subTextColor);
        orientationExpandingItem.setIndicatorIconRes(R.drawable.ic_keyboard_arrow_down_black_24dp);
    }

    /**
     * Helper method for setting exapnding list subItem title and subtitle text
     * @param view -
     * @param data -
     */
    private static void setSubItemTitleText(View view, String data){
        ((TextView) view.findViewById(R.id.sub_item_title)).setText(data);
    }

    /**
     * ALL METHODS RELATED TO DEFAULT IMAGE SETTINGS DIALOG BOX FROM HERE
     */
    private void setUpDefaultImageType(TextView currentDefaultTv){
        listOfImageTypes.add(getString(R.string.always_ask_image_type));
        listOfImageTypes.add(getString(R.string.jpeg_image_type));
        listOfImageTypes.add(getString(R.string.raw_image_type));

        preferences = getSharedPreferences(getString(R.string.default_image_format_base_pref_key), MODE_PRIVATE);
        updateCurrentDefaultTextViewContent(currentDefaultTv);
    }

    private void updateCurrentDefaultTextViewContent(TextView currentDefaultTv){
        currentDefault = preferences.getString(getString(R.string.default_image_format_pref_key), getString(R.string.always_ask_image_type));
        currentDefaultTv.setText(getString(R.string.current_default_string, currentDefault));
    }

    private void showDefaultImageFormatDialog(final TextView view){
        int selectedIndex = 0;
        if (currentDefault.equals(getString(R.string.raw_image_type))){
            selectedIndex = 2;
        } else if (currentDefault.equals(getString(R.string.jpeg_image_type))){
            selectedIndex = 1;
        } else if (currentDefault.equals(getString(R.string.always_ask_image_type))){
            selectedIndex = 0;
        }

        new MaterialDialog.Builder(MainActivity.this)
                .title(getString(R.string.choose_default_image_format_dialog_title))
                .items(listOfImageTypes)
                .typeface(ResourcesCompat.getFont(MainActivity.this, R.font.spacemono_bold), ResourcesCompat.getFont(MainActivity.this, R.font.spacemono_regular))
                .itemsCallbackSingleChoice(selectedIndex, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        preferences.edit().putString(getString(R.string.default_image_format_pref_key), text.toString()).commit();
                        updateCurrentDefaultTextViewContent(view);
                        return true;
                    }
                })
                .positiveText(getString(R.string.select_string))
                .show();
    }

    /* ------------------------------------------------------------------------------------------ */
}
