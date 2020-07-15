package com.news.gamersky.customizeview;

//https://www.jianshu.com/p/7d4e0a5b86c2

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.preference.PreferenceManager;

public class BottomNavigationBehavior extends CoordinatorLayout.Behavior<View> {

    private ObjectAnimator outAnimator, inAnimator;
    private boolean isFloatBottomBar;

    public BottomNavigationBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        isFloatBottomBar=sharedPreferences.getBoolean("float_bottombar",true);
    }

    // 垂直滑动
    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes,int type) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed,int type) {

        if(isFloatBottomBar) {
            if (dy > 0) {// 上滑隐藏
                if (outAnimator == null) {
                    outAnimator = ObjectAnimator.ofFloat(child, "translationY", 0, child.getHeight());
                    outAnimator.setDuration(300);
                }
                if (!outAnimator.isRunning() && child.getTranslationY() <= 0) {
                    outAnimator.start();
                }
            } else if (dy < 0) {// 下滑显示
                if (inAnimator == null) {
                    inAnimator = ObjectAnimator.ofFloat(child, "translationY", child.getHeight(), 0);
                    inAnimator.setDuration(300);
                }
                if (!inAnimator.isRunning() && child.getTranslationY() >= child.getHeight()) {
                    inAnimator.start();
                }
            }
        }
    }
}
