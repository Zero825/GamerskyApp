package com.news.gamersky.customizeview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class IndicatorView extends View {
    private final static String TAG="IndicatorView";
    private Paint paint;
    private int indicatorSize;
    private int nowPosition;

    public IndicatorView(Context context) {
        super(context);
        init();
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void init(){
        paint=new Paint();
        paint.setAntiAlias(true);
        indicatorSize=0;
        nowPosition=0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width=getWidth();
        int height=getHeight();
        int startX=width/2-(indicatorSize*30/2);
        int startY=height/2;
//        Log.i(TAG, "onDraw: "+width);
//        Log.i(TAG, "onDraw: "+height);
//        Log.i(TAG, "onDraw: "+startX);
//        Log.i(TAG, "onDraw: "+startY);
        paint.setColor(Color.GRAY);
        if(indicatorSize>=2) {
            for (int i = 0; i < indicatorSize; i++) {
                if (i == nowPosition) {
                    paint.setColor(Color.WHITE);
                }
                canvas.drawCircle(startX + i * 30, startY, 8, paint);
                if (i == nowPosition) {
                    paint.setColor(Color.GRAY);
                }
            }
        }
    }

    public void setIndicatorSize(int indicatorSize) {
        this.indicatorSize = indicatorSize;
        invalidate();
    }

    public void setNowPosition(int nowPosition) {
        this.nowPosition = nowPosition;
        invalidate();
    }
}
