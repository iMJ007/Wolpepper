package com.eclectik.wolpepper.activities;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.eclectik.wolpepper.R;
import com.github.chrisbanes.photoview.PhotoView;

public class PhotoViewActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);

        String wallpaperURL= getIntent().getStringExtra("wallpaper");

        PhotoView photoView = findViewById(R.id.photo_view);


        RequestBuilder<Drawable> thumbnailRequest = Glide.with(this)
                .load(wallpaperURL.concat("&w=200"));

        // setup Glide request without the into() method
//        DrawableRequestBuilder<String> thumbnailRequest = Glide
//                .with(this)
//                .load(wallpaperURL.concat("&w=200"));

        final ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 10);
        valueAnimator.setDuration(10000);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int dotCount = 0;
            int previousValue = -1;
            String text = "Loading full image";
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if ((int) animation.getAnimatedValue() != previousValue) {
                    if (dotCount < 3) {
                        ((TextView) findViewById(R.id.loading_full_image_text_view)).append(".");
                        dotCount++;
                    } else {
                        ((TextView) findViewById(R.id.loading_full_image_text_view)).setText(text);
                        dotCount = 0;
                    }
                    previousValue = (int) animation.getAnimatedValue();
                }
            }
        });
        valueAnimator.start();

        Glide.with(PhotoViewActivity.this).load(wallpaperURL).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                YoYo.with(Techniques.FadeOutDown)
                        .duration(200)
                        .onEnd(new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                valueAnimator.removeAllUpdateListeners();
                                valueAnimator.end();
                            }
                        })
                        .playOn(findViewById(R.id.more_loader));
                return false;
            }
        }).thumbnail(thumbnailRequest).transition(new DrawableTransitionOptions().crossFade(1000)).into(photoView);
//        Glide.with(PhotoViewActivity.this).load(wallpaperURL).listener(new RequestListener<String, GlideDrawable>() {
//            @Override
//            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
//                return false;
//            }
//
//            @Override
//            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                YoYo.with(Techniques.FadeOutDown)
//                        .duration(200)
//                        .onEnd(new YoYo.AnimatorCallback() {
//                            @Override
//                            public void call(Animator animator) {
//                                valueAnimator.removeAllUpdateListeners();
//                                valueAnimator.end();
//                            }
//                        })
//                        .playOn(findViewById(R.id.more_loader));
//                return false;
//            }
//        }).thumbnail(thumbnailRequest).into(photoView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
