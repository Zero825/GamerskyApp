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
import com.news.gamersky.fragment.ArticleFragment;
import com.news.gamersky.fragment.CommentFragment;


public class ArticleActivity extends AppCompatActivity{

    private String  data_src;
    private ImageView imageView1;
    private ImageView imageView2;
    private ViewPager2 viewPager;

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
        data_src = intent.getStringExtra("data_src");
        CollectionAdapter collectionAdapter = new CollectionAdapter(getSupportFragmentManager(), getLifecycle());
        imageView1=findViewById(R.id.imageView5);
        imageView2=findViewById(R.id.imageView12);
        viewPager = findViewById(R.id.pager2);
        viewPager.setAdapter(collectionAdapter);
        viewPager.setOffscreenPageLimit(2);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
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
        public CollectionAdapter(FragmentManager fm, Lifecycle lifecycle) {
            super(fm,lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment=new Fragment();
            if (position==0) fragment = new ArticleFragment();
            else if(position==1) fragment=new CommentFragment();
            Bundle args = new Bundle();
            args.putString("data_src",data_src);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 2;
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
                shareIntent.putExtra(Intent.EXTRA_TEXT,data_src);
                startActivity(Intent.createChooser(shareIntent, "分享到"));
            }
        });

    }

    @Override
    public void onBackPressed() {
//        if (viewPager.getCurrentItem()==0){
//            finish();
//        }else {
//            viewPager.setCurrentItem(viewPager.getCurrentItem()-1);
//        }
        super.onBackPressed();
    }

}
