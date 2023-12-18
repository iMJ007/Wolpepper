package com.eclectik.wolpepper.utils.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.eclectik.wolpepper.R;

public class GeneralReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()){
            case Intent.ACTION_MEDIA_BAD_REMOVAL:
                resetStorageToDefault(context);
                break;
            case Intent.ACTION_MEDIA_EJECT:
                resetStorageToDefault(context);
                break;
            case Intent.ACTION_MEDIA_REMOVED:
                resetStorageToDefault(context);
                break;
            case Intent.ACTION_MEDIA_UNMOUNTED:
                resetStorageToDefault(context);
                break;
        }
    }

    private void resetStorageToDefault(Context context){
        context.getSharedPreferences(context.getString(R.string.storage_preference_base_key), Context.MODE_PRIVATE).edit().clear().apply();
    }
}
