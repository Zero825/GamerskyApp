package com.news.gamersky.customizeview;

//https://www.jianshu.com/p/7d4e0a5b86c2

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

public class BottomNavigationBehavior extends CoordinatorLayout.Behavior<View> {

    private boolean isFloatBottomBar;
    private int minDis,maxDis,dy;

    public BottomNavigationBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        isFloatBottomBar=sharedPreferences.getBoolean("float_bottombar",true);
        minDis=3;
        maxDis=9;
        dy=100;
    }
    
    // 垂直滑动
    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes,int type) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed,int type) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        if(isFloatBottomBar) {


           //Log.i("onNestedPreScroll", String.valueOf(target.getScrollY())+"\t"+dy+"\t");
            if(dy>10||dy<-10) {
                //Log.i("TAG", "onNestedPreScroll: "+"fast"+type);
                if (dy > 0 && child.getTranslationY() + maxDis <= child.getHeight()+maxDis) {
                    child.setTranslationY(child.getTranslationY() + maxDis);

                }
                if (dy < 0 && child.getTranslationY() - maxDis >= 0) {
                    child.setTranslationY(child.getTranslationY() - maxDis);
                }
            }else {
                if(type==0) {
                    if (dy > 0 && child.getTranslationY() + minDis <= child.getHeight()) {
                        child.setTranslationY(child.getTranslationY() + minDis);

                    }
                    if (dy < 0 && child.getTranslationY() - minDis >= 0) {
                        child.setTranslationY(child.getTranslationY() - minDis);
                    }
                }
            }
           if(type==0)this.dy=dy;
        }

    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int type) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type);
        if(isFloatBottomBar) {
            float vh = child.getHeight();
            float vty = child.getTranslationY();
            //Log.i("test", "onStopNestedScroll: " + type + "\t" + vh + "\t" + vty + "\t" + dy);
            ObjectAnimator objectAnimator;
            if (dy > 0) {
                objectAnimator = ObjectAnimator.ofFloat(child, "translationY", vty, vh);
            } else {
                objectAnimator = ObjectAnimator.ofFloat(child, "translationY", vty, 0f);
            }
            objectAnimator.setDuration(300);
            if (type == 0 && !objectAnimator.isRunning()) {
                objectAnimator.start();
            }
        }
    }
}
