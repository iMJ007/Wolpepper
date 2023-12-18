package com.eclectik.wolpepper.fragments.MainActivityFragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.adapters.CategoriesAdapter;
import com.eclectik.wolpepper.dataStructures.PaperCategory;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesFragment extends Fragment {

    private View rootView;

    private ArrayList<PaperCategory> categoriesList = new ArrayList<>();

    private RecyclerView recyclerView;

    private CategoriesAdapter adapter;

    private BottomNavigationView bottomBar;

    public CategoriesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        getCategoryList();

        bottomBar = getActivity().findViewById(R.id.bottomBar);

        recyclerView = rootView.findViewById(R.id.recycler_view);

        rootView.findViewById(R.id.more_loader).setVisibility(View.GONE);
        rootView.findViewById(R.id.square_loading).setVisibility(View.GONE);

        RecyclerView.LayoutManager mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(mLayoutManager);

        adapter = new CategoriesAdapter();

        adapter.updateDataSet(categoriesList);

        recyclerView.setAdapter(adapter);

        recyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                if(velocityY > 0){
                    bottomBar.animate().translationY(bottomBar.getHeight()).start();
                } else {
                    bottomBar.animate().translationY(0).start();
                }
                return false;
            }
        });

        return rootView;
    }

    private void getCategoryList(){
        InputStream in_s = getResources().openRawResource(R.raw.walls_catg);

        byte[] b = new byte[0];
        try {
            b = new byte[in_s.available()];
            in_s.read(b);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            JSONArray jsonArray = new JSONArray(new String(b));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String imageUrl = jsonObject.getString("image");
                String categoryTitle = jsonObject.getString("title");
                categoriesList.add(new PaperCategory(categoryTitle, imageUrl));
            }
            Collections.shuffle(categoriesList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden && adapter != null){
            Collections.shuffle(categoriesList);
            adapter.updateDataSet(categoriesList);
            adapter.notifyDataSetChanged();
        }
        super.onHiddenChanged(hidden);
    }

    public void scrollListToTop(){
        if (adapter != null && adapter.getItemCount() > 0) {
            recyclerView.smoothScrollToPosition(0);
        }
    }
}
