package com.news.gamersky.customizeview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.news.gamersky.R;
import com.news.gamersky.util.AppUtil;

import java.util.ArrayList;
import java.util.Collections;

public class StatisticView extends View {
    private final static String TAG="StatisticView";
    private Paint paint;
    private ArrayList<Float> data;
    private int color;

    public StatisticView(Context context) {
        super(context);
        init();
    }

    public StatisticView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StatisticView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public StatisticView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void init(){
        paint=new Paint();
        paint.setAntiAlias(true);
        color=getResources().getColor(R.color.colorAccent);
        data=new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(data.size()>0) {
            int width = getWidth();
            int height = getHeight();
            float maxLength = width * Collections.max(data) * 0.7f;
            int perItemHeight = height / data.size();
            int textSize = AppUtil.sp2px(getContext(), 14f);
            float textSpace = 10f;
            float pillarSpace = 15f;
            float radius = 10f;
            float perPillarHeight = 25f;
            paint.setTextSize(textSize);
            for (int i = 0; i < data.size(); i++) {
                paint.setColor(Color.WHITE);
                String title = String.valueOf(data.size() - i);
                canvas.drawText(title, 0, textSize + perItemHeight * i, paint);
                paint.setColor(color);
                float titleWidth = paint.measureText(title) + textSpace;
                canvas.drawRoundRect(titleWidth, perItemHeight * i + pillarSpace, titleWidth + data.get(i) * maxLength, perPillarHeight + perItemHeight * i + pillarSpace, radius, radius, paint);
                paint.setColor(Color.WHITE);
                String percentage = String.format("%.1f", data.get(i) * 100) + "%";
                canvas.drawText(percentage, titleWidth + data.get(i) * maxLength + textSpace, textSize + perItemHeight * i, paint);
            }
        }

    }

    public void setData(ArrayList<Float> data) {
        this.data = data;
        invalidate();
    }

    public void setColor(int color) {
        this.color = color;
        invalidate();
    }
}
