package com.eclectik.wolpepper.activities.settingsActivities;

import static com.eclectik.wolpepper.utils.ConstantValues.BASE_UNSPLASH_URL_STRING;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.eclectik.wolpepper.BuildConfig;
import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.utils.NetworkUtils;
import com.eclectik.wolpepper.utils.UtilityMethods;

public class About extends AppCompatActivity {

    TextView firebaseIntentTv, glideIntentTv, circularViewIntentTv, authorGoogleOneIntentTv, authorGoogleTwoIntentTv, robotoIntentTv, unsplashIntentTv, androidImageCopperTv, alerterIntentTv, blurryIntentTv, fabOptionsIntentTv;
    TextView tapTargetViewIntentTv;
    TextView photoViewIntentTv;
    TextView muzeiIntentTv;
    TextView androidViewAnimationIntentTv;
    TextView revealatorIntentTv;
    TextView bottomBarIntentTv;
    TextView materialDialogsIntentTv;
    TextView squareLoadingLibraryIntentTv;
    TextView okHttpIntentTv;
    TextView diagonalLayoutIntentTv;
    TextView expandingViewIntentTv;
    ImageView authorOneIv, authorTwoIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setElevation(0);
        setTitle(getString(R.string.about_activity_title));

        setContentView(R.layout.activity_about);
        firebaseIntentTv = findViewById(R.id.firebase_intent);
        glideIntentTv = findViewById(R.id.glide_intent);
        circularViewIntentTv = findViewById(R.id.circular_intent);
        authorGoogleOneIntentTv = findViewById(R.id.author_google_one);
        authorGoogleTwoIntentTv = findViewById(R.id.author_google_two);
        robotoIntentTv = findViewById(R.id.roboto_intent);
        unsplashIntentTv = findViewById(R.id.unsplash_intent);
        androidImageCopperTv = findViewById(R.id.cropper_intent);
        alerterIntentTv = findViewById(R.id.alerter_intent);
        blurryIntentTv = findViewById(R.id.blurry_intent);
        fabOptionsIntentTv = findViewById(R.id.fab_options_intent);
        tapTargetViewIntentTv = findViewById(R.id.tap_target_intent);
        photoViewIntentTv = findViewById(R.id.photo_view_intent);
        muzeiIntentTv = findViewById(R.id.muzei_library_license_intent);
        androidViewAnimationIntentTv = findViewById(R.id.android_view_animation_library_license_intent);
        revealatorIntentTv = findViewById(R.id.revealator_library_license_intent);
        bottomBarIntentTv = findViewById(R.id.bottom_bar_library_license_intent);
        materialDialogsIntentTv = findViewById(R.id.material_dialogs_library_license_intent);
        squareLoadingLibraryIntentTv = findViewById(R.id.square_loading_library_license_intent);
        okHttpIntentTv = findViewById(R.id.okhttp_library_license_intent);
        diagonalLayoutIntentTv = findViewById(R.id.diagonal_layout_library_license_intent);
        expandingViewIntentTv = findViewById(R.id.expanding_layout_library_license_intent);
        ((TextView)findViewById(R.id.about_version_name)).setText(BuildConfig.VERSION_NAME);

        authorOneIv = findViewById(R.id.author_image_1);
        authorTwoIv = findViewById(R.id.author_image_2);


        authorGoogleTwoIntentTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://shahidshaikh.com"));
                startActivity(i);
            }
        });

        authorGoogleOneIntentTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://junaidgandhi.com"));
                startActivity(i);
            }
        });

        unsplashIntentTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(NetworkUtils.buildLinkWithUtmParameters(About.this, BASE_UNSPLASH_URL_STRING)); // not using below methods on this link because of UTM PARAMETERS
                startActivity(i);
            }
        });

        UtilityMethods.setTextViewOpenLinkClickListener(firebaseIntentTv, "https://github.com/firebase/FirebaseUI-Android");

        UtilityMethods.setTextViewOpenLinkClickListener(glideIntentTv, "https://github.com/bumptech/glide");

        UtilityMethods.setTextViewOpenLinkClickListener(circularViewIntentTv, "https://github.com/hdodenhof/CircleImageView");

        UtilityMethods.setTextViewOpenLinkClickListener(robotoIntentTv, "https://fonts.google.com/specimen/Space+Mono?selection.family=Space+Mono");

        UtilityMethods.setTextViewOpenLinkClickListener(androidImageCopperTv, getString(R.string.android_image_cropper_license_link));

        UtilityMethods.setTextViewOpenLinkClickListener(alerterIntentTv, getString(R.string.alerter_library_link));

        UtilityMethods.setTextViewOpenLinkClickListener(blurryIntentTv, getString(R.string.blury_library_link));

        UtilityMethods.setTextViewOpenLinkClickListener(fabOptionsIntentTv, getString(R.string.fab_options_library_license_link));

        UtilityMethods.setTextViewOpenLinkClickListener(tapTargetViewIntentTv, getString(R.string.tap_target_view_library_link));

        UtilityMethods.setTextViewOpenLinkClickListener(photoViewIntentTv, getString(R.string.photo_view_library_link));

        UtilityMethods.setTextViewOpenLinkClickListener(muzeiIntentTv, getString(R.string.muzei_library_link));

        UtilityMethods.setTextViewOpenLinkClickListener(androidViewAnimationIntentTv, getString(R.string.android_view_animation_library_link));

        UtilityMethods.setTextViewOpenLinkClickListener(revealatorIntentTv, getString(R.string.revealator_library_link));

        UtilityMethods.setTextViewOpenLinkClickListener(bottomBarIntentTv, getString(R.string.bottom_bar_library_link));

        UtilityMethods.setTextViewOpenLinkClickListener(materialDialogsIntentTv, getString(R.string.material_dialogs_library_link));

        UtilityMethods.setTextViewOpenLinkClickListener(squareLoadingLibraryIntentTv, getString(R.string.square_loader_library_link));

        UtilityMethods.setTextViewOpenLinkClickListener(okHttpIntentTv, getString(R.string.okhttp_library_link));

        UtilityMethods.setTextViewOpenLinkClickListener(diagonalLayoutIntentTv, getString(R.string.diagonal_layout_library_link));

        UtilityMethods.setTextViewOpenLinkClickListener(expandingViewIntentTv, getString(R.string.expanding_view_library_link));
    }

    public void openGPlus(String profile) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName("com.google.android.apps.plus",
                    "com.google.android.apps.plus.phone.UrlGatewayActivity");
            intent.putExtra("customAppUri", profile);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/" + profile + "/posts")));
        }
    }

}
