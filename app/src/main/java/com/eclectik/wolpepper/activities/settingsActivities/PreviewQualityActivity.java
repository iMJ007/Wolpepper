package com.eclectik.wolpepper.activities.settingsActivities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.eclectik.wolpepper.GlideApp;
import com.eclectik.wolpepper.R;
import com.tapadoo.alerter.Alerter;

import java.math.BigInteger;

public class PreviewQualityActivity extends AppCompatActivity {

    ImageView previewImage;

    TextView previewQualityTextView;
    SeekBar previewQualitySeekBar;

    int scaledHeight, scaledWidth;

    double percent = 20;

    SharedPreferences preferences;

    DisplayMetrics displayMetrics = new DisplayMetrics();

    boolean isImageProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_quality);

        preferences = getSharedPreferences(getString(R.string.preview_quality_base_pref_key), MODE_PRIVATE);

        percent = preferences.getFloat(getString(R.string.preview_quality_percent_pref_key), 45);

        previewImage = findViewById(R.id.preview_settings_wallpaper);


        previewQualitySeekBar = findViewById(R.id.preview_quality_seek_bar);
        previewQualityTextView = findViewById(R.id.quality_percent_text);

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        setUpSeekBar();
        new UpdatePreviewAsync().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_settings_menu, menu);
        return true;
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                preferences.edit().putFloat(getString(R.string.preview_quality_percent_pref_key), (float) percent).commit();
                Toast.makeText(PreviewQualityActivity.this, getString(R.string.restart_app_to_see_effects), Toast.LENGTH_LONG).show();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new MaterialDialog.Builder(PreviewQualityActivity.this)
                .typeface(ResourcesCompat.getFont(this, R.font.spacemono_bold), ResourcesCompat.getFont(this, R.font.spacemono_regular))
                .content(getString(R.string.exit_without_saving))
                .positiveText(getString(R.string.discard_changes))
                .negativeText(getString(R.string.keep_editing))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        finish();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void setUpSeekBar(){
        previewQualitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                percent = progress;
                previewQualityTextView.setText(getString(R.string.quality_percent_display) + progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isImageProcessing){
                    return;
                }
                if (percent >= 5) {
                    scaledWidth = (int) (displayMetrics.widthPixels * (percent / 100));
                    new UpdatePreviewAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    Alerter.create(PreviewQualityActivity.this)
                            .setTextTypeface(ResourcesCompat.getFont(PreviewQualityActivity.this, R.font.spacemono_regular))
                            .setTitleTypeface(ResourcesCompat.getFont(PreviewQualityActivity.this, R.font.spacemono_bold))
                            .setTextAppearance(R.style.alertBody)
                            .setTitleAppearance(R.style.alertTitle)
                            .setTitle(getString(R.string.preview_quality_error_title)).setText(getString(R.string.preview_quality_error_desc))
                            .setBackgroundColorRes(R.color.alert_default_error_background)
                            .show();
                    previewQualitySeekBar.setProgress(5);
                    scaledWidth = (int) (displayMetrics.widthPixels * (5.0 / 100));
                    new UpdatePreviewAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });

        // For first run
        previewQualitySeekBar.setProgress((int) percent);
        scaledWidth = (int) (displayMetrics.widthPixels * (percent / 100));
        new UpdatePreviewAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class UpdatePreviewAsync extends AsyncTask<Void, Void, Bitmap>{

        @Override
        protected void onPreExecute() {
            findViewById(R.id.updating_image_progressbar).setVisibility(View.VISIBLE);
            previewQualitySeekBar.setEnabled(false);
            isImageProcessing = true;
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                String previewImageLink = "https://images.unsplash.com/photo-1523731407965-2430cd12f5e4?ixlib=rb-1.2.1&q=80&fm=jpg&crop=entropy&cs=tinysrgb&w=" + displayMetrics.widthPixels + "&fit=max&ixid=eyJhcHBfaWQiOjExNDIwfQ";

                Bitmap bitmap = GlideApp.with(PreviewQualityActivity.this).asBitmap().load(previewImageLink).submit().get();

                getScaledDimension(bitmap);
                return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            isImageProcessing = false;
            previewQualitySeekBar.setEnabled(true);
            findViewById(R.id.updating_image_progressbar).setVisibility(View.GONE);
            if (bitmap != null) {
                previewImage.setImageBitmap(bitmap);
                previewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }
    }


    private void getScaledDimension(Bitmap bitmap) {
        BigInteger h = BigInteger.valueOf(bitmap.getHeight());

        BigInteger w = BigInteger.valueOf(bitmap.getWidth());

        scaledHeight = (int) ((h.doubleValue() / w.doubleValue()) * scaledWidth);

    }
}
