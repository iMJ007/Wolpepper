package com.eclectik.wolpepper.activities;

import static com.eclectik.wolpepper.utils.ConstantValues.CALLING_ACTIVITY_NAME;
import static com.eclectik.wolpepper.utils.ConstantValues.ErrorKeys.RESPONSE_SOMETHING_WENT_WRONG_ERROR;
import static com.eclectik.wolpepper.utils.ConstantValues.ErrorKeys.RESPONSE_TIMEOUT_ERROR;
import static com.eclectik.wolpepper.utils.ConstantValues.HeaderKeys.RATE_LIMIT_REMAINING_HEADER_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_LIKED_BY_USER_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_REGULAR_IMAGE_URL_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_URLS_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.RequestMethodKeys.DELETE_METHOD;
import static com.eclectik.wolpepper.utils.ConstantValues.RequestMethodKeys.POST_METHOD;
import static com.eclectik.wolpepper.utils.ConstantValues.URL_KEY;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.eclectik.wolpepper.BuildConfig;
import com.eclectik.wolpepper.GlideApp;
import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.dataStructures.Papers;
import com.eclectik.wolpepper.utils.ConstantValues;
import com.eclectik.wolpepper.utils.ConstantValues.PreferencesKeys;
import com.eclectik.wolpepper.utils.DownloadWallpaperUtils;
import com.eclectik.wolpepper.utils.NetworkUtils;
import com.eclectik.wolpepper.utils.SetWallpaperService;
import com.eclectik.wolpepper.utils.UtilityMethods;
import com.eclectik.wolpepper.widget.WolpepperAppWidget;
import com.eclectik.wolpepper.wolpepper;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.jaouan.revealator.Revealator;
import com.tapadoo.alerter.Alerter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainWallpapersActivity extends AppCompatActivity {

    private Papers currentPaper = new Papers();
    private SharedPreferences MainWallpapersActivityTapTargetPreference;
    private String regularResolutionUrl = "";

    // Views
    private ImageView wallpaperImageView;
    private ImageView viewOnUnsplashView;
    private ShapeableImageView authorProfileImageView;
    private View theAwesomeView;
    private FloatingActionButton fab;
    private ImageView theWonderfulButton;
    private TextView actionBarAuthorName, actionBarDate;

    // FABS IMAGE VIEWS
    private ImageView downloadButton, greyScaleButton, likeButton, shareButton;
    private FloatingActionButton applyWallpaperFab;

    // STATS TEXT VIEWS
    private TextView totalDownloadsTv, totalLikesTv, totalViewsTv;

    // STORY TEXT VIEWS
    private TextView storyTitleTv, storyBodyTv;

    // EXIF TEXT VIEWS
    private TextView exifCameraTv, exifExposureTimeTv, exifFocalLengthTv, exifAperture, exifResolutionTv, exifLocationTv, exifColorTv, exifIsoTv;

    private boolean isImageDownloading = false;
    private boolean isGreyEnabled = false;

    private TapTargetSequence sequence;

    private String defaultDownloadFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wallpapers);
        MainWallpapersActivityTapTargetPreference = PreferenceManager.getDefaultSharedPreferences(MainWallpapersActivity.this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        defaultDownloadFormat = getSharedPreferences(getString(R.string.default_image_format_base_pref_key), MODE_PRIVATE).getString(getString(R.string.default_image_format_pref_key), getString(R.string.always_ask_image_type));

        /* CHECK SHARED PREFERENCE IF THE IMAGE's EXTRA DETAIL JSON IS present in storage **/
        SharedPreferences imageDetailPrefs = getSharedPreferences(PreferencesKeys.IMAGE_EXTRA_DETAILS_PREFERENCES_BASE_KEY, Context.MODE_PRIVATE);

        if (getIntent().hasExtra(WolpepperAppWidget.WIDGET_WALLPAPER_DETAILS_EXTRA)) {
            currentPaper = getIntent().getParcelableExtra(WolpepperAppWidget.WIDGET_WALLPAPER_DETAILS_EXTRA);

            // used to update the element in recycle list if like/unlike changes

            // SETTING UP ALL VIEWS
            getViewsInitialized();

            updateInitialViews();

            setInitialViewsClickListeners();

            // GET JSON FROM PREFERENCES AND IF NOT PRESENT RETURN NUll
            String imageFullJson = imageDetailPrefs.getString(currentPaper.getImageId(), null);

            if (imageFullJson != null) {
                parseExtraDetails(imageFullJson);

                findViewById(R.id.extra_content_loading_progress_bar).setVisibility(View.GONE);

                findViewById(R.id.image_extra_content_layout).setVisibility(View.VISIBLE);

            } else {
                new LoadImageDataAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            isDownloadingCheck();

        }


        /*
         * This transparent code is working
         * */
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        /*Overriding Up Button as Back*/
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        setUpTapTargetsView();

    }

    private void getViewsInitialized() {
        authorProfileImageView = findViewById(R.id.profile_image);
        wallpaperImageView = findViewById(R.id.wallpaper_image);
        viewOnUnsplashView = findViewById(R.id.view_image_fullScreen_view);
        fab = findViewById(R.id.fab);
        theAwesomeView = findViewById(R.id.the_awesome_view);
        theWonderfulButton = findViewById(R.id.the_wonderful_button);

        // FAB BAR IMAGEVIEWS & FAB
        downloadButton = findViewById(R.id.dowload_button);
        greyScaleButton = findViewById(R.id.greyscale_button);
        applyWallpaperFab = findViewById(R.id.apply_wallpaper_fab);
        likeButton = findViewById(R.id.like_button);
        shareButton = findViewById(R.id.share_button);

        // Text Views
        actionBarAuthorName = findViewById(R.id.action_bar_author_name);
        actionBarDate = findViewById(R.id.action_bar_date);

        // Stats Text Views
        totalDownloadsTv = findViewById(R.id.total_downloads);
        totalLikesTv = findViewById(R.id.total_likes);
        totalViewsTv = findViewById(R.id.total_views);

        // Story Text Views
        storyTitleTv = findViewById(R.id.story_heading);
        storyBodyTv = findViewById(R.id.story_body);

        //Exif TextViews
        exifAperture = findViewById(R.id.exif_aperture);
        exifCameraTv = findViewById(R.id.exif_camera);
        exifColorTv = findViewById(R.id.exif_color);
        exifExposureTimeTv = findViewById(R.id.exif_exposure_time);
        exifFocalLengthTv = findViewById(R.id.exif_focal_length);
        exifIsoTv = findViewById(R.id.exif_iso);
        exifLocationTv = findViewById(R.id.exif_location);
        exifResolutionTv = findViewById(R.id.exif_resolution);

    }

    /**
     * Update Views which get Data from Intent
     */
    private void updateInitialViews() {
//        GlideApp.with(this).load().into(wallpaperImageView);
        GlideApp.with(MainWallpapersActivity.this).load(currentPaper.getFullImageUrl()).thumbnail(Glide.with(this).load(currentPaper.getDisplayImageUrl())).transition(new DrawableTransitionOptions().crossFade(500)).into(wallpaperImageView);
        GlideApp.with(this).load(currentPaper.getAuthorImageUrl()).into(authorProfileImageView);
        actionBarAuthorName.setText(currentPaper.getAuthorName());
        //Formatting Date
        String formattedDate = "";
        DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        fromFormat.setLenient(false);
        DateFormat toFormat = new SimpleDateFormat("d MMMM, yyyy");
        toFormat.setLenient(false);

        Date date = null;
        try {
            date = fromFormat.parse(currentPaper.getDate());
            formattedDate = toFormat.format(date);
            actionBarDate.setText(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            actionBarDate.setText(currentPaper.getDate());
        }

        setLikeButtonImage();
    }

    /**
     * Sets up the tap target views for this activity
     */
    private void setUpTapTargetsView() {
        if (!MainWallpapersActivityTapTargetPreference.getBoolean("firstTimer", false)) {
            sequence = new TapTargetSequence(MainWallpapersActivity.this)
                    .targets(
                            TapTarget.forView(applyWallpaperFab, getString(R.string.apply_wallpaper_fab_tap_target_title), getString(R.string.apply_wallpaper_fab_tap_target_desc))
                                    .outerCircleColor(R.color.colorPrimary)      // Specify a color for the outer circle
                                    .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                                    .targetCircleColor(R.color.white)   // Specify a color for the target circle
                                    .titleTextSize(22)                  // Specify the size (in sp) of the title text
                                    .titleTextColor(R.color.white)      // Specify the color of the title text
                                    .descriptionTextSize(18)            // Specify the size (in sp) of the description text
                                    .descriptionTextColor(R.color.colorPrimary)  // Specify the color of the description text
                                    .textColor(R.color.white)            // Specify a color for both the title and description text
                                    .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                    // If set, will dim behind the view with 30% opacity of the given color
                                    .drawShadow(true)                   // Whether to draw a drop shadow or not
                                    .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                    .tintTarget(false)                   // Whether to tint the target view's color
                                    .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                                    .targetRadius(60),
                            TapTarget.forView(greyScaleButton, getString(R.string.grayscale_button_tap_target_title), getString(R.string.grayscale_button_tap_target_desc))
                                    .outerCircleColor(R.color.colorPrimary)      // Specify a color for the outer circle
                                    .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                                    .targetCircleColor(R.color.white)   // Specify a color for the target circle
                                    .titleTextSize(22)                  // Specify the size (in sp) of the title text
                                    .titleTextColor(R.color.white)      // Specify the color of the title text
                                    .descriptionTextSize(18)            // Specify the size (in sp) of the description text
                                    .descriptionTextColor(R.color.colorPrimary)  // Specify the color of the description text
                                    .textColor(R.color.white)            // Specify a color for both the title and description text
                                    .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                    // If set, will dim behind the view with 30% opacity of the given color
                                    .drawShadow(true)                   // Whether to draw a drop shadow or not
                                    .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                    .tintTarget(true)                   // Whether to tint the target view's color
                                    .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                                    .targetRadius(60),        // Specify the target radius (in dp)
                            TapTarget.forView(downloadButton, getString(R.string.download_button_tap_target_title), getString(R.string.download_button_tap_target_desc))
                                    .outerCircleColor(R.color.colorPrimary)      // Specify a color for the outer circle
                                    .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                                    .targetCircleColor(R.color.white)   // Specify a color for the target circle
                                    .titleTextSize(22)                  // Specify the size (in sp) of the title text
                                    .titleTextColor(R.color.white)      // Specify the color of the title text
                                    .descriptionTextSize(18)            // Specify the size (in sp) of the description text
                                    .descriptionTextColor(R.color.colorPrimary)  // Specify the color of the description text
                                    .textColor(R.color.white)            // Specify a color for both the title and description text
                                    .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                    // If set, will dim behind the view with 30% opacity of the given color
                                    .drawShadow(true)                   // Whether to draw a drop shadow or not
                                    .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                    .tintTarget(true)                   // Whether to tint the target view's color
                                    .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                                    .targetRadius(60)        // Specify the target radius (in dp)
                    ).listener(new TapTargetSequence.Listener() {
                        @Override
                        public void onSequenceFinish() {
                            SharedPreferences.Editor editor = MainWallpapersActivityTapTargetPreference.edit();
                            editor.putBoolean("firstTimer", true);
                            editor.apply();
                        }

                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {
                            SharedPreferences.Editor editor = MainWallpapersActivityTapTargetPreference.edit();
                            editor.putBoolean("firstTimer", true);
                            editor.apply();
                        }
                    });
            sequence.start();


        }
    }

    private void setLikeButtonImage() {
        if (currentPaper.isLiked()) {
            likeButton.setImageResource(R.drawable.ic_favorite_white_24dp);
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
     * Set Click Listeners On views which got data from Intent
     */
    private void setInitialViewsClickListeners() {
        if (getIntent().hasExtra(WolpepperAppWidget.WIDGET_WALLPAPER_DETAILS_EXTRA) || !getIntent().getStringExtra(CALLING_ACTIVITY_NAME).equals(ProfileMainActivity.class.getSimpleName())) {
            authorProfileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainWallpapersActivity.this, ProfileMainActivity.class);
                    intent.putExtra(ConstantValues.IMAGE_NAME_OF_USER_KEY, currentPaper.getAuthorName());
                    intent.putExtra(ConstantValues.IMAGE_USER_PROFILE_PIC_KEY, currentPaper.getAuthorImageUrl());
                    intent.putExtra(ConstantValues.IMAGE_USERNAME_KEY, currentPaper.getAuthorUserName());
                    intent.putExtra(ConstantValues.IMAGE_USER_TOTAL_PHOTOS, currentPaper.getAuthorTotalPhotos());
                    intent.putExtra(ConstantValues.IMAGE_USER_TOTAL_LIKES, currentPaper.getAuthorTotalLikes());
                    intent.putExtra(ConstantValues.IMAGE_USER_TOTAL_COLLECTIONS, currentPaper.getAuthorTotalCollections());
                    intent.putExtra(ConstantValues.IMAGE_USER_PROFILE_LINK_KEY, currentPaper.getProfileUrl());
                    intent.putExtra(ConstantValues.IMAGE_USER_BIO_KEY, currentPaper.getAuthorBio());
                    intent.putExtra(ConstantValues.IMAGE_REGULAR_IMAGE_URL_KEY, currentPaper.getDisplayImageUrl());
                    intent.putExtra(ConstantValues.IMAGE_LOCATION_KEY, currentPaper.getLocation());
                    if (!TextUtils.isEmpty(regularResolutionUrl)) {
                        intent.putExtra(ConstantValues.IMAGE_FULL_IMAGE_URL_KEY, regularResolutionUrl);
                    }
                    startActivity(intent);

                }
            });
        }

        // Download Button Listener
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDefaultFormatAndDownloadImage();
            }
        });

        // View On Unsplash Image View Click Listener
        viewOnUnsplashView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, NetworkUtils.buildLinkWithUtmParameters(MainWallpapersActivity.this, currentPaper.getImageHtmlLink())));
            }
        });
        // GreyScale Button Listener
        greyScaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGreyEnabled) {
                    isGreyEnabled = false;
                    UtilityMethods.setImageViewColored(wallpaperImageView);
                    greyScaleButton.setImageResource(R.drawable.ic_color_lens_white_24dp);
                } else {
                    isGreyEnabled = true;
                    greyScaleButton.setImageResource(R.drawable.ic_action_ic_color_lens_black_cancel_24dp);
                    UtilityMethods.setImageViewGreyScale(wallpaperImageView);
                    Alerter.create(MainWallpapersActivity.this)
                            .setTextTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_regular))
                            .setTitleTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_bold))
                            .setTextAppearance(R.style.alertBody)
                            .setTitleAppearance(R.style.alertTitle)
                            .setTitle(getString(R.string.grey_scale_title))
                            .setText(getString(R.string.grey_scale_desc))
                            .show();
                }
            }
        });

        // Apply Wallpaper Button Listener
        applyWallpaperFab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                if (isMyServiceRunning(SetWallpaperService.class)) {

                    Alerter.create(MainWallpapersActivity.this)
                            .setTextTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_regular))
                            .setTitleTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_bold))
                            .setTextAppearance(R.style.alertBody)
                            .setTitleAppearance(R.style.alertTitle)
                            .setTitle(getString(R.string.something_went_wrong_error))
                            .setText(getString(R.string.apply_wallpaper_already_running))
                            .setBackgroundColorRes(R.color.alert_default_error_background)
                            .setDuration(5000)
                            .show();


                } else {
                    Intent intent = new Intent(MainWallpapersActivity.this, SetWallpaperService.class).putExtra(getString(R.string.set_wallpaper_intent_extra), currentPaper).putExtra(getString(R.string.grey_scale_identifier_intent_extra), isGreyEnabled);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent);
                    } else {
                        startService(intent);
                    }
                    Alerter.create(MainWallpapersActivity.this)
                            .setTextTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_regular))
                            .setTitleTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_bold))
                            .setTextAppearance(R.style.alertBody)
                            .setTitleAppearance(R.style.alertTitle)
                            .setTitle(getString(R.string.download_start_title))
                            .setText(getString(R.string.set_wallpaper_desc))
                            .show();

                }
            }
        });

        applyWallpaperFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // TODO shift this to service too
                if (isMyServiceRunning(SetWallpaperService.class)) {
                    Alerter.create(MainWallpapersActivity.this)
                            .setTextTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_regular))
                            .setTitleTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_bold))
                            .setTextAppearance(R.style.alertBody)
                            .setTitleAppearance(R.style.alertTitle)
                            .setTitle(getString(R.string.something_went_wrong_error))
                            .setText(getString(R.string.set_as_intent_already_downloading_desc))
                            .setBackgroundColorRes(R.color.alert_default_error_background)
                            .setDuration(5000)
                            .show();
                } else if (DownloadWallpaperUtils.checkImageExistOnStorage(MainWallpapersActivity.this, currentPaper.getImageId(), isGreyEnabled)) {
                    DownloadWallpaperUtils.launchSetAsIntent(MainWallpapersActivity.this, currentPaper.getImageId(), isGreyEnabled);
                } else {
//                    long referenceID = DownloadWallpaperUtils.downloadAndSetAsIntent(MainWallpapersActivity.this, Uri.parse(currentPaper.getFullImageUrl()), currentPaper, isGreyEnabled, false, false);
                    Intent intent = new Intent(MainWallpapersActivity.this, SetWallpaperService.class).putExtra(getString(R.string.set_wallpaper_intent_extra), currentPaper).putExtra(getString(R.string.grey_scale_identifier_intent_extra), isGreyEnabled).putExtra(getString(R.string.set_as_flag), true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent);
                    } else {
                        startService(intent);
                    }

                    Alerter.create(MainWallpapersActivity.this)
                            .setTextTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_regular))
                            .setTitleTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_bold))
                            .setTextAppearance(R.style.alertBody)
                            .setTitleAppearance(R.style.alertTitle)
                            .setTitle(getString(R.string.download_start_title))
                            .setText(getString(R.string.set_as_intent_alert_desc))
                            .setBackgroundColorRes(R.color.colorAccent)
                            .setDuration(5000)
                            .show();

                }


                return true;
            }
        });

        // Like Button Listener
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Animate
                YoYo.with(Techniques.Pulse)
                        .duration(500)
                        .playOn(likeButton);

                if (!UtilityMethods.isUserLoggedIn(MainWallpapersActivity.this)) {
                    Alerter.create(MainWallpapersActivity.this)
                            .setTextTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_regular))
                            .setTitleTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_bold))
                            .setTextAppearance(R.style.alertBody)
                            .setTitleAppearance(R.style.alertTitle)
                            .setBackgroundColorRes(R.color.orange_bright).setTitle(getString(R.string.sign_in_to_like_title)).setText(getString(R.string.sign_in_to_like_desc)).show();
                    return;
                }

                if (((wolpepper) MainWallpapersActivity.this.getApplication()).isImageLiked(currentPaper.getImageId())) {
                    likeButton.setImageResource(R.drawable.ic_favorite_border_white_24dp);
                    new LikeUnlikeAsyncTask().execute(currentPaper.getImageId(), DELETE_METHOD);
                } else {
                    likeButton.setImageResource(R.drawable.ic_favorite_white_24dp);
                    new LikeUnlikeAsyncTask().execute(currentPaper.getImageId(), POST_METHOD);
                }
            }
        });

        // Share button Listener
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DownloadWallpaperUtils.checkIfFileIsAlreadyDownloading(currentPaper.getImageId())) {
                    Alerter.create(MainWallpapersActivity.this)
                            .setTextTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_regular))
                            .setTitleTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_bold))
                            .setTitle(getString(R.string.something_went_wrong_error))
                            .setText(getString(R.string.share_image_already_downloading_desc))
                            .setBackgroundColorRes(R.color.alert_default_error_background)
                            .setTextAppearance(R.style.alertBody)
                            .setTitleAppearance(R.style.alertTitle)
                            .setDuration(5000)
                            .show();
                    return;
                }

                if (DownloadWallpaperUtils.checkImageExistOnStorage(MainWallpapersActivity.this, currentPaper.getImageId(), isGreyEnabled)) {
                    DownloadWallpaperUtils.shareImage(MainWallpapersActivity.this, currentPaper.getImageId(), isGreyEnabled, currentPaper.getAuthorName());
                } else {
                    long referenceID = DownloadWallpaperUtils.downloadAndSharePaper(MainWallpapersActivity.this, Uri.parse(currentPaper.getFullImageUrl()), currentPaper, isGreyEnabled, false, false);
                    if (referenceID != -1) {
                        Alerter.create(MainWallpapersActivity.this)
                                .setTextTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_regular))
                                .setTitleTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_bold))
                                .setTitle(getString(R.string.download_start_title))
                                .setText(getString(R.string.share_image_alert_desc))
                                .setTextAppearance(R.style.alertBody)
                                .setTitleAppearance(R.style.alertTitle)
                                .setBackgroundColorRes(R.color.colorAccent)
                                .setDuration(5000)
                                .show();
                    }
                }
            }
        });
    }

    /**
     * Sets up click listeners of views which gets data from json
     */
    private void setViewsClickListener() {

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /**This is the reveal likes, downloads view*/
                Revealator.reveal(theAwesomeView)
                        .from(fab)
                        .withCurvedTranslation()
                        .withChildsAnimation()
                        .withEndAction(new Runnable() {
                            @SuppressLint("RestrictedApi")
                            @Override
                            public void run() {
                                fab.setVisibility(View.GONE);
                            }
                        })
                        .start();
            }
        });


        theWonderfulButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Revealator.unreveal(theAwesomeView)
                        .to(fab)
                        .withCurvedTranslation()
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                //For now nothing is happening here
                            }
                        })
                        .start();
            }
        });


        wallpaperImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // DONE : (JUNAID) This is the wallpaper click, send image to PhotoViewActivity and set it there
                startActivity(new Intent(MainWallpapersActivity.this, PhotoViewActivity.class).putExtra("wallpaper", currentPaper.getFullImageUrl()));
            }
        });

    }


    private String loadImageDataFromLink(String link) {
        try {
            return NetworkUtils.getExtractedJson(MainWallpapersActivity.this, link, currentPaper.getImageId(), false);
        } catch (SocketTimeoutException e) {
            return null;
        } catch (IOException e) {
            return null;
        }

    }

    /**
     * Parse the json data retrieved
     *
     * @param json - the json retrieved
     * @return - return true if successful or else false
     */
    private boolean parseExtraDetails(String json) {
        try {


            JSONObject currentImageJsonObject = new JSONObject(json);
            String userId = currentImageJsonObject.getJSONObject(ConstantValues.IMAGE_USER_DETAILS_OBJECT_KEY).getString(ConstantValues.IMAGE_USER_ID_KEY);
            currentPaper.setAuthorId(userId);

            //Set Current Image Total  Downloads
            String totalDownloads = currentImageJsonObject.getString(ConstantValues.IMAGE_TOTAL_DOWNLOADS_KEY);
            currentPaper.setTotalDownloads(totalDownloads);

            Drawable placeholder = wallpaperImageView.getDrawable();
//            GlideApp.with(MainWallpapersActivity.this).load(currentImageJsonObject.getJSONObject(IMAGE_URLS_KEY).getString(IMAGE_REGULAR_IMAGE_URL_KEY)).thumbnail(Glide.with(this).load()).placeholder(placeholder).transition(new DrawableTransitionOptions().crossFade(2000)).into(wallpaperImageView);

            regularResolutionUrl = currentImageJsonObject.getJSONObject(IMAGE_URLS_KEY).getString(IMAGE_REGULAR_IMAGE_URL_KEY);

            // Set Current Image Total Views
            String totalViews = currentImageJsonObject.getString(ConstantValues.IMAGE_TOTAL_VIEWS_KEY);
            currentPaper.setTotalViews(totalViews);

            String totalLikes = currentImageJsonObject.getString(ConstantValues.IMAGE_TOTAL_LIKES_KEY);
            currentPaper.setTotalLikes(totalLikes);

            // Location Section
            if (currentImageJsonObject.has(ConstantValues.IMAGE_LOCATION_KEY)) {
                JSONObject locationJson = currentImageJsonObject.getJSONObject(ConstantValues.IMAGE_LOCATION_KEY);
                String location = locationJson.getString(ConstantValues.IMAGE_LOCATION_TITLE_KEY);
                currentPaper.setLocation(location);
            }

            // Set Current Image's Resolution
            int width = currentImageJsonObject.getInt(ConstantValues.IMAGE_WIDTH_KEY);
            int height = currentImageJsonObject.getInt(ConstantValues.IMAGE_HEIGHT_KEY);
            currentPaper.setWidth(width);
            currentPaper.setHeight(height);

            // Set Current Image COLOR
            String color = currentImageJsonObject.getString(ConstantValues.IMAGE_COLOR_KEY);
            currentPaper.setColor(color);

            // EXIF Section
            JSONObject exifJson = currentImageJsonObject.getJSONObject(ConstantValues.IMAGE_EXIF_KEY);
            String camera = exifJson.optString(ConstantValues.IMAGE_CAMERA_MAKE_KEY, "N/A") + " " + exifJson.optString(ConstantValues.IMAGE_CAMERA_MODEL_KEY, "N/A");
            String exposure = exifJson.optString(ConstantValues.IMAGE_EXPOSURE_TIME_KEY, "N/A");
            String aperture = exifJson.optString(ConstantValues.IMAGE_APERTURE_KEY, "N/A");
            String focalLength = exifJson.optString(ConstantValues.IMAGE_FOCAL_LENGTH_KEY, "N/A");
            String iso = exifJson.optString(ConstantValues.IMAGE_ISO_KEY, "N/A");


            currentPaper.setCamera(camera);
            currentPaper.setExposureTime(exposure);
            currentPaper.setAperture(aperture);
            currentPaper.setFocalLength(focalLength);
            currentPaper.setIso(iso);


//            // Category Section
//            JSONArray categoryArrayJson = currentImageJsonObject.getJSONArray(ConstantValues.IMAGE_CATEGORY_KEY);
//            String categories = "";
//            for (int i = 0; i < categoryArrayJson.length(); i++) {
//                if (TextUtils.isEmpty(categories)) {
//                    categories = categoryArrayJson.getString(i);
//                } else {
//                    categories = categories + ", " + categoryArrayJson.getString(i);
//                }
//            }

//            boolean likedByUser = currentImageJsonObject.getBoolean(ConstantValues.IMAGE_LIKED_BY_USER_KEY);
////            currentPaper.setLiked(likedByUser); IT will always give false due to html link


            // STORY DETAILS FROM JSON
//            JSONObject storyJsonObject = currentImageJsonObject.getJSONObject(ConstantValues.IMAGE_STORY_KEY);
//            String storyTitle = storyJsonObject.getString(ConstantValues.IMAGE_STORY_TITLE_KEY);
//            String storyDesc = storyJsonObject.getString(ConstantValues.IMAGE_STORY_DESCRIPTION_KEY);
//            currentPaper.setStoryTitle(storyTitle);
//            currentPaper.setStoryDesc(storyDesc);

            // RELATED TAGS FROM JSON   < CONVERT RELATED TAGS STRING TO ARRAYLIST AND CREATE A RECYCLER VIEW >
//            JSONArray relatedTagsJsonArray = currentImageJsonObject.getJSONArray(ConstantValues.IMAGE_RELATED_TAGS_KEY);
//            String relatedTags = "";
//            for (int i = 0; i < relatedTagsJsonArray.length(); i++) {
//                JSONObject relatedTagsObject = relatedTagsJsonArray.getJSONObject(i);
//                if (TextUtils.isEmpty(relatedTags)) {
//                    relatedTags = relatedTagsObject.getString(ConstantValues.IMAGE_RELATED_TAGS_TITLE_KEY);
//                } else {
//                    relatedTags = relatedTags + ", " + relatedTagsObject.getString(ConstantValues.IMAGE_RELATED_TAGS_TITLE_KEY);
//                }
//            }


            /* RELATED COLLECTIONS IS ON HOLD FOR SOME REASON.. WILL ADD IN NEXT UPDATE */

            updateExtraDetailsViews();
            return true;
        } catch (JSONException e) {
//            Log.e("exception", e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Method to update views which displays data retrieved in json from querying or preferences
     */
    private void updateExtraDetailsViews() {
        // UPDATE EXIFS
        UtilityMethods.updateTextView(exifAperture, currentPaper.getAperture());
        UtilityMethods.updateTextView(exifCameraTv, currentPaper.getCamera());
        UtilityMethods.updateTextView(exifColorTv, currentPaper.getColor());
        UtilityMethods.updateTextView(exifExposureTimeTv, currentPaper.getExposureTime());
        UtilityMethods.updateTextView(exifFocalLengthTv, currentPaper.getFocalLength());
        UtilityMethods.updateTextView(exifIsoTv, currentPaper.getIso());
        UtilityMethods.updateTextView(exifLocationTv, currentPaper.getLocation());
        UtilityMethods.updateTextView(exifResolutionTv, currentPaper.getWidth() + " x " + currentPaper.getHeight());
        // UPDATE STORY
        UtilityMethods.updateTextView(storyBodyTv, currentPaper.getStoryDesc());
        UtilityMethods.updateTextView(storyTitleTv, currentPaper.getStoryTitle());
        UtilityMethods.hideEmptyTextViews(storyBodyTv);
        UtilityMethods.hideEmptyTextViews(storyTitleTv);
        // UPDATE STATS
        UtilityMethods.updateTextView(totalDownloadsTv, currentPaper.getTotalDownloads());
        UtilityMethods.updateTextView(totalLikesTv, currentPaper.getTotalLikes());
        UtilityMethods.updateTextView(totalViewsTv, currentPaper.getTotalViews());
        setViewsClickListener();
    }

    /**
     * Displays Connection Error Alert
     */
    private void getAlertError() {
        final Alerter alerter = Alerter.create(MainWallpapersActivity.this)
                .setTextTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_regular))
                .setTitleTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_bold))
                .setTextAppearance(R.style.alertBody)
                .setTitleAppearance(R.style.alertTitle);

        alerter.setTitle(getString(R.string.connection_error))
                .setText(getString(R.string.connection_error_description))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new LoadImageDataAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        Alerter.hide();
                    }
                })
                .setBackgroundColorRes(R.color.alert_default_error_background)
                .enableInfiniteDuration(true)
                .show();
    }

    /**
     * Saves the json of this image to preference for 24 hrs to quickly get it back without loading again
     *
     * @param imageJson - Json data of image
     */
    private void savePaperDetailsToPrefs(String imageJson) {
        SharedPreferences preferences = getSharedPreferences(PreferencesKeys.IMAGE_EXTRA_DETAILS_PREFERENCES_BASE_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(currentPaper.getImageId(), imageJson).apply();
    }

    /**
     * Checks the default format selected by user in settings (raw, jpeg, ask always) and performs accordingly.
     */
    private void checkDefaultFormatAndDownloadImage() {
        if (defaultDownloadFormat.equals(getString(R.string.raw_image_type))) {
            if (DownloadWallpaperUtils.checkIfFileIsAlreadyDownloading(currentPaper.getImageId())) {

                UtilityMethods.fileAlreadyDownloadingAlert(MainWallpapersActivity.this, currentPaper);

            } else if (DownloadWallpaperUtils.checkRawImageExistOnStorage(MainWallpapersActivity.this, currentPaper.getImageId())) {
                UtilityMethods.fileAlreadyExistAlert(MainWallpapersActivity.this, currentPaper);

            } else {
                DownloadWallpaperUtils.downloadPaper(MainWallpapersActivity.this, Uri.parse(currentPaper.getRawImageUrl()), currentPaper, false, false, true);
                UtilityMethods.downloadStartedAlert(MainWallpapersActivity.this, currentPaper);

            }
        } else if (defaultDownloadFormat.equals(getString(R.string.jpeg_image_type))) {

            if (DownloadWallpaperUtils.checkIfFileIsAlreadyDownloading(currentPaper.getImageId())) {

                UtilityMethods.fileAlreadyDownloadingAlert(MainWallpapersActivity.this, currentPaper);

            } else if (DownloadWallpaperUtils.checkImageExistOnStorage(MainWallpapersActivity.this, currentPaper.getImageId(), isGreyEnabled)) {

                UtilityMethods.fileAlreadyExistAlert(MainWallpapersActivity.this, currentPaper);

            } else {
                new LoadImageDownloadApiCallAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                DownloadWallpaperUtils.downloadPaper(MainWallpapersActivity.this, Uri.parse(currentPaper.getFullImageUrl()), currentPaper, isGreyEnabled, false, false);
//                UtilityMethods.downloadStartedAlert(MainWallpapersActivity.this, currentPaper);

            }
        } else {

            final Alerter alerter = Alerter.create(this);
            alerter.setTitle("Format")
                    .setTextTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_regular))
                    .setTitleTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_bold))
                    .setTextAppearance(R.style.alertBody)
                    .setTitleAppearance(R.style.alertTitle)
                    .setText("Select The Image Format To Download")
                    .setPositiveActionText("RAW")
                    .setNegativeActionText("JPEG")
                    .setOnPositiveActionClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (DownloadWallpaperUtils.checkIfFileIsAlreadyDownloading(currentPaper.getImageId())) {

                                alerter.hide();
                                UtilityMethods.fileAlreadyDownloadingAlert(MainWallpapersActivity.this, currentPaper);

                            } else if (DownloadWallpaperUtils.checkRawImageExistOnStorage(MainWallpapersActivity.this, currentPaper.getImageId())) {

                                alerter.hide();
                                UtilityMethods.fileAlreadyExistAlert(MainWallpapersActivity.this, currentPaper);

                            } else {

                                alerter.hide();
                                DownloadWallpaperUtils.downloadPaper(MainWallpapersActivity.this, Uri.parse(currentPaper.getRawImageUrl()), currentPaper, false, false, true);
                                UtilityMethods.downloadStartedAlert(MainWallpapersActivity.this, currentPaper);

                            }

                        }
                    })
                    .setOnNegativeActionClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (DownloadWallpaperUtils.checkIfFileIsAlreadyDownloading(currentPaper.getImageId())) {

                                alerter.hide();
                                UtilityMethods.fileAlreadyDownloadingAlert(MainWallpapersActivity.this, currentPaper);

                            } else if (DownloadWallpaperUtils.checkImageExistOnStorage(MainWallpapersActivity.this, currentPaper.getImageId(), isGreyEnabled)) {

                                alerter.hide();
                                UtilityMethods.fileAlreadyExistAlert(MainWallpapersActivity.this, currentPaper);

                            } else {

                                alerter.hide();
                                new LoadImageDownloadApiCallAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                                DownloadWallpaperUtils.downloadPaper(MainWallpapersActivity.this, Uri.parse(currentPaper.getFullImageUrl()), currentPaper, isGreyEnabled, false, false);
//                                UtilityMethods.downloadStartedAlert(MainWallpapersActivity.this, currentPaper);

                            }
                        }
                    })
                    .setBackgroundColorRes(R.color.colorAccent)
                    .enableInfiniteDuration(true)
                    .show();
