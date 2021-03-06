package com.news.gamersky.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.news.gamersky.ArticleActivity;
import com.news.gamersky.GameDetailActivity;
import com.news.gamersky.R;
import com.news.gamersky.SearchActivity;
import com.news.gamersky.databean.GameListDataBean;
import com.news.gamersky.databean.NewDataBean;
import com.news.gamersky.setting.AppSetting;
import com.news.gamersky.util.AppUtil;
import com.news.gamersky.util.ReadingProgressUtil;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    ArrayList<NewDataBean> mData;
    private String key;
    private boolean moreData;
    private String category;

    public SearchAdapter(Context context,ArrayList<NewDataBean> mData, String category){
        this.context=context;
        this.mData=mData;
        moreData=true;
        this.category=category;
    }

    public void setKey(String key){
        this.key=key;
    }

    public void setNoMore(boolean b){
        moreData=!b;
    }

    @Override
    public int getItemViewType(int position){

        if(position==mData.size()){
            return 0;
        }else if(category.equals("ku")){
            return 2;
        }else {
            return 1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v=null;
        if(viewType==0){
            if(category.equals("ku")){
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_search_ku_footer, parent, false);
            }else {
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_footer, parent, false);
            }
            return new SearchAdapter.FooterViewHolder(v);
        }
        if(viewType==1){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_search, parent, false);
            return new SearchAdapter.SearchViewHolder(v);
        }
        if(viewType==2){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_search_ku, parent, false);
            return new SearchAdapter.KuSearchViewHolder(v);
        }
        return new SearchAdapter.SearchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int vt=holder.getItemViewType();
        if(vt==0){
            ((FooterViewHolder)holder).bindView(position);
        }
        if(vt==1){
            ((SearchViewHolder)holder).bindView(position);
        }
        if(vt==2){
            ((KuSearchViewHolder)holder).bindView(position);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size()+1;
    }

    public  class SearchViewHolder extends RecyclerView.ViewHolder {

        public TextView textView1;
        public TextView textView2;
        public TextView textView3;
        public TextView textView4;

        public SearchViewHolder(View v) {
            super(v);
            textView1=v.findViewById(R.id.textView19);
            textView2=v.findViewById(R.id.textView21);
            textView3=v.findViewById(R.id.textView22);
            textView4=v.findViewById(R.id.textView23);
        }

        public void bindView(final int position){
            textView1.setText(mData.get(position).sort);
            textView2.setText(AppUtil.keyTextColor(Html.fromHtml(mData.get(position).title).toString(),key, Color.parseColor("#F01A21")));
            textView3.setText(Html.fromHtml(mData.get(position).content));
            textView4.setText(mData.get(position).date);
            if(ReadingProgressUtil.getSearchClick(context,mData.get(position).id)){
                textView2.setTextColor(context.getColor(R.color.defaultColor));
                textView3.setTextColor(context.getColor(R.color.defaultColor));
            }else {
                textView2.setTextColor(context.getColor(R.color.textColorPrimary));
                textView3.setTextColor(context.getColor(R.color.textColorPrimary));
            }
            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(PreferenceManager.getDefaultSharedPreferences(context)
                            .getBoolean("save_article_click",true)){
                        textView2.setTextColor(context.getResources().getColor(R.color.defaultColor));
                        textView3.setTextColor(context.getResources().getColor(R.color.defaultColor));
                        ReadingProgressUtil.putSearchClick(context,mData.get(position).id,true);
                    }
                    NewDataBean newData=mData.get(position);
                    newData.title=Html.fromHtml(mData.get(position).title).toString();
                    if(category.equals("news")) {
                        newData.src = "https://wap.gamersky.com/news/Content-" + mData.get(position).id + ".html";
                    }
                    if(category.equals("handbook")){
                        newData.src = "https://wap.gamersky.com/gl/Content-" + mData.get(position).id + ".html";
                    }
                    Intent intent=new Intent(context, ArticleActivity.class);
                    intent.putExtra("new_data",mData.get(position));
                    context.startActivity(intent);
                }
            });
        }
    }

    public class KuSearchViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView textView;

        public KuSearchViewHolder(View v) {
            super(v);
            textView=v.findViewById(R.id.textView29);
            imageView=v.findViewById(R.id.imageView19);
        }

        public void bindView(final int position){
            textView.setText(mData.get(position).title);
            Glide.with(imageView)
                    .load(mData.get(position).imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(AppSetting.smallRoundCorner)))
                    .into(imageView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, GameDetailActivity.class);
                    GameListDataBean gameData=new GameListDataBean();
                    gameData.title=mData.get(position).title;
                    gameData.picUrl=mData.get(position).imageUrl;
                    gameData.itemUrl=mData.get(position).src;
                    intent.putExtra("gameData", gameData);
                    //startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(SearchActivity.this, imageView, "gameCover").toBundle());
                    context.startActivity(intent);
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

            if(mData.size()==0){
                //textView.setVisibility(View.GONE);
            }
            else {
                textView.setVisibility(View.VISIBLE);
                if(moreData) {
                    textView.setText("请稍等");
                }else {
                    textView.setText("没有了，没有奇迹了");
                }
            }
        }
    }
}
