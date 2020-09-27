package com.news.gamersky.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.news.gamersky.ArticleActivity;
import com.news.gamersky.R;
import com.news.gamersky.customizeview.RoundImageView;
import com.news.gamersky.databean.NewDataBean;
import com.news.gamersky.util.AppUtil;
import com.news.gamersky.util.ReadingProgressUtil;

import java.util.List;

public class NewsRecyclerViewAdapter extends RecyclerView.Adapter {
    private List<NewDataBean> mDataset;
    private Context context;
    private View hasheader;
    private boolean moreData;
    private boolean corner;
    private boolean imageSide;

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
            if(ReadingProgressUtil.getClick(context,mDataset.get(position).id)){
                textView.setTextColor(context.getResources().getColor(R.color.defaultColor));
            }else {
                textView.setTextColor(context.getResources().getColor(R.color.textColorPrimary));
            }
            textView3.setText(mDataset.get(position).sort);
            if (!mDataset.get(position).commentCount.equals("")) {
                textView4.setText(mDataset.get(position).commentCount + "评论");
            }else {
                textView4.setText("");
            }
            if(!corner){
                imageView.setRound(0);
            }
            Glide.with(imageView)
                    .load(mDataset.get(position).imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textView.setTextColor(context.getResources().getColor(R.color.defaultColor));
                    System.out.println("我是第"+position);
                    ReadingProgressUtil.putClick(context,mDataset.get(position).id,true);
                    Intent intent=new Intent(context, ArticleActivity.class);
                    intent.putExtra("new_data",mDataset.get(position));
                    context.startActivity(intent);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AppUtil.rockObjectAnimator(v).start();
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

        public void bindView(int position){

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

    public class HeaderViewHolder extends RecyclerView.ViewHolder{

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public NewsRecyclerViewAdapter(List<NewDataBean> dataset, Context context, View hasHeader) {
        this.mDataset = dataset;
        this.context = context;
        this.hasheader=hasHeader;
        this.moreData=true;
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        corner=sharedPreferences.getBoolean("corner",false);
        imageSide=sharedPreferences.getBoolean("new_image_side",false);
        //Log.i("TAG", "NewsAdapter: "+corner+"\t"+imageSide);
    }

    @Override
    public int getItemViewType(int position){
        int i=0;

        if(hasheader!=null){
            if(position==0) i=2;
            if(position==mDataset.size()+1) i=1;
        }else if(position==mDataset.size()){
            i=1;
        }
        return i;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v=null;
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
            return new HeaderViewHolder(hasheader);
        }
        return new NewsListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        int vt=holder.getItemViewType();
        //Log.i("TAG", "onBindViewHolder: "+vt+"    "+position);
        if(vt==0){
            if(hasheader!=null){
                ((NewsListViewHolder) holder).bindView(position-1);
            }else {
                ((NewsListViewHolder) holder).bindView(position);
            }
        }
        if(vt==1){
            ((FooterViewHolder)holder).bindView(position);
        }

    }

    @Override
    public int getItemCount() {
        if(hasheader!=null){
            return mDataset.size()+2;
        }
        return mDataset.size()+1;
    }

    public void setNoMore(boolean b){
        moreData=!b;
    }

}
