package com.eclectik.wolpepper.activities;

import static com.eclectik.wolpepper.utils.ConstantValues.SOLID_COLORS_STORAGE_FOLDER_NAME;
import static com.eclectik.wolpepper.utils.ConstantValues.SOLID_COLORS_STORAGE_FULL_PATH;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.utils.UtilityMethods;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.joaquimley.faboptions.FabOptions;
import com.tapadoo.alerter.Alerter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class GradientPaperActivity extends AppCompatActivity {

    private ArrayList<Integer> colorArray = new ArrayList<>();

    private GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[0]);

    private ImageView gradientCentreLocator;

    private ImageView rootLayout;

    private FabOptions fabOptions;

    private int previouslySavedRadius = 500;

    private int maxNumber = 3;

    private DisplayMetrics displayMetrics = new DisplayMetrics();

    private boolean isDarkColorsAllowed = false;

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gradient_paper);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(GradientPaperActivity.this, R.color.statusBar));
        }

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        initializeViews();

        generateInitialRandomColorArray();

        updateGradientColors(colorArray, drawable);
        setBackgroundToInitialType();
        rootLayout.setBackground(drawable);
        setFabClickListener(fabOptions);
    }

    private void setBackgroundToInitialType() {
        drawable.setGradientType(GradientDrawable.SWEEP_GRADIENT);
        drawable.setGradientCenter(0.5f, 1f);
        drawable.setGradientRadius(2000);
        drawable.setDither(true);
    }


    private void generateInitialRandomColorArray() {
        Random r = new Random();
        colorArray.clear();
        for (int i = 0; i <= maxNumber; i++) {
            if (isDarkColorsAllowed) {
                colorArray.add(Color.argb(r.nextInt(255), r.nextInt(256), r.nextInt(256), r.nextInt(256)));
            } else {
                colorArray.add(Color.argb(255, r.nextInt(156) + 100, r.nextInt(156) + 100, r.nextInt(156) + 100));
            }
        }
    }

    private void generateColorArray() {
        Random r = new Random();
        colorArray.clear();
        for (int i = 0; i < maxNumber; i++) {
            if (isDarkColorsAllowed) {
                colorArray.add(Color.argb(r.nextInt(255), r.nextInt(256), r.nextInt(256), r.nextInt(256)));
            } else {
                colorArray.add(Color.argb(255, r.nextInt(156) + 100, r.nextInt(156) + 100, r.nextInt(156) + 100));
            }
        }
    }

    private void initializeViews() {
        rootLayout = findViewById(R.id.root_background);
        fabOptions = findViewById(R.id.gradient_fab_options);
        gradientCentreLocator = findViewById(R.id.gradient_center_locator);
        initAds();
        setUpCenterLocatorTouchListener();
    }

    int adRetryCount = 0;
    private void initAds(){

        if (UtilityMethods.getAppInstance(this).getAdFreePurchase() == null && !UtilityMethods.getAppInstance(this).isAppTempPremium()) {
            InterstitialAd.load(this, getString(R.string.solid_interstitial_unit_id), new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    mInterstitialAd = interstitialAd;
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdShowedFullScreenContent() {
                            // Called when fullscreen content is shown.
                            // Make sure to set your reference to null so you don't
                            // show it a second time.
                            mInterstitialAd = null;
                            initAds();
                        }
                    });
                    adRetryCount = 0;
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    // Handle the error
                    mInterstitialAd = null;
                    if (adRetryCount < 5){
                        adRetryCount++;
                        initAds();
                    }
                }

            });


        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpCenterLocatorTouchListener() {
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gradientCentreLocator.setX(event.getX() - (gradientCentreLocator.getWidth() / 2));
                gradientCentreLocator.setY(event.getY() - (gradientCentreLocator.getHeight() / 2));
                drawable.setGradientCenter(event.getX() / rootLayout.getWidth(), event.getY() / rootLayout.getHeight());
                rootLayout.setBackground(drawable);
                return true;
            }
        });
    }

    private static void updateGradientColors(ArrayList<Integer> colorList, GradientDrawable drawable) {
        int[] colors = new int[colorList.size()];
        for (int i = 0; i < colorList.size(); i++) {
            colors[i] = colorList.get(i);
        }
        drawable.setColors(colors);
    }

    public void viewsOnClick(View view) {
        switch (view.getId()) {
            case R.id.apply_gradient_paper:
                break;
            case R.id.generate_gradient_color:
                generateColorArray();
                updateGradientColors(colorArray, drawable);
        }
    }

    private void setFabClickListener(final FabOptions fabOptions) {
        fabOptions.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (fabOptions.isOpen()) {
                    findViewById(R.id.apply_gradient_paper).animate().translationX(-fabOptions.getWidth()).start();
                    findViewById(R.id.generate_gradient_color).animate().translationX(fabOptions.getWidth()).start();
                } else {
                    findViewById(R.id.apply_gradient_paper).animate().translationX(0).start();
                    findViewById(R.id.generate_gradient_color).animate().translationX(0).start();
                }

            }
        });

        fabOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.fab_option_gradient_type:
                        showGradientTypeDialog();
                        break;
                    case R.id.fab_option_gradient_layer_count:
                        showColorCountDialog();
                        break;
                    case R.id.share_gradient_paper:
                        shareImage(getGradientBitmap());
                        break;
                    case R.id.save_gradient_paper:
                        saveImage(getGradientBitmap());
                        break;

                }
            }
        });

        findViewById(R.id.apply_gradient_paper).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WallpaperManager wpm = WallpaperManager.getInstance(GradientPaperActivity.this);
                try {
                    wpm.setBitmap(getGradientBitmap());
                    Alerter.create(GradientPaperActivity.this)
                            .setTextTypeface(ResourcesCompat.getFont(GradientPaperActivity.this, R.font.spacemono_regular))
                            .setTitleTypeface(ResourcesCompat.getFont(GradientPaperActivity.this, R.font.spacemono_bold))
                            .setTextAppearance(R.style.alertBody)
                            .setTitleAppearance(R.style.alertTitle)
                            .setTitle(getString(R.string.solid_color_save_title)) // Title and title color
                            .setText(getString(R.string.solid_color_set_desc)) // Message and message color
                            .setBackgroundColorRes(R.color.colorAccent)
                            .show();

                    if (UtilityMethods.getAppInstance(GradientPaperActivity.this).getAdFreePurchase() == null && !UtilityMethods.getAppInstance(GradientPaperActivity.this).isAppTempPremium() && mInterstitialAd != null) {
                        mInterstitialAd.show(GradientPaperActivity.this);
                    }
                } catch (IOException e) {
                    Toast.makeText(GradientPaperActivity.this, "Error Setting Wallpaper!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveImage(Bitmap finalBitmap) {
        File wallpaperDirectory;

        if (UtilityMethods.isDefaultStorage(GradientPaperActivity.this)) {
            wallpaperDirectory = new File(SOLID_COLORS_STORAGE_FULL_PATH);
            if (!wallpaperDirectory.exists()) {
                wallpaperDirectory.mkdirs();
            }
        } else {
            wallpaperDirectory = new File(UtilityMethods.getExternalStoragePath(GradientPaperActivity.this) + "/" + SOLID_COLORS_STORAGE_FOLDER_NAME + "/");
            if (!wallpaperDirectory.exists()) {
                wallpaperDirectory.mkdirs();
            }
        }

        String fname = (new Date().getTime()) + ".jpg";
        File file = new File(wallpaperDirectory, fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Alerter.create(GradientPaperActivity.this)
                    .setTextTypeface(ResourcesCompat.getFont(GradientPaperActivity.this, R.font.spacemono_regular))
                    .setTitleTypeface(ResourcesCompat.getFont(GradientPaperActivity.this, R.font.spacemono_bold))
                    .setTextAppearance(R.style.alertBody)
                    .setTitleAppearance(R.style.alertTitle)
                    .setTitle(getString(R.string.solid_color_save_title))
                    .setText(getString(R.string.solid_color_save_desc))
                    .setBackgroundColorRes(R.color.colorAccent)
                    .show();

            if (UtilityMethods.getAppInstance(GradientPaperActivity.this).getAdFreePurchase() == null && !UtilityMethods.getAppInstance(GradientPaperActivity.this).isAppTempPremium()) {
                mInterstitialAd.show(GradientPaperActivity.this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private Bitmap getGradientBitmap() {
        Bitmap returnedBitmap = Bitmap.createBitmap(displayMetrics.widthPixels, displayMetrics.heightPixels, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(returnedBitmap);

        drawable.draw(canvas);
        return returnedBitmap;
    }

    private void shareImage(Bitmap returnedBitmap) {
        String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), returnedBitmap, "title", null);
        Uri bitmapUri = Uri.parse(bitmapPath);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_image_subject));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shared_via_wolpepper));
        startActivity(Intent.createChooser(intent, "Share"));
    }

    private void showColorCountDialog() {
        View view = LayoutInflater.from(GradientPaperActivity.this).inflate(R.layout.input_colors, null, false);
        final EditText editText = view.findViewById(R.id.color_count_edit_text);

        new MaterialDialog.Builder(GradientPaperActivity.this)
                .title("No. of colors")
                .customView(view, false)
                .positiveText("Confirm")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (TextUtils.isEmpty(editText.getText())) {
                            Toast.makeText(GradientPaperActivity.this, "Please enter a value greater than 1", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (!TextUtils.isDigitsOnly(editText.getText())) {
                            return;
                        }

                        if (Integer.parseInt(editText.getText().toString()) >= 2) {
                            maxNumber = Integer.parseInt(editText.getText().toString());
                            generateColorArray();
                            updateGradientColors(colorArray, drawable);
                            dialog.dismiss();
                        }
                    }
                })
                .autoDismiss(false)
                .show();

    }

    private void showGradientTypeDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(GradientPaperActivity.this)
                .title("Select Gradient Type")
                .customView(R.layout.gradient_type_selector_dialog_layout, true)
                .positiveText("Done")
                .show();
        setUpGradientStyle(dialog);
    }

    private void setUpGradientStyle(MaterialDialog dialog) {
        final View dialogCustomView = dialog.getCustomView();
        if (dialogCustomView == null) {
            return;
        }
        final ImageView linearGradientIv, radialGradientIv, sweepGradientIv;
        final RadioGroup linearRadioGroup = dialogCustomView.findViewById(R.id.linear_gradient_radio_group);
        linearRadioGroup.check(R.id.top_to_bottom_radio);
        CheckBox darkAllowedCheckBox = dialogCustomView.findViewById(R.id.dark_colors_check_box);
        darkAllowedCheckBox.setChecked(isDarkColorsAllowed);

        /* image views initialization */
        linearGradientIv = dialogCustomView.findViewById(R.id.linear_gradient_selector);
        radialGradientIv = dialogCustomView.findViewById(R.id.radial_gradient_selector);
        sweepGradientIv = dialogCustomView.findViewById(R.id.sweep_gradient_selector);

        /* Create 3drawables to apply on image views before click */
        final GradientDrawable linearDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[0]);
        final GradientDrawable radialDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[0]);
        GradientDrawable sweepDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[0]);

        /* Update all 3 drawables */
        updateGradientColors(colorArray, linearDrawable);
        updateGradientColors(colorArray, radialDrawable);
        updateGradientColors(colorArray, sweepDrawable);

        /* configuring pre-click drawables */
        linearDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        radialDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        sweepDrawable.setGradientType(GradientDrawable.SWEEP_GRADIENT);
        radialDrawable.setGradientRadius(previouslySavedRadius);

        /* Setting drawables on image views */
        linearGradientIv.setImageDrawable(linearDrawable);
        radialGradientIv.setImageDrawable(radialDrawable);
        sweepGradientIv.setImageDrawable(sweepDrawable);

        darkAllowedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isDarkColorsAllowed = isChecked;
            }
        });

        /* Liner gradientRadio Group Click Listener */
        linearRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (linearRadioGroup.getCheckedRadioButtonId()) {
                    case R.id.top_to_bottom_radio:
                        linearDrawable.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
                        drawable.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
                        break;
                    case R.id.left_to_right_radio:
                        linearDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
                        drawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
                        break;
                    case R.id.top_left_to_bottom_right_radio:
                        linearDrawable.setOrientation(GradientDrawable.Orientation.TL_BR);
                        drawable.setOrientation(GradientDrawable.Orientation.TL_BR);
                        break;
                    case R.id.top_right_to_bottom_left_radio:
                        linearDrawable.setOrientation(GradientDrawable.Orientation.TR_BL);
                        drawable.setOrientation(GradientDrawable.Orientation.TR_BL);
                        break;
                }
            }
        });

        linearGradientIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gradientCentreLocator.setVisibility(View.GONE);
                dialogCustomView.findViewById(R.id.linear_gradient_config_view).setVisibility(View.VISIBLE);
                dialogCustomView.findViewById(R.id.radial_gradient_config_view).setVisibility(View.GONE);
                drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            }
        });

        radialGradientIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gradientCentreLocator.setVisibility(View.VISIBLE);
                dialogCustomView.findViewById(R.id.linear_gradient_config_view).setVisibility(View.GONE);
                dialogCustomView.findViewById(R.id.radial_gradient_config_view).setVisibility(View.VISIBLE);
                drawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
                SeekBar seekBar = dialogCustomView.findViewById(R.id.gradient_radius_seekbar);
                seekBar.setMax(displayMetrics.heightPixels * 2);
                seekBar.setProgress(previouslySavedRadius);
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        previouslySavedRadius = seekBar.getProgress();
                        float radiusSize = seekBar.getProgress();
                        float percentForPreview = (radiusSize / (displayMetrics.heightPixels * 2));
                        radialDrawable.setGradientRadius(percentForPreview * (radialGradientIv.getHeight() * 2));
                        drawable.setGradientRadius(radiusSize);
                    }
                });
            }
        });

        sweepGradientIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gradientCentreLocator.setVisibility(View.VISIBLE);
                dialogCustomView.findViewById(R.id.linear_gradient_config_view).setVisibility(View.GONE);
                dialogCustomView.findViewById(R.id.radial_gradient_config_view).setVisibility(View.GONE);
                drawable.setGradientType(GradientDrawable.SWEEP_GRADIENT);
            }
        });


    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }
}
