package com.news.gamersky.adapter;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.news.gamersky.R;
import com.news.gamersky.customizeview.RoundImageView;
import com.news.gamersky.databean.CommentDataBean;

import java.util.ArrayList;

public class GameCommentRecyclerViewAdapter extends RecyclerView.Adapter {
    private final static String TAG="GCRecyclerViewAdapter";
    private Context context;
    private ArrayList<CommentDataBean> data;
    private boolean moreData;

    public GameCommentRecyclerViewAdapter(Context context,ArrayList<CommentDataBean> data){
        this.context=context;
        this.data=data;
    }

    @Override
    public int getItemViewType(int position) {
        if(position==data.size()){
            return 1;
        }else {
            return 0;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if(viewType==0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_comment_game, parent, false);
            return new GameCommentViewHolder(view);
        }
        if(viewType==1){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_footer, parent, false);
            return new FooterViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int type=holder.getItemViewType();
        if(type==0){
            ((GameCommentViewHolder)holder).bindView(position);
        }
        if(type==1){
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

    public class GameCommentViewHolder extends RecyclerView.ViewHolder{
        private RoundImageView userImage;
        private TextView userName;
        private TextView msg;
        private TextView star;
        private TextView time;
        private TextView content;
        private TextView like;
        private TextView dislike;
        private LinearLayout repliesContainer;

        public GameCommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage=itemView.findViewById(R.id.userImage);
            userName=itemView.findViewById(R.id.userName);
            msg=itemView.findViewById(R.id.msg);
            star=itemView.findViewById(R.id.star);
            time=itemView.findViewById(R.id.time);
            content=itemView.findViewById(R.id.content);
            like=itemView.findViewById(R.id.like);
            dislike=itemView.findViewById(R.id.dislike);
            repliesContainer=itemView.findViewById(R.id.repliesContainer);
        }

        public void bindView(int position){
            CommentDataBean comment=data.get(position);
            Glide.with(userImage)
                    .load(comment.userImage)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(userImage);
            userName.setText(comment.userName);
            msg.setText(comment.gamePlatform);
            int score=Integer.parseInt(comment.scoreStar);
            if(score==0){
                star.setVisibility(View.INVISIBLE);
            }else {
                if(score>3){
                    star.setBackground(content.getResources().getDrawable(R.drawable.bg_good));
                }else if(score>2){
                    star.setBackground(content.getResources().getDrawable(R.drawable.bg_mid));
                }else {
                    star.setBackground(content.getResources().getDrawable(R.drawable.bg_bad));
                }
                star.setVisibility(View.VISIBLE);
            }
            star.setText(comment.scoreStar+content.getResources().getString(R.string.star));
            time.setText(comment.time);
            content.setText(Html.fromHtml(comment.content).toString().trim());
            like.setText(content.getResources().getString(R.string.upvote)+":"+comment.likeNum);
            dislike.setText(content.getResources().getString(R.string.downvote)+":"+comment.disLikeNum);
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
                    textView.setText(context.getResources().getString(R.string.wait));
                }else {
                    textView.setText(context.getResources().getString(R.string.no_more));
                }
            }
        }
    }


}
