package com.news.gamersky.customizeview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class MyViewPager extends ViewPager {
    float y1=0;
    float y2=0;
    float x1=0;
    float x2=0;

    public MyViewPager(@NonNull Context context) {
        super(context);
    }

    public MyViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        ViewGroup viewGroup = (ViewGroup) this.getParent();
        //System.out.println(ev.toString());
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                y2=ev.getY();
                x2=ev.getX();
                viewGroup.requestDisallowInterceptTouchEvent(true);
                float k=(y2-y1)/(x2-x1);
                //System.out.println(k);
                if(Math.abs(k)<1){
                    viewGroup.requestDisallowInterceptTouchEvent(true);
                } else{
                    viewGroup.requestDisallowInterceptTouchEvent(false);
                }
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
