package com.eclectik.wolpepper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eclectik.wolpepper.adapters.MuzeiAvailableListAdapter;
import com.eclectik.wolpepper.dataStructures.MuzeiList;
import com.eclectik.wolpepper.utils.UtilityMethods;

import java.util.ArrayList;

@SuppressLint("ApplySharedPref")
public class MuzeiSettingsActivity extends AppCompatActivity {

//    private EditText refreshIntervelEditText;

//    private TextInputLayout editTextErrorLayout;

    private RecyclerView recyclerView;

    private ArrayList<MuzeiList> muzeiListArrayList = new ArrayList<>();

    private MuzeiAvailableListAdapter adapter = new MuzeiAvailableListAdapter();

    private TextWatcher textWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_muzei_settings);

        setTitle("Muzei Settings");

        recyclerView = findViewById(R.id.muzei_list_recycle_view);

//        refreshIntervelEditText = (EditText) findViewById(R.id.refresh_interval);

//        editTextErrorLayout = findViewById(R.id.refresh_interval_input_error_indicator);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);

        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.setAdapter(adapter);

        getAllList();
//        setCurrentInterval();
//        setUpIntervalInput();

    }

//    private void setCurrentInterval(){
//        SharedPreferences refreshIntervalPreferences = getSharedPreferences(getString(R.string.muzei_refresh_interval_pref_base_key), MODE_PRIVATE);
//        int savedInterval = refreshIntervalPreferences.getInt(getString(R.string.muzei_refresh_interval_pref_key), 3);
////        refreshIntervelEditText.setText(String.valueOf(savedInterval));
//    }

    private void getAllList() {
        muzeiListArrayList.clear();
        for (String listName : UtilityMethods.getAllMuzeiListNames(this)) {
            muzeiListArrayList.add(UtilityMethods.getMuzeiList(this, listName));
        }
        adapter.updateDataSet(muzeiListArrayList);
    }

    @Override
    protected void onResume() {
        if (adapter != null){
            getAllList();
        }
        super.onResume();
    }


    @Override
    protected void onDestroy() {
//        refreshIntervelEditText.removeTextChangedListener(textWatcher);
        textWatcher = null;
        System.gc();
        super.onDestroy();
    }

//    /**
//     * Method To set Up Muzei Refresh Interval Input Layout
//     */
//    private void setUpIntervalInput() {
//        createTextChangeListener();
////        refreshIntervelEditText.addTextChangedListener(textWatcher);
//    }

//    /**
//     * Initialize Text Watcher Object
//     */
//    private void createTextChangeListener() {
//        textWatcher = new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                try {
//
//                    // Throw exception if input is is empty or not an integer
//                    int value = Integer.parseInt(s.toString());
//
//                    // Update Preferences only if input is greater than or equal to 1.
//                    if (value >= 1) {
//                        SharedPreferences preferences = getSharedPreferences(getString(R.string.muzei_refresh_interval_pref_base_key), MODE_PRIVATE);
//                        preferences.edit().putInt(getString(R.string.muzei_refresh_interval_pref_key), value).commit();
//
//                    }
//
//                } catch (NumberFormatException e) {
//
//                    if (!TextUtils.isEmpty(s)) {
//                        refreshIntervelEditText.setError("Enter only Integers");
//                    }
//
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        };
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && data.hasExtra("result")) {
            int position = data.getIntExtra("result", -1);
            if (position != -1) {
                adapter.removeItem(position);
            }
        }
        if (adapter != null){
            adapter.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
