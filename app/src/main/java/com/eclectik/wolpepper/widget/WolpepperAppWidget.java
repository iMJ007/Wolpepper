package com.eclectik.wolpepper.widget;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
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

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.bumptech.glide.request.target.AppWidgetTarget;
import com.eclectik.wolpepper.BuildConfig;
import com.eclectik.wolpepper.GlideApp;
import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.activities.MainWallpapersActivity;
import com.eclectik.wolpepper.dataStructures.Papers;
import com.eclectik.wolpepper.utils.ConstantValues;
import com.eclectik.wolpepper.utils.DownloadWallpaperUtils;
import com.eclectik.wolpepper.utils.NetworkUtils;
import com.eclectik.wolpepper.utils.SetWallpaperService;
import com.eclectik.wolpepper.utils.UtilityMethods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implementation of App Widget functionality.
 */
public class WolpepperAppWidget extends AppWidgetProvider {

    public static final String WIDGET_REFRESH_ACTION = "wolpepper_widget_refresh_action";
    public static final String WIDGET_SHARE_ACTION = "wolpepper_widget_share_action";
    public static final String WIDGET_DOWNLOAD_ACTION = "wolpepper_widget_download_action";
    public static final String WIDGET_APPLY_WALLPAPER_ACTION = "wolpepper_widget_apply_wallpaper_action";

    private static final String SHARE_PAPER_EXTRA = "share_paper_extra";
    private static final String DOWNLOAD_PAPER_EXTRA = "download_paper_extra";
    private static final String APPLY_WALLPAPER_EXTRA = "apple_paper_extra";
    public static final String WIDGET_WALLPAPER_DETAILS_EXTRA = "wolpepper_widget_full_details_extra";

