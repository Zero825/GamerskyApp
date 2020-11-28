package com.news.gamersky;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.news.gamersky.fragment.GalleryFragment;

public class GameGalleryActivity extends AppCompatActivity {
    private static final String TAG="GameGalleryActivity";

    private String src,title;
    private GalleryFragment galleryFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_gallery);
        init();
        loadData();
        startListen();
    }

    public void init(){
        src=getIntent().getStringExtra("src");
        title=getIntent().getStringExtra("title");
        Log.i(TAG, "init: "+src);
    }

    public void loadData(){

        ((TextView)findViewById(R.id.title)).setText(title);

        galleryFragment=new GalleryFragment();
        Bundle bundle=new Bundle();
        bundle.putString("src",src);
        bundle.putBoolean("isHomePage",false);
        galleryFragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container,galleryFragment,"GalleryFragment")
                .commit();
    }

    public void startListen(){
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryFragment.upTop();
            }
        });
    }

}
