package com.eclectik.wolpepper.muzei;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.eclectik.wolpepper.R;
import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

/**
 * Created by mj on 2/7/17.
 **/

public class WolPepperSource extends RemoteMuzeiArtSource {
    private static final String SOURCE_NAME = "wol:pepper";
    private Random random = new Random();
    private static int ROTATE_TIME_MILLIS = 60 * 60 * 1000; // rotate every 1 hour  = 1 * 60 * 60 * 1000

    public WolPepperSource() {
        super(SOURCE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
    }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        SharedPreferences refreshIntervalPreferences = getSharedPreferences(getString(R.string.muzei_refresh_interval_pref_base_key), MODE_PRIVATE);
        final int savedInterval = refreshIntervalPreferences.getInt(getString(R.string.muzei_refresh_interval_pref_key), 3);

        SharedPreferences activeCollectionPreference = getSharedPreferences(getString(R.string.muzei_active_list_base_prefs_key), MODE_PRIVATE);
        String activeListName = activeCollectionPreference.getString(getString(R.string.muzei_active_list_key), "");
        SharedPreferences imageIdPreferences = getSharedPreferences(getString(R.string.muzei_image_id_base_prefs_key), MODE_PRIVATE);
        SharedPreferences imageDatePreferences = getSharedPreferences(activeListName + getString(R.string.muzei_image_date_base_pref_key), MODE_PRIVATE);
        SharedPreferences imageAuthorPreferences = getSharedPreferences(activeListName + getString(R.string.muzei_image_author_base_prefs_key), MODE_PRIVATE);
        SharedPreferences imageDownloadLinksPreferences = getSharedPreferences(activeListName + getString(R.string.muzei_image_download_links), MODE_PRIVATE);
        Set<String> imageIdSet = imageIdPreferences.getStringSet(activeListName, null);

        if (imageIdSet != null && imageIdSet.size() > 0) {
            int previousIndex = activeCollectionPreference.getInt("previously_used_muzei_list_index", 0);
            ArrayList<String> idArray = new ArrayList<>(imageIdSet);
            int index = 0;

            if (previousIndex < idArray.size() - 1) {
                index = previousIndex + 1;
            }

            activeCollectionPreference.edit().putInt("previously_used_muzei_list_index", index).apply();

            String imageId = idArray.get(index);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);

            final String author = imageAuthorPreferences.getString(imageId, "");
            final String date = imageDatePreferences.getString(imageId, "");
            final String imageLink = imageDownloadLinksPreferences.getString(imageId, "").concat("&h=" + displayMetrics.heightPixels);

            publishArtwork(new Artwork.Builder()
                    .imageUri(Uri.parse(imageLink))
                    .title(author)
                    .byline(date)
                    .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(imageLink)))
                    .build());

            scheduleUpdate(System.currentTimeMillis() + (ROTATE_TIME_MILLIS * savedInterval));
        }
    }
}
