package com.news.gamersky;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;


import com.google.android.material.tabs.TabLayout;
import com.news.gamersky.adapter.ViewPagerFragmentAdapter;
import com.news.gamersky.customizeview.FixViewPager;
import com.news.gamersky.dao.UserFavoriteDao;
import com.news.gamersky.database.AppDataBaseSingleton;
import com.news.gamersky.databean.NewDataBean;
import com.news.gamersky.entity.UserFavorite;
import com.news.gamersky.fragment.ArticleFragment;
import com.news.gamersky.fragment.CommentFragment;
import com.news.gamersky.setting.AppSetting;
import com.news.gamersky.util.UserMsgUtil;

import java.util.ArrayList;
import java.util.List;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_SET_USER_VISIBLE_HINT;


public class ArticleActivity extends AppCompatActivity{
    private final static String TAG="ArticleActivity";

    private NewDataBean new_data;
    private ImageView imageView1;
    private ImageView imageView2;
    private FixViewPager viewPager;
    private ViewPagerFragmentAdapter viewPagerFragmentAdapter;
    private List<Fragment> fragments;
    private List<String> tabTitles;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        init();
        startListen();
    }

    public void init(){
//        getWindow().getDecorView()
//                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
        new_data = (NewDataBean) appLinkIntent.getSerializableExtra("new_data");
        if(new_data==null){
            String url=appLinkData.toString();
            new_data=new NewDataBean("",url);
        }
        if(new_data.src.contains("_")){
            new_data.src=new_data.src.substring(0,new_data.src.indexOf("_"))+".html";
        }

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

        viewPagerFragmentAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(),fragments,tabTitles,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(viewPagerFragmentAdapter);
        viewPager.setOffscreenPageLimit(viewPagerFragmentAdapter.getCount());
        tabLayout.setTabIndicatorFullWidth(false);
        tabLayout.setupWithViewPager(viewPager);
    }




    public void startListen(){

        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v);

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
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("load_pic_auto",true)) {
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    FragmentManager fragmentManager = ArticleActivity.this.getSupportFragmentManager();
                    Fragment fragment = fragmentManager.findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + 0);
                    if (position == 0) {
                        if (fragment != null) {
                            ((ArticleFragment) fragment).webViewResume();
                        }
                    } else {
                        if (fragment != null) {
                            ((ArticleFragment) fragment).webViewPause();
                        }
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                if(id==R.id.favorites){
                    addOrDeleteFavorite();
                }else if(id==R.id.share){
                    shareArticle();
                }
                return true;
            }
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.common_menu, popup.getMenu());
        popup.show();
    }

    public void shareArticle(){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        if(new_data.title.equals("")){
            FragmentManager fragmentManager=ArticleActivity.this.getSupportFragmentManager();
            Fragment fragment=fragmentManager.findFragmentByTag("android:switcher:"+viewPager.getId()+":"+0);
            if(fragment!=null) {
                new_data.title = ((ArticleFragment) fragment).getNewTitle();
            }
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT,new_data.title+new_data.src);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_to)));
    }

    public void addOrDeleteFavorite(){
        Thread databaseThread = new Thread(new Runnable() {
            @Override
            public void run() {
                UserFavoriteDao userFavoriteDao=AppDataBaseSingleton.getAppDatabase()
                        .userFavoriteDao();
                List<UserFavorite> userFavoriteArrayList=userFavoriteDao
                        .findByUserNameAndHref(UserMsgUtil.getUserName(ArticleActivity.this),new_data.src);
                FragmentManager fragmentManager=ArticleActivity.this.getSupportFragmentManager();
                final Fragment fragment=fragmentManager.findFragmentByTag("android:switcher:"+viewPager.getId()+":"+0);
                if(userFavoriteArrayList.size()==0){
                    Log.i(TAG, "run: ");
                    UserFavorite userFavorite=new UserFavorite();
                    if(new_data.title.equals("")){
                        if(fragment!=null) {
                            new_data.title=((ArticleFragment) fragment).getNewTitle();
                        }
                    }
                    userFavorite.userName=UserMsgUtil.getUserName(ArticleActivity.this);
                    userFavorite.title=new_data.title;
                    userFavorite.href=new_data.src;
                    if(userFavorite.href.contains("wap.gamersky.com/gl")){
                        userFavorite.type=UserFavorite.TYPE_HANDBOOK;
                    }else {
                        userFavorite.type=UserFavorite.TYPE_NEW;
                    }
                    userFavorite.time=String.valueOf(System.currentTimeMillis());
                    userFavoriteDao.insertUserFavorite(userFavorite);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(fragment!=null) {
                                ((ArticleFragment) fragment).showSnackbar(getString(R.string.success_favor));
                            }
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(fragment!=null) {
                                ((ArticleFragment) fragment).showSnackbar(getString(R.string.repeat_favor));
                            }
                        }
                    });
                }
            }
        });
        databaseThread.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
