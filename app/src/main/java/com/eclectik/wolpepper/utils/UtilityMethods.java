package com.eclectik.wolpepper.utils;

import static android.content.Context.MODE_PRIVATE;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_LOCAL_STORAGE_FOLDER_NAME;
import static com.eclectik.wolpepper.utils.ConstantValues.MuzeiReturnValues.IMAGE_ALREADY_EXIST_IN_LIST;
import static com.eclectik.wolpepper.utils.ConstantValues.MuzeiReturnValues.LIST_NAME_ALREADY_EXIST;
import static com.eclectik.wolpepper.utils.ConstantValues.MuzeiReturnValues.MAXIMUM_IMAGE_IN_A_LIST_REACHED;
import static com.eclectik.wolpepper.utils.ConstantValues.MuzeiReturnValues.MAXIMUM_LIST_ALLOWED_REACHED;
import static com.eclectik.wolpepper.utils.ConstantValues.MuzeiReturnValues.MUZEI_LIST_OPERATION_SUCCESSFUL;
import static com.eclectik.wolpepper.utils.ConstantValues.PREMIUM_UPGRADE_REQUIRED_WIDGET_TOAST;
import static com.eclectik.wolpepper.utils.ConstantValues.PreferencesKeys.FULL_WIDGET_UNLOCKED_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.PreferencesKeys.FULL_WIDGET_UNLOCKED_PREFENCES_BASE_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.UNSPLASH_LOGIN_CALLBACK_STRING;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.adapters.NewPapersAdapter;
import com.eclectik.wolpepper.dataStructures.MuzeiList;
import com.eclectik.wolpepper.dataStructures.Papers;
import com.eclectik.wolpepper.executors.AppExecutors;
import com.eclectik.wolpepper.wolpepper;
import com.tapadoo.alerter.Alerter;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by mj on 7/6/17.
 * THIS CLASS PROVIDES UTILITY METHODS
 */
@SuppressWarnings("WeakerAccess")
@SuppressLint("ApplySharedPref")
public class UtilityMethods {

    /**
     * This method updates the passed in TextView's Text to N/A
     * IF value of @param value is null or empty or "null" literal
     * otherwise set the value of @param value as text for the textView
     *
     * @param view  - The Text View to be Updated
     * @param value - The Value To be Set on Text View
     */
    public static void updateTextView(TextView view, String value) {
        if (value != null && !TextUtils.isEmpty(value) && !value.trim().toLowerCase().equals("null") && !value.trim().toLowerCase().equals("null null")) {
            view.setText(value);
        } else {
            view.setText("N/A");
        }
    }

    /**
     * Hides the TextView which have N/A as their Text
     *
     * @param view - View To be Updated
     */
    public static boolean hideEmptyTextViews(TextView view) {
        if (view.getText().toString().trim().equals("N/A")) {
            view.setVisibility(View.GONE);
            return true;
        } else {
            view.setVisibility(View.VISIBLE);
            return false;
        }
    }

