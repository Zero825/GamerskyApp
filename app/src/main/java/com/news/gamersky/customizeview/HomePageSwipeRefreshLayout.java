package com.news.gamersky.customizeview;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class HomePageSwipeRefreshLayout extends SwipeRefreshLayout {
    float x=0;
    float y=0;
    float y1=0;
    float y2=0;
    float x1=0;
    float x2=0;
    float stc;

    public HomePageSwipeRefreshLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public HomePageSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init(){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        stc=sharedPreferences.getInt("swipe_sides_sensitivity",35)*0.01f;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){
        ViewGroup viewGroup = (ViewGroup) this.getParent();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                y2=ev.getY();
                x2=ev.getX();
                float k=(y2-y1)/(x2-x1);
                float k1=(y2-y)/(x2-x);
                //System.out.println(k);
                if(x2-x1<0&&x2-x<0&&Math.abs(k)<stc&&Math.abs(k1)<stc){
                    viewGroup.requestDisallowInterceptTouchEvent(false);
                } else{
                    viewGroup.requestDisallowInterceptTouchEvent(true);
                }
                y1=ev.getY();
                x1=ev.getX();
                break;
            case MotionEvent.ACTION_DOWN:
                y1=ev.getY();
                x1=ev.getX();
                y=ev.getY();
                x=ev.getX();
                viewGroup.requestDisallowInterceptTouchEvent(true);
                break;
        }
        return  false;
    }


}