    private AppWidgetTarget appWidgetTarget;
    private Context context;

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId) {

        this.context = context;
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wolpepper_app_widget);
        views.setOnClickPendingIntent(R.id.widget_refresh_button, getSelfPendingIntent(context, WIDGET_REFRESH_ACTION, appWidgetId));

        appWidgetTarget = new AppWidgetTarget(context, R.id.widget_wallpaper_view, views, appWidgetId);
        updateImage(views, appWidgetId);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    void showProgressBar(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wolpepper_app_widget);
        views.setViewVisibility(R.id.widget_progress_bar, VISIBLE);
        views.setViewVisibility(R.id.widget_error_image_view, GONE);
        views.setOnClickPendingIntent(R.id.widget_download_button, null);
        views.setOnClickPendingIntent(R.id.widget_share_button, null);
        views.setOnClickPendingIntent(R.id.widget_refresh_button, null);
        views.setOnClickPendingIntent(R.id.widget_apply_wolpepper_button, null);
        views.setOnClickPendingIntent(R.id.widget_wallpaper_view, null);

        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        switch (intent.getAction()) {
            case WIDGET_REFRESH_ACTION:
                showProgressBar(context, AppWidgetManager.getInstance(context), appWidgetId);
                updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId);
                break;
            case WIDGET_SHARE_ACTION:
                // TODO check pro user condition
                if (!UtilityMethods.isFullWidget(context)) {
                    UtilityMethods.showFullWidgetRequiredToast(context);
                    return;
                }
                Papers sharedPaper = intent.getParcelableExtra(SHARE_PAPER_EXTRA);
                performPaperShare(context, sharedPaper);
                break;
            case WIDGET_DOWNLOAD_ACTION:
                // TODO check pro user condition
                if (!UtilityMethods.isFullWidget(context)) {
                    UtilityMethods.showFullWidgetRequiredToast(context);
                    return;
                }
                Papers downloadPaper = intent.getParcelableExtra(DOWNLOAD_PAPER_EXTRA);
                String defaultDownloadFormat = UtilityMethods.getDefaultDownloadFormat(context, R.string.jpeg_image_type);
                startPaperDownload(context, defaultDownloadFormat, downloadPaper);
                break;
            case WIDGET_APPLY_WALLPAPER_ACTION:
                // TODO check pro user condition
                if (!UtilityMethods.isFullWidget(context)) {
                    UtilityMethods.showFullWidgetRequiredToast(context);
                    return;
                }
                Papers appliedWallpaper = intent.getParcelableExtra(APPLY_WALLPAPER_EXTRA);
                performApplyWallpaper(context, appliedWallpaper);
                break;
            default:
                super.onReceive(context, intent);
                break;
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @SuppressLint("StaticFieldLeak")
    private void updateImage(final RemoteViews views, final int widgetId) {
        new AsyncTask<Void, Void, Integer>() {
            String json = "";
            final int RATE_LIMIT_REACHED_ERROR = 1;
            final int EXCEPTION_OCCURRED = 2;
            Papers papers = null;

            @Override
            protected Integer doInBackground(Void... voids) {
                URL url = NetworkUtils.buildRandomPhotosListUrl(BuildConfig.UNSPLASH_APP_ID, String.valueOf(1), "1");
                try {
                    json = NetworkUtils.getResponseFromHttpUrl(url, WolpepperAppWidget.this.context.getApplicationContext());
                    if (json != null && json.equals(RATE_LIMIT_REMAINING_HEADER_KEY)) {
                        return RATE_LIMIT_REACHED_ERROR;
                    }

                    papers = parseJson(WolpepperAppWidget.this.context, json);
                } catch (IOException e) {
                    e.printStackTrace();
                    return EXCEPTION_OCCURRED;
                }
                return 0;
            }

            @Override
            protected void onPostExecute(Integer error) {
                switch (error) {
                    case RATE_LIMIT_REACHED_ERROR:
                        // TODO SET ERROR
                        views.setViewVisibility(R.id.widget_progress_bar, GONE);
                        views.setTextViewText(R.id.widget_author_name_text_view, "Rate limit");
                        views.setTextViewText(R.id.widget_image_date_text_view, "reached");
                        views.setViewVisibility(R.id.widget_error_image_view, VISIBLE);
                        AppWidgetManager.getInstance(context).partiallyUpdateAppWidget(widgetId, views);
                        break;
                    case EXCEPTION_OCCURRED:
                        // TODO SET ERROR
                        views.setViewVisibility(R.id.widget_progress_bar, GONE);
                        views.setTextViewText(R.id.widget_author_name_text_view, "Some connection");
                        views.setTextViewText(R.id.widget_image_date_text_view, "error occurred");
                        views.setViewVisibility(R.id.widget_error_image_view, VISIBLE);
                        AppWidgetManager.getInstance(context).partiallyUpdateAppWidget(widgetId, views);
                        break;
                    default:
                        if (papers == null) {
                            return;
                        }

                        // TODO set CLICK PENDING INTENTS ON RESPECTIVE BUTTONS

                        views.setViewVisibility(R.id.widget_progress_bar, GONE);
                        views.setTextViewText(R.id.widget_author_name_text_view, papers.getAuthorName());
                        views.setTextViewText(R.id.widget_image_date_text_view, papers.getDate());
                        views.setOnClickPendingIntent(R.id.widget_share_button, getSharePendingIntent(context, widgetId, papers));
                        views.setOnClickPendingIntent(R.id.widget_download_button, getDownloadPendingIntent(context, widgetId, papers));
                        views.setOnClickPendingIntent(R.id.widget_apply_wolpepper_button, getApplyWallpaperPendingIntent(context, widgetId, papers));
                        views.setOnClickPendingIntent(R.id.widget_wallpaper_view, getWolpepperDetailPendingIntent(context, widgetId, papers));
                        GlideApp.with(context.getApplicationContext()) // safer!
                                .asBitmap()
                                .load(papers.getDisplayImageUrl())
                                .into(appWidgetTarget);
                }
            }
        }.execute();
    }

    private String getParsedDate(String nonParsedString) {
        //Formatting Date
        String formattedDate = "";
        DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        fromFormat.setLenient(false);
        DateFormat toFormat = new SimpleDateFormat("d MMMM, yyyy");
        toFormat.setLenient(false);
        try {
            Date date = fromFormat.parse(nonParsedString);
            formattedDate = toFormat.format(date);
            return formattedDate;
        } catch (ParseException e) {
            return "Unknown Date";
        }

    }

    /**
     * Parse the retrieved String to JSON and further Objects
     *
     * @param json - The JSON String to be parsed
     */
    private Papers parseJson(Context context, String json) {
        if (context == null) {
            return null;
        }

        try {
            if (json.substring(0, 1).equals("{") && new JSONObject(json).has("error")) {
                Toast.makeText(context, json, Toast.LENGTH_LONG).show();
                return null;
            }

            double qualityPercent = context.getSharedPreferences(context.getString(R.string.preview_quality_base_pref_key), Context.MODE_PRIVATE).getFloat(context.getString(R.string.preview_quality_percent_pref_key), 45);
            DisplayMetrics displayMetrics = new DisplayMetrics();

            ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);


            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                //Current Json Object
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String imageId = jsonObject.getString(ConstantValues.IMAGE_ID_KEY);
                String date = jsonObject.getString(ConstantValues.IMAGE_CREATED_DATE_KEY);
                date = getParsedDate(date);
                String color = jsonObject.getString(ConstantValues.IMAGE_COLOR_KEY);
                int height = jsonObject.getInt(IMAGE_HEIGHT_KEY);
                int width = jsonObject.getInt(IMAGE_WIDTH_KEY);

                boolean isLiked = jsonObject.getBoolean(ConstantValues.IMAGE_LIKED_BY_USER_KEY);

                //User Details from json object
                JSONObject userDetails = jsonObject.getJSONObject(ConstantValues.IMAGE_USER_DETAILS_OBJECT_KEY);
                String name = userDetails.getString(ConstantValues.IMAGE_NAME_OF_USER_KEY);
                String userName = userDetails.getString(ConstantValues.IMAGE_USERNAME_KEY);
//                String userId = userDetails.getString(ConstantValues.IMAGE_USER_ID_KEY);
                String userProfileImage = userDetails.getJSONObject(ConstantValues.IMAGE_USER_PROFILE_PIC_KEY).getString(ConstantValues.IMAGE_USER_PROFILE_PIC_MEDIUM_KEY);
                String userProfileLink = userDetails.getJSONObject(IMAGE_LINKS_KEY).getString(IMAGE_HTML_LINK_KEY);
                String userBio = userDetails.optString(IMAGE_USER_BIO_KEY, "null");
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
                paper.setDownloadImageApiCallUrl(apiDownloadSpot);
                return paper;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    /**
     * Start Wallpaper Download
     *
     * @param context
     * @param defaultDownloadFormat
     * @param downloadPaper
     */
    private void startPaperDownload(Context context, String defaultDownloadFormat, Papers downloadPaper) {
        if (defaultDownloadFormat.equals(context.getString(R.string.raw_image_type))) {
            if (DownloadWallpaperUtils.checkIfFileIsAlreadyDownloading(downloadPaper.getImageId())) {

                Toast.makeText(context, context.getString(R.string.image_already_downloading_desc_1) + downloadPaper.getAuthorName() + context.getString(R.string.image_already_downloading_desc_2) + downloadPaper.getImageId() + context.getString(R.string.image_already_downloading_desc_3), Toast.LENGTH_LONG).show();

            } else if (DownloadWallpaperUtils.checkRawImageExistOnStorage(context, downloadPaper.getImageId())) {

                Toast.makeText(context, "Image Of " + downloadPaper.getAuthorName() + " with Id:" + downloadPaper.getImageId() + " already exist on storage!", Toast.LENGTH_LONG).show();

            } else {

                DownloadWallpaperUtils.downloadPaper(context, Uri.parse(downloadPaper.getRawImageUrl()), downloadPaper, false, false, true);
                Toast.makeText(context, "Downloading RAW image Of " + downloadPaper.getAuthorName(), Toast.LENGTH_LONG).show();

            }
        } else if (defaultDownloadFormat.equals(context.getString(R.string.jpeg_image_type))) {

            if (DownloadWallpaperUtils.checkIfFileIsAlreadyDownloading(downloadPaper.getImageId())) {

                Toast.makeText(context, context.getString(R.string.image_already_downloading_desc_1) + downloadPaper.getAuthorName() + context.getString(R.string.image_already_downloading_desc_2) + downloadPaper.getImageId() + context.getString(R.string.image_already_downloading_desc_3), Toast.LENGTH_LONG).show();

            } else if (DownloadWallpaperUtils.checkImageExistOnStorage(context, downloadPaper.getImageId(), false)) {

                Toast.makeText(context, "Image Of " + downloadPaper.getAuthorName() + " with Id:" + downloadPaper.getImageId() + " already exist on storage!", Toast.LENGTH_LONG).show();

            } else {

                DownloadWallpaperUtils.downloadPaper(context, Uri.parse(downloadPaper.getFullImageUrl()), downloadPaper, false, false, false);
                Toast.makeText(context, "Downloading image Of " + downloadPaper.getAuthorName(), Toast.LENGTH_LONG).show();

            }
        }
    }

    /**
     * Start Wallpaper Share
     *
     * @param context
     * @param sharedPaper
     */
    private void performPaperShare(Context context, Papers sharedPaper) {
        if (DownloadWallpaperUtils.checkIfFileIsAlreadyDownloading(sharedPaper.getImageId())) {
            Toast.makeText(context, R.string.share_image_already_downloading_desc, Toast.LENGTH_LONG).show();
            return;
        }

        if (DownloadWallpaperUtils.checkImageExistOnStorage(context, sharedPaper.getImageId(), false)) {
            DownloadWallpaperUtils.shareImage(context, sharedPaper.getImageId(), false, sharedPaper.getAuthorName());
        } else {
            long referenceID = DownloadWallpaperUtils.downloadAndSharePaper(context, Uri.parse(sharedPaper.getFullImageUrl()), sharedPaper, false, false, false);
            if (referenceID != -1) {
                Toast.makeText(context, R.string.share_image_alert_desc, Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void performApplyWallpaper(Context context, Papers appliedWallpaper) {
        Intent intent = new Intent(context, SetWallpaperService.class).putExtra(context.getString(R.string.set_wallpaper_intent_extra), appliedWallpaper).putExtra(context.getString(R.string.grey_scale_identifier_intent_extra), false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }

        Toast.makeText(context, R.string.set_wallpaper_desc, Toast.LENGTH_LONG).show();
    }

    /* *************************************** PENDING INTENTS ***************************************/

    /**
     * Base pending intent
     *
     * @param context
     * @param action
     * @param widgetId
     * @return
     */
    private PendingIntent getSelfPendingIntent(Context context, String action, int widgetId) {
        Intent intent = new Intent(context, WolpepperAppWidget.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    /**
     * Share Pending Intent
     *
     * @param context
     * @param widgetId
     * @param paper
     * @return Share Pending Intent
     */
    private PendingIntent getSharePendingIntent(Context context, int widgetId, Papers paper) {
        Intent intent = new Intent(context, WolpepperAppWidget.class);
        intent.setAction(WIDGET_SHARE_ACTION);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.putExtra(SHARE_PAPER_EXTRA, paper);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getDownloadPendingIntent(Context context, int widgetId, Papers paper) {
        Intent intent = new Intent(context, WolpepperAppWidget.class);
        intent.setAction(WIDGET_DOWNLOAD_ACTION);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.putExtra(DOWNLOAD_PAPER_EXTRA, paper);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getApplyWallpaperPendingIntent(Context context, int widgetId, Papers paper) {
        Intent intent = new Intent(context, WolpepperAppWidget.class);
        intent.setAction(WIDGET_APPLY_WALLPAPER_ACTION);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.putExtra(APPLY_WALLPAPER_EXTRA, paper);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getWolpepperDetailPendingIntent(Context context, int widgetId, Papers papers) {
        /* This is intent for launching word details */
        Intent launchWordDetailsIntent = new Intent(context, MainWallpapersActivity.class);
        launchWordDetailsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);  // Identifies the particular widget...
        launchWordDetailsIntent.putExtra(WIDGET_WALLPAPER_DETAILS_EXTRA, papers);
        launchWordDetailsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Make the pending intent unique...
        launchWordDetailsIntent.setData(Uri.parse(launchWordDetailsIntent.toUri(Intent.URI_INTENT_SCHEME)));
        return PendingIntent.getActivity(context, 0, launchWordDetailsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}

