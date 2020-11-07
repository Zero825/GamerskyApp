package com.news.gamersky;

import android.app.Application;
import android.content.Context;

import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideImageLoader;


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
