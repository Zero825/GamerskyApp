package com.news.gamersky.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.news.gamersky.ArticleActivity;
import com.news.gamersky.R;
import com.news.gamersky.customizeview.RoundImageView;
import com.news.gamersky.databean.NewDataBean;
import com.news.gamersky.setting.AppSetting;
import com.news.gamersky.util.ReadingProgressUtil;

import java.util.List;

public class NewsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<NewDataBean> mDataset;
    private Context context;
    private View headerView;
    private boolean moreData;
    private boolean imageSide;



    public NewsRecyclerViewAdapter(List<NewDataBean> dataset, Context context, View headerView) {
        this.mDataset = dataset;
        this.context = context;
        this.headerView=headerView;
        this.moreData=true;
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        imageSide=sharedPreferences.getBoolean("new_image_side",false);
    }

    @Override
    public int getItemViewType(int position){
        int i=0;

        if(headerView!=null){
            if(position==0) i=2;
            if(position==mDataset.size()+1) i=1;
        }else if(position==mDataset.size()){
            i=1;
        }
        return i;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        //Log.i("TAG", "onCreateViewHolder: "+viewType);
        if(viewType==0){
            if(imageSide){
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_new_left, parent, false);
            }else {
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_new, parent, false);
            }
            return new NewsListViewHolder(v);
        }
        if(viewType==1){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_footer, parent, false);
            return new FooterViewHolder(v);
        }
        if(viewType==2){
            return new HeaderViewHolder(headerView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        int vt=holder.getItemViewType();
        //Log.i("TAG", "onBindViewHolder: "+vt+"    "+position);
        if(vt==0){
            if(headerView!=null){
                ((NewsListViewHolder) holder).bindView(position-1);
            }else {
                ((NewsListViewHolder) holder).bindView(position);
            }
        }
        if(vt==1){
            ((FooterViewHolder)holder).bindView();
        }

    }

    @Override
    public int getItemCount() {
        if(headerView!=null){
            return mDataset.size()+2;
        }
        return mDataset.size()+1;
    }

    public void setNoMore(boolean b){
        moreData=!b;
    }

    public  class NewsListViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public TextView textView2;
        public TextView textView3;
        public TextView textView4;
        public RoundImageView imageView;

        public NewsListViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.textView4);
            textView2 = v.findViewById(R.id.textView5);
            textView3 = v.findViewById(R.id.textView10);
            textView4=v.findViewById(R.id.textView17);
            imageView=v.findViewById(R.id.imageView3);
        }

        public void bindView(final int position){

            textView2.setText(mDataset.get(position).date);
            textView.setText(Html.fromHtml(mDataset.get(position).title));
            if(ReadingProgressUtil.getNewsClick(context,mDataset.get(position).id)){
                String id = mDataset.get(position).id;
                textView.setTextColor(context.getColor(R.color.defaultColor));
            }else {
                textView.setTextColor(context.getColor(R.color.textColorPrimary));
            }
            textView3.setText(mDataset.get(position).sort);
            if (!mDataset.get(position).commentCount.equals("")) {
                textView4.setText(mDataset.get(position).commentCount + context.getString(R.string.comment));
            }else {
                textView4.setText("");
            }
            Glide.with(imageView)
                    .load(mDataset.get(position).imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(AppSetting.smallRoundCorner)))
                    .into(imageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(PreferenceManager.getDefaultSharedPreferences(context)
                            .getBoolean("save_article_click",true)){
                        textView.setTextColor(context.getColor(R.color.defaultColor));
                        String id2 = mDataset.get(position).id;
                        ReadingProgressUtil.putNewsClick(context,mDataset.get(position).id,true);
                    }
                    Intent intent=new Intent(context, ArticleActivity.class);
                    intent.putExtra("new_data",mDataset.get(position));
                    context.startActivity(intent);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //AppUtil.rockObjectAnimator(v).start();
                    return true;
                }
            });
        }
    }

    public  class FooterViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;

        public FooterViewHolder(View v) {
            super(v);
            textView=v.findViewById(R.id.textView8);
        }

        public void bindView(){

            if(mDataset.size()==0){
                textView.setVisibility(View.GONE);
            }
            else {
                textView.setVisibility(View.VISIBLE);
                if(moreData) {
                    textView.setText(context.getResources().getString(R.string.wait));
                }else {
                    textView.setText(context.getResources().getString(R.string.no_more));
                }
            }
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder{

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
