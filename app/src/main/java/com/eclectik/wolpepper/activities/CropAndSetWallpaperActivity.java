package com.eclectik.wolpepper.activities;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.eclectik.wolpepper.GlideApp;
import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.dataStructures.Papers;
import com.eclectik.wolpepper.utils.ThreadType;
import com.eclectik.wolpepper.utils.UtilityMethods;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.joaquimley.faboptions.FabOptions;
import com.tapadoo.alerter.Alerter;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.wysaid.nativePort.CGENativeLibrary;

import java.io.IOException;
import java.math.BigInteger;

public class CropAndSetWallpaperActivity extends AppCompatActivity {
    FabOptions fabOptions;

    // Bottom bar image views
    private ImageView applyWallpaperButton, blurButton, greyscaleButton, aspectRatioButton, rotateButton, flipButton;

    //Seek bars
    private SeekBar blurSeekBar, greyscaleSeekBar;

    // Flip Control buttons
    private ImageView flipHorizontal, flipVertical;

    // Controls (SeekBArs) container
    private View blurControlsContainer, greyscaleControlsContainer, flipControlsContainer;

    private View bottomEditingToolbar;

    private float blurRadius = 0;

    private CropImageView cropImageView;

    private ImageView currentImageHolder;

    private Bitmap previewBitmap;

    private DisplayMetrics displayMetrics = new DisplayMetrics();

    private boolean isPortraitAspectRatio;

