package com.news.gamersky;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.news.gamersky.databean.GameListDataBean;
import com.news.gamersky.fragment.GalleryFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GameGalleryActivity extends AppCompatActivity {
    private static final String TAG="GameGalleryActivity";

    private static final String REVIEW_URL="ku.gamersky.com";
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Document document= Jsoup.connect(src).get();
                    Elements elements=document.getElementsByClass("topnav").get(0).getElementsByTag("a");
                    final Element element=document.getElementsByClass("zqtit").get(0)
                            .getElementsByTag("img").get(0);
                    for (final Element e:elements){
                        if(e.attr("href").contains(REVIEW_URL)){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ImageView reviewBtn=findViewById(R.id.review);
                                    reviewBtn.setVisibility(View.VISIBLE);
                                    reviewBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent=new Intent(GameGalleryActivity.this,GameDetailActivity.class);
                                            GameListDataBean gameListDataBean=new GameListDataBean();
                                            gameListDataBean.title=title;
                                            gameListDataBean.picUrl=element.attr("src");
                                            gameListDataBean.itemUrl=e.attr("href");
                                            intent.putExtra("gameData",gameListDataBean);
                                            startActivity(intent);
                                        }
                                    });
                                }
                            });
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
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
        findViewById(R.id.review).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

}
