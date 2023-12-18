package com.eclectik.wolpepper.activities;

import static com.eclectik.wolpepper.utils.ConstantValues.SOLID_COLORS_STORAGE_FOLDER_NAME;
import static com.eclectik.wolpepper.utils.ConstantValues.SOLID_COLORS_STORAGE_FULL_PATH;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.utils.UtilityMethods;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tapadoo.alerter.Alerter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class SolidColors extends AppCompatActivity implements ColorChooserDialog.ColorCallback {

    private FloatingActionButton floatingActionButton, download, share, set;
    private ImageView solidWallpaper;
    private String hexColor = "#03A9F4";
    private ColorDrawable colorDrawable;
    private DisplayMetrics displayMetrics = new DisplayMetrics();

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solid_colors_pickup);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(SolidColors.this, R.color.statusBar));
        }

        initAds();

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        colorDrawable = new ColorDrawable();
        colorDrawable.setColor(ContextCompat.getColor(this, R.color.colorAccent));

        floatingActionButton = findViewById(R.id.color_picker);
        download = findViewById(R.id.dowload);
        share = findViewById(R.id.share);
        set = findViewById(R.id.set);
        solidWallpaper = findViewById(R.id.solid_wallpaper);
        solidWallpaper.setImageDrawable(colorDrawable);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ColorChooserDialog.Builder(SolidColors.this, R.string.color_select)
                        .titleSub(R.string.color_select)  // title of dialog when viewing shades of a color
                        .doneButton(R.string.md_done_label)  // changes label of the done button
                        .cancelButton(R.string.md_cancel_label)  // changes label of the cancel button
                        .backButton(R.string.md_back_label)  // changes label of the back button
                        .dynamicButtonColor(false)  // defaults to true, false will disable changing action buttons' color to currently selected color
                        .show(SolidColors.this);
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveImage(getSolidColorBitmap());
                Alerter.create(SolidColors.this)
                        .setTextTypeface(ResourcesCompat.getFont(SolidColors.this, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(SolidColors.this, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setTitle(getString(R.string.solid_color_save_title))
                        .setText(getString(R.string.solid_color_save_desc))
                        .setBackgroundColorRes(R.color.colorAccent)
                        .show();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareWall(getSolidColorBitmap());
            }
        });

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                toSetWall(getSolidColorBitmap());

                WallpaperManager wpm = WallpaperManager.getInstance(SolidColors.this);
                try {
                    wpm.setBitmap(getSolidColorBitmap());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Alerter.create(SolidColors.this)
                        .setTextTypeface(ResourcesCompat.getFont(SolidColors.this, R.font.spacemono_regular))
                        .setTitleTypeface(ResourcesCompat.getFont(SolidColors.this, R.font.spacemono_bold))
                        .setTextAppearance(R.style.alertBody)
                        .setTitleAppearance(R.style.alertTitle)
                        .setTitle(getString(R.string.solid_color_save_title)) // Title and title color
                        .setText(getString(R.string.solid_color_set_desc)) // Message and message color
                        .setBackgroundColorRes(R.color.colorAccent)
                        .show();


                if (UtilityMethods.getAppInstance(SolidColors.this).getAdFreePurchase() == null && !UtilityMethods.getAppInstance(SolidColors.this).isAppTempPremium()){
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(SolidColors.this);
                    }
                }
            }
        });
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

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        hexColor = String.format("#%06X", (0xFFFFFF & selectedColor));
        colorDrawable.setColor(selectedColor);
        solidWallpaper.setImageDrawable(colorDrawable);
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {

    }


    private Bitmap getSolidColorBitmap() {
        Bitmap returnedBitmap = Bitmap.createBitmap(displayMetrics.widthPixels, displayMetrics.heightPixels, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(returnedBitmap);

        colorDrawable.draw(canvas);
        return returnedBitmap;
    }


//    public static Bitmap getBitmapFromView(View view) {
//        //Define a bitmap with the same size as the view
//        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
//        //Bind a canvas to it
//        Canvas canvas = new Canvas(returnedBitmap);
//        //Get the view's background
//        Drawable bgDrawable =view.getBackground();
//        if (bgDrawable!=null)
//            //has background drawable, then draw it on the canvas
//            bgDrawable.draw(canvas);
//        else
//            //does not have background drawable, then draw white background on the canvas
//            canvas.drawColor(Color.WHITE);
//        // draw the view on the canvas
//        view.draw(canvas);
//        //return the bitmap
//        return returnedBitmap;
//    }

    /*Save Wallpaper*/
    public void SaveImage(Bitmap finalBitmap) {

        File wallpaperDirectory;

        if (UtilityMethods.isDefaultStorage(SolidColors.this)) {
            wallpaperDirectory = new File(SOLID_COLORS_STORAGE_FULL_PATH);
            if (!wallpaperDirectory.exists()) {
                wallpaperDirectory.mkdirs();
            }
        } else {
            wallpaperDirectory = new File(UtilityMethods.getExternalStoragePath(SolidColors.this) + "/" + SOLID_COLORS_STORAGE_FOLDER_NAME + "/");
            if (!wallpaperDirectory.exists()) {
                wallpaperDirectory.mkdirs();
            }
        }

        String fname = (new Date().getTime()) + ".jpg";
        File file = new File(wallpaperDirectory, fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            if (UtilityMethods.getAppInstance(this).getAdFreePurchase() == null && !UtilityMethods.getAppInstance(this).isAppTempPremium()){
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(this);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * share bitmap woth others
     *
     * @param bitmap - bitmap of the color to share
     */
    public void shareWall(Bitmap bitmap) {
        String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "title", null);
        Uri bitmapUri = Uri.parse(bitmapPath);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_image_subject));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shared_via_wolpepper) + "\nColor : " + hexColor);
        startActivity(Intent.createChooser(intent, "Share"));
    }
}
