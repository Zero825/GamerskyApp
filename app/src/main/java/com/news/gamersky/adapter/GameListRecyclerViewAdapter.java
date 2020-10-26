package com.news.gamersky.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.news.gamersky.GameDetailActivity;
import com.news.gamersky.R;
import com.news.gamersky.customizeview.RoundImageView;
import com.news.gamersky.databean.GameListDataBean;
import com.news.gamersky.setting.AppSetting;
import com.news.gamersky.util.AppUtil;

import java.util.ArrayList;

public class GameListRecyclerViewAdapter extends RecyclerView.Adapter {
    private static final String TAG="GameRecyclerViewAdapter";
    private ArrayList<GameListDataBean> dataList;
    private View headerView;
    private Context context;
    private boolean moreData;

    public class GameViewHolder extends RecyclerView.ViewHolder {
        public RoundImageView roundImageView;
        public TextView ratingAverage;
        public TextView title;
        public TextView enTitle;
        public TextView gameMake;
        public TextView officialChinese;
        public TextView listedTime;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            roundImageView=itemView.findViewById(R.id.roundImageView);
            ratingAverage=itemView.findViewById(R.id.ratingAverage);
            title=itemView.findViewById(R.id.title);
            enTitle=itemView.findViewById(R.id.enTitle);
            gameMake=itemView.findViewById(R.id.gameMake);
            officialChinese=itemView.findViewById(R.id.officialChinese);
            listedTime=itemView.findViewById(R.id.listedTime);

        }

        public void bindView(final int position){
            Glide.with(roundImageView)
                    .load(dataList.get(position).picUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(AppSetting.smallRoundCorner)))
                    .into(roundImageView);
            ratingAverage.setText(dataList.get(position).ratingAverage);
            //Log.i(TAG, "bindView: "+dataList.get(position).title+dataList.get(position).ratingAverage);
            if(dataList.get(position).ratingAverage.equals("5.0")){
                ratingAverage.setText("--");
                ratingAverage.setBackgroundColor(context.getResources().getColor(R.color.ratingAverageGood));
            }else if(Float.parseFloat(dataList.get(position).ratingAverage)>=8.0f){
                ratingAverage.setBackgroundColor(context.getResources().getColor(R.color.ratingAverageGood));
            }else if(Float.parseFloat(dataList.get(position).ratingAverage)>=6.0f){
                ratingAverage.setBackgroundColor(context.getResources().getColor(R.color.ratingAverageMid));
            }else {
                ratingAverage.setBackgroundColor(context.getResources().getColor(R.color.ratingAverageBad));
            }
            title.setText(dataList.get(position).title);
            enTitle.setText(dataList.get(position).enTitle);
            gameMake.setText(context.getString(R.string.game_make)+":\t\t"+dataList.get(position).gameMake);
            if(!dataList.get(position).officialChinese.equals("")){
                officialChinese.setText(context.getString(R.string.chinese)+":\t\t"+dataList.get(position).officialChinese);
            }else {
                officialChinese.setText(context.getString(R.string.chinese)+":\t\t--");
            }

            listedTime.setText(context.getString(R.string.listed_time)+":\t\t"+dataList.get(position).listedTime.substring(0,10));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, GameDetailActivity.class);
                    intent.putExtra("gameData", dataList.get(position));
                    //context.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, roundImageView, "gameCover").toBundle());
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

    public class HeaderViewHolder extends RecyclerView.ViewHolder{

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public  class FooterViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;

        public FooterViewHolder(View v) {
            super(v);
            textView=v.findViewById(R.id.textView8);
        }

        public void bindView(int position){

            if(dataList.size()==0){
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

    public GameListRecyclerViewAdapter(ArrayList<GameListDataBean> dataList, View headerView, Context context){
        this.dataList = dataList;
        this.headerView=headerView;
        this.context=context;
        this.moreData=true;
    }

    @Override
    public int getItemViewType(int position){
        int i=0;
        if(position!=0){
            i=1;
        }
        if(position==dataList.size()+1){
            i=2;
        }
        return i;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v=null;
        if(viewType==0){
            return new HeaderViewHolder(headerView);
        }
        if(viewType==1) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_reviews_gameshow, parent, false);
            return new GameViewHolder(v);
        }
        if(viewType==2) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_footer, parent, false);
            return new FooterViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //Log.i(TAG, "onBindViewHolder: "+position);
        int type=holder.getItemViewType();
        if(type==1){
            ((GameViewHolder)holder).bindView(position-1);
        }
        if (type==2){
            ((FooterViewHolder)holder).bindView(position);
        }
    }

    public void setNoMore(boolean b){
        moreData=!b;
        notifyItemChanged(dataList.size()+1);
    }

    @Override
    public int getItemCount() {
        return dataList.size()+2;
    }
}
