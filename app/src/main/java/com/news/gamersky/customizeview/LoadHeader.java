package com.news.gamersky.customizeview;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;



import androidx.annotation.NonNull;

import com.news.gamersky.R;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshKernel;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;


public class LoadHeader extends LinearLayout implements RefreshHeader {
    private ImageView mProgressView;
    private AnimationDrawable animationDrawable;//刷新动画视图

    public LoadHeader(Context context) {
        super(context);
        initview(context);
    }

    public void initview(Context context){
        setGravity(Gravity.CENTER_HORIZONTAL);
        setOrientation(VERTICAL);
        mProgressView = new ImageView(context);
        animationDrawable=new AnimationDrawable();
        mProgressView.setImageResource(R.drawable.load_animation);
        animationDrawable=(AnimationDrawable)mProgressView.getDrawable();
       // mProgressView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        addView(mProgressView,282,200);
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @NonNull
    @Override
    public SpinnerStyle getSpinnerStyle() {
        return SpinnerStyle.FixedBehind;
    }

    @Override
    public void setPrimaryColors(int... colors) {

    }

    @Override
    public void onInitialized(@NonNull RefreshKernel kernel, int height, int maxDragHeight) {

    }

    @Override
    public void onMoving(boolean isDragging, float percent, int offset, int height, int maxDragHeight) {

    }

    @Override
    public void onReleased(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {

    }

    @Override
    public void onStartAnimator(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {
        animationDrawable.start();//开始动画
    }

    @Override
    public int onFinish(@NonNull RefreshLayout refreshLayout, boolean success) {
        animationDrawable.stop();//停止动画
        if (success){
        } else {
        }
        return 500;//延迟500毫秒之后再弹回
    }

    @Override
    public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {

    }

    @Override
    public boolean isSupportHorizontalDrag() {
        return false;
    }

    @Override
    public void onStateChanged(@NonNull RefreshLayout refreshLayout, @NonNull RefreshState oldState, @NonNull RefreshState newState) {

    }
}
