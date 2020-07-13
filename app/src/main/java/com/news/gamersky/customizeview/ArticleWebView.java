package com.news.gamersky.customizeview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.webkit.WebView;

public class ArticleWebView extends WebView {
    float x=0;
    float y=0;
    float y1=0;
    float y2=0;
    float x1=0;
    float x2=0;

    public ArticleWebView(Context context) {
        super(context);
    }

    public ArticleWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ArticleWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ArticleWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
                if(x2-x1<0&&Math.abs(k)<0.25&&Math.abs(k1)<0.25){
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
        return  super.dispatchTouchEvent(ev);
    }

}
