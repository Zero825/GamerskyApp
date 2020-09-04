package com.news.gamersky.customizeview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.news.gamersky.R;

public class RoundImageView extends AppCompatImageView {
    private Paint mPaint;
    private Xfermode mXfermode;
    private boolean isCircle;
    private int round;

    public RoundImageView(Context context) {
        super(context);
        init();
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView);
        isCircle=typedArray.getBoolean(R.styleable.RoundImageView_isCircle,false);
        round=typedArray.getDimensionPixelSize(R.styleable.RoundImageView_round,0);
        typedArray.recycle();
        init();
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView);
        isCircle=typedArray.getBoolean(R.styleable.RoundImageView_isCircle,false);
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
        Bitmap bitmap = Bitmap.createBitmap(getWidth(),
                getHeight(), Bitmap.Config.ARGB_8888);
        Canvas bitmapCanvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(bitmapCanvas);

        int sc=canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);

        if(isCircle){
            canvas.drawCircle(width / 2, height / 2, Math.min(width / 2, height / 2), mPaint);
        }else{
            canvas.drawRoundRect(new RectF(0, 0, width,height), round, round,
                    mPaint);
        }


        mPaint.setXfermode(mXfermode);
        //Log.i("TAG", "onDraw: "+"isCircle:"+isCircle+"round:"+round+"width:"+width+"height:"+height);
        canvas.drawBitmap(bitmap,0,0,mPaint);

        mPaint.setXfermode(null);
        canvas.restoreToCount(sc);



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
