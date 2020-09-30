package com.news.gamersky;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.news.gamersky.setting.AppSetting;
import com.news.gamersky.util.NightModeUtil;


public class ThisApp extends Application{

    private static final String TAG="ThisApp";
    private static Context context;

    @Override
    public void onCreate()
    {
        super.onCreate();
        BigImageViewer.initialize(GlideImageLoader.with(this));
        context = getApplicationContext();
    }

    public static Context getContext(){
        return context;
    }


}
