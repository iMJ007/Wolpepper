package com.eclectik.wolpepper.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.eclectik.wolpepper.fragments.ProfileActivityFragments.ProfileCollectionsFragment;
import com.eclectik.wolpepper.fragments.ProfileActivityFragments.ProfileLikesFragment;
import com.eclectik.wolpepper.fragments.ProfileActivityFragments.ProfilePhotosFragment;

/**
 * Created by MJ on 8/22/2016.
 */
public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

    private String tabTitles[] = new String[] { "Photos", "Likes","Collections" };
    public SimpleFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0){
            return new ProfilePhotosFragment();
        }
        else if (position == 1){
            return new ProfileLikesFragment();
        }
        else{
            return new ProfileCollectionsFragment();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public int getCount() {
        return 3;
    }
}