    /**
     * Make the passed in image view Grayscale
     *
     * @param v - Image View
     */
    public static void setImageViewGreyScale(final ImageView v) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1, 0);
        final ColorMatrix matrix = new ColorMatrix();
        valueAnimator.setDuration(1250);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                matrix.setSaturation((Float) animation.getAnimatedValue());  //0 means grayscale
                ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
                v.setColorFilter(cf);
                if ((Float) animation.getAnimatedValue() == 0){
                    animation.removeAllUpdateListeners();
                }
            }
        });
        valueAnimator.start();
    }

    public static String getPremiumSkuString(){
        return ConstantValues.SKU_PREMIUM;
    }

    /**
     * Make the passed in image view Grayscale
     *
     * @param v - Image View
     */
    public static void setImageViewGreyScale(final ImageView v, final float amount, float previousAmount) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(previousAmount, amount);
        final ColorMatrix matrix = new ColorMatrix();
        valueAnimator.setDuration(500);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                matrix.setSaturation((Float) animation.getAnimatedValue());  //0 means grayscale
                ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
                v.setColorFilter(cf);
                if ((Float) animation.getAnimatedValue() == amount){
                    animation.removeAllUpdateListeners();
                }
            }
        });
        valueAnimator.start();
    }

    /**
     * Make the passed in image View display CColored image
     *
     * @param v - Image View
     */
    public static void setImageViewColored(final ImageView v) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        final ColorMatrix matrix = new ColorMatrix();
        valueAnimator.setDuration(1250);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                matrix.setSaturation((Float) animation.getAnimatedValue());  //0 means grayscale
                ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
                v.setColorFilter(cf);
                if ((Float) animation.getAnimatedValue() == 1){
                    animation.removeAllUpdateListeners();
                }
            }
        });
        valueAnimator.start();
    }

    public static String getBase64Key() {
        return "Svvj+v0WUkU/" + getBase64keyFromChild();
    }

    public static void setTextViewOpenLinkClickListener(@NonNull TextView textView, final String link) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(link));
                v.getContext().startActivity(intent);
            }
        });
    }

    private static String getBase64keyFromChild() {
        return "rz2IpPwR4r8UULnF9Gdg1plrKAZVXXWQnVPHd5rjOiUmB/" + getAnotherBits();
    }

    public static boolean checkConnection(Context context) {
        //THIS IS NETWORK STATUS CHECK CODE-------------------------------------
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        //CODE ENDS HERE ---------------------------------------------------------

        return isConnected;

    }

    public static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }

    }

    private static String getAnotherBits() {
        return "WsadaEE+Z0K2bPyp5QAeLgv/Ca+G5mWZwkRgk9fYTwJVhbl8sUdL0indfKqOp/FqZbmT0SUlbTEDFKEFDRV7DbN/vlTO2OuLAUyHMTHFk8Cg2/Pc1apUQS9p3XeGLaHhl6Ic1L3O955d+jN/" + getLastBits();
    }

    private static String getLastBits() {
        return "AkLMMSuuljHGTZDE1rmbR5LUKjxaJR8IiGUQ2izvrJZpDNUL4lRSbXLJpe5Jz4Smlx6ujkWIRac0Y3rP7NgdVpPrwPI/6OOu4jUYBjUkwIDAQAB";
    }

    /**
     * Display rate limit reached Alert
     *
     * @param activity - Activity Context
     */
    public static void showRateLimitReachedAlert(Activity activity) {
        Alerter alerter = Alerter.create(activity);

        alerter.setTitle("Apologies!")
                .setTextTypeface(ResourcesCompat.getFont(activity, R.font.spacemono_regular))
                .setTitleTypeface(ResourcesCompat.getFont(activity, R.font.spacemono_bold))
                .setTextAppearance(R.style.alertBody)
                .setTitleAppearance(R.style.alertTitle)
                .setText("Our hourly API rate limit has reached. Please try again after some time.")
                .setBackgroundColorRes(R.color.alert_default_error_background)
                .enableInfiniteDuration(true)
                .enableVibration(true)
                .show();
    }

    /**
     * Display download wallpaper Alert
     *
     * @param activity       - Activity Context
     * @param currentPaper   - Paper object of selected wallpaper
     * @param isGreyEnabled  - is Grayscale enabled
     * @param isSetWallpaper - is SetWallpaper enabled
     */
    public static void downloadImageAlert(final Activity activity, final Papers currentPaper, final boolean isGreyEnabled, final boolean isSetWallpaper) {
        final Alerter alerter = Alerter.create(activity);
        alerter.setTitle("Format")
                .setText("Select The Image Format To Download")
                .setPositiveActionText("RAW")
                .setNegativeActionText("JPEG")
                .setOnPositiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (DownloadWallpaperUtils.checkIfFileIsAlreadyDownloading(currentPaper.getImageId())) {

                            alerter.hide();
                            fileAlreadyDownloadingAlert(activity, currentPaper);

                        } else if (DownloadWallpaperUtils.checkRawImageExistOnStorage(activity, currentPaper.getImageId())) {

                            alerter.hide();
                            fileAlreadyExistAlert(activity, currentPaper);

                        } else {

                            alerter.hide();
                            DownloadWallpaperUtils.downloadPaper(activity, Uri.parse(currentPaper.getRawImageUrl()), currentPaper, false, false, true);
                            downloadStartedAlert(activity, currentPaper);

                        }

                    }
                })
                .setOnNegativeActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (DownloadWallpaperUtils.checkIfFileIsAlreadyDownloading(currentPaper.getImageId())) {

                            alerter.hide();
                            fileAlreadyDownloadingAlert(activity, currentPaper);

                        } else if (DownloadWallpaperUtils.checkImageExistOnStorage(activity, currentPaper.getImageId(), isGreyEnabled)) {

                            alerter.hide();
                            fileAlreadyExistAlert(activity, currentPaper);

                        } else {

                            alerter.hide();
                            DownloadWallpaperUtils.downloadPaper(activity, Uri.parse(currentPaper.getFullImageUrl()), currentPaper, isGreyEnabled, isSetWallpaper, false);
                            downloadStartedAlert(activity, currentPaper);

                        }
                    }
                })
                .setBackgroundColorRes(R.color.colorAccent)
                .enableInfiniteDuration(true)
                .show();

    }

    /**
     * Download of selected image has started Alert
     *
     * @param activity     - Activity Context
     * @param currentPaper - Paper Object
     */
    public static void downloadStartedAlert(Activity activity, Papers currentPaper) {
        Alerter.create(activity)
                .setTitle("Downloading...")
                .setText("Image Of " + currentPaper.getAuthorName())
                .setTextAppearance(R.style.alertBody)
                .setTitleAppearance(R.style.alertTitle)
                .show();
    }

    /**
     * File Already exist Alert
     *
     * @param activity
     * @param currentPaper
     */
    public static void fileAlreadyExistAlert(Activity activity, Papers currentPaper) {
        Alerter.create(activity)
                .setTitle("Hey There!")
                .setText("Image Of " + currentPaper.getAuthorName() + " with Id:" + currentPaper.getImageId() + " already exist on storage!")
                .setBackgroundColorRes(R.color.alert_default_error_background)
                .setTextAppearance(R.style.alertBody)
                .setTitleAppearance(R.style.alertTitle)
                .setDuration(3000)
                .show();
    }

    /**
     * File Already downloading alert
     *
     * @param activity     - Activity Context
     * @param currentPaper - Paper Object
     */
    public static void fileAlreadyDownloadingAlert(Activity activity, Papers currentPaper) {
        Alerter.create(activity)
                .setTitle(R.string.already_downloading_title)
                .setText(activity.getString(R.string.image_already_downloading_desc_1) + currentPaper.getAuthorName() + activity.getString(R.string.image_already_downloading_desc_2) + currentPaper.getImageId() + activity.getString(R.string.image_already_downloading_desc_3))
                .setTextAppearance(R.style.alertBody)
                .setTitleAppearance(R.style.alertTitle)
                .setBackgroundColorRes(R.color.alert_default_error_background)
                .setDuration(3000)
                .show();
    }

    /**
     * Returns the height of status bar
     *
     * @param context - Context
     * @return - Height Of Status Bar
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void openGPlus(Context context, String profile) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName("com.google.android.apps.plus",
                    "com.google.android.apps.plus.phone.UrlGatewayActivity");
            intent.putExtra("customAppUri", profile);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/communities/" + profile)));
        }
    }

    /**
     * Build and get login url string
     *
     * @param c      - Context
     * @param APP_ID - App Id
     * @return - Login Url
     */
    public static String getLoginUrl(Context c, String APP_ID) {
        return ConstantValues.BASE_UNSPLASH_URL_STRING + "oauth/authorize"
                + "?client_id=" + APP_ID
                + "&redirect_uri=" + "wolpepper%3A%2F%2F" + UNSPLASH_LOGIN_CALLBACK_STRING
                + "&response_type=" + "code"
                + "&scope=" + "public+read_user+write_user+read_photos+write_photos+write_likes+write_followers+read_collections+write_collections";
    }

    /**
     * To identify whether user is logged in or not
     *
     * @param context - Context
     * @return - True if user is logged in else false
     */
    public static boolean isUserLoggedIn(Context context) {
        if (context == null){
            return false;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.user_base_preference_key), MODE_PRIVATE);
        return sharedPreferences.getBoolean(context.getString(R.string.is_user_logged_in), false);
    }

    public static String getAccessToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.user_base_preference_key), MODE_PRIVATE);
        return preferences.getString(context.getString(R.string.access_token), null);
    }

    public static String getLoggedInUserDetailsJson(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.user_base_preference_key), MODE_PRIVATE);
        return preferences.getString(context.getString(R.string.user_json), "");
    }

    /**
     * Will implement after understanding custom tabs completely
     *
     * @param activityContext - The Activity which Starts this Intent
     */
    public static void startLoginIntent(Context activityContext, String appId) {
        String packageName = "com.android.chrome";
        Intent browserIntent = new Intent();
        browserIntent.setPackage(packageName);
        List<ResolveInfo> activitiesList = activityContext.getPackageManager().queryIntentActivities(
                browserIntent, -1);
        if (activitiesList.size() > 0) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setShowTitle(true);
            builder.setToolbarColor(ContextCompat.getColor(activityContext, R.color.colorPrimary));
            builder.setStartAnimations(activityContext, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            builder.setExitAnimations(activityContext, android.R.anim.slide_in_left, android.R.anim.slide_out_right);

            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(activityContext, Uri.parse(UtilityMethods.getLoginUrl(activityContext, appId)));
        } else {
            activityContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(UtilityMethods.getLoginUrl(activityContext, appId))));
        }
    }

    public static void loadAdInHolder(NewPapersAdapter.NewPapersViewHolder holder){

    }

    /**
     * Clear all cached data of image details
     *
     * @param context - Context
     */
    public static void clearCachePreferences(Context context) {
        context.getSharedPreferences(ConstantValues.PreferencesKeys.IMAGE_EXTRA_DETAILS_PREFERENCES_BASE_KEY, MODE_PRIVATE).edit().clear().apply();
    }

    /**
     * Add Wallpaper image to selected muzei Source List
     *
     * @param context      - Context
     * @param imageId      - Image Id
     * @param date         - Date
     * @param authorName   - Name Of Author
     * @param downloadLink - Download LLink Of Image
     * @return - Result Of Operation
     */
    public static int addImageToSelectedMuzeiList(Context context, String listName, String imageId, String date, String authorName, String downloadLink) {
        SharedPreferences imageIdPreferences = context.getSharedPreferences(context.getString(R.string.muzei_image_id_base_prefs_key), MODE_PRIVATE);
        SharedPreferences imageDatePreferences = context.getSharedPreferences(listName + context.getString(R.string.muzei_image_date_base_pref_key), MODE_PRIVATE);
        SharedPreferences imageAuthorPreferences = context.getSharedPreferences(listName + context.getString(R.string.muzei_image_author_base_prefs_key), MODE_PRIVATE);
        SharedPreferences imageDownloadLinksPreferences = context.getSharedPreferences(listName + context.getString(R.string.muzei_image_download_links), MODE_PRIVATE);

        /* get image ID Set which has key of active muzei List */
        Set<String> imageIdSet = imageIdPreferences.getStringSet(listName, null);

        if (!((wolpepper) ((Activity) context).getApplication()).isAppTempPremium()) {
            if (imageIdSet != null && imageIdSet.size() >= Integer.parseInt(context.getString(R.string.maximum_allowed_image_per_list))) {
                return MAXIMUM_IMAGE_IN_A_LIST_REACHED;
            }
        }

        if (imageIdSet == null) {
            imageIdSet = new HashSet<>();
            imageIdSet.add(imageId);
        } else if (imageIdSet.contains(imageId)) {
            return IMAGE_ALREADY_EXIST_IN_LIST;
        } else {
            imageIdSet = new HashSet<>(imageIdPreferences.getStringSet(listName, null));
            imageIdSet.add(imageId);
        }


        imageDatePreferences.edit().putString(imageId, date).commit();
        imageAuthorPreferences.edit().putString(imageId, authorName).commit();
        imageDownloadLinksPreferences.edit().putString(imageId, downloadLink).commit();
        imageIdPreferences.edit().putStringSet(listName, imageIdSet).commit();

        return MUZEI_LIST_OPERATION_SUCCESSFUL;
    }

    /**
     * Add Wallpaper image to active muzei Source List
     *
     * @param context      - Context
     * @param imageId      - image Id
     * @param date         - Date
     * @param authorName   - Name Of Author
     * @param downloadLink - Download Link Of Image
     * @return - Result Of Operation
     */
    public static int addImageToActiveMuzeiList(Context context, String imageId, String date, String authorName, String downloadLink) {
        SharedPreferences activeCollectionPreference = context.getSharedPreferences(context.getString(R.string.muzei_active_list_base_prefs_key), MODE_PRIVATE);
        String activeListName = activeCollectionPreference.getString(context.getString(R.string.muzei_active_list_key), "");

        SharedPreferences imageIdPreferences = context.getSharedPreferences(context.getString(R.string.muzei_image_id_base_prefs_key), MODE_PRIVATE);
        SharedPreferences imageDatePreferences = context.getSharedPreferences(activeListName + context.getString(R.string.muzei_image_date_base_pref_key), MODE_PRIVATE);
        SharedPreferences imageAuthorPreferences = context.getSharedPreferences(activeListName + context.getString(R.string.muzei_image_author_base_prefs_key), MODE_PRIVATE);
        SharedPreferences imageDownloadLinksPreferences = context.getSharedPreferences(activeListName + context.getString(R.string.muzei_image_download_links), MODE_PRIVATE);

        /* get image ID Set which has key of active muzei List */
        Set<String> imageIdSet = imageIdPreferences.getStringSet(activeListName, null);

        if (!((wolpepper) ((Activity) context).getApplication()).isAppTempPremium()) {
            if (imageIdSet != null && imageIdSet.size() >= Integer.parseInt(context.getString(R.string.maximum_allowed_image_per_list))) {
                return MAXIMUM_IMAGE_IN_A_LIST_REACHED;
            }
        }

        if (imageIdSet == null) {
            imageIdSet = new HashSet<>();
            imageIdSet.add(imageId);
        } else if (imageIdSet.contains(imageId)) {
            return IMAGE_ALREADY_EXIST_IN_LIST;
        } else {
            imageIdSet = new HashSet<>(imageIdPreferences.getStringSet(activeListName, null));
            imageIdSet.add(imageId);
        }


        imageDatePreferences.edit().putString(imageId, date).commit();
        imageAuthorPreferences.edit().putString(imageId, authorName).commit();
        imageDownloadLinksPreferences.edit().putString(imageId, downloadLink).commit();
        imageIdPreferences.edit().putStringSet(activeListName, imageIdSet).commit();

        return MUZEI_LIST_OPERATION_SUCCESSFUL;
    }

    /**
     * Returns the app instance (casted to type wolpepper)
     * @param context - Context used to get app instance
     * @return - app instance of type wolpepper
     */
    public static wolpepper getAppInstance(Context context){
        return (((wolpepper)(context.getApplicationContext())));
    }

    /**
     * Remove Wallpaper image from active muzei Source List
     *
     * @param context - Context
     * @param imageId - Image Id
     * @return - Result of operation
     */
    public static int removeImageFromActiveMuzeiList(Context context, String imageId) {
        SharedPreferences activeCollectionPreference = context.getSharedPreferences(context.getString(R.string.muzei_active_list_base_prefs_key), MODE_PRIVATE);
        String activeListName = activeCollectionPreference.getString(context.getString(R.string.muzei_active_list_key), "");

        SharedPreferences imageIdPreferences = context.getSharedPreferences(context.getString(R.string.muzei_image_id_base_prefs_key), MODE_PRIVATE);
        SharedPreferences imageDatePreferences = context.getSharedPreferences(activeListName + context.getString(R.string.muzei_image_date_base_pref_key), MODE_PRIVATE);
        SharedPreferences imageAuthorPreferences = context.getSharedPreferences(activeListName + context.getString(R.string.muzei_image_author_base_prefs_key), MODE_PRIVATE);
        SharedPreferences imageDownloadLinksPreferences = context.getSharedPreferences(activeListName + context.getString(R.string.muzei_image_download_links), MODE_PRIVATE);

        /* get image ID Set which has key of active muzei List */
        Set<String> imageIdSet = imageIdPreferences.getStringSet(activeListName, null);

        if (imageIdSet != null && imageIdSet.contains(imageId)) {
            imageIdSet = new HashSet<>(imageIdPreferences.getStringSet(activeListName, null));
            imageIdSet.remove(imageId);
        }

        imageDatePreferences.edit().remove(imageId).commit();
        imageAuthorPreferences.edit().remove(imageId).commit();
        imageDownloadLinksPreferences.edit().remove(imageId).commit();
        imageIdPreferences.edit().putStringSet(activeListName, imageIdSet).commit();

        return MUZEI_LIST_OPERATION_SUCCESSFUL;
    }

    /**
     * Remove Wallpaper image from selected muzei Source List
     *
     * @param context - Context
     * @param imageId - image Id
     * @return - Result of operation
     */
    public static int removeImageFromSelectedMuzeiList(Context context, String listName, String imageId) {
        SharedPreferences imageIdPreferences = context.getSharedPreferences(context.getString(R.string.muzei_image_id_base_prefs_key), MODE_PRIVATE);
        SharedPreferences imageDatePreferences = context.getSharedPreferences(listName + context.getString(R.string.muzei_image_date_base_pref_key), MODE_PRIVATE);
        SharedPreferences imageAuthorPreferences = context.getSharedPreferences(listName + context.getString(R.string.muzei_image_author_base_prefs_key), MODE_PRIVATE);
        SharedPreferences imageDownloadLinksPreferences = context.getSharedPreferences(listName + context.getString(R.string.muzei_image_download_links), MODE_PRIVATE);

        /* get image ID Set which has key of active muzei List */
        Set<String> imageIdSet = imageIdPreferences.getStringSet(listName, null);

        if (imageIdSet != null && imageIdSet.contains(imageId)) {
            imageIdSet = new HashSet<>(imageIdPreferences.getStringSet(listName, null));
            imageIdSet.remove(imageId);
        }

        imageDatePreferences.edit().remove(imageId).commit();
        imageAuthorPreferences.edit().remove(imageId).commit();
        imageDownloadLinksPreferences.edit().remove(imageId).commit();
        imageIdPreferences.edit().putStringSet(listName, imageIdSet).commit();

        return MUZEI_LIST_OPERATION_SUCCESSFUL;
    }

    /**
     * Method to check if image is in active list or not
     *
     * @param context - Context
     * @param imageId - Image Id
     * @return - True if image is present in list else false
     */
    public static boolean isInMuzeiActiveList(Context context, String imageId) {
        if (context == null) {
            return false;
        }

        SharedPreferences activeCollectionPreference = context.getSharedPreferences(context.getString(R.string.muzei_active_list_base_prefs_key), MODE_PRIVATE);

        String activeListName = activeCollectionPreference.getString(context.getString(R.string.muzei_active_list_key), "");

        SharedPreferences imageIdPreferences = context.getSharedPreferences(context.getString(R.string.muzei_image_id_base_prefs_key), MODE_PRIVATE);

        /* get image ID Set which has key of active muzei List */
        Set<String> imageIdSet = imageIdPreferences.getStringSet(activeListName, null);
        if (imageIdSet != null) {
            return imageIdSet.contains(imageId);
        } else {
            return false;
        }
    }

    /**
     * Set List as active Muzei Source
     *
     * @param context  - Context
     * @param listName - Name of list
     */
    public static void setListAsActiveMuzeiList(Context context, String listName) {
        SharedPreferences activeCollectionPreference = context.getSharedPreferences(context.getString(R.string.muzei_active_list_base_prefs_key), MODE_PRIVATE);
        activeCollectionPreference.edit().putString(context.getString(R.string.muzei_active_list_key), listName).commit();
        activeCollectionPreference.edit().putInt("previously_used_muzei_list_index", 0).apply();
    }

    /**
     * Method to query if any Muzei list is created or not
     *
     * @param context - Context
     * @return - True if lists are already created else false
     */
    public static boolean isAnyListCreated(Context context) {
        SharedPreferences activeCollectionPreference = context.getSharedPreferences(context.getString(R.string.muzei_active_list_base_prefs_key), MODE_PRIVATE);
        if (TextUtils.isEmpty(activeCollectionPreference.getString(context.getString(R.string.muzei_active_list_key), ""))) {
            return false;
        }
        return true;
    }

    /**
     * Create New List for muzei
     *
     * @param context      - Context
     * @param listName     - Name of List
     * @param imageId      - Image Id
     * @param date         - Date
     * @param authorName   - Name OF Author
     * @param downloadLink - Download Link
     * @param setActive    - Set this list as active?
     */
    public static int createNewMuzeiList(Context context, String listName, String imageId, String date, String authorName, String downloadLink, boolean setActive) {
        // imageIdPreferences have KEYS as ListName and Values as StringSet of image ID's in that list
        SharedPreferences imageIdPreferences = context.getSharedPreferences(context.getString(R.string.muzei_image_id_base_prefs_key), MODE_PRIVATE);
        // Take a good look where listName is used
        SharedPreferences imageDatePreferences = context.getSharedPreferences(listName + context.getString(R.string.muzei_image_date_base_pref_key), MODE_PRIVATE);
        SharedPreferences imageAuthorPreferences = context.getSharedPreferences(listName + context.getString(R.string.muzei_image_author_base_prefs_key), MODE_PRIVATE);
        SharedPreferences imageDownloadLinksPreferences = context.getSharedPreferences(listName + context.getString(R.string.muzei_image_download_links), MODE_PRIVATE);

        if (!((wolpepper) ((Activity) context).getApplication()).isAppTempPremium()) {
            if (imageIdPreferences.getAll().size() >= Integer.parseInt(context.getString(R.string.maximum_allowed_lists))) {
                return MAXIMUM_LIST_ALLOWED_REACHED;
            }
        }

        if (imageIdPreferences.contains(listName)) {
            return LIST_NAME_ALREADY_EXIST;
        }

        /* get image ID Set which has key of active muzei List */
        Set<String> imageIdSet = new HashSet<>();
        imageIdSet.add(imageId);

        imageDatePreferences.edit().putString(imageId, date).commit();

        imageAuthorPreferences.edit().putString(imageId, authorName).commit();

        imageDownloadLinksPreferences.edit().putString(imageId, downloadLink).commit();

        imageIdPreferences.edit().putStringSet(listName, imageIdSet).commit();

        if (setActive) {
            setListAsActiveMuzeiList(context, listName);
        }
        return MUZEI_LIST_OPERATION_SUCCESSFUL;
    }

    /**
     * Delete List for muzei
     *
     * @param context  - Context
     * @param listName - Name of List
     */
    public static int deleteMuzeiList(Context context, String listName) {
        // imageIdPreferences have KEYS as ListName and Values as StringSet of image ID's in that list
        SharedPreferences imageIdPreferences = context.getSharedPreferences(context.getString(R.string.muzei_image_id_base_prefs_key), MODE_PRIVATE);
        // Take a good look where listName is used
        SharedPreferences imageDatePreferences = context.getSharedPreferences(listName + context.getString(R.string.muzei_image_date_base_pref_key), MODE_PRIVATE);
        SharedPreferences imageAuthorPreferences = context.getSharedPreferences(listName + context.getString(R.string.muzei_image_author_base_prefs_key), MODE_PRIVATE);
        SharedPreferences imageDownloadLinksPreferences = context.getSharedPreferences(listName + context.getString(R.string.muzei_image_download_links), MODE_PRIVATE);

        imageDatePreferences.edit().clear().commit();

        imageAuthorPreferences.edit().clear().commit();

        imageDownloadLinksPreferences.edit().clear().commit();

        imageIdPreferences.edit().remove(listName).commit();

        return MUZEI_LIST_OPERATION_SUCCESSFUL;
    }

    /**
     * Method To get active muzei list name
     *
     * @param context - Context
     * @return - The name of currently active Muzei list
     */
    public static String getActiveMuzeiListName(Context context) {
        SharedPreferences activeCollectionPreference = context.getSharedPreferences(context.getString(R.string.muzei_active_list_base_prefs_key), MODE_PRIVATE);

        return activeCollectionPreference.getString(context.getString(R.string.muzei_active_list_key), "");
    }

    /**
     * Method to get all active list name
     *
     * @param context - Context
     * @return - ArrayList of names of all the list created
     */
    public static ArrayList<String> getAllMuzeiListNames(Context context) {
        SharedPreferences imageIdPreferences = context.getSharedPreferences(context.getString(R.string.muzei_image_id_base_prefs_key), MODE_PRIVATE);

        ArrayList<String> list = new ArrayList<>();

        Collections.addAll(list, imageIdPreferences.getAll().keySet().toArray(new String[0]));

        return list;
    }

    /**
     * Method to get specific muzei list
     *
     * @param context  - Context
     * @param listName - Name of the list
     * @return - The requested Muzei list
     */
    public static MuzeiList getMuzeiList(Context context, String listName) {
        if (!isAnyListCreated(context)) {
            return null;
        }
        // imageIdPreferences have KEYS as ListName and Values as StringSet of image ID's in that list
        SharedPreferences imageIdPreferences = context.getSharedPreferences(context.getString(R.string.muzei_image_id_base_prefs_key), MODE_PRIVATE);
        SharedPreferences imageDownloadLinksPreferences = context.getSharedPreferences(listName + context.getString(R.string.muzei_image_download_links), MODE_PRIVATE);

        Set<String> imageIdSet = new HashSet<>(imageIdPreferences.getStringSet(listName, null));

        String[] imageIdArray = imageIdSet.toArray(new String[0]);

        ArrayList<Papers> papersList = new ArrayList<>();

        for (String imageId : imageIdArray) {
            Papers paper = new Papers();
            paper.setFullImageUrl(imageDownloadLinksPreferences.getString(imageId, ""));
            paper.setImageId(imageId);
            papersList.add(paper);
        }

        MuzeiList muzeiList = new MuzeiList();

        muzeiList.setListName(listName);
        muzeiList.setListOfPapersInList(papersList);
        if (getActiveMuzeiListName(context).equals(listName)) {
            muzeiList.setActive(true);
        }

        return muzeiList;
    }

