package com.news.gamersky;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;

import com.billy.android.swipe.SmartSwipe;
import com.billy.android.swipe.SmartSwipeBack;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideImageLoader;

import static com.billy.android.swipe.SwipeConsumer.DIRECTION_LEFT;


public class ThisApp extends Application{

    private static final String TAG="ThisApp";
    private static Context context;

    @Override
    public void onCreate()
    {
        super.onCreate();
        BigImageViewer.initialize(GlideImageLoader.with(this));
        SmartSwipeBack.activitySlidingBack(this, new SmartSwipeBack.ActivitySwipeBackFilter() {
            @Override
            public boolean onFilter(Activity activity) {
                if(activity instanceof RepliesActivity){
                    return false;
                }
                return !(activity instanceof MainActivity);
            }
        }, SmartSwipe.dp2px(20, this), Color.TRANSPARENT,Color.TRANSPARENT,0,0.5f,DIRECTION_LEFT);
        context = getApplicationContext();
    }

    public static Context getContext(){
        return context;
    }


}
