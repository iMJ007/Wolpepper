package com.eclectik.wolpepper.adapters;

import static com.eclectik.wolpepper.utils.ConstantValues.ErrorKeys.RESPONSE_SOMETHING_WENT_WRONG_ERROR;
import static com.eclectik.wolpepper.utils.ConstantValues.ErrorKeys.RESPONSE_TIMEOUT_ERROR;
import static com.eclectik.wolpepper.utils.ConstantValues.HeaderKeys.RATE_LIMIT_REMAINING_HEADER_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_LIKED_BY_USER_KEY;
import static com.eclectik.wolpepper.utils.ConstantValues.MuzeiReturnValues.IMAGE_ALREADY_EXIST_IN_LIST;
import static com.eclectik.wolpepper.utils.ConstantValues.MuzeiReturnValues.LIST_NAME_ALREADY_EXIST;
import static com.eclectik.wolpepper.utils.ConstantValues.MuzeiReturnValues.MAXIMUM_IMAGE_IN_A_LIST_REACHED;
import static com.eclectik.wolpepper.utils.ConstantValues.MuzeiReturnValues.MAXIMUM_LIST_ALLOWED_REACHED;
import static com.eclectik.wolpepper.utils.ConstantValues.MuzeiReturnValues.MUZEI_LIST_OPERATION_SUCCESSFUL;
import static com.eclectik.wolpepper.utils.ConstantValues.RequestMethodKeys.DELETE_METHOD;
import static com.eclectik.wolpepper.utils.ConstantValues.RequestMethodKeys.POST_METHOD;
import static com.eclectik.wolpepper.utils.ConstantValues.URL_KEY;
import static com.eclectik.wolpepper.utils.UtilityMethods.getAppInstance;
import static com.eclectik.wolpepper.widget.WolpepperAppWidget.WIDGET_WALLPAPER_DETAILS_EXTRA;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.eclectik.wolpepper.BuildConfig;
import com.eclectik.wolpepper.GlideApp;
import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.activities.MainWallpapersActivity;
import com.eclectik.wolpepper.dataStructures.Papers;
import com.eclectik.wolpepper.executors.AppExecutors;
import com.eclectik.wolpepper.listenerInterfaces.RecyclerViewDataUpdateRequester;
import com.eclectik.wolpepper.utils.ConstantValues;
import com.eclectik.wolpepper.utils.DownloadWallpaperUtils;
import com.eclectik.wolpepper.utils.NetworkUtils;
import com.eclectik.wolpepper.utils.UtilityMethods;
import com.eclectik.wolpepper.wolpepper;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.material.card.MaterialCardView;
import com.tapadoo.alerter.Alerter;

import org.json.JSONException;
import org.json.JSONObject;
import org.wysaid.nativePort.CGENativeLibrary;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by mj on 5/6/17.
 **/
@SuppressLint({"SimpleDateFormat", "ApplySharedPref"})
public class NewPapersAdapter extends RecyclerView.Adapter<NewPapersAdapter.NewPapersViewHolder> {

    private ArrayList<Papers> papersDataSet = new ArrayList<>();
    private Activity context;
    private SharedPreferences prefs;
    private TapTargetSequence sequence;
    private RecyclerViewDataUpdateRequester updateRequester;
    private wolpepper wolpepper;
    private static final int ADVIEW_TYPE = 1234;
    private DateFormat fromFormat, toFormat;

    public NewPapersAdapter(RecyclerViewDataUpdateRequester updateRequester, Activity context) {
        this.context = context;
        wolpepper = (wolpepper) context.getApplication();
        this.updateRequester = updateRequester;
        prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        fromFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        fromFormat.setLenient(false);

        toFormat = new SimpleDateFormat("d MMMM, yyyy");
        toFormat.setLenient(false);
    }

