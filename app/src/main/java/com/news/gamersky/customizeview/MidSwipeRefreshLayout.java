package com.news.gamersky.customizeview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MidSwipeRefreshLayout extends SwipeRefreshLayout {
    float x=0;
    float y=0;
    float y1=0;
    float y2=0;
    float x1=0;
    float x2=0;

    public MidSwipeRefreshLayout(@NonNull Context context) {
        super(context);
    }

    public MidSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                y2=ev.getY();
                x2=ev.getX();
                float k=(y2-y1)/(x2-x1);
                float k1=(y2-y)/(x2-x);
                //System.out.println(k);
                if(Math.abs(k)<0.25&&Math.abs(k1)<0.25){
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else{
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                y1=ev.getY();
                x1=ev.getX();
                break;
            case MotionEvent.ACTION_DOWN:
                y1=ev.getY();
                x1=ev.getX();
                y=ev.getY();
                x=ev.getX();
                break;
        }
        return  false;
    }
}
