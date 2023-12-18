package com.eclectik.wolpepper.activities.settingsActivities;

import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_LOCAL_STORAGE_FOLDER_NAME;
import static com.eclectik.wolpepper.utils.ConstantValues.IMAGE_LOCAL_STORAGE_FULL_PATH;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.os.EnvironmentCompat;
import androidx.core.widget.CompoundButtonCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.eclectik.wolpepper.R;

import java.io.File;

@SuppressLint("ApplySharedPref")
public class StorageSettingsActivity extends AppCompatActivity {

    private CheckBox defaultStorageSwitch;
    private SharedPreferences storagePreference;
    private TextView storagePathTextView;
    private TextView currentStorageTypeTextView;
//    private Uri customLocationUri;
    private String customStoragePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_settings);

        initializeViews();

        storagePreference = getSharedPreferences(getString(R.string.storage_preference_base_key), MODE_PRIVATE);

        boolean isDefaultStorage = storagePreference.getBoolean(getString(R.string.storage_type_pref_key), true);

        defaultStorageSwitch.setChecked(isDefaultStorage);

        currentStorageTypeTextView.setText(isDefaultStorage ? getString(R.string.current_using_default_storage) : getString(R.string.current_using_custom_storage));

        if (checkIfMediaExist()){
            customStoragePath = getExternalMediaPath();
            findViewById(R.id.custom_storage_location_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.warning_layout).setVisibility(View.GONE);
        } else {
            findViewById(R.id.warning_layout).setVisibility(View.VISIBLE);
            if (!isDefaultStorage){
                resetStorageToDefault();
            }
        }

        storagePathTextView.setText(isDefaultStorage ? getString(R.string.storage_path_string, IMAGE_LOCAL_STORAGE_FULL_PATH) : getString(R.string.storage_path_string, customStoragePath));

        defaultStorageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    storagePathTextView.setText(getString(R.string.storage_path_string, IMAGE_LOCAL_STORAGE_FULL_PATH));
                    currentStorageTypeTextView.setText(getString(R.string.current_using_default_storage));
                } else {
                    storagePathTextView.setText(getString(R.string.storage_path_string, customStoragePath));
                    currentStorageTypeTextView.setText(getString(R.string.current_using_custom_storage));
                    checkIfMediaExist();
                }
            }
        });

    }

    private void resetStorageToDefault(){
        storagePreference.edit().clear().commit();
    }

    @SuppressLint("NewApi")
    private boolean checkIfMediaExist() {
        File[] fileArray = getExternalFilesDirs(IMAGE_LOCAL_STORAGE_FOLDER_NAME);
        if (fileArray.length > 1 && fileArray[fileArray.length - 1] != null){
            boolean isMediaMounted = EnvironmentCompat.getStorageState(fileArray[1].getAbsoluteFile()).equals(Environment.MEDIA_MOUNTED);
            if (isMediaMounted) {
                defaultStorageSwitch.setEnabled(true);
                return true;
            } else {
                defaultStorageSwitch.setEnabled(false);
                return false;
            }
        } else {
            defaultStorageSwitch.setEnabled(false);
            return false;
        }

    }

    @SuppressLint("NewApi")
    private String getExternalMediaPath(){
        File[] fileArray = getExternalFilesDirs(IMAGE_LOCAL_STORAGE_FOLDER_NAME);
        if (fileArray.length > 1 && fileArray[fileArray.length - 1] != null){
            return fileArray[fileArray.length - 1].getAbsolutePath();
        }
        return fileArray[0].getAbsolutePath();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_settings_menu, menu);
        return true;
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                storagePreference.edit()
                        .putBoolean(getString(R.string.storage_type_pref_key), defaultStorageSwitch.isChecked()).commit();

                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new MaterialDialog.Builder(StorageSettingsActivity.this)
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

    private void initializeViews() {
        storagePathTextView = findViewById(R.id.storage_path_text_view);
        currentStorageTypeTextView = findViewById(R.id.currently_using_storage_type_desc_text_view);
        defaultStorageSwitch = findViewById(R.id.default_storage_switch);
        CompoundButtonCompat.setButtonTintList(defaultStorageSwitch, ContextCompat.getColorStateList(StorageSettingsActivity.this, R.color.storage_check_box_color_state_list));
    }
}
