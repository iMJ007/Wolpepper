package com.eclectik.wolpepper.fragments.introScreenFragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eclectik.wolpepper.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScreenOneFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    public ScreenOneFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_screen_one, container, false);

            return rootView;
    }

    public static ScreenOneFragment newInstance(int sectionNumber) {
        ScreenOneFragment fragment = new ScreenOneFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

}
