package com.news.gamersky;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

import com.bumptech.glide.Glide;
import com.github.piasy.biv.BigImageViewer;
import com.google.android.material.snackbar.Snackbar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings,new SettingsFragment() ,"setting")
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);


    }



    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.setting, rootKey);
            final Preference preference=findPreference("manual_clear_cache");
            SwitchPreference switchPreference=findPreference("swpie_back");
            final SeekBarPreference seekBarPreference=findPreference("swipe_back_distance");
            final SeekBarPreference seekBarPreference1=findPreference("swipe_sides_sensitivity");
            final SwitchPreference switchPreference1=findPreference("no_bottombar");
            final SwitchPreference switchPreference2=findPreference("float_bottombar");
            if(switchPreference.isChecked()){
                seekBarPreference.setVisible(true);
                seekBarPreference1.setVisible(true);
            }
            if(!switchPreference1.isChecked()){
                switchPreference2.setVisible(true);
            }
            switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((boolean)newValue){
                        seekBarPreference.setVisible(true);
                        seekBarPreference1.setVisible(true);
                    }else {
                        seekBarPreference.setVisible(false);
                        seekBarPreference1.setVisible(false);
                    }
                    return true;
                }
            });
            switchPreference1.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((boolean)newValue){
                        switchPreference2.setVisible(false);
                        switchPreference2.setChecked(false);
                    }else {
                        switchPreference2.setVisible(true);
                        switchPreference2.setChecked(true);
                    }
                    return true;
                }
            });
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    clearGlideDiskCache();
                    return false;
                }
            });
        }
        public void clearGlideDiskCache(){

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Glide.get(SettingsFragment.this.getContext()).clearDiskCache();
                    BigImageViewer.imageLoader().cancelAll();
                    SettingsFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(SettingsFragment.this.getView(), "缓存已清除", Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            }).start();

        }
    }

}