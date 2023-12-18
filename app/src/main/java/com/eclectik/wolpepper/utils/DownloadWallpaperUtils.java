package com.eclectik.wolpepper.utils;

import static android.content.Context.DOWNLOAD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.eclectik.wolpepper.GlideApp;
import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.activities.CropAndSetWallpaperActivity;
import com.eclectik.wolpepper.dataStructures.PaperCollections;
import com.eclectik.wolpepper.dataStructures.Papers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by mj on 9/6/17.
 */
@SuppressLint("ApplySharedPref")
public class DownloadWallpaperUtils {
    private static DownloadManager downloadManager;

    /**
     * Start downloading image with download manager
     *
     * @param context
     * @param imageUri
     * @param papers
     * @param isGreyScale
     * @param setWallpaper
     * @param isRaw
     * @return - Download reference ID
     */
    public static long downloadPaper(final Context context, Uri imageUri, final Papers papers, boolean isGreyScale, boolean setWallpaper, boolean isRaw) {

        if (isGreyScale && !setWallpaper) {
            GlideApp.with(context).asBitmap().load(imageUri).addListener(new RequestListener<Bitmap>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                    convertAndSaveBitmapToGreyScale(context, resource, papers.getImageId());
                    return false;
                }
            }).submit();
            return 0;
        }

        long downloadReference;

        downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(imageUri);

        //Setting title of request
        request.setTitle(papers.getImageId());

        //Setting description of request
        request.setDescription("Getting Wol:Pepper");

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // Checking if storage folders are created else create them
        File folder = new File(ConstantValues.IMAGE_LOCAL_RAW_STORAGE_FULL_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (UtilityMethods.isDefaultStorage(context)) {
            //Set the local destination for the downloaded file to a path within the application's external files directory
            if (isRaw) {
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,ConstantValues.IMAGE_LOCAL_STORAGE_FOLDER_NAME + "/RAW/"+ papers.getImageId() + ".jpg");
            } else {
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,ConstantValues.IMAGE_LOCAL_STORAGE_FOLDER_NAME + "/" + papers.getImageId() + ".jpg");
            }
        } else {
            //Set the local destination for the downloaded file to a path within the application's external files directory
            if (isRaw) {
                request.setDestinationUri(UtilityMethods.getExternalStorageUri(context).buildUpon().appendPath("RAW").appendPath(papers.getImageId() + ".jpg").build());
            } else {
                request.setDestinationUri(UtilityMethods.getExternalStorageUri(context).buildUpon().appendPath(papers.getImageId() + ".jpg").build());
            }
        }

        //Enqueue download and save the referenceId
        downloadReference = downloadManager.enqueue(request);

        /* SAVE THE VALUES IN PREFS IF GREYSCALE AND/OR SETWALLPAPER IS ENABLED */
        SharedPreferences preferences = context.getSharedPreferences(String.valueOf(downloadReference), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (isGreyScale && setWallpaper) {
            // IF BOTH GREY AND SET WALLPAPER IS ENABLED
            editor.putString(String.valueOf(downloadReference), papers.getImageId());
            editor.putBoolean(ConstantValues.PreferencesKeys.IMAGE_DOWNLOAD_IS_GREY_ENABLED_KEY, isGreyScale);
            editor.putBoolean(ConstantValues.PreferencesKeys.IMAGE_DOWNLOAD_SET_WALLPAPER_ENABLED_KEY, setWallpaper);

        } else if (isGreyScale) {
            // IF ONLY GREY IS ENABLED
            editor.putString(String.valueOf(downloadReference), papers.getImageId());
            editor.putBoolean(ConstantValues.PreferencesKeys.IMAGE_DOWNLOAD_IS_GREY_ENABLED_KEY, isGreyScale);

        } else if (setWallpaper) {
            // IF ONLY SETWALLPAPAER IS ENABLED
            editor.putString(String.valueOf(downloadReference), papers.getImageId());
            editor.putBoolean(ConstantValues.PreferencesKeys.IMAGE_DOWNLOAD_SET_WALLPAPER_ENABLED_KEY, setWallpaper);

        }

        editor.commit();
        return downloadReference;
    }

    /**
     * Start downloading image and share on completion
     *
     * @param context
     * @param imageUri
     * @param papers
     * @param isGreyScale
     * @param setWallpaper
     * @param isRaw
     * @return
     */
    public static long downloadAndSharePaper(final Context context, Uri imageUri, final Papers papers, final boolean isGreyScale, boolean setWallpaper, boolean isRaw) {
        GlideApp.with(context).asBitmap().load(imageUri).addListener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                if (isGreyScale){
                    resource = convertBitmapToGrey(resource);
                }
                launchBitmapShareIntent(context, resource, papers);
                return false;
            }
        }).submit();
        return 0;
    }

    /**
     * Start downloading image and show set as intent on completion
     *
     * @param context
     * @param imageUri
     * @param papers
     * @param isGreyScale
     * @param setWallpaper
     * @param isRaw
     * @return
     */
    public static long downloadAndSetAsIntent(Context context, Uri imageUri, Papers papers, boolean isGreyScale, boolean setWallpaper, boolean isRaw) {
        long downloadReference;

        downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(imageUri);

        //Setting title of request
        request.setTitle(papers.getImageId());

        //Setting description of request
        request.setDescription("Getting Wol:Pepper");

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        //Set the local destination for the downloaded file to a path within the application's external files directory
        if (UtilityMethods.isDefaultStorage(context)) {
            //Set the local destination for the downloaded file to a path within the application's external files directory
            if (isRaw) {
                request.setDestinationInExternalPublicDir(ConstantValues.IMAGE_LOCAL_STORAGE_FOLDER_NAME + "/RAW", papers.getImageId() + ".jpg");
            } else {
                request.setDestinationInExternalPublicDir(ConstantValues.IMAGE_LOCAL_STORAGE_FOLDER_NAME, papers.getImageId() + ".jpg");
            }
        } else {
            //Set the local destination for the downloaded file to a path within the application's external files directory
            if (isRaw) {
                request.setDestinationUri(UtilityMethods.getExternalStorageUri(context).buildUpon().appendPath("RAW").appendPath(papers.getImageId() + ".jpg").build());
            } else {
                request.setDestinationUri(UtilityMethods.getExternalStorageUri(context).buildUpon().appendPath(papers.getImageId() + ".jpg").build());
            }
        }

        //Enqueue download and save the referenceId
        downloadReference = downloadManager.enqueue(request);

        /* SAVE THE VALUES IN PREFS IF GREYSCALE AND/OR SETWALLPAPER IS ENABLED */
        SharedPreferences preferences = context.getSharedPreferences(String.valueOf(downloadReference), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(String.valueOf(downloadReference), papers.getImageId());

        if (isGreyScale) {
            // IF ONLY GREY IS ENABLED
            editor.putBoolean(ConstantValues.PreferencesKeys.IMAGE_DOWNLOAD_IS_GREY_ENABLED_KEY, isGreyScale);

        } else if (setWallpaper) {
            // IF ONLY SETWALLPAPAER IS ENABLED
            editor.putBoolean(ConstantValues.PreferencesKeys.IMAGE_DOWNLOAD_SET_WALLPAPER_ENABLED_KEY, setWallpaper);

        }

        editor.putString("imageAuthor", papers.getAuthorName());

        editor.putBoolean(ConstantValues.PreferencesKeys.IMAGE_SET_AS_INTENT_KEY, true);

        editor.commit();

        return downloadReference;
    }

    public static long downloadCollection(Context context, Uri imageUri, PaperCollections collections) {
        long downloadReference;

        downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(imageUri);

        //Setting title of request
        request.setTitle(collections.getCollectionTitle());

        //Setting description of request
        request.setDescription("Getting " + collections.getTotalPhotos() + " Wol:Peppers");

        // OnComplete Notification
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        if (UtilityMethods.isDefaultStorage(context)) {
            //Set the local destination for the downloaded file to a path within the application's external files directory
            request.setDestinationInExternalPublicDir(ConstantValues.IMAGE_LOCAL_STORAGE_FOLDER_NAME + "/Collections", collections.getCollectionId() + ".zip");

        } else {
            //Set the local destination for the downloaded file to a path within the application's external files directory
            request.setDestinationUri(UtilityMethods.getExternalStorageUri(context).buildUpon().appendPath("Collections").appendPath(collections.getCollectionId() + ".zip").build());
        }

        //Enqueue download and save the referenceId
        downloadReference = downloadManager.enqueue(request);

        return downloadReference;
    }

    /**
     * AVAILABLE COLUMN NAMES
     * <p>
     * _id, local_filename, mediaprovider_uri, destination, title, description, uri, status, hint, media_type, total_size, last_modified_timestamp, bytes_so_far, allow_write, local_uri, reason
     */
    public static boolean checkIfFileIsAlreadyDownloading(String imageId) {
        if (downloadManager == null) {
            return false;
        }
        DownloadManager.Query query = new DownloadManager.Query();
        //Query the download manager about downloads that have been requested.
        query.setFilterByStatus(DownloadManager.STATUS_RUNNING);


        Cursor cursor = downloadManager.query(query);
        boolean isAlreadyDownloading = false;
        while (cursor.moveToNext()) {
            int colTitleIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE);
            String title = cursor.getString(colTitleIndex);
            if (title.equals(imageId)) {
                isAlreadyDownloading = true;
                break;
            }
        }
        cursor.close();
        return isAlreadyDownloading;
    }

    /**
     * Returns the download ID of requested wallpaper if it is downloading
     *
     * @param imageId - Id of the image
     * @return download ID of the wallpaper
     */
    public static long getDownloadId(String imageId) {
        if (downloadManager == null) {
            return 0;
        }
        DownloadManager.Query query = new DownloadManager.Query();
        //Query the download manager about downloads that have been requested.
        query.setFilterByStatus(DownloadManager.STATUS_RUNNING);


        Cursor cursor = downloadManager.query(query);
        while (cursor.moveToNext()) {
            int colTitleIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE);
            int colIdIndex = cursor.getColumnIndex(DownloadManager.COLUMN_ID);
            String title = cursor.getString(colTitleIndex);
            if (title.equals(imageId)) {
                cursor.close();
                return cursor.getLong(colIdIndex);
            }
        }
        cursor.close();
        return 0;
    }

    /**
     * Returns the downloaded progress of current wallpaper
     *
     * @param downloadId Id of the Download of which progress check is requested
     * @return Floor percentile of progress
     */
    public static int checkDownloadProgress(long downloadId) {
        if (downloadId == 0) {
            return -1;
        }

        DownloadManager.Query query = new DownloadManager.Query();
        //Query the download manager about downloads that have been requested.
        query.setFilterById(downloadId);

        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            return (int) ((bytes_downloaded * 100L) / bytes_total);
        }
        return -1;
    }

    public static int getDownloadStatus(long downloadReferenceId) {
        if (downloadManager == null) {
            return -1;
        }

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadReferenceId);

        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            return cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
        } else {
            return -1;
        }
    }

    /**
     * Checks if the requested image exist on storage or not
     *
     * @param imageId       - name of the file
     * @param isGreyEnabled - is requested files greyscaled?
     * @return - Returns true if file exist and false if it doesn't
     */
    public static boolean checkImageExistOnStorage(Context context, String imageId, boolean isGreyEnabled) {
        if (imageId == null || TextUtils.isEmpty(imageId)) {
            return false;
        }

        if (UtilityMethods.isDefaultStorage(context)) {
            if (isGreyEnabled) {
                File file = new File(ConstantValues.IMAGE_LOCAL_STORAGE_FULL_PATH + imageId + "BW.jpg");
                return file.exists();
            } else {
                File file = new File(ConstantValues.IMAGE_LOCAL_STORAGE_FULL_PATH + imageId + ".jpg");
                return file.exists();
            }
        } else {
            if (isGreyEnabled){
                File file = new File(UtilityMethods.getExternalStoragePath(context) + "/" + imageId + "BW.jpg");
                return file.exists();
            } else {
                File file = new File(UtilityMethods.getExternalStoragePath(context) + "/" + imageId + ".jpg");
                return file.exists();
            }
        }
    }

    /**
     * Usually called by broadcast receiver if further processing on image is to be done.
     *
     * @param context        - Context used to get instance of wallpaper manager
     * @param imageId        - ID of Image/File name too
     * @param isGreyEnabled  - is image to be converted to greyscale?
     * @param isSetWallpaper - should image be set as wallpaper?
     */
    public static void processImageFurther(Context context, String imageId, boolean isGreyEnabled, boolean isSetWallpaper, boolean isShareEnabled, String authorName, boolean isSetAsIntentEnabled) {
        if (!checkImageExistOnStorage(context, imageId, false)) {
            return;
        }

        if (isGreyEnabled && isSetWallpaper) {
            greyScaleImage(context, imageId);
            setWallpaper(context, imageId, isGreyEnabled);
        } else if (isGreyEnabled) {
            greyScaleImage(context, imageId);
        } else if (isSetWallpaper) {
            setWallpaper(context, imageId, isGreyEnabled);
        }

        if (isShareEnabled) {
            shareImage(context, imageId, isGreyEnabled, authorName);
        }

        if (isSetAsIntentEnabled) {
            launchSetAsIntent(context, imageId, isGreyEnabled);
        }
    }

    public static void launchBitmapShareIntent(Context context, Bitmap bitmap, Papers unsplashImage) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
        intent.putExtra(android.content.Intent.EXTRA_TEXT, context.getString(R.string.share_image_text) + unsplashImage.getAuthorName() + "\n" + context.getString(R.string.shared_via_wolpepper));
        intent.putExtra(Intent.EXTRA_STREAM, getBitmapUri(context, bitmap, unsplashImage));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_image_title)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
    }


    private static Uri getBitmapUri(Context inContext, Bitmap inImage, Papers image) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, image.getImageId(), image.getAuthorName());
        return Uri.parse(path);
    }

    /**
     * Share Image Method
     *
     * @param context
     * @param imageId
     * @param isGreyEnabled
     */
    public static void shareImage(Context context, String imageId, boolean isGreyEnabled, String authorName) {

        File file = getImageFile(context, imageId, isGreyEnabled);
        Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
//        if (UtilityMethods.isDefaultStorage(context)) {
//            if (isGreyEnabled) {
//                uri = Uri.fromFile(new File(ConstantValues.IMAGE_LOCAL_STORAGE_FULL_PATH + imageId + "BW.jpg"));
//            } else {
//                uri = Uri.fromFile(new File(ConstantValues.IMAGE_LOCAL_STORAGE_FULL_PATH + imageId + ".jpg"));
//            }
//        } else {
//            if (isGreyEnabled) {
//                uri = UtilityMethods.getExternalStorageUri(context).buildUpon().appendPath(imageId + "BW.jpg").build();
//            } else {
//                uri = UtilityMethods.getExternalStorageUri(context).buildUpon().appendPath(imageId + ".jpg").build();
//            }
//        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, context.getString(R.string.share_image_subject));
        intent.putExtra(android.content.Intent.EXTRA_TEXT, context.getString(R.string.share_image_text) + authorName + "\n" + context.getString(R.string.shared_via_wolpepper));
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_image_title)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
    }

    private static Bitmap convertBitmapToGrey(Bitmap bitmap){
        Canvas c = new Canvas(bitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bitmap, 0, 0, paint);
        return bitmap;
    }

    private static void convertAndSaveBitmapToGreyScale(Context context, Bitmap bitmap, String imageId){
        boolean isDefaultStorage = UtilityMethods.isDefaultStorage(context);

        bitmap = convertBitmapToGrey(bitmap);

        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            File wallpaperDirectory;
            if (isDefaultStorage) {
                wallpaperDirectory = new File(ConstantValues.IMAGE_LOCAL_STORAGE_FULL_PATH + imageId + "BW.jpg");
            } else {
                wallpaperDirectory = new File(UtilityMethods.getExternalStoragePath(context) + "/" + imageId + "BW.jpg");
            }

            // have the object build the directory structure, if needed.
            wallpaperDirectory.createNewFile();
            //write the bytes in file
            FileOutputStream fo = new FileOutputStream(wallpaperDirectory);
            fo.write(bytes.toByteArray());
            // remember close de FileOutput
            fo.close();
            Toast.makeText(context, "WallPaper Saved!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("Exception!", e.getMessage());
        }

    }

    /**
     * Converts the the image to greyscale
     *
     * @param imageId - name of the image file to be greyscaled.
     */
    public static void greyScaleImage(Context context, String imageId) {
        /*This is just getting the bitmap of saved image to pass it further*/
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inMutable = true;
        Bitmap bitmap;
        boolean isDefaultStorage = UtilityMethods.isDefaultStorage(context);

        if (isDefaultStorage) {
            bitmap = BitmapFactory.decodeFile(ConstantValues.IMAGE_LOCAL_STORAGE_FULL_PATH + imageId + ".jpg", options);
        } else {
            String path = UtilityMethods.getExternalStoragePath(context);
            bitmap = BitmapFactory.decodeFile(path + "/" + imageId + ".jpg", options);
        }

        Canvas c = new Canvas(bitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bitmap, 0, 0, paint);

        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bytes);
            File wallpaperDirectory;
            if (isDefaultStorage) {
                wallpaperDirectory = new File(ConstantValues.IMAGE_LOCAL_STORAGE_FULL_PATH + imageId + "BW.jpg");
            } else {
                wallpaperDirectory = new File(UtilityMethods.getExternalStoragePath(context) + "/" + imageId + "BW.jpg");
            }

            // have the object build the directory structure, if needed.
            wallpaperDirectory.createNewFile();
            //write the bytes in file
            FileOutputStream fo = new FileOutputStream(wallpaperDirectory);
            fo.write(bytes.toByteArray());
            // remember close de FileOutput
            fo.close();
        } catch (IOException e) {
            Log.e("Exception!", e.getMessage());
        }
    }

    @SuppressLint("StaticFieldLeak")
    public static void setWallpaper(final Context context, final String imageId, final boolean isGreyEnabled) {
        boolean isQuickSetEnabled = context.getSharedPreferences(context.getString(R.string.quick_set_wallpaper_base_pref), MODE_PRIVATE).getBoolean(context.getString(R.string.quick_set_wallpaper_pref), false);
        if (isQuickSetEnabled) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    publishProgress();
                    setQuickWallpaper(context, imageId, isGreyEnabled);
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
                    Toast.makeText(context, "Wallpaper set.", Toast.LENGTH_LONG).show();
                }
            }.execute();
            return;
        }

        File file;
        if (UtilityMethods.isDefaultStorage(context)) {
            if (isGreyEnabled) {
                file = new File(ConstantValues.IMAGE_LOCAL_STORAGE_FULL_PATH + imageId + "BW.jpg");
            } else {
                file = new File(ConstantValues.IMAGE_LOCAL_STORAGE_FULL_PATH + imageId + ".jpg");
            }
        } else {
            if (isGreyEnabled) {
                file = new File(UtilityMethods.getExternalStoragePath(context) + "/" + imageId + "BW.jpg");
            } else {
                file = new File(UtilityMethods.getExternalStoragePath(context) + "/" + imageId + ".jpg");
            }
        }

        Intent intent = new Intent(context, CropAndSetWallpaperActivity.class);
        intent.putExtra("setWallPaper", file.getAbsolutePath());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void setQuickWallpaper(Context context, String imageId, boolean isGreyEnabled) {
        if (!checkImageExistOnStorage(context, imageId, isGreyEnabled)) {
            return;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inMutable = true;
        Bitmap bitmap;
        if (UtilityMethods.isDefaultStorage(context)) {
            if (isGreyEnabled) {
                bitmap = BitmapFactory.decodeFile(ConstantValues.IMAGE_LOCAL_STORAGE_FULL_PATH + imageId + "BW.jpg", options);
            } else {
                bitmap = BitmapFactory.decodeFile(ConstantValues.IMAGE_LOCAL_STORAGE_FULL_PATH + imageId + ".jpg", options);
            }
        } else {
            if (isGreyEnabled){
                String path = UtilityMethods.getExternalStoragePath(context);
                bitmap = BitmapFactory.decodeFile(path + "/" + imageId + "BW.jpg", options);
            } else {
                String path = UtilityMethods.getExternalStoragePath(context);
                bitmap = BitmapFactory.decodeFile(path + "/" + imageId + ".jpg", options);
            }
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);

        float imageHeight = options.outHeight;
        float imageWidth = options.outWidth;

        int scaledHeight = displayMetrics.heightPixels;
        int scaledWidth = (int) ((imageWidth / imageHeight) * scaledHeight);
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
            wallpaperManager.forgetLoadedWallpaper();
            wallpaperManager.setBitmap(Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true));
            bitmap.recycle();
            wallpaperManager = null;
            displayMetrics = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkRawImageExistOnStorage(Context context, String imageId) {
        File file;
        if (UtilityMethods.isDefaultStorage(context)) {
            file = new File(ConstantValues.IMAGE_LOCAL_RAW_STORAGE_FULL_PATH + imageId + ".jpg");
            return file.exists();
        } else {
            file = new File(UtilityMethods.getExternalStoragePath(context) + "/" + imageId + ".jpg");
            return file.exists();
        }
    }

    private static Uri getFileUri(Context context, String imageId, boolean isGrayEnabled) {
        if (checkImageExistOnStorage(context, imageId, isGrayEnabled)) {
            File file;
            if (UtilityMethods.isDefaultStorage(context)) {
                if (isGrayEnabled) {
                    file = new File(ConstantValues.IMAGE_LOCAL_STORAGE_FULL_PATH + imageId + "BW.jpg");
                } else {
                    file = new File(ConstantValues.IMAGE_LOCAL_STORAGE_FULL_PATH + imageId + ".jpg");
                }
            } else {
                if (isGrayEnabled) {
                    file = new File(UtilityMethods.getExternalStoragePath(context) + "/" + imageId + "BW.jpg");
                } else {
                    file = new File(UtilityMethods.getExternalStoragePath(context) + "/" + imageId + ".jpg");
                }
            }

            return Uri.fromFile(file);
        }
        return null;
    }

    private static File getImageFile(Context context, String imageId, boolean isGrayEnabled) {
        if (checkImageExistOnStorage(context, imageId, isGrayEnabled)) {
            File file;
            if (UtilityMethods.isDefaultStorage(context)) {
                if (isGrayEnabled) {
                    file = new File(ConstantValues.IMAGE_LOCAL_STORAGE_FULL_PATH + imageId + "BW.jpg");
                } else {
                    file = new File(ConstantValues.IMAGE_LOCAL_STORAGE_FULL_PATH + imageId + ".jpg");
                }
            } else {
                if (isGrayEnabled) {
                    file = new File(UtilityMethods.getExternalStoragePath(context) + "/" + imageId + "BW.jpg");
                } else {
                    file = new File(UtilityMethods.getExternalStoragePath(context) + "/" + imageId + ".jpg");
                }
            }

            return file;
        }
        return null;
    }

    public static void launchSetAsIntent(Context context, String imageId, boolean isGrayEnabled) {
//        Uri imageUri = getFileUri(context, imageId, isGrayEnabled);
        File file = getImageFile(context, imageId, isGrayEnabled);
        Uri imageUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
        if (imageUri != null) {
            Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndType(imageUri, "image/jpeg");
            intent.putExtra("mimeType", "image/jpeg");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(intent, "Set as:").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
        }
    }
}
