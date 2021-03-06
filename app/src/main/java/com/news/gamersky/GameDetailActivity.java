package com.news.gamersky;

import android.app.WallpaperColors;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.news.gamersky.adapter.GameHeaderViewpager2Adapter;
import com.news.gamersky.adapter.GameCommentRecyclerViewAdapter;
import com.news.gamersky.customizeview.IndicatorView;
import com.news.gamersky.customizeview.LineFeedRadioGroup;
import com.news.gamersky.customizeview.RoundImageView;
import com.news.gamersky.customizeview.StatisticView;
import com.news.gamersky.databean.CommentDataBean;
import com.news.gamersky.databean.GameDetailDataBean;
import com.news.gamersky.databean.GameListDataBean;
import com.news.gamersky.setting.AppSetting;
import com.news.gamersky.util.AppUtil;
import com.news.gamersky.util.NightModeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.news.gamersky.util.AppUtil.is2s;

public class GameDetailActivity extends AppCompatActivity {
    private final static String TAG="GameDetailActivity";

    private GameListDataBean gameData;
    private ArrayList<String> gamePicList;
    private GameDetailDataBean gameDetailData;
    private ImageView backBtn;
    private TextView title;
    private AppBarLayout appBarLayout;
    private RoundImageView roundImageView;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private GameCommentRecyclerViewAdapter gameCommentRecyclerViewAdapter;
    private ViewPager2 viewPager2;
    private IndicatorView indicatorView;
    private TabLayout reviewsTab;
    private int primaryColor;
    private boolean cardIsDark;
    private String placeholdersPic;
    private int dateType;
    private int loadType;
    private ArrayList<CommentDataBean> commentDataArrayList;
    private ExecutorService executor;
    private  int page;
    private  int flag;
    private int lastFlag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);
        init();
        loadData();
        startListen();
    }

    public void init(){
        dateType=1;
        loadType=1;
        page=1;
        flag=0;
        lastFlag=0;
        executor= Executors.newSingleThreadExecutor();
        commentDataArrayList=new ArrayList<>();

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);

        roundImageView=findViewById(R.id.roundImageView);
        recyclerView=findViewById(R.id.recyclerView);
        viewPager2=findViewById(R.id.viewPager2);
        appBarLayout=findViewById(R.id.appBarLayout);
        indicatorView=findViewById(R.id.indicatorView);
        title=findViewById(R.id.title);
        backBtn=findViewById(R.id.back);
        reviewsTab=findViewById(R.id.reviewsTab);

        gameDetailData=new GameDetailDataBean();
        gamePicList=new ArrayList<>();
        gameData= (GameListDataBean) getIntent().getSerializableExtra("gameData");

        linearLayoutManager=new LinearLayoutManager(this);
        gameCommentRecyclerViewAdapter=new GameCommentRecyclerViewAdapter(this,commentDataArrayList);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(gameCommentRecyclerViewAdapter);
        viewPager2.setAdapter(new GameHeaderViewpager2Adapter(gamePicList));
        viewPager2.setOffscreenPageLimit(2);

        title.setText(gameData.title);
        title.setAlpha(0);

        primaryColor=-1;
        cardIsDark=false;
        //placeholdersPic="https://fakeimg.pl/440x230/282828/eae0d0/?retina=1&text=nopic?";
        placeholdersPic="file:///android_asset/pic/placeholders_pic_null.png";

        findViewById(R.id.header).setMinimumHeight(AppUtil.getStatusBarHeight(this)+AppUtil.dip2px(this,46f));
        ((CardView)findViewById(R.id.cardView)).setRadius(AppSetting.bigRoundCorner);
        ((CardView)findViewById(R.id.cardView1)).setRadius(AppSetting.bigRoundCorner);
        ((CardView)findViewById(R.id.cardView1)).setVisibility(View.GONE);
        Glide.with(roundImageView)
                .asBitmap()
                .load(gameData.picUrl)
                .transition(BitmapTransitionOptions.withCrossFade())
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(AppSetting.smallRoundCorner)))
                .addListener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        primaryColor=WallpaperColors.fromBitmap(resource).getPrimaryColor().toArgb();
                        ((CardView)findViewById(R.id.cardView)).setCardBackgroundColor(primaryColor);
                        findViewById(R.id.appBarLayout).setBackgroundColor(primaryColor);
                        GradientDrawable gradientDrawable=new GradientDrawable();
                        gradientDrawable.setColors(new int[]{Color.TRANSPARENT,primaryColor});
                        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                        findViewById(R.id.gradientView).setBackground(gradientDrawable);
                        cardIsDark=AppUtil.isDark(primaryColor);
                        int color;
                        if(cardIsDark){
                            color=Color.WHITE;
                        }else {
                            color=Color.BLACK;
                        }
                        title.setTextColor(color);
                        ((TextView)findViewById(R.id.gameTitle)).setTextColor(color);
                        ((TextView)findViewById(R.id.enTitle)).setTextColor(color);
                        ((TextView)findViewById(R.id.gameTime)).setTextColor(color);
                        ((TextView)findViewById(R.id.tag)).setTextColor(color);
                        ((TextView)findViewById(R.id.chinese)).setTextColor(color);
                        ((TextView)findViewById(R.id.issue)).setTextColor(color);
                        ((TextView)findViewById(R.id.time)).setTextColor(color);
                        ((TextView)findViewById(R.id.more_images)).setTextColor(color);
                        ((TextView)findViewById(R.id.introduction)).setTextColor(color);
                        LineFeedRadioGroup lineFeedRadioGroup=((LineFeedRadioGroup)findViewById(R.id.lineFeedRadioGroup));
                        for(int i=0;i<lineFeedRadioGroup.getChildCount();i++){
                            RadioButton radioButton= (RadioButton) lineFeedRadioGroup.getChildAt(i);
                            radioButton.setTextColor(color);
                        }
                        return false;
                    }
                })
                .into(roundImageView);

    }

    public void loadData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc= Jsoup.connect(gameData.itemUrl).get();

                    final ArrayList<String> tempStringList = new ArrayList<>();
                    if(doc.getElementsByClass("piclist jscroll-c").size()!=0) {
                        Elements gamePicElements = doc.getElementsByClass("piclist jscroll-c").get(0)
                                .getElementsByTag("a");
                        for (Element e : gamePicElements) {
//                            String dataUrl = e.attr("data-url");
//                            if (dataUrl.indexOf("?") != -1) {
//                                tempStringList.add(dataUrl.substring(dataUrl.indexOf("?") + 1));
//                            }else {
                                tempStringList.add(e.attr("data-pic"));
//                            }
                        }

                    }

                    String id="";
                    String title="";
                    String enTitle="";
                    final ArrayList<GameDetailDataBean.GamePlatformAndTime> gamePlatformAndTimeArrayList=new ArrayList<>();
                    final ArrayList<GameDetailDataBean.Review> reviews=new ArrayList<>();
                    String gameTime="";
                    String gameType="";
                    String supportChinese=" - ";
                    String issue="";
                    String introduction="";
                    String imagesUrl="";

                    if(doc.getElementsByClass("tit_CH").size()!=0){
                        id=doc.getElementsByClass("tit_CH").get(0).attr("gameid");
                        title=doc.getElementsByClass("tit_CH").get(0).html();
                    }
                    if(doc.getElementsByClass("tit_EN").size()!=0){
                        enTitle=doc.getElementsByClass("tit_EN").get(0).html();
                    }
                    if(doc.getElementsByClass("win").size()!=0){
                        for(int i=0;i<doc.getElementsByClass("win").get(0).getElementsByTag("a").size();i++){
                            Element e=doc.getElementsByClass("win").get(0).getElementsByTag("a").get(i);
                            gamePlatformAndTimeArrayList.add(new GameDetailDataBean.GamePlatformAndTime(e.html(),e.attr("data-time")));
                        }
                    }
                    if(doc.getElementsByClass("MTPF-con").size()!=0){
                        Elements reviewsElements=doc.getElementsByClass("MTPF-con").get(0).getElementsByTag("a");
                        for(int i=0;i<reviewsElements.size();i++){
                            Element reviewElement=reviewsElements.get(i);
                            String mediaName,score,content,url;
                            if(reviewElement.getElementsByClass("t2").size()!=0) {
                                mediaName = reviewElement.getElementsByClass("t2").get(0).html();
                                score = reviewElement.getElementsByClass("t1").get(0).getElementsByTag("em").get(0).html()
                                        + reviewElement.getElementsByClass("t1").get(0).getElementsByTag("i").get(0).html();
                                if(Jsoup.parse(reviewElement.attr("data-txt")).getElementsByTag("p").size()!=0) {
                                    content = Jsoup.parse(reviewElement.attr("data-txt")).getElementsByTag("p").get(0).html();
                                }else {
                                    content="";
                                }
                                url = reviewElement.attr("data-url");
                                reviews.add(new GameDetailDataBean.Review(mediaName,score,content,url));
                            }
                        }
                    }
                    if(doc.getElementsByClass("clock").size()!=0){
                        gameTime=doc.getElementsByClass("clock").html();
                    }
                    if(doc.getElementsByClass("tag").size()!=0
                            &&doc.getElementsByClass("tag").get(0).getElementsByTag("a").size()!=0){
                        gameType=doc.getElementsByClass("tag").get(0).getElementsByTag("a").get(0).html();
                    }
                    if(doc.getElementsByClass("div3").get(0).getElementsByClass("tt1").size()>=2){
                        supportChinese=doc.getElementsByClass("div3").get(0)
                                .getElementsByClass("tt1").get(1)
                                .getElementsByClass("txt").get(0)
                                .html();
                    }
                    if(doc.getElementsByClass("div3").get(0).getElementsByClass("tt2").size()>=2){
                        issue=doc.getElementsByClass("div3").get(0)
                                .getElementsByClass("tt2").get(1)
                                .getElementsByClass("txt").get(0)
                                .html();
                    }
                    if(doc.getElementsByClass("con-hide").size()!=0){
                        introduction=doc.getElementsByClass("con-hide").get(0).html();
                    }
                    if(doc.getElementsByClass("more").size()!=0){
                        imagesUrl=doc.getElementsByClass("more").get(0).attr("href");
                    }

                    gameDetailData.id=id;
                    gameDetailData.title=title;
                    gameDetailData.enTitle=enTitle;
                    gameDetailData.gamePlatformAndTimeArrayList=gamePlatformAndTimeArrayList;
                    gameDetailData.reviews=reviews;
                    gameDetailData.gameTime=gameTime;
                    gameDetailData.gameType=gameType;
                    gameDetailData.supportChinese=supportChinese;
                    gameDetailData.issue=issue;
                    gameDetailData.introduction=introduction;
                    gameDetailData.imagesUrl=imagesUrl;

                    //加载评分表
                    loadStatistics(gameDetailData.id);
                    //加载评分
                    loadScore(gameDetailData.id);

                    loadComment(gameDetailData.id);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gamePicList.clear();
                            gamePicList.addAll(tempStringList);
                            if(gamePicList.size()==0){
                                gamePicList.add(placeholdersPic);
                            }
                            viewPager2.getAdapter().notifyDataSetChanged();
                            indicatorView.setIndicatorSize(Math.min(8,tempStringList.size()));

                            ((TextView)findViewById(R.id.gameTitle)).setText(gameDetailData.title);
                            ((TextView)findViewById(R.id.enTitle)).setText(gameDetailData.enTitle);
                            ((TextView)findViewById(R.id.gameTime)).setText(gameDetailData.gameTime);
                            ((TextView)findViewById(R.id.tag)).setText(gameDetailData.gameType);
                            ((TextView)findViewById(R.id.chinese)).setText(getString(R.string.chinese)+":"+gameDetailData.supportChinese);
                            ((TextView)findViewById(R.id.issue)).setText(getString(R.string.game_make)+":"+gameDetailData.issue);
                            if(Html.fromHtml(gameDetailData.introduction).length()>60){
                                ((TextView)findViewById(R.id.introduction)).setText(Html.fromHtml(gameDetailData.introduction).subSequence(0,60)+"...");
                                ((TextView)findViewById(R.id.introduction)).setOnClickListener(new View.OnClickListener() {
                                    private boolean textOpen=false;
                                    @Override
                                    public void onClick(View v) {
                                        if(!textOpen) {
                                            ((TextView) v).setText(Html.fromHtml(gameDetailData.introduction));
                                            textOpen=true;
                                        }else {
                                            ((TextView) v).setText(Html.fromHtml(gameDetailData.introduction).subSequence(0,60)+"...");
                                            textOpen=false;
                                        }

                                    }
                                });
                            }else {
                                ((TextView)findViewById(R.id.introduction)).setText(Html.fromHtml(gameDetailData.introduction));
                            }
                            LineFeedRadioGroup lineFeedRadioGroup=((LineFeedRadioGroup)findViewById(R.id.lineFeedRadioGroup));
                            final TextView time=(TextView)findViewById(R.id.time);
                            lineFeedRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(RadioGroup group, int checkedId) {
                                    RadioButton tempRadioButton=((RadioButton)group.findViewById(checkedId));
                                    if(tempRadioButton!=null) {
                                        for (int i = 0; i < gamePlatformAndTimeArrayList.size(); i++) {
                                            if (tempRadioButton.getText().equals(gamePlatformAndTimeArrayList.get(i).gamePlatform)) {
                                                time.setText(gamePlatformAndTimeArrayList.get(i).issueDate);
                                            }
                                        }
                                    }
                                }
                            });

                            for(int i=0;i<gameDetailData.gamePlatformAndTimeArrayList.size();i++){
                                RadioButton radioButton =new RadioButton(GameDetailActivity.this);
                                radioButton.setText(gameDetailData.gamePlatformAndTimeArrayList.get(i).gamePlatform);
                                radioButton.setButtonDrawable(null);
                                radioButton.setBackgroundResource(R.drawable.bg_selected);
                                if(cardIsDark){
                                    radioButton.setTextColor(Color.WHITE);
                                }else {
                                    radioButton.setTextColor(getResources().getColorStateList(R.color.radio_button_text_color_selector));
                                }
                                radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
                                Context context=lineFeedRadioGroup.getContext();
                                radioButton.setPaddingRelative(AppUtil.dip2px(context,6f),AppUtil.dip2px(context,0f),
                                        AppUtil.dip2px(context,6f),AppUtil.dip2px(context,0f));
                                ViewGroup.MarginLayoutParams marginLayoutParams=new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, AppUtil.dip2px(context,26f));
                                marginLayoutParams.setMargins(AppUtil.dip2px(context,4f),AppUtil.dip2px(context,4f),
                                        AppUtil.dip2px(context, 4f),AppUtil.dip2px(context,0f));
                                lineFeedRadioGroup.addView(radioButton,marginLayoutParams);
                                if(i==0){
                                    radioButton.setChecked(true);
                                }
                            }

                            for(int i=0;i<gameDetailData.reviews.size();i++){
                                TabLayout.Tab tab=reviewsTab.newTab();
                                Log.i(TAG, "run: "+gameDetailData.reviews.get(i).content);
                                reviewsTab.addTab(tab);
                                tab.setText(gameDetailData.reviews.get(i).score+"\n"+gameDetailData.reviews.get(i).mediaName);
                                if(gameDetailData.reviews.get(i).content.equals("")){
                                    ((LinearLayout)reviewsTab.getChildAt(0)).getChildAt(i).setClickable(false);
                                }
                            }
                            if(gameDetailData.reviews.size()>0&&!gameDetailData.reviews.get(0).content.equals("")){
                                ((TextView)findViewById(R.id.reviewText)).setText(gameDetailData.reviews.get(0).content);
                                if(!gameDetailData.reviews.get(0).url.equals("")){
                                    ((TextView)findViewById(R.id.more)).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent();
                                            String url;
                                            if(gameDetailData.reviews.get(0).url.contains("www.gamersky.com")){
                                                url="https://wap.gamersky.com/news/Content-"+AppUtil.urlToId(gameDetailData.reviews.get(0).url);
                                            }else {
                                                url=gameDetailData.reviews.get(0).url;
                                            }
                                            Uri contentUrl = Uri.parse(url);
                                            intent.setAction(Intent.ACTION_VIEW);
                                            intent.setData(contentUrl);
                                            startActivity(intent);
                                        }
                                    });
                                    ((TextView)findViewById(R.id.more)).setVisibility(View.VISIBLE);
                                }else {
                                    ((TextView)findViewById(R.id.more)).setVisibility(View.GONE);
                                }
                            }else {
                                reviewsTab.setVisibility(View.GONE);
                                ((TextView)findViewById(R.id.reviewText)).setVisibility(View.GONE);
                                ((TextView)findViewById(R.id.more)).setVisibility(View.GONE);
                            }
                            reviewsTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                                @Override
                                public void onTabSelected(TabLayout.Tab tab) {
                                    final int p=tab.getPosition();
                                    ((TextView)findViewById(R.id.reviewText)).setText(gameDetailData.reviews.get(p).content);
                                    if(!gameDetailData.reviews.get(p).url.equals("")){
                                        ((TextView)findViewById(R.id.more)).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent();
                                                String url;
                                                if(gameDetailData.reviews.get(p).url.contains("www.gamersky.com")){
                                                    url="https://wap.gamersky.com/news/Content-"+AppUtil.urlToId(gameDetailData.reviews.get(p).url);
                                                }else {
                                                    url=gameDetailData.reviews.get(p).url;
                                                }
                                                Uri contentUrl = Uri.parse(url);
                                                intent.setAction(Intent.ACTION_VIEW);
                                                intent.setData(contentUrl);
                                                startActivity(intent);
                                            }
                                        });
                                        ((TextView)findViewById(R.id.more)).setVisibility(View.VISIBLE);
                                    }else {
                                        ((TextView)findViewById(R.id.more)).setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onTabUnselected(TabLayout.Tab tab) {

                                }

                                @Override
                                public void onTabReselected(TabLayout.Tab tab) {

                                }
                            });

                            if(!gameDetailData.imagesUrl.equals("")){
                                findViewById(R.id.more_images).setVisibility(View.VISIBLE);
                                findViewById(R.id.more_images).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent=new Intent(GameDetailActivity.this, GameGalleryActivity.class);
                                        intent.putExtra("src",gameDetailData.imagesUrl);
                                        intent.putExtra("title",gameDetailData.title);
                                        startActivity(intent);
                                    }
                                });
                            }

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void loadStatistics(final String genneralId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String src = "https://cm1.gamersky.com/apirating/starstatistics?" +
                            "GenneralId=" +genneralId;
                    URL url = new URL(src);
                    //得到connection对象。
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    //设置请求方式
                    connection.setRequestMethod("GET");
                    //连接
                    connection.connect();
                    //得到响应码
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        //得到响应流
                        InputStream inputStream = connection.getInputStream();
                        //将响应流转换成字符串
                        String result = is2s(inputStream);//将流转换为字符串。
                        result=result.substring(1,result.length()-2);
                        JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
                        final ArrayList<Float> statistics=new ArrayList<>();
                        for (int i=jsonArray.length()-1;i>=0;i--){
                            double d=jsonArray.getJSONObject(i).getDouble("percentAge")/100d;
                            statistics.add((float)d);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(statistics.size()>0){
                                    ((StatisticView)findViewById(R.id.statisticView)).setData(statistics);

                                }
                            }
                        });
                    }
                }catch (IOException | JSONException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void loadScore(final String genneralId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String src = "https://cm1.gamersky.com/apirating/getplayersscore?" +
                            "jsondata=" +
                            "{\"genneralId\":" + genneralId+","+
                            "\"num\":" + "10" + "}";
                    URL url = new URL(src);
                    //得到connection对象。
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    //设置请求方式
                    connection.setRequestMethod("GET");
                    //连接
                    connection.connect();
                    //得到响应码
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        //得到响应流
                        InputStream inputStream = connection.getInputStream();
                        //将响应流转换成字符串
                        String result = is2s(inputStream);//将流转换为字符串。
                        result=result.substring(1,result.length()-2);
                        Log.i(TAG, "run: "+result);
                        final String sorce = new JSONObject(result).getString("sorce");
                        final String scoreNum=new JSONObject(result).getString("timesRandom");
                        Log.i(TAG, "run: "+sorce);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                float scoref = 0f;
                                try {
                                    scoref=Float.parseFloat(sorce);
                                    Log.i(TAG, "run: "+scoref);
                                }catch (NumberFormatException e){
                                    e.printStackTrace();
                                }
                                CardView cardView=findViewById(R.id.cardView1);
                                if(scoref!=0f){
                                    if(scoref>=8.0f){
                                        cardView.setCardBackgroundColor(getResources().getColor(R.color.ratingAverageGood));
                                    }else if(scoref>=6.0f){
                                        cardView.setCardBackgroundColor(getResources().getColor(R.color.ratingAverageMid));
                                    }else {
                                        cardView.setCardBackgroundColor(getResources().getColor(R.color.ratingAverageBad));
                                    }
                                    ((TextView)findViewById(R.id.score)).setText(sorce);
                                    ((TextView)findViewById(R.id.scoreNum)).setText(getString(R.string.game_score)+"("+scoreNum+getString(R.string.man)+")");
                                }else {
                                    cardView.setCardBackgroundColor(getResources().getColor(R.color.ratingAverageGood));
                                    ((TextView)findViewById(R.id.score)).setText("--");
                                    ((TextView)findViewById(R.id.scoreNum)).setText(getString(R.string.not_enough_man_reviews));
                                }
                                cardView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }catch (IOException | JSONException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void loadComment(final String genneralId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    page=1;
                    gameCommentRecyclerViewAdapter.setMoreData(true);
                    ImageView loadPic=findViewById(R.id.load_pic);
                    ((AnimationDrawable)loadPic.getDrawable()).start();
                    String pageIndex="1";
                    String pageSize="10";
                    String foorPageSize="5";
                    String articleId=genneralId;
                    String src = "https://cm1.gamersky.com/api/GetComment?" +
                            "jsondata=" +
                            "{\"dateType\":" + dateType + "," +  //
                            "\"loadType\":" + loadType + "," +   //
                            "\"pageIndex\":" + pageIndex + "," +  //
                            "\"pageSize\":" + pageSize + "," +
                            "\"foorPageSize\":" + foorPageSize + "," +
                            "\"articleId\":" + articleId + "}";
                    URL url = new URL(src);
                    //得到connection对象。
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    //设置请求方式
                    connection.setRequestMethod("GET");
                    //连接
                    connection.connect();
                    //得到响应码
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        //得到响应流
                        InputStream inputStream = connection.getInputStream();
                        //将响应流转换成字符串
                        String result = is2s(inputStream);//将流转换为字符串。
                        result = result.substring(1, result.length() - 1);
                        JSONObject commentsJSONObject=new JSONObject(result);
                        String commentsHtml=new JSONObject(commentsJSONObject.getString("body")).getString("Comment");
                        Document commentDocument=Jsoup.parse(commentsHtml);
                        Elements commentElements=commentDocument.getElementsByClass("remark-list-floor");
                        final ArrayList<CommentDataBean> tempData=new ArrayList<>();
                        for(int i=0;i<commentElements.size();i++){
                            Element commentElement=commentElements.get(i);

                            CommentDataBean comment=new CommentDataBean();
                            String commentId="";
                            String userImage="";
                            String userName="";
                            String gamePlatform="";
                            String scoreStar="";
                            String time="";
                            String content="";
                            String likeNum="";
                            String disLikeNum="";

                            try {
                                commentId = commentElement.attr("cmtid");
                                userImage = commentElement.getElementsByTag("img").attr("src");
                                userName = commentElement.getElementsByClass("user-name").get(0).getElementsByTag("a").get(0).html();
                                gamePlatform=commentElement.getElementsByClass("user-xin").get(0).getElementsByClass("txt").get(0).html();
                                scoreStar=commentElement.getElementsByClass("xin").get(0).child(0).attr("class").substring(1);
                                time=commentElement.getElementsByClass("user-time").get(0).getElementsByTag("a").get(0).html();
                                content=commentElement.getElementsByClass("content").get(0).html();
                                likeNum=commentElement.getElementsByClass("remark-support").get(0).html();
                                disLikeNum=commentElement.getElementsByClass("remark-notsupport").get(0).html();
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            comment.commentId=commentId;
                            comment.userImage=userImage;
                            comment.userName=userName;
                            comment.gamePlatform=gamePlatform;
                            comment.scoreStar=scoreStar;
                            comment.time=time;
                            comment.content=content;
                            comment.likeNum=likeNum;
                            comment.disLikeNum=disLikeNum;
                            tempData.add(comment);
                        }

                        String ids="";
                        for(Element e:commentElements){
                            ids+=e.attr("cmtid")+",";
                        }

                        String src1 = "https://cm1.gamersky.com/api/getlike?" +
                                "jsondata=" +ids ;
                        URL url1 = new URL(src1);
                        //得到connection对象。
                        HttpURLConnection connection1 = (HttpURLConnection) url1.openConnection();
                        //设置请求方式
                        connection1.setRequestMethod("GET");
                        //连接
                        connection1.connect();
                        //得到响应码
                        int responseCode1 = connection1.getResponseCode();
                        if (responseCode1 == HttpURLConnection.HTTP_OK) {
                            //得到响应流
                            InputStream inputStream1 = connection1.getInputStream();
                            //将响应流转换成字符串
                            String result1 = is2s(inputStream1);//将流转换为字符串。
                            result1 = result1.substring(1, result1.length() - 1);
                            JSONObject resultJSONObject=new JSONObject(result1);
                            JSONArray commentsJSONArray=new JSONArray(resultJSONObject.getString("body"));
                            for(int i=0;i<commentsJSONArray.length();i++){
                                JSONObject commentJSONObject=commentsJSONArray.getJSONObject(i);
                                tempData.get(i).likeNum=commentJSONObject.getString("Sp");
                                tempData.get(i).disLikeNum=commentJSONObject.getString("Step");
                            }
                            Log.i(TAG, "run: "+result1);
                        }


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ImageView loadPic=findViewById(R.id.load_pic);
                                loadPic.setVisibility(View.GONE);
                                ((AnimationDrawable)loadPic.getDrawable()).stop();
                                commentDataArrayList.clear();
                                commentDataArrayList.addAll(tempData);
                                if(tempData.size()<10){
                                    gameCommentRecyclerViewAdapter.setMoreData(false);
                                }
                                gameCommentRecyclerViewAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }catch (IOException | JSONException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public Thread loadMoreComments(final String genneralId){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    page++;
                    String pageSize="10";
                    String foorPageSize="5";
                    String articleId=genneralId;
                    String src = "https://cm1.gamersky.com/api/GetComment?" +
                            "jsondata=" +
                            "{\"dateType\":" + dateType + "," +  //
                            "\"loadType\":" + loadType + "," +   //
                            "\"pageIndex\":" + page + "," +  //
                            "\"pageSize\":" + pageSize + "," +
                            "\"foorPageSize\":" + foorPageSize + "," +
                            "\"articleId\":" + articleId + "}";
                    URL url = new URL(src);
                    //得到connection对象。
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    //设置请求方式
                    connection.setRequestMethod("GET");
                    //连接
                    connection.connect();
                    //得到响应码
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        //得到响应流
                        InputStream inputStream = connection.getInputStream();
                        //将响应流转换成字符串
                        String result = is2s(inputStream);//将流转换为字符串。
                        result = result.substring(1, result.length() - 1);
                        JSONObject commentsJSONObject=new JSONObject(result);
                        String commentsHtml=new JSONObject(commentsJSONObject.getString("body")).getString("Comment");
                        Document commentDocument=Jsoup.parse(commentsHtml);
                        Elements commentElements=commentDocument.getElementsByClass("remark-list-floor");
                        final ArrayList<CommentDataBean> tempData=new ArrayList<>();
                        for(int i=0;i<commentElements.size();i++){
                            Element commentElement=commentElements.get(i);

                            CommentDataBean comment=new CommentDataBean();
                            String commentId="";
                            String userImage="";
                            String userName="";
                            String gamePlatform="";
                            String scoreStar="";
                            String time="";
                            String content="";
                            String likeNum="";
                            String disLikeNum="";

                            try {
                                commentId = commentElement.attr("cmtid");
                                userImage = commentElement.getElementsByTag("img").attr("src");
                                userName = commentElement.getElementsByClass("user-name").get(0).getElementsByTag("a").get(0).html();
                                gamePlatform=commentElement.getElementsByClass("user-xin").get(0).getElementsByClass("txt").get(0).html();
                                scoreStar=commentElement.getElementsByClass("xin").get(0).child(0).attr("class").substring(1);
                                time=commentElement.getElementsByClass("user-time").get(0).getElementsByTag("a").get(0).html();
                                content=commentElement.getElementsByClass("content").get(0).html();
                                likeNum=commentElement.getElementsByClass("remark-support").get(0).html();
                                disLikeNum=commentElement.getElementsByClass("remark-notsupport").get(0).html();
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            comment.commentId=commentId;
                            comment.userImage=userImage;
                            comment.userName=userName;
                            comment.gamePlatform=gamePlatform;
                            comment.scoreStar=scoreStar;
                            comment.time=time;
                            comment.content=content;
                            comment.likeNum=likeNum;
                            comment.disLikeNum=disLikeNum;
                            tempData.add(comment);
                        }

                        String ids="";
                        for(Element e:commentElements){
                            ids+=e.attr("cmtid")+",";
                        }

                        String src1 = "https://cm1.gamersky.com/api/getlike?" +
                                "jsondata=" +ids ;
                        URL url1 = new URL(src1);
                        //得到connection对象。
                        HttpURLConnection connection1 = (HttpURLConnection) url1.openConnection();
                        //设置请求方式
                        connection1.setRequestMethod("GET");
                        //连接
                        connection1.connect();
                        //得到响应码
                        int responseCode1 = connection1.getResponseCode();
                        if (responseCode1 == HttpURLConnection.HTTP_OK) {
                            //得到响应流
                            InputStream inputStream1 = connection1.getInputStream();
                            //将响应流转换成字符串
                            String result1 = is2s(inputStream1);//将流转换为字符串。
                            result1 = result1.substring(1, result1.length() - 1);
                            JSONObject resultJSONObject=new JSONObject(result1);
                            JSONArray commentsJSONArray=new JSONArray(resultJSONObject.getString("body"));
                            for(int i=0;i<commentsJSONArray.length();i++){
                                JSONObject commentJSONObject=commentsJSONArray.getJSONObject(i);
                                tempData.get(i).likeNum=commentJSONObject.getString("Sp");
                                tempData.get(i).disLikeNum=commentJSONObject.getString("Step");
                            }
                            Log.i(TAG, "run: "+result1);
                        }


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                commentDataArrayList.addAll(tempData);
                                gameCommentRecyclerViewAdapter.notifyDataSetChanged();
                                gameCommentRecyclerViewAdapter.notifyItemRangeInserted(commentDataArrayList.size()-tempData.size(),tempData.size());
                                if(tempData.size()==0||tempData.size()<10){
                                    gameCommentRecyclerViewAdapter.setMoreData(false);
                                    gameCommentRecyclerViewAdapter.notifyItemChanged(gameCommentRecyclerViewAdapter.getItemCount()-1);
                                }
                            }
                        });
                    }
                }catch (IOException | JSONException e){
                    e.printStackTrace();
                    page--;
                    flag=lastFlag;
                }
            }
        });
    }

    public void startListen(){
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            ConstraintLayout bar=findViewById(R.id.back_bar);
            ConstraintLayout gameHeader=findViewById(R.id.game_header);
            ColorDrawable colorDrawable;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                //Log.i(TAG, "onOffsetChanged: "+verticalOffset+"\t"+appBarLayout.getTotalScrollRange());
                if(colorDrawable==null&&primaryColor!=-1){
                    colorDrawable=new ColorDrawable(primaryColor);
                }
                if(colorDrawable==null){
                    return;
                }
                int offset=2000;
                int alpha=-verticalOffset-viewPager2.getHeight();
                Log.i(TAG, "onOffsetChanged: "+alpha);
                bar.setTranslationY(-verticalOffset);
                if(alpha<=255&&alpha>=0) {
                    colorDrawable.setAlpha(alpha);
                }else if(alpha<=255+offset&&alpha>=0){
                    colorDrawable.setAlpha(255);
                }else {
                    colorDrawable.setAlpha(0);
                }
                bar.setBackground(colorDrawable);
                title.setAlpha(alpha/255f);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                indicatorView.setNowPosition(position);
            }
        });

        findViewById(R.id.sortHot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dateType==0){
                    dateType=1;
                    ((TextView)v).setTextColor(getResources().getColor(R.color.colorAccent));
                    ((TextView)findViewById(R.id.sortTime)).setTextColor(getResources().getColor(R.color.defaultColor));
                    loadComment(gameDetailData.id);
                }
            }
        });

        findViewById(R.id.sortTime).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dateType==1){
                    dateType=0;
                    ((TextView)v).setTextColor(getResources().getColor(R.color.colorAccent));
                    ((TextView)findViewById(R.id.sortHot)).setTextColor(getResources().getColor(R.color.defaultColor));
                    loadComment(gameDetailData.id);
                }
            }
        });

        findViewById(R.id.commentPlayer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.smoothScrollToPosition(0);
            }
        });

        findViewById(R.id.title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appBarLayout.setExpanded(true,true);
                recyclerView.scrollToPosition(0);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastItem=linearLayoutManager.findLastVisibleItemPosition();
                int dataNum=commentDataArrayList.size();
                int advanceNum=3;
                int line=dataNum-advanceNum;

                //System.out.println(lastItem+"      "+flag+"       "+line);
                if(lastItem!=flag&&lastItem==line){
                    lastFlag=flag;
                    flag=lastItem;
                    executor.submit(loadMoreComments(gameDetailData.id));
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
