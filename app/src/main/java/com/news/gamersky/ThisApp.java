package com.news.gamersky;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.news.gamersky.util.NightModeUtil;


public class ThisApp extends Application{

    private static Context context;

    @Override
    public void onCreate()
    {
        super.onCreate();
        BigImageViewer.initialize(GlideImageLoader.with(this));
        context = getApplicationContext();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        NightModeUtil.changeNightMode(Integer.parseInt(sharedPreferences.getString("night_mode","2")));
    }

    public static Context getContext(){
        return context;
    }
}