//            UtilityMethods.downloadImageAlert(MainWallpapersActivity.this, currentPaper, isGreyEnabled, false);
        }
    }

    /**
     * Is Image already downloading checker
     */
    private void isDownloadingCheck() {
        if (DownloadWallpaperUtils.checkIfFileIsAlreadyDownloading(currentPaper.getImageId())) {
            Alerter.create(this)
                    .setTextTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_regular))
                    .setTitleTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_bold))
                    .setTextAppearance(R.style.alertBody)
                    .setTitleAppearance(R.style.alertTitle)
                    .enableInfiniteDuration(true)
                    .setText(getString(R.string.wolpepper_already_downloading))
                    .show();
            isImageDownloading = true;
        }
    }

    /**
     * load Image Data Async Task
     */
    private class LoadImageDataAsync extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            findViewById(R.id.extra_content_loading_progress_bar).setVisibility(View.VISIBLE);
            findViewById(R.id.image_extra_content_layout).setVisibility(View.GONE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String url = NetworkUtils.buildPhotoDetailUrl(currentPaper.getImageId()).toString();
            return loadImageDataFromLink(url);
        }

        @Override
        protected void onPostExecute(String json) {
            if (isFinishing()) {
                return;
            }
            if (json != null) {
                if (!parseExtraDetails(json)) {
                    getAlertError();
                    findViewById(R.id.extra_content_loading_progress_bar).setVisibility(View.GONE);
                } else {
                    savePaperDetailsToPrefs(json);
                    findViewById(R.id.extra_content_loading_progress_bar).setVisibility(View.GONE);
                    findViewById(R.id.image_extra_content_layout).setVisibility(View.VISIBLE);
                }
            } else {
                getAlertError();
                findViewById(R.id.extra_content_loading_progress_bar).setVisibility(View.GONE);
            }

        }
    }

    public class LoadImageDownloadApiCallAsync extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            YoYo.with(Techniques.ZoomOut).onEnd(new YoYo.AnimatorCallback() {
                @Override
                public void call(Animator animator) {
                    YoYo.with(Techniques.ZoomIn).onStart(new YoYo.AnimatorCallback() {
                        @Override
                        public void call(Animator animator) {
                            findViewById(R.id.download_progress_bar).setVisibility(View.VISIBLE);
                        }
                    }).playOn(findViewById(R.id.download_progress_bar));
                }
            }).playOn(downloadButton);
        }

        @Override
        protected String doInBackground(Void... voids) {
            Uri uri = Uri.parse(currentPaper.getDownloadImageApiCallUrl()).buildUpon()
                    .appendQueryParameter("client_id", BuildConfig.UNSPLASH_APP_ID).build();

            URL url = null;

            try {
                url = new URL(uri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {

                String json = NetworkUtils.getResponseFromHttpUrl(url, MainWallpapersActivity.this);
                return new JSONObject(json).getString(URL_KEY);

            } catch (Exception e) {

                e.printStackTrace();

            }

            return currentPaper.getFullImageUrl();
        }

        @Override
        protected void onPostExecute(String s) {
            DownloadWallpaperUtils.downloadPaper(MainWallpapersActivity.this, Uri.parse(s), currentPaper, isGreyEnabled, false, false);
            UtilityMethods.downloadStartedAlert(MainWallpapersActivity.this, currentPaper);
            YoYo.with(Techniques.ZoomOut).onEnd(new YoYo.AnimatorCallback() {
                @Override
                public void call(Animator animator) {
                    findViewById(R.id.download_progress_bar).setVisibility(View.GONE);
                    YoYo.with(Techniques.ZoomIn).playOn(downloadButton);
                }
            }).playOn(findViewById(R.id.download_progress_bar));
        }
    }

    private class LikeUnlikeAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String imageId = params[0];
            String method = params[1];

            URL url = NetworkUtils.buildLikeUnlikeUrl(imageId, ((wolpepper) getApplicationContext()).getAPP_ID());

            try {

                String responseJson = NetworkUtils.getActionResponse(MainWallpapersActivity.this, url, method);

                if (responseJson != null && responseJson.contains(imageId)) {
                    boolean isLiked = new JSONObject(responseJson).getJSONObject("photo").getBoolean(IMAGE_LIKED_BY_USER_KEY);
                    currentPaper.setLiked(isLiked);
                    ((wolpepper) MainWallpapersActivity.this.getApplication()).updateIdLikeStatusMap(currentPaper.getImageId(), isLiked);
                    return "success";
                } else if (responseJson != null && responseJson.equals(RATE_LIMIT_REMAINING_HEADER_KEY)) {
                    return RATE_LIMIT_REMAINING_HEADER_KEY;
                }

                return null;

            } catch (IOException e) {

                e.printStackTrace();
                return RESPONSE_TIMEOUT_ERROR;

            } catch (JSONException e) {

                e.printStackTrace();
                return RESPONSE_SOMETHING_WENT_WRONG_ERROR;
            }

        }

        @Override
        protected void onPostExecute(String response) {
            if (isFinishing()) {
                return;
            }

            switch (response) {
                case RESPONSE_SOMETHING_WENT_WRONG_ERROR:
                    showSomethingWentWrongAlert();
                    break;
                case RESPONSE_TIMEOUT_ERROR:
                    showTimeOutAlert();
                    break;
                case RATE_LIMIT_REMAINING_HEADER_KEY:
                    UtilityMethods.showRateLimitReachedAlert(MainWallpapersActivity.this);
                    break;
                default:
                    break;
            }

            setLikeButtonImage();
            super.onPostExecute(response);
        }

        private void showTimeOutAlert() {
            if (!isFinishing()) {
                Alerter.create(MainWallpapersActivity.this)
                        .setTextTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setTitle(getString(R.string.connection_error)).setText(getString(R.string.connection_error_description))
                        .setBackgroundColorRes(R.color.alert_default_error_background)
                        .show();
            }
        }

        private void showSomethingWentWrongAlert() {
            if (!isFinishing()) {
                Alerter.create(MainWallpapersActivity.this).setTitle(getString(R.string.something_went_wrong_error))
                        .setTextTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(MainWallpapersActivity.this, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setText(getString(R.string.something_went_wrong_description))
                        .setBackgroundColorRes(R.color.alert_default_error_background)
                        .show();
            }
        }
    }
}
