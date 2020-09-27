package com.news.gamersky.customizeview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatImageView;

import com.news.gamersky.R;

public class RoundImageView extends AppCompatImageView {
    private static final String TAG="RoundImageView";

    private Paint mPaint;
    private Xfermode mXfermode;
    private boolean isCircle;
    private int round;
    private boolean isAntiAlias;

    public RoundImageView(Context context) {
        super(context);
        init();
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView);
        isCircle=typedArray.getBoolean(R.styleable.RoundImageView_isCircle,false);
        isAntiAlias=typedArray.getBoolean(R.styleable.RoundImageView_isAntiAlias,true);
        round=typedArray.getDimensionPixelSize(R.styleable.RoundImageView_round,0);
        typedArray.recycle();
        init();
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView);
        isCircle=typedArray.getBoolean(R.styleable.RoundImageView_isCircle,false);
        isAntiAlias=typedArray.getBoolean(R.styleable.RoundImageView_isAntiAlias,true);
        round=typedArray.getInt(R.styleable.RoundImageView_round,0);
        typedArray.recycle();
        init();
    }

    public void init(){
        mPaint=new Paint();
        mPaint.setAntiAlias(true);
        mXfermode=new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

    }


    @Override
    protected void onDraw(Canvas canvas) {

        if(!isCircle&&round==0){
            super.onDraw(canvas);
            return;
        }

        Drawable drawable = getDrawable();
        if(drawable==null){
            return;
        }

        int width=getWidth();
        int height=getHeight();

        if(!isAntiAlias) {
            Path path = new Path();
            RectF rect = new RectF(0, 0, width, height);
            if (isCircle) {
                path.addRoundRect(rect, width / 2.0f, height / 2.0f, Path.Direction.CW);
            } else {
                path.addRoundRect(rect, round, round, Path.Direction.CW);
            }
            canvas.clipPath(path);
            super.onDraw(canvas);
        }else {
            Bitmap bitmap = Bitmap.createBitmap(width,
                        height, Bitmap.Config.ARGB_8888);
            RectF rectF = new RectF(0, 0, width, height);
            Canvas bitmapCanvas = new Canvas(bitmap);

            drawable.setBounds(0, 0, width, height);
            drawable.draw(bitmapCanvas);

            int sc = canvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);

            if (isCircle) {
                canvas.drawCircle(width / 2.0f, height / 2.0f, Math.min(width / 2.0f, height / 2.0f), mPaint);
            } else {
                canvas.drawRoundRect(rectF, round, round, mPaint);
            }

            mPaint.setXfermode(mXfermode);
            //Log.i(TAG, "onDraw: "+"isCircle:"+isCircle+"round:"+round+"width:"+width+"height:"+height);
            canvas.drawBitmap(bitmap, 0, 0, mPaint);

            mPaint.setXfermode(null);
            canvas.restoreToCount(sc);
        }

    }


    public boolean isCircle() {
        return isCircle;
    }

    public void setCircle(boolean circle) {
        isCircle = circle;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

}
