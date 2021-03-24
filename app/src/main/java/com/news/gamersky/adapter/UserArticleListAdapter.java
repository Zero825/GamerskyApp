package com.news.gamersky.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.news.gamersky.ArticleActivity;
import com.news.gamersky.R;
import com.news.gamersky.databean.NewDataBean;
import com.news.gamersky.entity.UserFavorite;
import com.news.gamersky.util.AppUtil;

import java.util.ArrayList;
import java.util.List;

public class UserArticleListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_FOOTER=1;
    private static final int TYPE_LIST=0;

    private List<UserFavorite> data;

    public UserArticleListAdapter(List<UserFavorite> data) {
        this.data = data;
    }

    @Override
    public int getItemViewType(int position){
        if(position==data.size()){
            return TYPE_FOOTER;
        }else {
            return TYPE_LIST;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==TYPE_FOOTER){
            return new FooterViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_footer, parent, false));
        }else {
            return new UserArticleListViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_user_article, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if(holder.getItemViewType()==TYPE_FOOTER){
            ((FooterViewHolder)holder).textView.setText(R.string.no_more);
        }else {
            UserArticleListViewHolder userArticleListViewHolder= (UserArticleListViewHolder) holder;
            userArticleListViewHolder.title.setText(data.get(position).title);
            userArticleListViewHolder.time.setText(AppUtil.format(Long.parseLong(data.get(position).time)));
            userArticleListViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(v.getContext(), ArticleActivity.class);
                    NewDataBean newDataBean=new NewDataBean(data.get(position).title,data.get(position).href);
                    intent.putExtra("new_data",newDataBean);
                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size()+1;
    }

    private static class UserArticleListViewHolder extends RecyclerView.ViewHolder{
        private TextView title;
        private TextView time;

        public UserArticleListViewHolder(@NonNull View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.title);
            time=itemView.findViewById(R.id.time);
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;

        public FooterViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.textView8);
        }
    }
}