    @Override
    public NewPapersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.wallpapers_row, parent, false);
        return new NewPapersViewHolder(view, viewType == ADVIEW_TYPE);
    }


    @Override
    public void onBindViewHolder(final NewPapersViewHolder holder, int position) {
        final Papers papers = papersDataSet.get(position);

        setupTapTarget(holder, holder.getAdapterPosition());

        if (papers.getColor() != null && !TextUtils.isEmpty(papers.getColor())) {
            try {
                holder.rootCard.setCardBackgroundColor(Color.parseColor(papers.getColor()));
            } catch (Exception e) {
                holder.rootCard.setCardBackgroundColor(Color.parseColor("#474747"));
            }
        }

        setUpHiddenLayout(holder);
        holder.nameTv.setText(papers.getAuthorName());
        GlideApp.with(context).asBitmap().load(papers.getDisplayImageUrl()).placeholder(holder.wallpaperImageView.getBackground()).format(DecodeFormat.PREFER_RGB_565).transition(new BitmapTransitionOptions().crossFade()).into(holder.wallpaperImageView);

        if (wolpepper.isImageLiked(papers.getImageId())) {
            holder.likeButtonImageView.setImageResource(R.drawable.ic_favorite_white_24dp);
        } else {
            holder.likeButtonImageView.setImageResource(R.drawable.ic_favorite_border_white_24dp);
        }

        if (papers.getHeight() > papers.getWidth() + 500) {
            holder.imageOrientationImageView.setImageResource(R.drawable.ic_stay_current_portrait_black_24dp);
        } else {
            holder.imageOrientationImageView.setImageResource(R.drawable.ic_stay_current_landscape_black_24dp);
        }

        //Formatting Date
        String formattedDate = "";

        try {
            Date date = fromFormat.parse(papers.getDate());
            formattedDate = toFormat.format(date);
            holder.dateTv.setText(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        setLikeButtonClickListener(holder, position);

        downloadClickListener(holder, papers);

        if (UtilityMethods.isAnyListCreated(context)) { // Run long click only if a list is already created
            setUpAddToMuzeiButtonLongClickListener(holder, papers, formattedDate);
        }

        setUpAddToMuzeiButtonListener(holder, papers, formattedDate);

        // CLICK LISTENER FOR INTENT
        setWallpaperClickListener(holder, papers, formattedDate);

        /* Check if its the last item being binded to the current view and
           if yes trigger the updater to update dataSet with more wallpaper */
        if (position == getItemCount() - 1) {
            // Call to Update requester interface implemented in host activity/fragment
            updateRequester.dataSetUpdateRequested();
        }

    }


    @Override
    public int getItemViewType(int position) {
        if (getAppInstance(context).getAdFreePurchase() != null || getAppInstance(context).isAppTempPremium()) {
            return super.getItemViewType(position);
        }

        if (position % 5 == 0 && position != 0) {
            return ADVIEW_TYPE;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return papersDataSet.size();
    }

    /**
     * Update Datat set Of recycler View
     *
     * @param dataSet
     */
    public void updateDataSet(ArrayList<Papers> dataSet) {
        if (dataSet != null) {
            papersDataSet = dataSet;
        }
    }

    /**
     * Wallpaper Image view Click Listener Setup
     *
     * @param holder
     * @param papers
     * @param finalFormattedDate
     */
    private void setWallpaperClickListener(NewPapersViewHolder holder, final Papers papers, final String finalFormattedDate) {
        holder.wallpaperImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainWallpapersActivity.class);
                intent.putExtra(WIDGET_WALLPAPER_DETAILS_EXTRA, papers);
                intent.putExtra(ConstantValues.CALLING_ACTIVITY_NAME, context.getClass().getSimpleName());
                context.startActivity(intent);
            }
        });

    }

    /**
     * Like Button Click Listener Setup
     *
     * @param holder
     * @param position
     */
    private void setLikeButtonClickListener(final NewPapersViewHolder holder, final int position) {
        holder.likeButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // animate
                YoYo.with(Techniques.Pulse)
                        .duration(500)
                        .playOn(holder.likeButtonImageView);

                if (!UtilityMethods.isUserLoggedIn(context)) {
                    Alerter.create(context)
                            .setTextTypeface(ResourcesCompat.getFont(context, R.font.spacemono_regular))
                            .setTitleTypeface(ResourcesCompat.getFont(context, R.font.spacemono_bold))
                            .setTextAppearance(R.style.alertBody)
                            .setTitleAppearance(R.style.alertTitle)
                            .setBackgroundColorRes(R.color.orange_bright).setTitle(context.getString(R.string.sign_in_to_like_title)).setText(context.getString(R.string.sign_in_to_like_desc)).show();
                    return;
                }

                // work further
                String imageId = papersDataSet.get(position).getImageId();
                if (wolpepper.isImageLiked(imageId)) { //run unlike code    /* get liked status from app's context from hash map
                    new likeUnlikeAsyncTask().execute(imageId, DELETE_METHOD, String.valueOf(position));
                    holder.likeButtonImageView.setImageResource(R.drawable.ic_favorite_border_white_24dp);
                } else { // run like code
                    new likeUnlikeAsyncTask().execute(imageId, POST_METHOD, String.valueOf(position));
                    holder.likeButtonImageView.setImageResource(R.drawable.ic_favorite_white_24dp);
                }
            }
        });
    }

    private void downloadClickListener(final NewPapersViewHolder holder, final Papers currentPaper) {
        holder.downloadImageView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                if (DownloadWallpaperUtils.checkIfFileIsAlreadyDownloading(currentPaper.getImageId())) {

                    UtilityMethods.fileAlreadyDownloadingAlert(context, currentPaper);

                } else if (DownloadWallpaperUtils.checkImageExistOnStorage(context, currentPaper.getImageId(), false)) {

                    UtilityMethods.fileAlreadyExistAlert(context, currentPaper);

                } else {
                    YoYo.with(Techniques.ZoomOut).onEnd(new YoYo.AnimatorCallback() {
                        @Override
                        public void call(Animator animator) {
                            YoYo.with(Techniques.ZoomIn).onStart(new YoYo.AnimatorCallback() {
                                @Override
                                public void call(Animator animator) {
                                    holder.itemView.findViewById(R.id.download_progress_bar).setVisibility(View.VISIBLE);
                                }
                            }).playOn(holder.itemView.findViewById(R.id.download_progress_bar));
                        }
                    }).playOn(holder.downloadImageView);

                    AppExecutors.getInstance().networkIO().execute(() -> {
                        Uri uri = Uri.parse(currentPaper.getDownloadImageApiCallUrl()).buildUpon()
                                .appendQueryParameter("client_id", BuildConfig.UNSPLASH_APP_ID).build();

                        URL url = null;

                        try {
                            url = new URL(uri.toString());
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                        String result = currentPaper.getFullImageUrl();
                        try {

                            String json = NetworkUtils.getResponseFromHttpUrl(url, context);
                            result = new JSONObject(json).getString(URL_KEY);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        String finalResult = result;
                        AppExecutors.getInstance().mainThread().execute(() -> {
                            DownloadWallpaperUtils.downloadPaper(context, Uri.parse(finalResult), currentPaper, false, false, false);
                            UtilityMethods.downloadStartedAlert(context, currentPaper);
                            YoYo.with(Techniques.ZoomOut).onEnd(new YoYo.AnimatorCallback() {
                                @Override
                                public void call(Animator animator) {
                                    holder.itemView.findViewById(R.id.download_progress_bar).setVisibility(View.GONE);
                                    YoYo.with(Techniques.ZoomIn).playOn(holder.downloadImageView);
                                }
                            }).playOn(holder.itemView.findViewById(R.id.download_progress_bar));
                        });
                    });

                }
            }
        });
    }

    /**
     * Setting Up hidden layout and its click listener
     *
     * @param holder
     */
    private void setUpHiddenLayout(final NewPapersViewHolder holder) {
        holder.detailLayout.setVisibility(View.GONE);

        holder.infoButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.detailLayout.getVisibility() == View.GONE) {
                    if (!prefs.getBoolean("firstTimeInternalMuzeiControlView", false)) {
                        TapTargetView.showFor(context,                 // `this` is an Activity
                                TapTarget.forView(holder.addToMuzeiButtonImageView, context.getString(R.string.muzei_button_tap_target_title), context.getString(R.string.muzei_button_tap_target_desc))
                                        // All options below are optional
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
                                        .targetRadius(60));
                        prefs.edit().putBoolean("firstTimeInternalMuzeiControlView", true).commit();
                    }

                    AppExecutors.getInstance().networkIO().execute(() -> {

                        try {
                            Bitmap bitmap = CGENativeLibrary.filterImage_MultipleEffects(GlideApp.with(context).asBitmap().load(papersDataSet.get(holder.getAdapterPosition()).getDisplayImageUrl()).format(DecodeFormat.PREFER_ARGB_8888).submit().get(), "#unpack @blur lerp 1", 0.70f);
                            AppExecutors.getInstance().mainThread().execute(() -> {
                                GlideApp.with(context).asBitmap().load(bitmap).placeholder(holder.wallpaperImageView.getDrawable().getCurrent()).format(DecodeFormat.PREFER_RGB_565).transition(new BitmapTransitionOptions().crossFade(250)).into(holder.wallpaperImageView);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    YoYo.with(Techniques.FadeIn)
                            .duration(250)
                            .onStart(new YoYo.AnimatorCallback() {
                                @Override
                                public void call(Animator animator) {
                                    holder.detailLayout.setVisibility(View.VISIBLE);
                                }
                            })
                            .playOn(holder.detailLayout);
                } else {
                    GlideApp.with(context).load(papersDataSet.get(holder.getAdapterPosition()).getDisplayImageUrl()).placeholder(holder.wallpaperImageView.getDrawable().getCurrent()).format(DecodeFormat.PREFER_RGB_565).transition(new DrawableTransitionOptions().crossFade(350)).into(holder.wallpaperImageView);
                    YoYo.with(Techniques.FadeOut)
                            .duration(250)
                            .onEnd(new YoYo.AnimatorCallback() {
                                @Override
                                public void call(Animator animator) {
                                    holder.detailLayout.setVisibility(View.GONE);
                                }
                            })
                            .playOn(holder.detailLayout);
                }
            }
        });
    }

    /**
     * Add to current Image to active muzei list if list already created else popup new list dialog
     *
     * @param holder -
     * @param paper -
     * @param formattedDate -
     */
    private void setUpAddToMuzeiButtonListener(final NewPapersViewHolder holder, final Papers paper, final String formattedDate) {
        if (paper.isInMuzeiList()) {
            holder.addToMuzeiButtonImageView.setImageResource(R.drawable.ic_action_remove_from_muzei);  // IT IS IN MUZEI SO SHOW ICON TO REMOVE IT FROM MUZEI LIST
        } else {
            holder.addToMuzeiButtonImageView.setImageResource(R.drawable.ic_action_add_to_muzei); // IT IS NOT IN MUZEI LIST SO SHOW ADD TO MUZEI ICON
        }

        holder.addToMuzeiButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UtilityMethods.isAnyListCreated(context)) {
                    showCreateFirstMuzeiListDialog(paper, formattedDate);
                } else {
                    if (paper.isInMuzeiList()) {
                        if (removeFromActiveListOfMuzei(context, paper)) {
                            YoYo.with(Techniques.FadeIn)
                                    .onStart(new YoYo.AnimatorCallback() {
                                        @Override
                                        public void call(Animator animator) {
                                            holder.addToMuzeiButtonImageView.setImageResource(R.drawable.ic_action_add_to_muzei); // IT IS removed from MUZEI LIST SO SHOW ADD TO MUZEI ICON
                                        }
                                    }).playOn(holder.addToMuzeiButtonImageView);
                        }

                    } else {
                        if (addToActiveListOfMuzei(context, paper, formattedDate)) {
                            YoYo.with(Techniques.FadeIn)
                                    .onStart(new YoYo.AnimatorCallback() {
                                        @Override
                                        public void call(Animator animator) {
                                            holder.addToMuzeiButtonImageView.setImageResource(R.drawable.ic_action_remove_from_muzei);  // IT IS ADDED TO MUZEI SO SHOW ICON TO REMOVE IT FROM MUZEI LIST
                                        }
                                    }).playOn(holder.addToMuzeiButtonImageView);
                        }

                    }
                }
            }
        });

    }

    /**
     * Long click listener for add to Muzei
     *
     * @param holder -
     * @param paper -
     * @param formattedDate -
     */
    private void setUpAddToMuzeiButtonLongClickListener(final NewPapersViewHolder holder, final Papers paper, final String formattedDate) {
        holder.addToMuzeiButtonImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ArrayList<String> muzeiLists = UtilityMethods.getAllMuzeiListNames(context);
                // TODO : Create a recycle adapter and make a better UI
                new MaterialDialog.Builder(context)
                        .typeface(ResourcesCompat.getFont(context, R.font.spacemono_bold), ResourcesCompat.getFont(context, R.font.spacemono_regular))
                        .title(context.getString(R.string.muzei_select_list_to_add_wallpaper_to))
                        .items(muzeiLists)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {

                                String activeListName = UtilityMethods.getActiveMuzeiListName(context);

                                boolean isSuccess = addToSelectedMuzeiList(context, text.toString(), paper, formattedDate);

                                if (text.toString().equals(activeListName) && isSuccess) {
                                    paper.setInMuzeiList(true);
                                    YoYo.with(Techniques.FadeIn)
                                            .onStart(new YoYo.AnimatorCallback() {
                                                @Override
                                                public void call(Animator animator) {
                                                    holder.addToMuzeiButtonImageView.setImageResource(R.drawable.ic_action_remove_from_muzei);
                                                }
                                            })
                                            .playOn(holder.addToMuzeiButtonImageView);
                                }
                            }
                        })
                        .positiveText(context.getString(R.string.create_new_list))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                showCreateNewMuzeiListDialog(paper, formattedDate);
                            }
                        }).show();
                return true;
            }
        });
    }

    /**
     * Remove selected Wallpaper from muzei List
     *
     * @param context -
     * @param paper -
     * @return - true If transaction is successful
     */
    private boolean removeFromActiveListOfMuzei(Context context, Papers paper) {
        switch (UtilityMethods.removeImageFromActiveMuzeiList(context, paper.getImageId())) {
            case MUZEI_LIST_OPERATION_SUCCESSFUL:
                Alerter.create((Activity) context)
                        .setTextTypeface(ResourcesCompat.getFont(context, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(context, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setText(context.getString(R.string.muzei_image_removed_from_list))
                        .setBackgroundColorRes(R.color.colorAccent)
                        .show();
                paper.setInMuzeiList(false);
                return true;
            default:
                Alerter.create((Activity) context)
                        .setTextTypeface(ResourcesCompat.getFont(context, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(context, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setTitle(context.getString(R.string.something_went_wrong_error))
                        .setText(context.getString(R.string.something_went_wrong_description))
                        .setBackgroundColorRes(R.color.alert_default_error_background)
                        .setDuration(3000)
                        .show();
                return false;
        }
    }

    /**
     * Add selected wallpaper to active Muzei List.
     *
     * @param context -
     * @param paper -
     * @param formattedDate -
     * @return - True if transaction is successful
     */
    private boolean addToActiveListOfMuzei(Context context, Papers paper, String formattedDate) {
        switch (UtilityMethods.addImageToActiveMuzeiList(context, paper.getImageId(), formattedDate, paper.getAuthorName(), paper.getFullImageUrl())) {
            case MAXIMUM_IMAGE_IN_A_LIST_REACHED:
                Alerter.create((Activity) context)
                        .setTextTypeface(ResourcesCompat.getFont(context, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(context, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setTitle(context.getString(R.string.maximum_image_in_list_reached_title))
                        .setText(context.getString(R.string.maximum_image_in_list_reached_desc))
                        .setBackgroundColorRes(R.color.alert_default_error_background)
                        .setDuration(10000)
                        .show();
                return false;
            case IMAGE_ALREADY_EXIST_IN_LIST:
                Alerter.create((Activity) context)
                        .setTextTypeface(ResourcesCompat.getFont(context, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(context, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setText(context.getString(R.string.muzei_image_already_exist_in_list_desc))
                        .setBackgroundColorRes(R.color.orange_bright)
                        .setDuration(1500)
                        .show();
                return false;
            default:
                Alerter.create((Activity) context)
                        .setTextTypeface(ResourcesCompat.getFont(context, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(context, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setText(context.getString(R.string.muzei_image_added_to_active_list))
                        .setBackgroundColorRes(R.color.colorAccent)
                        .show();
                paper.setInMuzeiList(true);
                return true;
        }
    }

    /**
     * Add image to selected muzei list
     *
     * @param context -
     * @param listName -
     * @param paper -
     * @param formattedDate -
     * @return -
     */
    private boolean addToSelectedMuzeiList(Context context, String listName, Papers paper, String formattedDate) {
        switch (UtilityMethods.addImageToSelectedMuzeiList(context, listName, paper.getImageId(), formattedDate, paper.getAuthorName(), paper.getFullImageUrl())) {
            case MAXIMUM_IMAGE_IN_A_LIST_REACHED:
                Alerter.create((Activity) context)
                        .setTextTypeface(ResourcesCompat.getFont(context, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(context, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setTitle(context.getString(R.string.maximum_image_in_list_reached_title))
                        .setText(context.getString(R.string.maximum_image_in_list_reached_desc))
                        .setBackgroundColorRes(R.color.alert_default_error_background)
                        .setDuration(10000)
                        .show();
                return false;
            case IMAGE_ALREADY_EXIST_IN_LIST:
                Alerter.create((Activity) context)
                        .setTextTypeface(ResourcesCompat.getFont(context, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(context, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setText(context.getString(R.string.muzei_image_already_exist_in_list_desc))
                        .setBackgroundColorRes(R.color.orange_bright)
                        .setDuration(1500)
                        .show();
                return false;
            default:
                Alerter.create((Activity) context)
                        .setTextTypeface(ResourcesCompat.getFont(context, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(context, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setText(context.getString(R.string.muzei_image_added_to_selected_list))
                        .setBackgroundColorRes(R.color.colorAccent)
                        .show();
                paper.setInMuzeiList(true);
                return true;
        }
    }

    /**
     * Show dialog if no list is created yet
     *
     * @param paper -
     * @param formattedDate -
     */
    private void showCreateFirstMuzeiListDialog(final Papers paper, final String formattedDate) {
        new MaterialDialog.Builder(context)
                .typeface(ResourcesCompat.getFont(context, R.font.spacemono_bold), ResourcesCompat.getFont(context, R.font.spacemono_regular))
                .title(context.getString(R.string.muzei_create_list_title))
                .content(context.getString(R.string.muzei_create_first_list_desc))
                .inputRange(3, 25, ContextCompat.getColor(context, R.color.alert_default_error_background))
                .inputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .input(context.getString(R.string.list_name_hint), null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        UtilityMethods.createNewMuzeiList(context, input.toString(), paper.getImageId(), formattedDate, paper.getAuthorName(), paper.getFullImageUrl(), true);
                        UtilityMethods.addImageToActiveMuzeiList(context, paper.getImageId(), formattedDate, paper.getAuthorName(), paper.getFullImageUrl());

                        Alerter alerter = Alerter.create(context);
                        alerter
                                .setTextTypeface(ResourcesCompat.getFont(context, R.font.spacemono_regular))
                                .setTitleTypeface(ResourcesCompat.getFont(context, R.font.spacemono_bold))
                                .setTextAppearance(R.style.alertBody)
                                .setTitleAppearance(R.style.alertTitle)
                                .setTitle(input.toString() + context.getString(R.string.list_created_toast_text))
                                .setText(context.getString(R.string.muzei_select_as_source_note))
                                .setBackgroundColorRes(R.color.colorAccent)
                                .setOnClickListener(startMuzeiApp(alerter))
                                .enableInfiniteDuration(true)
                                .show();

                        paper.setInMuzeiList(true);
                        notifyDataSetChanged();
                    }
                })
                .dividerColor(ContextCompat.getColor(context, R.color.cardview_dark_background))
                .negativeText("Cancel")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private View.OnClickListener startMuzeiApp(final Alerter alerter) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = context.getPackageManager().getLaunchIntentForPackage("net.nurik.roman.muzei");
                if (intent == null) {
                    // Bring user to the market or let them choose an app?
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=" + "net.nurik.roman.muzei"));
                    // check if play store app is installed or not
                    if (intent.resolveActivity(context.getPackageManager()) == null) {
                        intent.setData(Uri.parse("https://play.google.com/store/apps/" + "net.nurik.roman.muzei"));
                    }
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                Alerter.hide();
            }
        };
    }

    /**
     * Show Alert is the given name already exist
     */
    private void getMuzeiNameAlreadyExistAlert() {
        Alerter.create(context)
                .setTextTypeface(ResourcesCompat.getFont(context, R.font.spacemono_regular))
                .setTitleTypeface(ResourcesCompat.getFont(context, R.font.spacemono_bold))
                .setTextAppearance(R.style.alertBody)
                .setTitleAppearance(R.style.alertTitle)
                .setTitle(context.getString(R.string.muzei_name_already_exist_title))
                .setText(context.getString(R.string.muzei_name_already_exist_desc))
                .setBackgroundColorRes(R.color.alert_default_error_background)
                .setDuration(5000)
                .show();
    }

    /**
     * Show the dialog to create and name a new Muzei list
     *
     * @param paper
     * @param formattedDate
     */
    private void showCreateNewMuzeiListDialog(final Papers paper, final String formattedDate) {
        new MaterialDialog.Builder(context)
                .title(context.getString(R.string.muzei_create_list_title))
                .content(context.getString(R.string.muzei_create_list_desc))
                .typeface(ResourcesCompat.getFont(context, R.font.spacemono_bold), ResourcesCompat.getFont(context, R.font.spacemono_regular))
                .inputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .inputRange(3, 25, ContextCompat.getColor(context, R.color.alert_default_error_background))
                .input(context.getString(R.string.list_name_hint), null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        switch (UtilityMethods.createNewMuzeiList(context, input.toString(), paper.getImageId(), formattedDate, paper.getAuthorName(), paper.getFullImageUrl(), false)) {
                            case MAXIMUM_LIST_ALLOWED_REACHED:
                                Alerter.create(context)
                                        .setTextTypeface(ResourcesCompat.getFont(context, R.font.spacemono_regular))
                                        .setTitleTypeface(ResourcesCompat.getFont(context, R.font.spacemono_bold))
                                        .setTextAppearance(R.style.alertBody)
                                        .setTitleAppearance(R.style.alertTitle)
                                        .setTitle(context.getString(R.string.maximum_image_in_list_reached_title))
                                        .setText(context.getString(R.string.maximum_list_reached_desc))
                                        .setBackgroundColorRes(R.color.alert_default_error_background)
                                        .show();
                                break;
                            case LIST_NAME_ALREADY_EXIST:
                                getMuzeiNameAlreadyExistAlert();
                                break;
                            default:
                                addToSelectedMuzeiList(context, input.toString(), paper, formattedDate);
                                Alerter.create(context)
                                        .setTextTypeface(ResourcesCompat.getFont(context, R.font.spacemono_regular))
                                        .setTitleTypeface(ResourcesCompat.getFont(context, R.font.spacemono_bold))
                                        .setTextAppearance(R.style.alertBody)
                                        .setTitleAppearance(R.style.alertTitle)
                                        .setText(input.toString() + context.getString(R.string.list_created_toast_text)).setBackgroundColorRes(R.color.colorAccent).show();
                                break;
                        }
                    }
                }).show();
    }

    /**
     * View Holder Class
     */
    public static class NewPapersViewHolder extends RecyclerView.ViewHolder {

        public View mView;
        public ImageView wallpaperImageView, likeButtonImageView, addToMuzeiButtonImageView, downloadImageView, infoButtonImageView, imageOrientationImageView;
        public TextView dateTv, nameTv;
        public MaterialCardView rootCard;
        public RelativeLayout detailLayout;
        public RelativeLayout rootSizeLayout;
        private AdView adView;
        private Context context;

        public NewPapersViewHolder(View itemView, boolean isAdViewType) {
            super(itemView);
            mView = itemView;
            context = itemView.getContext();
            wallpaperImageView = mView.findViewById(R.id.wallpaper);
            nameTv = mView.findViewById(R.id.author_name);
            dateTv = mView.findViewById(R.id.date);
            rootCard = mView.findViewById(R.id.wallpaper_row_root_card);
            likeButtonImageView = mView.findViewById(R.id.like_button);
            detailLayout = mView.findViewById(R.id.hidden_layout);
            addToMuzeiButtonImageView = mView.findViewById(R.id.add_to_muzei_button);
            downloadImageView = mView.findViewById(R.id.download_button);
            infoButtonImageView = mView.findViewById(R.id.hide_reveal_button);
            imageOrientationImageView = mView.findViewById(R.id.image_orientation_view);
            rootSizeLayout = mView.findViewById(R.id.root_size_layout);


            if (isAdViewType && PreferenceManager.getDefaultSharedPreferences(context).getLong("adClickedTime", -1) < System.currentTimeMillis()) {
                adView = new AdView(itemView.getContext());

                adView.setAdUnitId(itemView.getContext().getString(R.string.main_list_unit_id));
                ((LinearLayout) itemView).addView(adView);
                loadBanner(adView);
                adView.setAdListener(new AdListener() {
                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong("adClickedTime", System.currentTimeMillis() + TimeUnit.HOURS.toMillis(6)).commit();
                    }
                });
            }
        }

        private void loadBanner(AdView adView) {
            // Create an ad request. Check your logcat output for the hashed device ID
            // to get test ads on a physical device, e.g.,
            // "Use AdRequest.Builder.addTestDevice("ABCDE0123") to get test ads on this
            // device."
            AdRequest adRequest =
                    new AdRequest.Builder()
                            .build();

            AdSize adSize = getAdSize();
            // Step 4 - Set the adaptive ad size on the ad view.
            adView.setAdSize(adSize);

            // Step 5 - Start loading the ad in the background.
            adView.loadAd(adRequest);
        }

        private AdSize getAdSize() {
            // Step 2 - Determine the screen width (less decorations) to use for the ad width.
            if (!(context instanceof Activity)) {
                return null;
            }

            Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);

            float widthPixels = outMetrics.widthPixels - 50;
            float density = outMetrics.density;
//
            int adWidth = (int) (widthPixels / density);

            // Step 3 - Get adaptive ad size and return for setting on the ad view.
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
        }
    }

    /**
     * Like Unlike Async Task
     */
    private class likeUnlikeAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String imageId = params[0];
            String method = params[1];
            int position = Integer.parseInt(params[2]);

            URL url = NetworkUtils.buildLikeUnlikeUrl(imageId, wolpepper.getAPP_ID());

            try {

                String responseJson = NetworkUtils.getActionResponse(context, url, method);

                if (responseJson != null && responseJson.contains(imageId)) {
                    boolean isLiked = new JSONObject(responseJson).getJSONObject("photo").getBoolean(IMAGE_LIKED_BY_USER_KEY);
                    papersDataSet.get(position).setLiked(isLiked);
                    wolpepper.updateIdLikeStatusMap(imageId, isLiked);
                    return imageId;
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
            switch (response) {
                case RESPONSE_SOMETHING_WENT_WRONG_ERROR:
                    showSomethingWentWrongAlert();
                    break;
                case RESPONSE_TIMEOUT_ERROR:
                    showTimeOutAlert();
                    break;
                case RATE_LIMIT_REMAINING_HEADER_KEY:
                    UtilityMethods.showRateLimitReachedAlert(context);
                    break;
                default:
                    break;
            }
            notifyDataSetChanged();
            super.onPostExecute(response);
        }

        private void showTimeOutAlert() {
            Alerter.create(context)
                    .setTextTypeface(ResourcesCompat.getFont(context, R.font.spacemono_regular))
                    .setTitleTypeface(ResourcesCompat.getFont(context, R.font.spacemono_bold))
                    .setTextAppearance(R.style.alertBody)
                    .setTitleAppearance(R.style.alertTitle)
                    .setTitle(R.string.connection_error).setText(R.string.connection_error_description).setBackgroundColorRes(R.color.alert_default_error_background).show();
        }

        private void showSomethingWentWrongAlert() {
            Alerter.create(context)
                    .setTextTypeface(ResourcesCompat.getFont(context, R.font.spacemono_regular))
                    .setTitleTypeface(ResourcesCompat.getFont(context, R.font.spacemono_bold))
                    .setTextAppearance(R.style.alertBody)
                    .setTitleAppearance(R.style.alertTitle)
                    .setTitle(R.string.something_went_wrong_error).setText(R.string.something_went_wrong_description).setBackgroundColorRes(R.color.alert_default_error_background).show();
        }
    }

    /**
     * Update If active list Changed
     */
    public void updateMuzeiStatusOfWholeDataSet() {
        if (papersDataSet != null) {
            for (int i = 0; i < papersDataSet.size(); i++) {
                papersDataSet.get(i).setInMuzeiList(UtilityMethods.isInMuzeiActiveList(context, papersDataSet.get(i).getImageId()));
            }
            notifyDataSetChanged();
        }
    }

    private void setupTapTarget(NewPapersViewHolder holder, int position) {
        if (!prefs.getBoolean("firstTime", false)) {
            if (position == 0) {
                sequence = new TapTargetSequence(context)
                        .targets(
                                TapTarget.forView(holder.imageOrientationImageView, context.getString(R.string.orientation_image_tap_target_title), context.getString(R.string.orientation_image_tap_target_desc))
                                        .outerCircleColor(R.color.colorPrimary)      // Specify a color for the outer circle
                                        .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                                        .targetCircleColor(R.color.white)   // Specify a color for the target circle
                                        .titleTextSize(22)                  // Specify the size (in sp) of the title text
                                        .titleTextColor(R.color.white)      // Specify the color of the title text
                                        .descriptionTextSize(18)            // Specify the size (in sp) of the description text
                                        .descriptionTextColor(R.color.colorIconTint)  // Specify the color of the description text
                                        .textColor(R.color.white)            // Specify a color for both the title and description text
                                        .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                        .dimColor(R.color.colorPrimary)            // If set, will dim behind the view with 30% opacity of the given color
                                        .drawShadow(true)                   // Whether to draw a drop shadow or not
                                        .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                        .tintTarget(true)                   // Whether to tint the target view's color
                                        .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                                        .targetRadius(60),
                                TapTarget.forView(holder.infoButtonImageView, context.getString(R.string.info_button_tap_target_title), context.getString(R.string.info_button_tap_target_desc))
                                        .outerCircleColor(R.color.colorPrimary)      // Specify a color for the outer circle
                                        .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                                        .targetCircleColor(R.color.white)   // Specify a color for the target circle
                                        .titleTextSize(22)                  // Specify the size (in sp) of the title text
                                        .titleTextColor(R.color.white)      // Specify the color of the title text
                                        .descriptionTextSize(18)            // Specify the size (in sp) of the description text
                                        .descriptionTextColor(R.color.colorIconTint)  // Specify the color of the description text
                                        .textColor(R.color.white)            // Specify a color for both the title and description text
                                        .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                        .dimColor(R.color.colorPrimary)            // If set, will dim behind the view with 30% opacity of the given color
                                        .drawShadow(true)                   // Whether to draw a drop shadow or not
                                        .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                        .tintTarget(true)                   // Whether to tint the target view's color
                                        .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                                        .targetRadius(60)        // Specify the target radius (in dp)
                        ).listener(new TapTargetSequence.Listener() {
                            @Override
                            public void onSequenceFinish() {
                            }

                            @Override
                            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                            }

                            @Override
                            public void onSequenceCanceled(TapTarget lastTarget) {

                            }
                        });
                sequence.start();
            }
            prefs.edit().putBoolean("firstTime", true).commit();
        }
    }
}
