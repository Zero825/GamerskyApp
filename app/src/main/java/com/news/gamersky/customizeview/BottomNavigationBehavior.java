package com.news.gamersky.customizeview;

//https://www.jianshu.com/p/7d4e0a5b86c2

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.preference.PreferenceManager;

public class BottomNavigationBehavior extends CoordinatorLayout.Behavior<View> {

    private boolean isFloatBottomBar;
    private ObjectAnimator showAnimator;
    private ObjectAnimator hideAnimator;

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
        if(nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL&&type==0){
            showAnimator=ObjectAnimator.ofFloat(child,"translationY",child.getHeight(),0);
            showAnimator.setDuration(300);
            hideAnimator=ObjectAnimator.ofFloat(child,"translationY",0,child.getHeight());
            hideAnimator.setDuration(300);
        }
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL&&type==0;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed,int type) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        //Log.i("TAG", "onNestedPreScroll: "+dy+"\t");
        if(isFloatBottomBar
                &&!showAnimator.isRunning()&&!hideAnimator.isRunning()) {
            if(dy>10&&child.getTranslationY()==0){
                hideAnimator.start();
            }
            if(dy<-10&&child.getTranslationY()==child.getHeight()){
                showAnimator.start();
            }

        }

    }
}
