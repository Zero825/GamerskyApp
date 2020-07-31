package com.news.gamersky;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;


import com.google.android.material.tabs.TabLayout;
import com.news.gamersky.adapter.ViewPagerFragmentAdapter;
import com.news.gamersky.databean.NewsDataBean;
import com.news.gamersky.fragment.ArticleFragment;
import com.news.gamersky.fragment.CommentFragment;

import java.util.ArrayList;
import java.util.List;


public class ArticleActivity extends AppCompatActivity{

    private NewsDataBean new_data;
    private ImageView imageView1;
    private ImageView imageView2;
    private ViewPager viewPager;
    private ViewPagerFragmentAdapter viewPagerFragmentAdapter;
    private List<Fragment> fragments;
    private List<String> tabTitles;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        init();
        setListen();
    }

    public void init(){
//        getWindow().getDecorView()
//                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        Intent intent = getIntent();
        new_data = (NewsDataBean) intent.getSerializableExtra("new_data");

        imageView1=findViewById(R.id.imageView5);
        imageView2=findViewById(R.id.imageView12);
        viewPager = findViewById(R.id.pager2);
        tabLayout = findViewById(R.id.tab_layout);

        tabTitles=new ArrayList<>();
        tabTitles.add(getResources().getString(R.string.title_article_tab1));
        tabTitles.add(getResources().getString(R.string.title_article_tab2));

        fragments=new ArrayList<>();
        fragments.add(new ArticleFragment());
        fragments.add(new CommentFragment());

        Bundle bundle = new Bundle();
        bundle.putString( "data_src", new_data.src);
        for(int i=0;i<fragments.size();i++){
            fragments.get(i).setArguments(bundle);
        }

        viewPagerFragmentAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(),fragments,tabTitles,0);
        viewPager.setAdapter(viewPagerFragmentAdapter);
        viewPager.setOffscreenPageLimit(viewPagerFragmentAdapter.getCount());
        tabLayout.setTabIndicatorFullWidth(false);
        tabLayout.setupWithViewPager(viewPager);
    }




    public void setListen(){

        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT,new_data.title+new_data.src);
                startActivity(Intent.createChooser(shareIntent, "分享到"));
            }
        });

        for(int i=0;i<tabLayout.getTabCount();i++){
            final int p = i;
            tabLayout.getTabAt(i).view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fragmentManager=ArticleActivity.this.getSupportFragmentManager();
                    Fragment fragment=fragmentManager.findFragmentByTag("android:switcher:"+viewPager.getId()+":"+p);
                    if(p==0&&p==viewPager.getCurrentItem()) {
                        ((ArticleFragment) fragment).upTop();
                    }
                    if(p==1&&p==viewPager.getCurrentItem()) {
                        ((CommentFragment) fragment).upTop();
                    }
                }
            });
        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
