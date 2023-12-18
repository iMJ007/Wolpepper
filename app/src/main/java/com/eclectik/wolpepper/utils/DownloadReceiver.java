package com.eclectik.wolpepper.utils;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

/**
 * Created by mj on 9/6/17...
 *
 */

public class DownloadReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(final Context context, Intent intent) {
        final PendingResult pendingResult = goAsync();
        //check if the broadcast message is for our Enqueued download
        final long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @SuppressLint("ApplySharedPref")
            @Override
            protected Void doInBackground(Void... params) {
                SharedPreferences preferences = context.getSharedPreferences(String.valueOf(referenceId), Context.MODE_PRIVATE);
                if (DownloadWallpaperUtils.getDownloadStatus(referenceId) == DownloadManager.STATUS_FAILED){
                    preferences.edit().clear().commit();
                    pendingResult.finish();
                    return null;
                }

                if(!preferences.getString(String.valueOf(referenceId), "").equals("")){
                    String imageId = preferences.getString(String.valueOf(referenceId), "");
                    boolean isGreyEnabled, isSetWallpaper, isShareEnabled, isSetAsIntentEnabled;
                    isGreyEnabled = preferences.getBoolean(ConstantValues.PreferencesKeys.IMAGE_DOWNLOAD_IS_GREY_ENABLED_KEY, false);
                    isSetWallpaper = preferences.getBoolean(ConstantValues.PreferencesKeys.IMAGE_DOWNLOAD_SET_WALLPAPER_ENABLED_KEY, false);
                    isShareEnabled = preferences.getBoolean(ConstantValues.PreferencesKeys.IMAGE_SHARE_ENABLED_KEY, false);
                    isSetAsIntentEnabled = preferences.getBoolean(ConstantValues.PreferencesKeys.IMAGE_SET_AS_INTENT_KEY, false);
                    String authorName = preferences.getString("imageAuthor", "Unknown");
                    preferences.edit().clear().commit();
                    DownloadWallpaperUtils.processImageFurther(context, imageId, isGreyEnabled, isSetWallpaper, isShareEnabled, authorName, isSetAsIntentEnabled);
                }
                pendingResult.finish();
                return null;
            }
        };

        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


    }



}
