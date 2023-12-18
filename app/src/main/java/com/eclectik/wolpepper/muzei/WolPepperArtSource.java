package com.eclectik.wolpepper.muzei;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.WINDOW_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.eclectik.wolpepper.R;
import com.google.android.apps.muzei.api.provider.Artwork;
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider;
import com.google.android.apps.muzei.api.provider.ProviderContract;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class WolPepperArtSource extends MuzeiArtProvider {

    private static final String SOURCE_NAME = "wol:pepper";
    private Random random = new Random();
    private static int ROTATE_TIME_MILLIS = 60 * 60 * 1000; // rotate every 1 hour  = 1 * 60 * 60 * 1000

    @NonNull
    @Override
    public List<Uri> setArtwork(@NonNull Iterable<Artwork> artwork) {
        return super.setArtwork(artwork);
    }


    @Override
    public boolean isArtworkValid(@NonNull Artwork artwork) {
        if (getContext().getSharedPreferences(getContext().getString(R.string.muzei_active_list_base_prefs_key), MODE_PRIVATE).getBoolean(getContext().getString(R.string.is_active_list_changed_pref), false)){
            onLoadRequested(true);
        }
        return super.isArtworkValid(artwork);
    }


    @SuppressLint("ApplySharedPref")
    @Override
    protected void onLoadRequested(boolean initial) {
//        Log.e("HELLO", "ELLO");
        Context context = getContext();

        ProviderContract.getProviderClient(context.getApplicationContext(), "com.eclectik.wolpepper.muzei.WolPepperArtSource")
        .setArtwork(getArtworkList(context));

    }

    public static List<Artwork> getArtworkList(Context context){
        SharedPreferences refreshIntervalPreferences = context.getSharedPreferences(context.getString(R.string.muzei_refresh_interval_pref_base_key), MODE_PRIVATE);
        final int savedInterval = refreshIntervalPreferences.getInt(context.getString(R.string.muzei_refresh_interval_pref_key), 3);

        SharedPreferences activeCollectionPreference = context.getSharedPreferences(context.getString(R.string.muzei_active_list_base_prefs_key), MODE_PRIVATE);
        activeCollectionPreference.edit().putBoolean(context.getString(R.string.is_active_list_changed_pref), false).commit();
        String activeListName = activeCollectionPreference.getString(context.getString(R.string.muzei_active_list_key), "");
        SharedPreferences imageIdPreferences = context.getSharedPreferences(context.getString(R.string.muzei_image_id_base_prefs_key), MODE_PRIVATE);
        SharedPreferences imageDatePreferences = context.getSharedPreferences(activeListName + context.getString(R.string.muzei_image_date_base_pref_key), MODE_PRIVATE);
        SharedPreferences imageAuthorPreferences = context.getSharedPreferences(activeListName + context.getString(R.string.muzei_image_author_base_prefs_key), MODE_PRIVATE);
        SharedPreferences imageDownloadLinksPreferences = context.getSharedPreferences(activeListName + context.getString(R.string.muzei_image_download_links), MODE_PRIVATE);
        Set<String> imageIdSet = imageIdPreferences.getStringSet(activeListName, null);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);

        List<Artwork> artworkList = new ArrayList<>();
        for (String imageId : imageIdSet) {

            final String author = imageAuthorPreferences.getString(imageId, "");
            final String date = imageDatePreferences.getString(imageId, "");
            final String imageLink = imageDownloadLinksPreferences.getString(imageId, "").concat("&h=" + displayMetrics.heightPixels);

            artworkList.add(new Artwork.Builder()
                    .token(imageId)
                    .persistentUri(Uri.parse(imageLink))
                    .title(author)
                    .byline(date)
                    .attribution("Unsplash.com")
                    .webUri(Uri.parse(imageLink))
                    .build());
        }

        return artworkList;
    }
}
