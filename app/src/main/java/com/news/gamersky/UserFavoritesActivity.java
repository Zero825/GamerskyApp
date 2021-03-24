package com.news.gamersky;

import android.content.ContentUris;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.news.gamersky.adapter.ViewPagerFragmentAdapter;
import com.news.gamersky.databean.PictureDataBean;
import com.news.gamersky.entity.UserFavorite;
import com.news.gamersky.fragment.CommonNewsFragment;
import com.news.gamersky.fragment.HomePageFragment;
import com.news.gamersky.fragment.UserArticleListFragment;

import java.util.ArrayList;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class UserFavoritesActivity extends AppCompatActivity {
    private final static String TAG="UserFavoritesActivity";

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ViewPagerFragmentAdapter fragmentAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_favorites);
        init();
        startListen();
    }

    public void init(){
        viewPager=findViewById(R.id.viewPager);
        tabLayout=findViewById(R.id.tabLayout);

        ArrayList<String> tabTitles=new ArrayList<>();
        tabTitles.add(getString(R.string.article));
        tabTitles.add(getString(R.string.handbook));

        ArrayList<Fragment> fragments=new ArrayList<>();
        fragments.add(new UserArticleListFragment());
        fragments.add(new UserArticleListFragment());

        for(int i=0;i<fragments.size();i++){
            Bundle bundle = new Bundle();
            if(i==0){
                bundle.putInt( "type", UserFavorite.TYPE_NEW);
            }
            if(i==1){
                bundle.putInt( "type", UserFavorite.TYPE_HANDBOOK);
            }
            fragments.get(i).setArguments(bundle);
        }

        fragmentAdapter= new ViewPagerFragmentAdapter(getSupportFragmentManager(),fragments,tabTitles,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setOffscreenPageLimit(fragmentAdapter.getCount());
        tabLayout.setupWithViewPager(viewPager);
    }

    public void startListen(){
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
