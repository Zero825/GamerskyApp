package com.news.gamersky;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.news.gamersky.adapter.ViewPagerFragmentAdapter;
import com.news.gamersky.entity.UserFavorite;
import com.news.gamersky.fragment.UserArticleListFragment;

import java.util.ArrayList;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class UserFavoritesActivity extends AppCompatActivity {
    private final static String TAG="UserFavoritesActivity";

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ViewPagerFragmentAdapter fragmentAdapter;
    private ArrayList<Fragment> fragments;

    public static final int NORMAL_MODE=0;
    public static final int DELETE_MODE=1;
    private int mode=0;

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

        fragments=new ArrayList<>();
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
        findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mode==NORMAL_MODE){
                    showOrHideDeleteBar(true);
                    mode=DELETE_MODE;
                    for(Fragment fragment:fragments){
                        ((UserArticleListFragment)fragment).enterDeleteMode();
                    }
                }else if(mode==DELETE_MODE){
                    showOrHideDeleteBar(false);
                    mode=NORMAL_MODE;
                    for(Fragment fragment:fragments){
                        ((UserArticleListFragment)fragment).exitDeleteMode();
                    }
                }
            }
        });
        findViewById(R.id.delete_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(Fragment fragment:fragments){
                    ((UserArticleListFragment)fragment).deleteFavorites();
                }
            }
        });
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrHideDeleteBar(false);
                mode=NORMAL_MODE;
                for(Fragment fragment:fragments){
                    ((UserArticleListFragment)fragment).exitDeleteMode();
                }
            }
        });
    }

    public void showOrHideDeleteBar(boolean show){
        View view=findViewById(R.id.delete_bar);
        if(show){
            ObjectAnimator.ofFloat(view,"translationY",view.getTranslationY(),0f)
                    .setDuration(300).start();
        }else {
            ObjectAnimator.ofFloat(view,"translationY",view.getTranslationY(),(float) view.getHeight())
                    .setDuration(300).start();
        }
    }

}
