package com.news.gamersky.adapter;

import android.app.WallpaperColors;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.news.gamersky.ArticleActivity;
import com.news.gamersky.R;
import com.news.gamersky.databean.NewDataBean;
import com.news.gamersky.setting.AppSetting;
import com.news.gamersky.util.AppUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class  BannerAdapter extends PagerAdapter {
    private final ArrayList<NewDataBean> myData;


    public BannerAdapter(ArrayList<NewDataBean> myData){
        this.myData=myData;
    }
    @Override
    public int getCount() {
        return myData.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }

    @NotNull
    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View v = LayoutInflater.from(container.getContext())
                .inflate(R.layout.viewpager_banner, container, false);
        final TextView textView=v.findViewById(R.id.textView);
        final ImageView imageView=v.findViewById(R.id.imageView);
        final ImageView textBg=v.findViewById(R.id.textBg);
        textView.setText(myData.get(position).title);
        if(!AppSetting.isRoundCorner){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                imageView.setForeground(v.getContext().getDrawable(R.drawable.pressed_drawable_roundimage));
            }
        }
        Glide.with(imageView)
                .asBitmap()
                .load(myData.get(position).imageUrl)
                .transition(BitmapTransitionOptions.withCrossFade())
                .apply(new RequestOptions()
                        .transform(new MultiTransformation
                                (new CenterCrop(), new RoundedCorners(AppSetting.bigRoundCorner)))
                )
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(final Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                            Thread getColorThread=new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    final Color primaryColor = WallpaperColors.fromBitmap(resource).getPrimaryColor();
                                    final int bg=Color.argb(127f,primaryColor.red(),primaryColor.green(),primaryColor.blue());
                                    textBg.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Glide.with(textBg)
                                                    .load(new ColorDrawable(bg))
                                                    .transition(DrawableTransitionOptions.withCrossFade())
                                                    .into(textBg);
                                            if(AppUtil.isDark(primaryColor.toArgb())){
                                                textView.setTextColor(textView.getContext().getColor(R.color.textColorBanner));
                                            }else {
                                                textView.setTextColor(textView.getContext().getColor(R.color.darkBackground));
                                            }
                                        }
                                    });
                                }
                            });
                            getColorThread.start();

                        }
                        return false;
                    }
                })
                .into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(v.getContext(), ArticleActivity.class);
                intent.putExtra("new_data",myData.get(position));
                v.getContext().startActivity(intent);
            }
        });
        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NotNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(@NotNull Object object) {
        return POSITION_NONE;
    }
}