//    public static Bitmap setGreyscaleLevel(Bitmap bitmap, float greyscaleLevel){
//        Canvas c = new Canvas(bitmap);
//        Paint paint = new Paint();
//        ColorMatrix cm = new ColorMatrix();
//        cm.setSaturation(greyscaleLevel);
//        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
//        paint.setColorFilter(f);
//        c.drawBitmap(bitmap, 0, 0, paint);
//        return bitmap;
//    }

    /**
     * Checks which storage is currently used and set by user in settings.
     * @param context - Context for checking storage.
     * @return - return a boolean value based on whether the default storage is selected or custom
     */
    public static boolean isDefaultStorage(Context context){
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.storage_preference_base_key), MODE_PRIVATE);
        return  preferences.getBoolean(context.getString(R.string.storage_type_pref_key), true);
    }

    public static boolean isAppPremium(Context context){
        return getAppInstance(context).isAppTempPremium();
    }

    /**
     * Checks and returns the String path of the currently used directory for storage and download
     * @param context - Context for checking storage.
     * @return - A string value containing the path of the current storage location
     */
    @SuppressLint("NewApi")
    public static String getExternalStoragePath(Context context){
        File[] fileArray = context.getExternalFilesDirs(IMAGE_LOCAL_STORAGE_FOLDER_NAME);

        if (fileArray.length > 1){
            return fileArray[fileArray.length - 1].getAbsolutePath();
        }
        return fileArray[0].getAbsolutePath();
    }

    /**
     * Checks and returns the file Uri of the currently used directory for storage and download
     * @param context - Context for checking storage.
     * @return - A file uri value containing the path of the current storage location
     */
    @SuppressLint("NewApi")
    public static Uri getExternalStorageUri(Context context){
        File[] fileArray = context.getExternalFilesDirs(IMAGE_LOCAL_STORAGE_FOLDER_NAME);

        if (fileArray.length > 1){
            return Uri.fromFile(fileArray[fileArray.length - 1]);
        }
        return Uri.fromFile(fileArray[0]);
    }

    public static long getTempPremium(Context context){
        return context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE).getLong("grant", -1);
    }

    public static void grantTempPremium(Context context){
        context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE).edit().putLong("grant", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)).commit();
    }

    public static String getDefaultDownloadFormat(Context context, @StringRes int defaultType){
        return context.getSharedPreferences(context.getString(R.string.default_image_format_base_pref_key), MODE_PRIVATE).getString(context.getString(R.string.default_image_format_pref_key), context.getString(defaultType));
    }


    public static void updateFullWidgetUnlocked(Context context, boolean isFull){
        context.getSharedPreferences(FULL_WIDGET_UNLOCKED_PREFENCES_BASE_KEY, MODE_PRIVATE).edit().putBoolean(FULL_WIDGET_UNLOCKED_KEY, isFull).apply();
    }

    public static boolean isFullWidget(Context context){
        return System.currentTimeMillis() < UtilityMethods.getTempPremium(context);
    }

    public static void showFullWidgetRequiredToast(Context context){
        Toast.makeText(context, PREMIUM_UPGRADE_REQUIRED_WIDGET_TOAST,  Toast.LENGTH_LONG).show();
    }



    public static void executeRunnable(ThreadType whichThread, Runnable runnable) {
        switch (whichThread) {
            case MAIN:
                AppExecutors.getInstance().mainThread().execute(runnable);
                break;

            case NETWORK:
                AppExecutors.getInstance().networkIO().execute(runnable);
                break;
        }
    }
}
