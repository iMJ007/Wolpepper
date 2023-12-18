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
public class ScreenTwoFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    public ScreenTwoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_screen_two, container, false);

        return rootView;
    }

    public static ScreenTwoFragment newInstance(int sectionNumber) {
        ScreenTwoFragment fragment = new ScreenTwoFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }
}
