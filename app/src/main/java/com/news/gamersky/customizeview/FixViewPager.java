package com.news.gamersky.customizeview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Field;

public class FixViewPager extends ViewPager {
    private final static String TAG="FixViewPager";

    public FixViewPager(@NonNull Context context) {
        super(context);
        init();
    }

    public FixViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        }catch (IllegalArgumentException ex){
            ex.printStackTrace();
        }
       return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            return super.onTouchEvent(event);
        }catch (IllegalArgumentException ex){
            ex.printStackTrace();
        }
        return false;
    }

    public void init(){
        Log.i(TAG, "init: FixViewPager");
        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            CustomizeSpeedScroller scroller = new CustomizeSpeedScroller(this.getContext(),new DecelerateInterpolator());
            field.set(this, scroller);
            scroller.setmDuration(300);
        }catch (NoSuchFieldException | IllegalAccessException e){
            e.printStackTrace();
        }

    }




}
