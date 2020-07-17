package com.news.gamersky;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
    private long exitTime;
    private BottomNavigationView navView;
    NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        startListen();
    }

    public void init(){
        exitTime=0;

        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        navView = findViewById(R.id.nav_view);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);
    }

    public void startListen(){
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        exitApp();
    }

    private void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Snackbar.make(navView,"再按一次退出应用",1000).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    public void showNav(){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(navView, "translationY", navView.getTranslationY(), 0f);
        objectAnimator.setDuration(300);
        objectAnimator.start();
    }

}