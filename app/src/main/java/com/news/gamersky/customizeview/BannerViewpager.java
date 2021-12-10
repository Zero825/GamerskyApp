package com.news.gamersky.customizeview;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

public class BannerViewpager extends ViewPager {
    private final static String TAG="BannerViewpager";

    //轮播时间
    private final static int DELAYTIME = 10000;
    //动画间隔时间
    private final static int SCROLLER_DURATION=300;

    private CustomizeSpeedScroller scroller;
    private Timer timer;

    public BannerViewpager(@NonNull Context context) {
        super(context);
        init();
    }

    public BannerViewpager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        startListen();
    }

    public void init(){
        Log.i(TAG, "init: BannerViewpager");

        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            scroller = new CustomizeSpeedScroller(this.getContext(),
                    new DecelerateInterpolator());
            field.set(this, scroller);
            scroller.setmDuration(SCROLLER_DURATION);
        }catch (NoSuchFieldException | IllegalAccessException e){
            e.printStackTrace();
        }

        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                nextItem();
            }
        },DELAYTIME);
    }

    public void startListen(){
        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                timer.cancel();
                timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        nextItem();
                    }
                },DELAYTIME);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void nextItem(){
        int nextNum=getCurrentItem()+1;
        if(getCurrentItem()==getChildCount()-1){
            nextNum=0;
            scroller.setmDuration(getChildCount()*SCROLLER_DURATION/2);
        }
        setCurrentItem(nextNum);
        scroller.setmDuration(SCROLLER_DURATION);
    }

    public void setIndicatorView(final IndicatorView indicatorView){
        setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                indicatorView.setIndicatorSize(getChildCount());
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                indicatorView.setIndicatorSize(getChildCount());
            }
        });
        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                indicatorView.setNowPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }



    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            Field mFirstLayout = ViewPager.class.getDeclaredField("mFirstLayout");
            mFirstLayout.setAccessible(true);
            mFirstLayout.set(this, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (((Activity) getContext()).isFinishing()) {
            super.onDetachedFromWindow();
        }
    }


}
