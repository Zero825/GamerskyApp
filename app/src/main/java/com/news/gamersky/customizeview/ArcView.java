package com.news.gamersky.customizeview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.news.gamersky.R;


public class ArcView extends View {
    private Paint paint;
    private static final String TAG="ArcView";

    public ArcView(Context context) {
        super(context);
        init();
    }

    public ArcView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ArcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void init(){
        paint=new Paint();
        paint.setAntiAlias(true);
        paint.setColor(getContext().getResources().getColor(R.color.colorPrimary));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width=getWidth();
        int height=getHeight();

        Log.i(TAG, "onDraw: "+width+"\t"+height);

        Path path = new Path();
        path.moveTo(0, 0);
        path.quadTo(width/2, height*2, width, 0);
        path.lineTo(width,height);
        path.lineTo(0,height);
        path.lineTo(0,0);
        path.close();
        canvas.drawPath(path, paint);
    }
}
