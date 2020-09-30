package com.news.gamersky.customizeview;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.core.view.NestedScrollingChild;
import androidx.preference.PreferenceManager;

public class ArticleWebView extends WebView {

    public ArticleWebView(Context context) {
        super(context);
        init();
    }

    public ArticleWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArticleWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ArticleWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    /**
     * 使WebView不可横向滚动
     * */

    @Override
    public boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY,
                                int scrollRangeX, int scrollRangeY, int maxOverScrollX,
                                int maxOverScrollY, boolean isTouchEvent) {

        return super.overScrollBy(0,deltaY,0,scrollY,0,scrollRangeY,0,maxOverScrollY,isTouchEvent);
    }

    public void init(){
    }

}
