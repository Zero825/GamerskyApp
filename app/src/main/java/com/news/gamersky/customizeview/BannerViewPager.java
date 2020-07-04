package com.news.gamersky.customizeview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class BannerViewPager extends ViewPager {
    float x=0;
    float y=0;
    float y1=0;
    float y2=0;
    float x1=0;
    float x2=0;

    public BannerViewPager(@NonNull Context context) {
        super(context);
    }

    public BannerViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        ViewGroup viewGroup = (ViewGroup) this.getParent();
        System.out.println(ev.toString());
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                y2=ev.getY();
                x2=ev.getX();
                viewGroup.requestDisallowInterceptTouchEvent(true);
                float k=(y2-y1)/(x2-x1);
                float k1=(y2-y)/(x2-x);
                System.out.println(k);
                if(Math.abs(k1)<1||Math.abs(k)<1||Float.isNaN(k)){
                    viewGroup.requestDisallowInterceptTouchEvent(true);
                } else{
                    viewGroup.requestDisallowInterceptTouchEvent(false);
                }
                y1=ev.getY();
                x1=ev.getX();
                break;
            case MotionEvent.ACTION_DOWN:
                y1=ev.getY();
                x1=ev.getX();
                viewGroup.requestDisallowInterceptTouchEvent(true);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }


}
