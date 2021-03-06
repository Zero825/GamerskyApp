package com.news.gamersky.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.news.gamersky.ImagesBrowserActivity;
import com.news.gamersky.R;
import com.news.gamersky.RepliesActivity;
import com.news.gamersky.customizeview.RoundImageView;
import com.news.gamersky.databean.CommentDataBean;
import com.news.gamersky.util.CommentEmojiUtil;

import java.util.ArrayList;

public class RepliesAdapter extends RecyclerView.Adapter {
    private CommentDataBean commentData;
    private ArrayList<CommentDataBean> repliesData;
    private Context context;
    private boolean moreData;


    public RepliesAdapter(Context context,CommentDataBean commentData,ArrayList<CommentDataBean> repliesData){
        this.context=context;
        this.commentData=commentData;
        this.repliesData=repliesData;
        moreData=true;
    }

    public  class CommentViewHolder extends RecyclerView.ViewHolder {

        public TextView textView1;
        public TextView textView2;
        public TextView textView3;
        public TextView textView4;
        public TextView textView5;
        public TextView textView6;
        public RoundImageView imageView;
        public GridLayout gridLayout;
        public LinearLayout linearLayout;

        public CommentViewHolder(View v) {
            super(v);
            textView1=v.findViewById(R.id.textView9);
            textView2=v.findViewById(R.id.textView11);
            textView3=v.findViewById(R.id.textView12);
            textView4=v.findViewById(R.id.textView13);
            textView5=v.findViewById(R.id.textView14);
            imageView=v.findViewById(R.id.imageView6);
            gridLayout=v.findViewById(R.id.imageContainer);
            textView6=v.findViewById(R.id.textView18);
            linearLayout=v.findViewById(R.id.repliesContainer);
        }

        public void bindView(int position){
            textView1.setText( commentData.userName);
            if( commentData.content.equals("")){
                textView2.setVisibility(View.GONE);
            }else {
                textView2.setVisibility(View.VISIBLE);
                textView2.setText(CommentEmojiUtil.getEmojiString( commentData.content));
            }
            textView6.setVisibility(View.GONE);

            textView3.setText( commentData.time);
            textView4.setText( commentData.floor);
            textView5.setText( commentData.likeNum);
            if(! commentData.userImage.equals("")) {
                Glide.with( imageView)
                        .load( commentData.userImage)
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into( imageView);
            }
            gridLayout.removeAllViews();
            final ImageView[] imageViews=new ImageView[ commentData.images.size()];
            for (int i=0;i< commentData.images.size();i++){
                View ic = LayoutInflater.from(context)
                        .inflate(R.layout.gridlayout_comment_image, null, false);
                imageViews[i]=ic.findViewById(R.id.imageView7);
                Glide.with(imageViews[i])
                        .load( commentData.images.get(i))
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .centerCrop()
                        .into(imageViews[i]);
                final int finalI = i;
                final CommentDataBean finalTempData= commentData;
                imageViews[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ImagesBrowserActivity.class);
                        intent.putExtra("imagesSrc", finalTempData.imagesJson);
                        intent.putExtra("imagePosition", finalI);
                        //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                        context.startActivity(intent);
                    }
                });
                gridLayout.addView(ic);
            }
            if(commentData.images.size()==0){
                gridLayout.setVisibility(View.GONE);
            }else {
                gridLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    public  class RepliesViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case


        public TextView textView1;
        public TextView textView2;
        public TextView textView3;
        public TextView textView4;
        public TextView textView5;
        public TextView textView6;
        public RoundImageView imageView;

        public RepliesViewHolder(View v) {
            super(v);
            imageView=v.findViewById(R.id.imageView6_2);
            textView1=v.findViewById(R.id.textView9_2);
            textView2=v.findViewById(R.id.textView20_2);
            textView3=v.findViewById(R.id.textView11_2);
            textView4=v.findViewById(R.id.textView12_2);
            textView5=v.findViewById(R.id.textView14_2);
            textView6=v.findViewById(R.id.textView19_2);
        }

        public void bindView(int position){
            int p=position-1;
//                imageView.getLayoutParams().width=100;
////                imageView.getLayoutParams().height=100;
            CommentDataBean tempCommentDataBean=repliesData.get(p);
            if(!tempCommentDataBean.userImage.equals("")) {
                Glide.with(imageView)
                        .load(tempCommentDataBean.userImage)
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(imageView);
            }

            textView1.setText(tempCommentDataBean.userName);
            if(tempCommentDataBean.objectUserName.equals(commentData.userName)){
                textView2.setVisibility(View.GONE);
                textView6.setVisibility(View.GONE);
            }else {
                textView2.setText(tempCommentDataBean.objectUserName);
                textView2.setVisibility(View.VISIBLE);
                textView6.setVisibility(View.VISIBLE);
            }

            textView3.setText(CommentEmojiUtil.getEmojiString(tempCommentDataBean.content));
            textView4.setText(tempCommentDataBean.time);
            textView5.setText(tempCommentDataBean.likeNum);
        }
    }

    public  class FooterViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;

        public FooterViewHolder(View v) {
            super(v);
            textView=v.findViewById(R.id.textView8);
        }

        public void bindView(int position){

            if(repliesData.size()==0){
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

    @Override
    public int getItemViewType(int position){
        int i=1;
        if(position==0){
            i=0;
        }
        if(position==repliesData.size()+1){
            i=2;
        }
        return i;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v=null;
        if(viewType==0){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_comment, parent, false);
            return new RepliesAdapter.CommentViewHolder(v);
        }
        if(viewType==1){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_item_reply, parent, false);
            return new RepliesAdapter.RepliesViewHolder(v);
        }
        if(viewType==2){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_footer, parent, false);
            return new RepliesAdapter.FooterViewHolder(v);
        }

        return new RepliesAdapter.RepliesViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int vt=holder.getItemViewType();
        if(vt==0){
            ((CommentViewHolder)holder).bindView(position);
        }
        if(vt==1){
            ((RepliesViewHolder)holder).bindView(position);
        }

        if(vt==2){
            ((FooterViewHolder)holder).bindView(position);
        }

    }

    @Override
    public int getItemCount() {
        return repliesData.size()+2;

    }
    public void setNoMore(boolean b){
        moreData=!b;
    }
}
