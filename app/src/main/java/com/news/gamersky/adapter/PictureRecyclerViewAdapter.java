package com.news.gamersky.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.news.gamersky.GameGalleryActivity;
import com.news.gamersky.ImagesBrowserActivity;
import com.news.gamersky.R;
import com.news.gamersky.databean.PictureDataBean;
import com.news.gamersky.setting.AppSetting;
import com.news.gamersky.util.AppUtil;

import java.util.ArrayList;

public class PictureRecyclerViewAdapter extends RecyclerView.Adapter {
    private static final String TAG="PicRecyclerViewAdapter";

    private Context context;
    private ArrayList<PictureDataBean> data;
    private boolean moreData;

    public PictureRecyclerViewAdapter(Context context, ArrayList<PictureDataBean> data) {
        this.context = context;
        this.data = data;
        moreData=true;
    }

    @Override
    public int getItemViewType(int position) {
        int type=0;
        if(position==data.size()){
            type=1;
        }
        return type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType==0){
            view= LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_picture,parent,false);
            return new PictureViewHolder(view);
        }else if(viewType==1){
            view= LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_search_ku_footer,parent,false);
            return new FooterViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int type=holder.getItemViewType();
        //Log.i(TAG, "onBindViewHolder: "+position+"\t"+type);
        if (type==0){
            ((PictureViewHolder)holder).bindView(position);
        }else if(type==1){
            ((FooterViewHolder)holder).bindView(position);
        }
    }

    @Override
    public int getItemCount() {
        return data.size()+1;
    }

    public void setMoreData(boolean moreData) {
        this.moreData = moreData;
    }

    public class PictureViewHolder extends RecyclerView.ViewHolder{

        private int cardViewLeftMargin,cardViewRightMargin,imageViewLeftMargin,imageViewRightMargin;
        private ConstraintLayout msgBar;
        private CardView cardView;
        private ImageView imageView;
        private TextView hot,title;

        public PictureViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView=itemView.findViewById(R.id.cardView);
            imageView=itemView.findViewById(R.id.imageView);
            hot=itemView.findViewById(R.id.hot);
            title=itemView.findViewById(R.id.title);
            msgBar=itemView.findViewById(R.id.msgBar);

            cardView.setRadius(AppSetting.bigRoundCorner);
            ViewGroup.MarginLayoutParams imageViewMarginLayoutParams = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
            ViewGroup.MarginLayoutParams cardViewMarginLayoutParams = (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
            imageViewLeftMargin=imageViewMarginLayoutParams.leftMargin;
            imageViewRightMargin=imageViewMarginLayoutParams.rightMargin;
            cardViewLeftMargin=cardViewMarginLayoutParams.leftMargin;
            cardViewRightMargin=cardViewMarginLayoutParams.rightMargin;
        }

        public void bindView(final int position){
            final PictureDataBean tempData=data.get(position);
            ViewGroup.LayoutParams layoutParams=imageView.getLayoutParams();

            int viewHeight=(int) ((AppUtil.getDisplayWidth(imageView.getContext())/2f
                    -imageViewLeftMargin-imageViewRightMargin
                    -cardViewLeftMargin-cardViewRightMargin)
                    *(tempData.originHeight/(float)tempData.originWidth));
            //Log.i(TAG, "bindView: "+ (tempData.originHeight/(float)tempData.originwidth));
            layoutParams.height= viewHeight;
            imageView.setLayoutParams(layoutParams);
            Glide.with(imageView)
                    .load(tempData.smallPictureUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(AppSetting.bigRoundCorner)))
                    .into(imageView);
            hot.setText(tempData.hot);
            title.setText(tempData.title);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ImagesBrowserActivity.class);
                    intent.putExtra("imagesSrc", tempData.imagesJSON);
                    intent.putExtra("imagePosition", position);
                    //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                    context.startActivity(intent);
                }
            });
            msgBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: "+tempData.itemUrl);
                    Intent intent=new Intent(context, GameGalleryActivity.class);
                    intent.putExtra("src",tempData.itemUrl);
                    intent.putExtra("title",tempData.itemTitle);
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

            if(data.size()==0){
                textView.setVisibility(View.GONE);
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
