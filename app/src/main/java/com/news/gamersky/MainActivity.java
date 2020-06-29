package com.news.gamersky;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.piasy.biv.BigImageViewer;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.news.gamersky.fragment.EntertainmentFragment;
import com.news.gamersky.fragment.HomePageFragment;
import com.news.gamersky.fragment.InterestingImagesFragment;

public class MainActivity extends AppCompatActivity {

    private ImageView logo;
    private ImageView searchBtn;
    private ImageView setBtn;
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private FragmentAdapter fragmentAdapter;
    private SharedPreferences sharedPreferences;
    private long exitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        startListen();
    }

    private void init(){
        exitTime=0;

        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        logo=findViewById(R.id.imageView4);
        searchBtn=findViewById(R.id.imageView11);
        setBtn=findViewById(R.id.imageView10);
        viewPager2=findViewById(R.id.viewPager);
        tabLayout=findViewById(R.id.tabLayout);
        fragmentAdapter=new FragmentAdapter(this);
        viewPager2.setAdapter(fragmentAdapter);
        //viewPager2.setOffscreenPageLimit(2);
        new TabLayoutMediator(tabLayout, viewPager2,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        if(position==0) tab.setText("首页");
                        if(position==1)tab.setText("娱乐");
                        if(position==2) tab.setText("囧图");
                    }
                }
        ).attach();
        sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        clearGlideDiskCache(sharedPreferences.getBoolean("auto_clear_cache",true));
    }

    private void startListen(){
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,SearchActivity.class));
            }
        });
        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,SettingsActivity.class));
            }
        });
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int p=viewPager2.getCurrentItem();
                try {
                    if(p==0) {
                        ((HomePageFragment) getSupportFragmentManager().getFragments().get(p)).upTop();
                    }
                    if(p==1) {
                        ((EntertainmentFragment) getSupportFragmentManager().getFragments().get(p+1)).upTop();
                    }
                    if(p==2) {
                        ((InterestingImagesFragment) getSupportFragmentManager().getFragments().get(p+1)).upTop();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


            }
        });
    }

    public void clearGlideDiskCache(boolean b){
        if(b) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Glide.get(MainActivity.this).clearDiskCache();
                    BigImageViewer.imageLoader().cancelAll();
                }
            }).start();
        }
    }



    @Override
    public void onBackPressed() {
        exitApp();
    }

    private void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Snackbar.make(viewPager2,"再按一次退出应用",1000).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    public class FragmentAdapter extends FragmentStateAdapter {

        public FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if(position==0){
                return new HomePageFragment();
            }
            if(position==1){
                return new EntertainmentFragment();
            }
            if(position==2){
                return new InterestingImagesFragment();
            }
            return null;
        }



        @Override
        public int getItemCount() {
            return 3;
        }
    }
}