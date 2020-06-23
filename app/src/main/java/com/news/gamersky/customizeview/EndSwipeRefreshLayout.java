package com.news.gamersky.customizeview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class EndSwipeRefreshLayout extends SwipeRefreshLayout {
    float x=0;
    float y=0;
    float y1=0;
    float y2=0;
    float x1=0;
    float x2=0;

    public EndSwipeRefreshLayout(@NonNull Context context) {
        super(context);
    }

    public EndSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
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
                float k1=(y2-y)/(x2-x);
                //System.out.println(k);
                if(x2-x1>0&&Math.abs(k)<0.2&&Math.abs(k1)<0.2){
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
                break;
        }
        return  super.dispatchTouchEvent(ev);
    }
}
