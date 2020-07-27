package com.news.gamersky.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.piasy.biv.BigImageViewer;
import com.google.android.material.tabs.TabLayout;
import com.news.gamersky.MainActivity;
import com.news.gamersky.R;
import com.news.gamersky.SearchActivity;
import com.news.gamersky.SettingsActivity;
import com.news.gamersky.adapter.ViewPagerFragmentAdapter;

import java.util.ArrayList;
import java.util.List;

public class NewsFragment extends Fragment {

    private ImageView logo;
    private ImageView searchBtn;
    private ImageView setBtn;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ViewPagerFragmentAdapter fragmentAdapter;
    private List<Fragment> fragments;
    private List<String> tabTitles;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_news, container, false);
        init(view);
        startListen();
        return view;
    }


    private void init(View view){

        logo=view.findViewById(R.id.imageView4);
        searchBtn=view.findViewById(R.id.imageView11);
        setBtn=view.findViewById(R.id.imageView10);
        viewPager=view.findViewById(R.id.viewPager);
        tabLayout=view.findViewById(R.id.tabLayout);

        tabTitles=new ArrayList<>();
        tabTitles.add(getResources().getString(R.string.title_news_tab1));
        tabTitles.add(getResources().getString(R.string.title_news_tab2));
        tabTitles.add(getResources().getString(R.string.title_news_tab3));

        fragments=new ArrayList<>();
        fragments.add(new HomePageFragment());
        fragments.add(new CommonNewsFragment());
        fragments.add(new CommonNewsFragment());

        for(int i=0;i<fragments.size();i++){
            Bundle bundle = new Bundle();
            if(i==1){
                bundle.putString( "src", "https://www.gamersky.com/ent/xz");
                bundle.putInt("nodeIdPos",0);
            }
            if(i==2){
                bundle.putString( "src", "https://www.gamersky.com/ent/qw");
                bundle.putInt("nodeIdPos",0);
            }
            fragments.get(i).setArguments(bundle);
        }

        fragmentAdapter= new ViewPagerFragmentAdapter(getChildFragmentManager(),fragments,tabTitles,0);
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setOffscreenPageLimit(fragmentAdapter.getCount());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        clearGlideDiskCache(sharedPreferences.getBoolean("auto_clear_cache",true));
    }

    private void startListen(){
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), SearchActivity.class));
            }
        });
        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), SettingsActivity.class));
            }
        });
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upTop();
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                startAnimator(tab.view);
                viewPager.setCurrentItem(tab.getPosition(),true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                endAnimator(tab.view);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public void upTop(){
        int p=viewPager.getCurrentItem();
        FragmentManager fragmentManager=getChildFragmentManager();
        Fragment fragment=fragmentManager.findFragmentByTag("android:switcher:"+viewPager.getId()+":"+p);
        Log.i("TAG", "onClick: "+fragmentManager.getFragments()+"\n"+viewPager.getId());
        if(fragment!=null){
            Log.i("TAG", "onClick: "+"返回顶部");
            if(p==0) {
                ((HomePageFragment) fragment).upTop();
            }
            else  {
                ((CommonNewsFragment) fragment).upTop();
            }

        }
        if(getActivity()!=null){
            ((MainActivity)getActivity()).showNav();
        }
    }

    public void clearGlideDiskCache(boolean b){
        if(b) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Glide.get(getContext()).clearDiskCache();
                    BigImageViewer.imageLoader().cancelAll();
                }
            }).start();
        }
    }

    public void startAnimator(View view){
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view, "ScaleX", view.getScaleX(),1.15f);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(view, "ScaleY", view.getScaleY(), 1.15f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(objectAnimator1,objectAnimator2);
        animSet.setDuration(300);
        animSet.start();
    }

    public void endAnimator(View view){
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view, "ScaleX", view.getScaleX(), 1.0f);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(view, "ScaleY", view.getScaleY(), 1.0f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(objectAnimator1,objectAnimator2);
        animSet.setDuration(300);
        animSet.start();
    }

}