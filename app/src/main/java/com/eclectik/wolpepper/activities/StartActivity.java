package com.eclectik.wolpepper.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.fragments.introScreenFragments.ScreenOneFragment;
import com.eclectik.wolpepper.fragments.introScreenFragments.ScreenTwoFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class StartActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private ImageView dot1,dot2;
    private FloatingActionButton done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start);

        /*One Time code for Intro Fragments*/
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(!getIntent().hasExtra("settings")) {
            if(!preferences.getBoolean("firstTimeStart", false)) {
                // run your one time code
                preferences.edit().putBoolean("firstTimeStart", true).apply();

            } else {
                startActivity(new Intent(StartActivity.this,MainActivity.class));
                finish();
            }
        }

        mViewPager = findViewById(R.id.container);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        dot1= findViewById(R.id.dot_oe);
        dot2= findViewById(R.id.dot_two);
        done= findViewById(R.id.done_fab);

        mViewPager.setAdapter(mSectionsPagerAdapter);


        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, MainActivity.class));
                finish();
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {

                    dot1.setImageResource(R.drawable.selected_drawable);
                    dot2.setImageResource(R.drawable.unselected_drawable);
                    done.setVisibility(View.GONE);
                } else
                    {
                    dot1.setImageResource(R.drawable.unselected_drawable);
                    dot2.setImageResource(R.drawable.selected_drawable);
                    done.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new ScreenOneFragment();
            } else {
                return new ScreenTwoFragment();
            }
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";

            }
            return null;
        }
    }
}

