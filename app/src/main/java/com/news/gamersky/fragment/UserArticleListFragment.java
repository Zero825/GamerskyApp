package com.news.gamersky.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.news.gamersky.R;
import com.news.gamersky.adapter.UserArticleListAdapter;
import com.news.gamersky.dao.UserFavoriteDao;
import com.news.gamersky.database.AppDataBaseSingleton;
import com.news.gamersky.entity.UserFavorite;
import com.news.gamersky.util.UserMsgUtil;

import java.util.ArrayList;
import java.util.List;

public class UserArticleListFragment extends Fragment {
    private final static String TAG="UserArticleListFragment";

    private RecyclerView recyclerView;
    private UserArticleListAdapter userArticleListAdapter;
    private List<UserFavorite> userFavoriteList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_article,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle=getArguments();
        if(bundle!=null) {
            init();
            loadData();
        }
    }

    public void init(){
        userFavoriteList=new ArrayList<>();
        userArticleListAdapter=new UserArticleListAdapter(userFavoriteList);

        recyclerView=getView().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(userArticleListAdapter);
    }

    public void loadData(){
        final int type=getArguments().getInt("type",-1);
        if(type!=-1){
            Thread databaseThread =new Thread(new Runnable() {
                @Override
                public void run() {
                    final List<UserFavorite> tempData=AppDataBaseSingleton.getAppDatabase()
                            .userFavoriteDao().findByUserNameAndType(UserMsgUtil.getUserName(getContext()),type);
                    Log.i(TAG, "run: "+tempData.size());
                    if(tempData.size()>0) {
                        userFavoriteList.clear();
                        userFavoriteList.addAll(tempData);
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.getAdapter().notifyDataSetChanged();
                            }
                        });
                    }
                }
            });
            databaseThread.start();
        }
    }

    public void enterDeleteMode(){
        if(userArticleListAdapter!=null){
            userArticleListAdapter.enterDeleteMode();
        }
    }

    public void deleteFavorites(){
        Thread databaseThread =new Thread(new Runnable() {
            @Override
            public void run() {
                if(userArticleListAdapter==null){
                    return;
                }

                UserFavoriteDao userFavoriteDao=AppDataBaseSingleton.getAppDatabase()
                        .userFavoriteDao();

                String userName=UserMsgUtil.getUserName(requireContext());

                List<String> hrefs = userArticleListAdapter.getDeleteList();


                for(String href:hrefs){
                    userFavoriteDao.deleteByUserNameAndHref(userName,href);
                }

                loadData();

            }
        });
        databaseThread.start();
    }

    public void exitDeleteMode(){
        if(userArticleListAdapter!=null){
            userArticleListAdapter.exitDeleteMode();
        }
    }
}
