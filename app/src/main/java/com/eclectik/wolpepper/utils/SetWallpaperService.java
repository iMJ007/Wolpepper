package com.eclectik.wolpepper.utils;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.eclectik.wolpepper.GlideApp;
import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.activities.CropAndSetWallpaperActivity;
import com.eclectik.wolpepper.activities.MainActivity;
import com.eclectik.wolpepper.dataStructures.Papers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SetWallpaperService extends Service {
    private Papers unsplashImage;
    private boolean isGreyScale = false;
    boolean isSetAs = false;

    public SetWallpaperService() {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        unsplashImage = intent.getParcelableExtra(getString(R.string.set_wallpaper_intent_extra));
        isGreyScale = intent.getBooleanExtra(getString(R.string.grey_scale_identifier_intent_extra), false);
        startForegroundServiceForThisInstance();
        isSetAs = intent.getBooleanExtra(getString(R.string.set_as_flag), false);
        downloadAndSetWallpaper();
        return super.onStartCommand(intent, flags, startId);
    }


    private void downloadAndSetWallpaper() {
        boolean isQuickSetEnabled = getSharedPreferences(getString(R.string.quick_set_wallpaper_base_pref), MODE_PRIVATE).getBoolean(getString(R.string.quick_set_wallpaper_pref), false);

        if (!isQuickSetEnabled && !isSetAs) {
            Intent intent = new Intent(this, CropAndSetWallpaperActivity.class);
            intent.putExtra("image", unsplashImage);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            stopForeground(true);
            stopSelf();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                GlideApp.with(SetWallpaperService.this).asBitmap().load(unsplashImage.getFullImageUrl()).listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Toast.makeText(SetWallpaperService.this, "An error occurred while downloading image. Try Again!", Toast.LENGTH_LONG).show();
                        stopForeground(true);
                        stopSelf();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (isSetAs) {
//                            DownloadWallpaperUtils.launchSetAsIntent(SetWallpaperService.this, resource, unsplashImage);
                            launchSetAs(SetWallpaperService.this, resource, isGreyScale);
                        } else {
                            setWallpaper(SetWallpaperService.this, resource, isGreyScale);
                            Toast.makeText(SetWallpaperService.this, "Wallpaper Fetched.", Toast.LENGTH_LONG).show();
                        }
                        return false;
                    }
                }).submit();
            }
        }).start();

    }

    private void startForegroundServiceForThisInstance() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        startForeground(001, NotificationUtils.showDownloadingAndSettingWallpaperNotification(this, pendingIntent));
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @SuppressLint("StaticFieldLeak")
    private void launchSetAs(final Context context, final Bitmap bitmap, final boolean isGreyEnabled) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                publishProgress();

                if (isGreyEnabled){
                    return greyScaleImage(bitmap);
                }
                return bitmap;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
                Toast.makeText(context, "Launching set as menu.", Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onPostExecute(Bitmap bitmap1) {
                super.onPostExecute(bitmap1);
                launchSetAsIntent(context, bitmap1, unsplashImage);
                stopForeground(true);
                stopSelf();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static void launchSetAsIntent(Context context, Bitmap image, Papers unsplashImage) {
        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(getImageUri(context, image, unsplashImage), "image/jpeg");
        intent.putExtra("mimeType", "image/jpeg");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Set as:").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage, Papers image) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, image.getImageId(), null);
        return Uri.parse(path);
    }

    @SuppressLint("StaticFieldLeak")
    private void setWallpaper(final Context context, final Bitmap bitmap, final boolean isGreyEnabled) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                publishProgress();
                setQuickWallpaper(context, bitmap, isGreyEnabled);
                return null;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
                Toast.makeText(context, "Scaling and setting wallpaper.", Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(context, "Wallpaper set successfully.", Toast.LENGTH_LONG).show();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setQuickWallpaper(Context context, Bitmap bitmap, boolean isGreyEnabled) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inMutable = true;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);

        float imageHeight = bitmap.getHeight();
        float imageWidth = bitmap.getWidth();

        float scaledHeight = displayMetrics.heightPixels;
        float scaledWidth = ((imageWidth / imageHeight) * scaledHeight);

        bitmap = Bitmap.createScaledBitmap(bitmap, (int) scaledWidth, (int) scaledHeight, true);

        if (isGreyEnabled) {
            bitmap = greyScaleImage(bitmap);
        }

        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
            wallpaperManager.forgetLoadedWallpaper();
            wallpaperManager.setBitmap(bitmap);
            bitmap.recycle();
            wallpaperManager = null;
            displayMetrics = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        stopForeground(true);
        stopSelf();
    }


    /**
     * Converts the the image to greyscale
     */
    private Bitmap greyScaleImage(Bitmap bitmap) {
        /*This is just getting the bitmap of saved image to pass it further*/
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inMutable = true;

        Canvas c = new Canvas(bitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bitmap, 0, 0, paint);
        return bitmap;
    }
}
