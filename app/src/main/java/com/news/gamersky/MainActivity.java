package com.news.gamersky;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.news.gamersky.fragment.GalleryFragment;
import com.news.gamersky.fragment.HandBookFragment;
import com.news.gamersky.fragment.NewsFragment;
import com.news.gamersky.fragment.ReviewsFragment;
import com.news.gamersky.setting.AppSetting;
import com.news.gamersky.util.AppUtil;
import com.news.gamersky.util.NightModeUtil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import static android.view.Gravity.CENTER;


public class MainActivity extends AppCompatActivity {
    private static final String TAG="MainActivity";

    private long exitTime;
    private BottomNavigationView navView;
    private ObjectAnimator showAnimator;
    private ObjectAnimator hideAnimator;
    private FragmentManager fragmentManager;
    private FrameLayout hostContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppSetting.init(this);
        init();
        startListen();
    }

    public void init(){
        exitTime=0;

        showAnimator=new ObjectAnimator();
        hideAnimator=new ObjectAnimator();

        navView = findViewById(R.id.nav_view);
        hostContainer=findViewById(R.id.nav_host_container);

        fragmentManager=getSupportFragmentManager();
        if(fragmentManager.findFragmentByTag("NewsFragment")==null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.nav_host_container, new NewsFragment(), "NewsFragment");
            fragmentTransaction.commit();
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences.getBoolean("no_bottombar",false)){
            navView.setVisibility(View.GONE);
        }
    }

    public void startListen(){

        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                hideAllFragment(fragmentTransaction);
                Log.i("TAG", "onNavigationItemSelected: show"+item.getItemId());
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        if(fragmentManager.findFragmentByTag("NewsFragment")!=null){
                            fragmentTransaction.show(fragmentManager.findFragmentByTag("NewsFragment"));
                        }else {
                            fragmentTransaction.add(R.id.nav_host_container, new NewsFragment(), "NewsFragment");
                        }
                        break;
                    case R.id.navigation_game_guide:
                        if(fragmentManager.findFragmentByTag("HandBookFragment")!=null){
                            fragmentTransaction.show(fragmentManager.findFragmentByTag("HandBookFragment"));
                        }else {
                            fragmentTransaction.add(R.id.nav_host_container, new HandBookFragment(), "HandBookFragment");
                        }
                        break;
                    case R.id.navigation_game_reviews:
                        if(fragmentManager.findFragmentByTag("ReviewsFragment")!=null){
                            fragmentTransaction.show(fragmentManager.findFragmentByTag("ReviewsFragment"));
                        }else {
                            fragmentTransaction.add(R.id.nav_host_container, new ReviewsFragment(), "ReviewsFragment");
                        }
                        break;
                    case R.id.navigation_gallery:
                        if(fragmentManager.findFragmentByTag("GalleryFragment")!=null){
                            fragmentTransaction.show(fragmentManager.findFragmentByTag("GalleryFragment"));
                        }else {
                            fragmentTransaction.add(R.id.nav_host_container, new GalleryFragment(), "GalleryFragment");
                        }
                        break;
                }
                Log.i("TAG", "onNavigationItemSelected: "+item+item.getItemId());
                fragmentTransaction.commit();
                return true;
            }


        });

        navView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        if(fragmentManager.findFragmentByTag("NewsFragment")!=null){
                            Fragment fragment=fragmentManager.findFragmentByTag("NewsFragment");
                            ((NewsFragment) fragment).upTop();
                        }
                        break;
                    case R.id.navigation_game_guide:
                        if(fragmentManager.findFragmentByTag("HandBookFragment")!=null){
                            Fragment fragment=fragmentManager.findFragmentByTag("HandBookFragment");
                            ((HandBookFragment) fragment).upTop();
                        }
                        break;
                    case R.id.navigation_game_reviews:
                        if(fragmentManager.findFragmentByTag("ReviewsFragment")!=null){
                            Fragment fragment=fragmentManager.findFragmentByTag("ReviewsFragment");
                            ((ReviewsFragment) fragment).upTop();
                        }
                        break;
                    case R.id.navigation_gallery:
                        if(fragmentManager.findFragmentByTag("GalleryFragment")!=null){
                            Fragment fragment=fragmentManager.findFragmentByTag("GalleryFragment");
                            ((GalleryFragment) fragment).upTop();
                        }
                        break;
                }
            }
        });

    }

    private void hideAllFragment(FragmentTransaction fragmentTransaction){
        if(fragmentManager.findFragmentByTag("NewsFragment")!=null){
            fragmentTransaction.hide(fragmentManager.findFragmentByTag("NewsFragment"));
        }
        if(fragmentManager.findFragmentByTag("HandBookFragment")!=null){
            fragmentTransaction.hide(fragmentManager.findFragmentByTag("HandBookFragment"));
        }
        if(fragmentManager.findFragmentByTag("ReviewsFragment")!=null){
            fragmentTransaction.hide(fragmentManager.findFragmentByTag("ReviewsFragment"));
        }
        if(fragmentManager.findFragmentByTag("GalleryFragment")!=null){
            fragmentTransaction.hide(fragmentManager.findFragmentByTag("GalleryFragment"));
        }
    }

    @Override
    public void onBackPressed() {
        exitApp();
    }

    private void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            AppUtil.getSnackbar(this,hostContainer,"再次点击退出应用",false,true).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    public void showNav(){
        if(!showAnimator.isRunning()&&!hideAnimator.isRunning()) {
            showAnimator = ObjectAnimator.ofFloat(navView, "translationY", navView.getTranslationY(), 0f);
            showAnimator.setDuration(300);
            showAnimator.start();
        }
    }

    public void hideNav(){
        if(!showAnimator.isRunning()&&!hideAnimator.isRunning()) {
            hideAnimator = ObjectAnimator.ofFloat(navView, "translationY", navView.getTranslationY(), navView.getHeight());
            hideAnimator.setDuration(300);
            hideAnimator.start();
        }
    }

    public void showOrHideSplash(boolean show){
        final View view=findViewById(R.id.splash);
        final View container=findViewById(R.id.container);
        if(show){
        }else {
            if(container.getVisibility()==View.INVISIBLE){
                container.setAlpha(0);
            }
            container.setVisibility(View.VISIBLE);
            //Log.i(TAG, "showOrHideSplash: "+"隐藏动画"+container.getAlpha());
            Drawable[] layers=new Drawable[2];
            layers[0]=getDrawable(R.color.colorBackground);
            layers[1]=getDrawable(R.drawable.bg_404);
            final LayerDrawable layerDrawable=new LayerDrawable(layers);
            layerDrawable.setLayerGravity(1,CENTER);
            layerDrawable.setLayerWidth(1,AppUtil.dip2px(this,270f));
            layerDrawable.setLayerHeight(1,AppUtil.dip2px(this,400f));
            ValueAnimator valueAnimator=ValueAnimator.ofInt(255,0);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int alpha=(int)animation.getAnimatedValue();
                    layerDrawable.getDrawable(1).setAlpha(alpha);
                    getWindow().setBackgroundDrawable(layerDrawable);
                    container.setAlpha(1 - alpha / 255f);
                    if(alpha==0){
                        layerDrawable.getDrawable(1).setAlpha(255);
                        getWindow().setBackgroundDrawableResource(R.color.colorBackground);
                        view.setVisibility(View.GONE);
                    }
                    view.setAlpha(alpha/255f);
                }
            });
            if(container.getAlpha()!=1.0f) {
                valueAnimator.setDuration(300).start();
            }

        }
    }

}