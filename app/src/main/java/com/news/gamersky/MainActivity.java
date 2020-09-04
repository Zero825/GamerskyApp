package com.news.gamersky;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.news.gamersky.fragment.HandBookFragment;
import com.news.gamersky.fragment.NewsFragment;
import com.news.gamersky.util.AppUtil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;


public class MainActivity extends AppCompatActivity {
    private long exitTime;
    private BottomNavigationView navView;
    private ObjectAnimator showAnimator;
    private FragmentManager fragmentManager;
    private FrameLayout hostContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        startListen();
    }

    public void init(){
        exitTime=0;

//        getWindow().getDecorView()
//                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        navView = findViewById(R.id.nav_view);
        hostContainer=findViewById(R.id.nav_host_container);

        fragmentManager=getSupportFragmentManager();
        if(fragmentManager.findFragmentByTag("newsFragment")==null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.nav_host_container, new NewsFragment(), "newsFragment");
            fragmentTransaction.commit();
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences.getBoolean("no_bottombar",true)){
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
                        if(fragmentManager.findFragmentByTag("newsFragment")!=null){
                            fragmentTransaction.show(fragmentManager.findFragmentByTag("newsFragment"));
                        }else {
                            fragmentTransaction.add(R.id.nav_host_container, new NewsFragment(), "newsFragment");
                        }
                        break;
                    case R.id.navigation_game_guide:
                        if(fragmentManager.findFragmentByTag("HandBookFragment")!=null){
                            fragmentTransaction.show(fragmentManager.findFragmentByTag("HandBookFragment"));
                        }else {
                            fragmentTransaction.add(R.id.nav_host_container, new HandBookFragment(), "HandBookFragment");
                        }
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
                        if(fragmentManager.findFragmentByTag("newsFragment")!=null){
                            Fragment fragment=fragmentManager.findFragmentByTag("newsFragment");
                            ((NewsFragment) fragment).upTop();
                        }
                        break;
                    case R.id.navigation_game_guide:
                        if(fragmentManager.findFragmentByTag("HandBookFragment")!=null){
                            Fragment fragment=fragmentManager.findFragmentByTag("HandBookFragment");
                            ((HandBookFragment) fragment).upTop();
                        }
                        break;
                }
            }
        });

    }

    private void hideAllFragment(FragmentTransaction fragmentTransaction){
        if(fragmentManager.findFragmentByTag("newsFragment")!=null){
            fragmentTransaction.hide(fragmentManager.findFragmentByTag("newsFragment"));
        }
        if(fragmentManager.findFragmentByTag("HandBookFragment")!=null){
            fragmentTransaction.hide(fragmentManager.findFragmentByTag("HandBookFragment"));
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
        if(showAnimator==null||!showAnimator.isRunning()) {
            showAnimator = ObjectAnimator.ofFloat(navView, "translationY", navView.getTranslationY(), 0f);
            showAnimator.setDuration(300);
            showAnimator.start();
        }
    }

}