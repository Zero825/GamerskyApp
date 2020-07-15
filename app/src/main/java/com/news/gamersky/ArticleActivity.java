package com.news.gamersky;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;



import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.news.gamersky.databean.NewsDataBean;
import com.news.gamersky.fragment.ArticleFragment;
import com.news.gamersky.fragment.CommentFragment;

import java.util.HashMap;


public class ArticleActivity extends AppCompatActivity{

    private NewsDataBean new_data;
    private ImageView imageView1;
    private ImageView imageView2;
    private ViewPager2 viewPager;
    private CollectionAdapter collectionAdapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        init();
        setListen();
    }

    public void init(){
        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        Intent intent = getIntent();
        new_data = (NewsDataBean) intent.getSerializableExtra("new_data");
        collectionAdapter = new CollectionAdapter(getSupportFragmentManager(), getLifecycle());
        imageView1=findViewById(R.id.imageView5);
        imageView2=findViewById(R.id.imageView12);
        viewPager = findViewById(R.id.pager2);
        viewPager.setAdapter(collectionAdapter);
        viewPager.setOffscreenPageLimit(2);
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setTabIndicatorFullWidth(false);
        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        if(position==0) tab.setText("正文");
                        else if (position==1)tab.setText("评论");
                    }
                }
        ).attach();
    }


    public class CollectionAdapter extends FragmentStateAdapter {
        public HashMap<Integer,Fragment> fragmentHashMap;

        public CollectionAdapter(FragmentManager fm, Lifecycle lifecycle) {
            super(fm,lifecycle);
            fragmentHashMap=new HashMap<>();
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment=new Fragment();
            if (position==0) {
                fragment = new ArticleFragment();
            }
            if(position==1) {
                fragment = new CommentFragment();
            }
            Bundle args = new Bundle();
            args.putString("data_src",new_data.src);
            fragment.setArguments(args);
            fragmentHashMap.put(position,fragment);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 2;
        }

        public Fragment getFragment(int position){
            if(fragmentHashMap.containsKey(position)){
                return fragmentHashMap.get(position);
            }else {
                return null;
            }
        }

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

        tabLayout.getTabAt(0).view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewPager.getCurrentItem()==0)
                    ((ArticleFragment)collectionAdapter.getFragment(0)).upTop();
            }
        });
        tabLayout.getTabAt(1).view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewPager.getCurrentItem()==1)
                ((CommentFragment)collectionAdapter.getFragment(1)).upTop();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(collectionAdapter.getFragment(0)!=null)
            ((ArticleFragment)collectionAdapter.getFragment(0)).pauseWebView();

    }

    @Override
    public void onResume() {
        super.onResume();
        if(collectionAdapter.getFragment(0)!=null)
            ((ArticleFragment)collectionAdapter.getFragment(0)).resumeWebView();
    }

}
