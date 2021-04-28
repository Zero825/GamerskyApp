package com.news.gamersky.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.news.gamersky.ArticleActivity;
import com.news.gamersky.R;
import com.news.gamersky.UserFavoritesActivity;
import com.news.gamersky.databean.NewDataBean;
import com.news.gamersky.entity.UserFavorite;
import com.news.gamersky.fragment.UserArticleListFragment;
import com.news.gamersky.util.AppUtil;

import java.util.ArrayList;
import java.util.List;

public class UserArticleListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_FOOTER=1;
    private static final int TYPE_LIST=0;

    private List<UserFavorite> data;
    private List<String> deleteList=new ArrayList<>();
    private int mode= UserFavoritesActivity.NORMAL_MODE;

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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,int position) {
        if(holder.getItemViewType()==TYPE_FOOTER){
            ((FooterViewHolder)holder).textView.setText(R.string.no_more);
        }else {
            UserArticleListViewHolder userArticleListViewHolder= (UserArticleListViewHolder) holder;
            userArticleListViewHolder.title.setText(data.get(position).title);
            userArticleListViewHolder.time.setText(AppUtil.format(Long.parseLong(data.get(position).time)));

            if(userArticleListViewHolder.itemOnCheckedChangeListener.deleteList==null){
                userArticleListViewHolder.itemOnCheckedChangeListener.deleteList=deleteList;
            }
            userArticleListViewHolder.checkBox.setChecked(false);
            if(mode==UserFavoritesActivity.NORMAL_MODE) {
                userArticleListViewHolder.checkBox.setVisibility(View.GONE);
                userArticleListViewHolder.itemOnClickListener.enable=true;
                userArticleListViewHolder.itemOnClickListener.position=position;
                userArticleListViewHolder.itemOnClickListener.userFavorite=data.get(position);
            }else {
                userArticleListViewHolder.checkBox.setVisibility(View.VISIBLE);
                userArticleListViewHolder.itemOnClickListener.enable=false;
                userArticleListViewHolder.itemOnCheckedChangeListener.userFavorite=data.get(position);

            }



        }
    }

    @Override
    public int getItemCount() {
        return data.size()+1;
    }

    public void enterDeleteMode(){
        mode= UserFavoritesActivity.DELETE_MODE;
        notifyItemRangeChanged(0,getItemCount());
    }

    public List<String> getDeleteList(){
        return deleteList;
    }

    public void exitDeleteMode(){
        mode= UserFavoritesActivity.NORMAL_MODE;
        deleteList.clear();
        notifyItemRangeChanged(0,getItemCount());
    }

    private static class ItemOnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        private UserFavorite userFavorite;
        private boolean enable=true;
        private List<String> deleteList;

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                deleteList.add(userFavorite.href);
            }else {
                deleteList.remove(userFavorite.href);
            }
        }
    }

    private static class ItemOnClickListener implements View.OnClickListener {

        private int position=-1;
        private UserFavorite userFavorite;
        private boolean enable=true;

        @Override
        public void onClick(View v) {
            if(enable&&userFavorite!=null) {
                Intent intent = new Intent(v.getContext(), ArticleActivity.class);
                NewDataBean newDataBean = new NewDataBean(userFavorite.title, userFavorite.href);
                intent.putExtra("new_data", newDataBean);
                v.getContext().startActivity(intent);
            }
        }
    }

    private static class UserArticleListViewHolder extends RecyclerView.ViewHolder{
        private TextView title;
        private TextView time;
        private CheckBox checkBox;
        private ItemOnClickListener itemOnClickListener;
        private ItemOnCheckedChangeListener itemOnCheckedChangeListener;

        public UserArticleListViewHolder(@NonNull View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.title);
            time=itemView.findViewById(R.id.time);
            checkBox=itemView.findViewById(R.id.checkBox);
            itemView.setOnClickListener(itemOnClickListener=new ItemOnClickListener());
            checkBox.setOnCheckedChangeListener(itemOnCheckedChangeListener=new ItemOnCheckedChangeListener());
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
