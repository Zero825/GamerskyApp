package com.news.gamersky.customizeview;

//https://www.jianshu.com/p/7d4e0a5b86c2

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

public class BottomNavigationBehavior extends CoordinatorLayout.Behavior<View> {

    private boolean isFloatBottomBar;
    private int minDis,maxDis;

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
                Log.i("TAG", "onNestedPreScroll: "+"fast"+type);
                if (dy > 0 && child.getTranslationY() + maxDis <= child.getHeight()) {
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

        }

    }
}
