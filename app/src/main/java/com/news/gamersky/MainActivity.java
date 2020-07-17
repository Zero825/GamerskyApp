package com.news.gamersky;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.piasy.biv.BigImageViewer;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.news.gamersky.fragment.EntertainmentFragment;
import com.news.gamersky.fragment.HomePageFragment;
import com.news.gamersky.fragment.InterestingImagesFragment;

import java.lang.reflect.Field;
import java.util.HashMap;

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
        viewPager2.setOffscreenPageLimit(fragmentAdapter.getItemCount());
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
                if(fragmentAdapter.getFragment(p)!=null){
                    Fragment fragment=fragmentAdapter.getFragment(p);
                    if(p==0) {
                        ((HomePageFragment) fragment).upTop();
                    }
                    if(p==1) {
                        ((EntertainmentFragment) fragment).upTop();
                    }
                    if(p==2) {
                        ((InterestingImagesFragment) fragment).upTop();
                    }
                }
            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                for (int i=0;i<fragmentAdapter.getItemCount();i++){
                   endAnimator(tabLayout.getTabAt(i).view);
                }
                startAnimator(tabLayout.getTabAt(position).view);

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
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
        public HashMap<Integer,Fragment> fragmentHashMap;

        public FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
            fragmentHashMap=new HashMap<>();
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment=new Fragment();
            if(position==0){
                fragment= new HomePageFragment();
            }
            if(position==1){
                fragment= new EntertainmentFragment();
            }
            if(position==2){
                fragment= new InterestingImagesFragment();
            }
            fragmentHashMap.put(position,fragment);
            return fragment;
        }



        @Override
        public int getItemCount() {
            return 3;
        }

        public Fragment getFragment(int position){
            if(fragmentHashMap.containsKey(position)){
                return fragmentHashMap.get(position);
            }else {
                return null;
            }
        }
    }
}