    private float greyscaleAmount = 1;


    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_and_set_wallpaper);

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        fabOptions = findViewById(R.id.fab_options);
        initializeViews();

        cropImageView = findViewById(R.id.cropImageView);

        final Papers papers = getIntent().getParcelableExtra("image");


        UtilityMethods.executeRunnable(ThreadType.NETWORK, new Runnable() {
            @Override
            public void run() {
                GlideApp.with(CropAndSetWallpaperActivity.this).asBitmap().load(papers.getFullImageUrl()).listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);

                        float imageHeight = resource.getHeight();
                        float imageWidth = resource.getWidth();

                        float scaledHeight = displayMetrics.heightPixels;
                        float scaledWidth = ((imageWidth / imageHeight) * scaledHeight);

                        resource = Bitmap.createScaledBitmap(resource, (int) scaledWidth, (int) scaledHeight, true);

                        Bitmap finalResource = resource;
                        UtilityMethods.executeRunnable(ThreadType.MAIN, new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                                cropImageView.setImageBitmap(finalResource);
                                currentImageHolder = cropImageView.getmImageView();
                                setUpClickListener();
                                setEditingToolsClickListener();
                                setPortraitAspectRatio();

                                cropImageView.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
                                    @Override
                                    public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
                                        Bitmap bitmap = result.getBitmap();
                                        setBitmapAsWallpaper(bitmap);
                                    }
                                });
                            }
                        });

                        return false;
                    }
                }).submit();
            }
        });
    }

    private void initializeViews() {
        applyWallpaperButton = findViewById(R.id.apply_wallpaper_button);
        blurButton = findViewById(R.id.blur_button);
        greyscaleButton = findViewById(R.id.greyscale_button);
        aspectRatioButton = findViewById(R.id.aspect_ratio_button);
        rotateButton = findViewById(R.id.rotate_button);
        flipButton = findViewById(R.id.flip_button);

        blurSeekBar = findViewById(R.id.blur_seek_bar);
        greyscaleSeekBar = findViewById(R.id.greyscale_seek_bar);

        flipHorizontal = findViewById(R.id.horizontal_flip_button);
        flipVertical = findViewById(R.id.vertical_flip_button);

        blurControlsContainer = findViewById(R.id.blur_seek_bar_container);
        flipControlsContainer = findViewById(R.id.flip_controls_container);
        greyscaleControlsContainer = findViewById(R.id.greyscale_seek_bar_container);

        bottomEditingToolbar = findViewById(R.id.bottom_editing_tool_bar);

        initAds();
    }

    int adRetryCount = 0;
    private void initAds(){

        if (UtilityMethods.getAppInstance(this).getAdFreePurchase() == null && !UtilityMethods.getAppInstance(this).isAppTempPremium()) {
            InterstitialAd.load(this, getString(R.string.interstitial_unit_id), new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
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

    private void setEditingToolsClickListener() {
        applyWallpaperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.getCroppedImageAsync();

                YoYo.with(Techniques.SlideOutDown)
                        .duration(500)
                        .playOn(bottomEditingToolbar);

                if (UtilityMethods.getAppInstance(CropAndSetWallpaperActivity.this).getAdFreePurchase() == null && !UtilityMethods.getAppInstance(CropAndSetWallpaperActivity.this).isAppTempPremium()){
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(CropAndSetWallpaperActivity.this);
                    }
                }
            }
        });

        blurButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blurControlsContainer.getVisibility() == View.VISIBLE) {
                    blurControlsContainer.setVisibility(View.GONE);
                } else {
                    flipControlsContainer.setVisibility(View.GONE);
                    greyscaleControlsContainer.setVisibility(View.GONE);

                    blurControlsContainer.setVisibility(View.VISIBLE);
                }
            }
        });

        greyscaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (greyscaleControlsContainer.getVisibility() == View.VISIBLE) {
                    greyscaleControlsContainer.setVisibility(View.GONE);
                } else {
                    blurControlsContainer.setVisibility(View.GONE);
                    flipControlsContainer.setVisibility(View.GONE);

                    greyscaleControlsContainer.setVisibility(View.VISIBLE);
                }
            }
        });

        flipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flipControlsContainer.getVisibility() == View.VISIBLE) {
                    flipControlsContainer.setVisibility(View.GONE);
                } else {
                    blurControlsContainer.setVisibility(View.GONE);
                    greyscaleControlsContainer.setVisibility(View.GONE);

                    flipControlsContainer.setVisibility(View.VISIBLE);
                }
            }
        });

        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.rotateImage(-90);
            }
        });

        aspectRatioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPortraitAspectRatio) {
                    setPortraitAspectRatio();
                } else {
                    cropImageView.setAspectRatio(1, 1);
                    isPortraitAspectRatio = false;
                    Alerter.create(CropAndSetWallpaperActivity.this)
                            .setTextTypeface(ResourcesCompat.getFont(CropAndSetWallpaperActivity.this, R.font.spacemono_regular))
                            .setTitleTypeface(ResourcesCompat.getFont(CropAndSetWallpaperActivity.this, R.font.spacemono_bold))
                            .setTextAppearance(R.style.alertBody)
                            .setTitleAppearance(R.style.alertTitle)
                            .setText("Useful if your launcher provides wallpaper scrolling")
                            .setBackgroundColorRes(R.color.colorAccent)
                            .show();
                }
            }
        });
        setUpSeekBars();
    }


    private void setUpSeekBars() {
        BitmapDrawable drawable = (BitmapDrawable) currentImageHolder.getDrawable().mutate();
        previewBitmap = drawable.getBitmap();

        blurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                blurRadius = seekBar.getProgress();
                if (blurRadius == 0) {
                    currentImageHolder.setImageBitmap(previewBitmap);
                    return;
                }
                Bitmap bitmap = previewBitmap.copy(Bitmap.Config.ARGB_8888, true);
                currentImageHolder.setImageBitmap(CGENativeLibrary.filterImage_MultipleEffects(bitmap, "#unpack @blur lerp " + blurRadius / 75, blurRadius));
//                currentImageHolder.setImageBitmap(BlurBuilder.blur(CropAndSetWallpaperActivity.this, bitmap, 1, blurRadius));

            }
        });

        greyscaleSeekBar.setProgress(100);

        greyscaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            float previousGreyscaleAmount = 1;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float greyscaleSeekBarAmount = seekBar.getProgress();
                UtilityMethods.setImageViewGreyScale(currentImageHolder, greyscaleSeekBarAmount / 100, previousGreyscaleAmount);
                previousGreyscaleAmount = greyscaleSeekBarAmount / 100;
                greyscaleAmount = greyscaleSeekBarAmount / 100;
            }
        });

        flipVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.flipImageVertically();
            }
        });

        flipHorizontal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.flipImageHorizontally();
            }
        });
    }

    /**
     * Fab layout click listener
     */
    private void setUpClickListener() {
        fabOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.fab_option_flip_horizontal:
                        cropImageView.flipImageHorizontally();
                        break;
                    case R.id.fab_option_flip_vertical:
                        cropImageView.flipImageVertically();
                        break;
                    case R.id.fab_option_aspect_ratio:
                        if (!isPortraitAspectRatio) {

                            setPortraitAspectRatio();

                        } else {

                            cropImageView.setAspectRatio(1, 1);

                            isPortraitAspectRatio = false;

                            Alerter.create(CropAndSetWallpaperActivity.this)
                                    .setTextTypeface(ResourcesCompat.getFont(CropAndSetWallpaperActivity.this, R.font.spacemono_regular))
                                    .setTitleTypeface(ResourcesCompat.getFont(CropAndSetWallpaperActivity.this, R.font.spacemono_bold))
                                    .setTextAppearance(R.style.alertBody)
                                    .setTitleAppearance(R.style.alertTitle)
                                    .setText("Useful if your launcher provides wallpaper scrolling")
                                    .setBackgroundColorRes(R.color.colorAccent)
                                    .show();

                        }
                        break;
                    case R.id.fab_option_apply_crop:
                        cropImageView.getCroppedImageAsync();

                        YoYo.with(Techniques.SlideOutDown)
                                .duration(500)
                                .playOn(fabOptions);

                        break;
                    case R.id.fab_option_rotate_left:
                        cropImageView.rotateImage(-90);
                        break;
                    case R.id.fab_option_rotate_right:
                        cropImageView.rotateImage(90);
                        break;
                }
            }
        });
    }

    /**
     * Set the provided bitmap as wallpaper
     *
     * @param bitmap - Bitmap to set as wallpaper
     */
    private void setBitmapAsWallpaper(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(CropAndSetWallpaperActivity.this, "Unable to set wallpaper. Try again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {

            // GET IMAGE IN BITMAP FROM URI
            //bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
            bitmap.setPremultiplied(true);

            Bitmap bitmap1;

            if (isPortraitAspectRatio) {

                bitmap1 = Bitmap.createScaledBitmap(bitmap, displayMetrics.widthPixels, displayMetrics.heightPixels, true);

            } else {

                bitmap1 = Bitmap.createScaledBitmap(bitmap, displayMetrics.heightPixels, displayMetrics.heightPixels, true);

            }

            Canvas c = new Canvas(bitmap1);
            Paint paint = new Paint();
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(greyscaleAmount);
            ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
            paint.setColorFilter(f);
            c.drawBitmap(bitmap1, 0, 0, paint);

            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            wallpaperManager.forgetLoadedWallpaper();
            if (blurRadius > 0) {
                wallpaperManager.setBitmap(CGENativeLibrary.filterImage_MultipleEffects(bitmap1, "#unpack @blur lerp " + blurRadius / 85, blurRadius));
            } else {
                wallpaperManager.setBitmap(bitmap1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
        finish();
    }

    /**
     * Set aspect ratio to users screen aspect ratio if portrait aspect ratio is selected
     */
    private void setPortraitAspectRatio() {
        BigInteger h = BigInteger.valueOf(displayMetrics.heightPixels);

        BigInteger w = BigInteger.valueOf(displayMetrics.widthPixels);

        int gcd = h.gcd(w).intValue();

        cropImageView.setAspectRatio(displayMetrics.widthPixels / gcd, displayMetrics.heightPixels / gcd);

        isPortraitAspectRatio = true;
    }
}
