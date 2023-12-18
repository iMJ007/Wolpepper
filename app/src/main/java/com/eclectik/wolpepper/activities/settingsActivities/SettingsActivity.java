package com.eclectik.wolpepper.activities.settingsActivities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.eclectik.wolpepper.MuzeiSettingsActivity;
import com.eclectik.wolpepper.R;
import com.eclectik.wolpepper.utils.UtilityMethods;

import java.util.ArrayList;

@SuppressLint("ApplySharedPref")
public class SettingsActivity extends AppCompatActivity {

    private LinearLayout muzeiClickLayout, shareWolpepperLayout, joinCommunityLayout, feedbackLayout, otherAppsLayout;
    private SharedPreferences preferences;
    private ArrayList<String> listOfImageTypes = new ArrayList<>();
    private TextView currentDefaultTv;
    private String currentDefault;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setElevation(0);

        setUpDefaultImageThings();

        muzeiClickLayout = findViewById(R.id.muzei_settings);
        otherAppsLayout = findViewById(R.id.other_apps);
        feedbackLayout = findViewById(R.id.feedback);
        joinCommunityLayout = findViewById(R.id.community);
        shareWolpepperLayout = findViewById(R.id.share_app);

        muzeiClickLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, MuzeiSettingsActivity.class));
            }
        });

        otherAppsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=6428398715878309895&hl=en")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=6428398715878309895&hl=en")));
                }
            }
        });

        feedbackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent send = new Intent(Intent.ACTION_SENDTO);
                String uriText = "mailto:" + Uri.encode("eclectik.devs@gmail.com") +
                        "?subject=" + Uri.encode("Wol:Pepper Feedback") +
                        "&body=" + Uri.encode("Enter your valuable feedback");
                Uri uri = Uri.parse(uriText);

                send.setData(uri);
                startActivity(Intent.createChooser(send, "Send mail..."));
            }
        });

        joinCommunityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UtilityMethods.openGPlus(SettingsActivity.this, "113751339756670851051");
            }
        });
        shareWolpepperLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, "Wolpepper");
                    String sAux = getString(R.string.let_me_recommend_app);
                    sAux = sAux + "http://play.google.com/store/apps/details?id=" + getPackageName();
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "Choose one"));
                } catch (Exception e) {
                    //e.toString();
                }
            }
        });
    }

    private void setUpDefaultImageThings(){
        listOfImageTypes.add(getString(R.string.always_ask_image_type));
        listOfImageTypes.add(getString(R.string.jpeg_image_type));
        listOfImageTypes.add(getString(R.string.raw_image_type));

        preferences = getSharedPreferences(getString(R.string.default_image_format_base_pref_key), MODE_PRIVATE);
        currentDefaultTv = findViewById(R.id.current_format_text_view);
        updateCurrentDefaultTextViewContent();
    }

    private void updateCurrentDefaultTextViewContent(){
        currentDefault = preferences.getString(getString(R.string.default_image_format_pref_key), getString(R.string.always_ask_image_type));
        currentDefaultTv.setText(getString(R.string.current_default_string) + currentDefault);
    }

    

    public void openPreviewSettings(View view){
        startActivity(new Intent(this, PreviewQualityActivity.class));
    }

    public void showDefaultImageFormatDialog(View view){
        int selectedIndex = 0;
        if (currentDefault.equals(getString(R.string.raw_image_type))){
            selectedIndex = 2;
        } else if (currentDefault.equals(getString(R.string.jpeg_image_type))){
            selectedIndex = 1;
        } else if (currentDefault.equals(getString(R.string.always_ask_image_type))){
            selectedIndex = 0;
        }

        new MaterialDialog.Builder(SettingsActivity.this)
                .title(getString(R.string.choose_default_image_format_dialog_title))
                .items(listOfImageTypes)
                .typeface("SpaceMonoBold.ttf", "SpaceMonoRegular.ttf")
                .itemsCallbackSingleChoice(selectedIndex, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        preferences.edit().putString(getString(R.string.default_image_format_pref_key), text.toString()).commit();
                        updateCurrentDefaultTextViewContent();
                        return true;
                    }
                })
                .positiveText(getString(R.string.select_string))
                .show();
    }
}
