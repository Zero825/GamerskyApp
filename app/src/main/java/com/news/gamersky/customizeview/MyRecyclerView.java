package com.news.gamersky.customizeview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class MyRecyclerView extends RecyclerView {
    int y1=0;
    int y2=0;
    int x1=0;
    int x2=0;

    public MyRecyclerView(@NonNull Context context) {
        super(context);
    }

    public MyRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        ViewGroup viewGroup = (ViewGroup) this.getParent();
        //System.out.println(ev.toString());
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                x2=(int)ev.getX();
                y2= (int) ev.getY();
                viewGroup.requestDisallowInterceptTouchEvent(true);
                if(Math.abs(y1-y2)<15&&(x2-x1>0)){
                    viewGroup.requestDisallowInterceptTouchEvent(false);
                }
                else{
                    viewGroup.requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_DOWN:
                y1=(int)ev.getY();
                x1=(int)ev.getX();
                viewGroup.requestDisallowInterceptTouchEvent(true);
                break;
        }
        return  super.dispatchTouchEvent(ev);
    }


//    @Override
//    public boolean fling(int velocityX,int velocityY) {
//       return super.fling(velocityX*10/12,velocityY*10/12);
//    }
}
