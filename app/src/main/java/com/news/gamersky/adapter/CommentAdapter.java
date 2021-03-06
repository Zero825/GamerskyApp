package com.news.gamersky.adapter;

import android.app.ActivityOptions;
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
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.news.gamersky.ArticleActivity;
import com.news.gamersky.ImagesBrowserActivity;
import com.news.gamersky.R;
import com.news.gamersky.RepliesActivity;
import com.news.gamersky.customizeview.RoundImageView;
import com.news.gamersky.databean.CommentDataBean;
import com.news.gamersky.util.CommentEmojiUtil;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {
    private static final String TAG="CommentAdapter";

    private Context context;
    private ArrayList<CommentDataBean> hotData;
    private ArrayList<CommentDataBean> allData;
    private boolean moreData;
    private boolean unfoldReplies;


    public CommentAdapter(Context context,ArrayList<CommentDataBean> hotData,ArrayList<CommentDataBean> allData){
        this.context=context;
        this.hotData=hotData;
        this.allData=allData;
        moreData=true;
        unfoldReplies= PreferenceManager.getDefaultSharedPreferences(context).getBoolean("unfold_replies",true);
    }



    @Override
    public int getItemViewType(int position){
        int i=2;
        if(position==0){
            i=0;
        }
        if(position==hotData.size()+1){
            i=1;
        }
        if(position==hotData.size()+allData.size()+2){
            i=3;
        }
        return i;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v=null;
        if(viewType==0){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_header, parent, false);
        }
        if(viewType==1){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_header, parent, false);
        }
        if(viewType==2){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_comment, parent, false);
            GridLayout gridlayout=v.findViewById(R.id.imageContainer);
            LinearLayout linearLayout=v.findViewById(R.id.repliesContainer);
            for(int i=0;i<9;i++){
                gridlayout.addView(LayoutInflater.from(context)
                        .inflate(R.layout.gridlayout_comment_image, gridlayout, false));
            }
            if(unfoldReplies) {
                for (int i = 0; i < 5; i++) {
                    linearLayout.addView(LayoutInflater.from(context)
                            .inflate(R.layout.recyclerview_item_reply, linearLayout, false));
                }
            }
        }
        if(viewType==3){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_footer, parent, false);
        }
        return new MyViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        int vt=holder.getItemViewType();
        if(vt==0){
            if(hotData.size()==0){
                holder.textView.setVisibility(View.GONE);
            }
            else {
                holder.textView.setVisibility(View.VISIBLE);
                holder.textView.setText("热门评论");
            }
        }
        if(vt==1){
            if(allData.size()==0){
                holder.textView.setVisibility(View.GONE);
            }
            else {
                holder.textView.setVisibility(View.VISIBLE);
                holder.textView.setText("全部评论");
            }
        }
        if(vt==2){
            int p;
            final CommentDataBean tempData;
            if(position<=hotData.size()){
                p=position-1;
                tempData=hotData.get(p);


            }else {
                p=position-2-hotData.size();
                tempData=allData.get(p);
            }
            holder.textView1.setText(tempData.userName);
            if(tempData.content.equals("")){
                holder.textView2.setVisibility(View.GONE);
            }else {
                holder.textView2.setVisibility(View.VISIBLE);
                holder.textView2.setText(CommentEmojiUtil.getEmojiString(tempData.content));
            }
            if(tempData.repliesCount<=5&&unfoldReplies||tempData.repliesCount==0){
                holder.textView6.setVisibility(View.GONE);
            }else {
                holder.textView6.setVisibility(View.VISIBLE);
                holder.textView6.setText("全部"+tempData.repliesCount+"条回复");
                holder.textView6.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(context, RepliesActivity.class);
                        intent.putExtra("commentId",tempData.commentId);
                        intent.putExtra("clubContentId",tempData.clubContentId);
                        intent.putExtra("comment", tempData);
                        context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation((ArticleActivity)context).toBundle());
                        // startActivity(intent);
                    }
                });
            }
            holder.textView3.setText(tempData.time);
            holder.textView4.setText(tempData.floor);
            holder.textView5.setText(tempData.likeNum);
            if(!tempData.userImage.equals("")) {
                Glide.with(holder.imageView)
                        .load(tempData.userImage)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(holder.imageView);
            }

            if(tempData.images.size()==0){
                holder.gridLayout.setVisibility(View.GONE);
            }else {
                holder.gridLayout.setVisibility(View.VISIBLE);
            }

            for(int i=0;i<tempData.images.size();i++){
                ImageViewHolder imageViewHolder;
                if(holder.gridLayout.getChildAt(i).getTag()==null){
                    imageViewHolder= new ImageViewHolder(holder.gridLayout.getChildAt(i));
                    holder.gridLayout.getChildAt(i).setTag(imageViewHolder);
                }else {
                    imageViewHolder= (ImageViewHolder) holder.gridLayout.getChildAt(i).getTag();
                }
                Glide.with(imageViewHolder.imageView)
                        .load(tempData.images.get(i))
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .centerCrop()
                        .into(imageViewHolder.imageView);
                final int finalI = i;
                imageViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ImagesBrowserActivity.class);
                        intent.putExtra("imagesSrc", tempData.imagesJson);
                        intent.putExtra("imagePosition", finalI);
                        //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                        context.startActivity(intent);
                    }
                });

            }
            for(int i=0;i<holder.gridLayout.getChildCount();i++){
                if(i<tempData.images.size()){
                    holder.gridLayout.getChildAt(i).setVisibility(View.VISIBLE);
                }else {
                    holder.gridLayout.getChildAt(i).setVisibility(View.GONE);
                }
            }


            if(tempData.replies.size()==0||!unfoldReplies){
                holder.linearLayout.setVisibility(View.GONE);
            }else {
                holder.linearLayout.setVisibility(View.VISIBLE);
            }

            if(unfoldReplies) {
                for (int i = 0; i < Math.min(tempData.replies.size(), 5); i++) {
                    CommentDataBean commentDataBean = tempData.replies.get(i);
                    ReplyViewHolder replyViewHolder;
                    if (holder.linearLayout.getChildAt(i).getTag() == null) {
                        replyViewHolder = new ReplyViewHolder(holder.linearLayout.getChildAt(i));
                        holder.linearLayout.getChildAt(i).setTag(replyViewHolder);
                    } else {
                        replyViewHolder = (ReplyViewHolder) holder.linearLayout.getChildAt(i).getTag();
                    }
                    if (!commentDataBean.userImage.equals("")) {
                        Glide.with(replyViewHolder.imageView)
                                .load(commentDataBean.userImage)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                .into(replyViewHolder.imageView);
                    }
                    replyViewHolder.textView1.setText(commentDataBean.userName);
                    if (commentDataBean.objectUserName.equals(tempData.userName)) {
                        replyViewHolder.textView2.setVisibility(View.GONE);
                        replyViewHolder.textView6.setVisibility(View.GONE);
                    } else {
                        replyViewHolder.textView2.setText(commentDataBean.objectUserName);
                        replyViewHolder.textView2.setVisibility(View.VISIBLE);
                        replyViewHolder.textView6.setVisibility(View.VISIBLE);
                    }
                    replyViewHolder.textView3.setText(CommentEmojiUtil.getEmojiString(commentDataBean.content));
                    replyViewHolder.textView4.setText(commentDataBean.time);
                    replyViewHolder.textView5.setText("赞:" + commentDataBean.likeNum);
                }
                for (int i = 0; i < holder.linearLayout.getChildCount(); i++) {
                    if (i < tempData.replies.size()) {
                        holder.linearLayout.getChildAt(i).setVisibility(View.VISIBLE);
                    } else {
                        holder.linearLayout.getChildAt(i).setVisibility(View.GONE);
                    }
                }
            }
        }

        if(vt==3){
            if(allData.size()==0){
                holder.textView.setVisibility(View.GONE);
            }
            else {
                holder.textView.setVisibility(View.VISIBLE);
                if(moreData) {
                    holder.textView.setText("请稍等");
                }else {
                    holder.textView.setText("没有了，没有奇迹了");
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return hotData.size()+allData.size()+3;

    }
    public void setNoMore(boolean b){
        moreData=!b;
    }

    public  class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;
        public TextView textView1;
        public TextView textView2;
        public TextView textView3;
        public TextView textView4;
        public TextView textView5;
        public TextView textView6;
        public RoundImageView imageView;
        public GridLayout gridLayout;

        public LinearLayout linearLayout;

        public MyViewHolder(View v) {
            super(v);
            textView=v.findViewById(R.id.textView8);

            textView1=v.findViewById(R.id.textView9);
            textView2=v.findViewById(R.id.textView11);
            textView3=v.findViewById(R.id.textView12);
            textView4=v.findViewById(R.id.textView13);
            textView5=v.findViewById(R.id.textView14);
            textView6=v.findViewById(R.id.textView18);
            imageView=v.findViewById(R.id.imageView6);

            gridLayout=v.findViewById(R.id.imageContainer);
            linearLayout=v.findViewById(R.id.repliesContainer);


        }
    }

    public class ReplyViewHolder{
        public TextView textView1,textView2,textView3,textView4,textView5,textView6;
        public RoundImageView imageView;

        public ReplyViewHolder(View view) {
            imageView= view.findViewById(R.id.imageView6_2);
            textView1= view.findViewById(R.id.textView9_2);
            textView2= view.findViewById(R.id.textView20_2);
            textView3= view.findViewById(R.id.textView11_2);
            textView4= view.findViewById(R.id.textView12_2);
            textView5= view.findViewById(R.id.textView14_2);
            textView6= view.findViewById(R.id.textView19_2);
        }
    }

    public class ImageViewHolder{
        public RoundImageView imageView;

        public ImageViewHolder(View view){
            imageView=view.findViewById(R.id.imageView7);
        }

    }
}
