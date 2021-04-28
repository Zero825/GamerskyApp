package com.news.gamersky.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.WallpaperColors;
import android.content.Intent;


import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.news.gamersky.ArticleActivity;
import com.news.gamersky.R;
import com.news.gamersky.adapter.BannerAdapter;
import com.news.gamersky.adapter.NewsRecyclerViewAdapter;
import com.news.gamersky.customizeview.BannerViewpager;
import com.news.gamersky.customizeview.FixViewPager;
import com.news.gamersky.customizeview.IndicatorView;
import com.news.gamersky.customizeview.RoundImageView;
import com.news.gamersky.setting.AppSetting;
import com.news.gamersky.util.AppUtil;
import com.news.gamersky.databean.NewDataBean;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.news.gamersky.util.AppUtil.is2s;


public class HomePageFragment extends Fragment {
    private static final String TAG="HomePageFragment";

    private View view,headerView;
    private SwipeRefreshLayout refreshLayout;
    private BannerViewpager vp;
    private TextView toptv1;
    private TextView toptv2;
    private ImageView topiv;
    private RecyclerView recyclerView;
    private CircularProgressIndicator progressBar;
    private LinearLayoutManager layoutManager;
    private ArrayList<NewDataBean> bannerData;
    private ArrayList<NewDataBean> topData ;
    private ArrayList<NewDataBean> newsList ;
    private ArrayList<NewDataBean> tempBannerData;
    private ArrayList<NewDataBean> tempTopData;
    private ArrayList<NewDataBean> tempNewsList;
    private NewsRecyclerViewAdapter newsRecyclerViewAdapter;
    private BannerAdapter bannerAdapter;
    private String nodeId;
    private RefreshHandler refreshHandler;
    private ExecutorService executor;
    private int flag;
    private int lastFlag;
    private boolean firstRun;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_homepage, container, false);
        headerView=inflater.inflate(R.layout.recyclerview_home_header, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init(view,headerView);
        loadNews();
        startListen();
    }

    public void init(@NotNull View view, @NotNull View headerView){

        flag=0;
        lastFlag=0;
        firstRun=true;

        bannerData=new ArrayList<>();
        topData = new ArrayList<>();
        newsList = new ArrayList<>();
        tempBannerData=new ArrayList<>();
        tempTopData = new ArrayList<>();
        tempNewsList = new ArrayList<>();

        progressBar=view.findViewById(R.id.progressBar3);

        vp =headerView.findViewById(R.id.pager);
        vp.setPageMargin(AppUtil.dip2px(getContext(),8f));
        bannerAdapter=new BannerAdapter(bannerData);
        vp.setAdapter(bannerAdapter);
        vp.setOffscreenPageLimit(10);

        toptv1=headerView.findViewById(R.id.textView2);
        toptv2=headerView.findViewById(R.id.textView3);
        topiv=headerView.findViewById(R.id.imageView2);
        topiv.setVisibility(View.INVISIBLE);


        recyclerView = view.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        newsRecyclerViewAdapter= new NewsRecyclerViewAdapter(newsList, getContext(),headerView);
        recyclerView.setAdapter(newsRecyclerViewAdapter);
        recyclerView.setHasFixedSize(true);


        refreshLayout= view.findViewById(R.id.refreshLayout1);
        refreshLayout.setColorSchemeResources(R.color.colorAccent);


        refreshHandler=new RefreshHandler();

        executor= Executors.newSingleThreadExecutor();

        sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());


    }

    @SuppressLint("ClickableViewAccessibility")
    public void startListen(){
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               loadNews();

            }
        });
        toptv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("你点击的是第一条头条");
                Intent intent=new Intent(getActivity(),ArticleActivity.class);
                intent.putExtra("new_data",topData.get(0));
                startActivity(intent);
            }
        });
        toptv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("你点击的是第二条头条");
                Intent intent=new Intent(getActivity(),ArticleActivity.class);
                intent.putExtra("new_data",topData.get(1));
                startActivity(intent);
            }
        });


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastItem=layoutManager.findLastVisibleItemPosition();
                int dataNum=newsRecyclerViewAdapter.getItemCount();
                int line=dataNum-5;


                if(lastItem>100&&lastItem!=flag&&lastItem==line){
                    lastFlag=flag;
                    flag=lastItem;
                    //Log.i(TAG, "onScrolled: loadMoreNews");
                    executor.submit(loadMoreNews());
                }
            }
        });
    }

    public void loadNews(){
        tempBannerData.clear();
        tempTopData.clear();
        tempNewsList.clear();
        flag=0;
        lastFlag=0;

        new Thread(){
            @Override
            public void run(){

                Document doc = null;
                try {

                    doc = Jsoup.connect("https://wap.gamersky.com/").get();
                    nodeId=doc.getElementsByAttribute("data-nodeId").attr("data-nodeId");

                    Document wwwDoc=Jsoup.connect("https://www.gamersky.com/").get();

                    Elements content = wwwDoc.getElementsByClass("Bimg").get(0).getElementsByTag("li");
                    for(int i=0;i<content.size();i++) {
                        Elements e1 = content.get(i).getElementsByTag("img");
                        Elements e2 = content.get(i).getElementsByClass("txt");
                        String imageUrl=e1.attr("src");
                        String title=e2.text();
                        String src=content.get(i).getElementsByTag("a").attr("href");
                        if(AppUtil.urlToId(src)!=null) {
                            src = "https://wap.gamersky.com/news/Content-" + AppUtil.urlToId(src);
                            tempBannerData.add(new NewDataBean(imageUrl, title, src));
                        }
                    }

                    Elements content1 = wwwDoc.getElementsByClass("bgx").get(0)
                            .getElementsByClass("h1");
                    Elements links = content1;

                    for (int i=0;i<content1.size();i++) {
                        String linkHref = content1.get(i).getElementsByTag("a").attr("href");
                        String linkTitle=content1.get(i).getElementsByTag("a").text();
                        if(AppUtil.urlToId(linkHref)!=null) {
                            linkHref="https://wap.gamersky.com/news/Content-" + AppUtil.urlToId(linkHref);
                        }
                        tempTopData.add(new NewDataBean(linkTitle,linkHref));
                    }

                    Element es=doc.getElementById("listDataArea");

                    Elements content2 = es.getElementsByTag("li");

                    for(int i=0;i<content2.size();i++){
                        Elements e=content2.get(i).getElementsByTag("img");
                        Elements e1=content2.get(i).getElementsByTag("p");
                        Elements e2=content2.get(i).getElementsByTag("h5");
                        String s0=content2.get(i).attr("data-id");
                        String s1=e.get(0).attr("data-lazysrc");
                        String s2=e2.get(0).html();
                        s2=s2.substring(s2.indexOf("</span>")+7);
                        String s3=content2.get(i).getElementsByTag("a").attr("href");
                        String s4=e1.get(0).getElementsByTag("time").text();
                        String s5=e2.get(0).getElementsByTag("strong").text();
                        tempNewsList.add(new NewDataBean(s0,s1,s2,s3,s4,s5,""));
                    }

                    if(sharedPreferences.getBoolean("load_comments_count",false)) {
                        String topic_source_id = "";
                        for (int i = 0; i < tempNewsList.size(); i++) {
                            topic_source_id = topic_source_id + tempNewsList.get(i).id + ",";
                        }
                        String src = "https://cm.gamersky.com/commentapi/count?" +
                                "topic_source_id=" + topic_source_id;
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
                            JSONObject jsonObject = new JSONObject(result).getJSONObject("result");
                            for (int i = 0; i < tempNewsList.size(); i++) {
                                String s = jsonObject.getJSONObject(tempNewsList.get(i).id).getString("comments");
                                tempNewsList.get(i).setCommentCount(s);
                            }
                            connection.disconnect();
                        }
                    }
                    
                    Message message=Message.obtain();
                    message.what=1;
                    refreshHandler.sendMessage(message);


                } catch (Exception e) {
                    e.printStackTrace();
                    Message message=Message.obtain();
                    message.what=0;
                    refreshHandler.sendMessage(message);

                }


            }
        }.start();
    }

    //这个请求有点慢
    public Thread loadMoreNews(){
        Log.i(TAG, "loadMoreNews: ");
        return new Thread(new Runnable() {
            @Override
            public void run() {
                String id=newsList.get(newsList.size()-1).id;
                String page="1";
                try{
                    String src = "https://db2.gamersky.com/LabelJsonpAjax.aspx?" +
                            "jsondata=" +
                            "{\"type\":\"getwaplabelpage\"," +
                            "\"isCache\":\"true\"," +
                            "\"cacheTime\":\"60\"," +
                            "\"templatekey\":\"newshot\"," +
                            "\"id\":" + id + "," +
                            "\"nodeId\":" + nodeId + "," +
                            "\"page\":" + page + "}";
                    URL url = new URL(src);
                    //得到connection对象。
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    //设置请求方式
                    connection.setRequestMethod("GET");
                    //连接
                    connection.connect();
                    //得到响应码
                    int responseCode = connection.getResponseCode();
                    final ArrayList<NewDataBean> tempData=new ArrayList<>();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        //得到响应流
                        InputStream inputStream = connection.getInputStream();
                        //将响应流转换成字符串
                        String result = is2s(inputStream);//将流转换为字符串。
                        result=result.substring(1,result.length()-1);
                        JSONObject jsonObject=new JSONObject(result);
                        String s = jsonObject.getString("body");
                        Document document=Jsoup.parse(s);
                        Elements content2 = document.getElementsByTag("li");

                        for(int i=0;i<content2.size();i++){
                            Elements e=content2.get(i).getElementsByTag("img");
                            Elements e1=content2.get(i).getElementsByTag("p");
                            Elements e2=content2.get(i).getElementsByTag("h5");
                            String s0=content2.get(i).attr("data-id");
                            String s1=e.get(0).attr("src");
                            String s2=e2.get(0).html();
                            s2=s2.substring(s2.indexOf("</span>")+7);
                            String s3=content2.get(i).getElementsByTag("a").attr("href");
                            String s4=e1.get(0).getElementsByTag("time").text();
                            String s5=e2.get(0).getElementsByTag("strong").text();
                            tempData.add(new NewDataBean(s0,s1,s2,s3,s4,s5,""));

                        }

                        if(sharedPreferences.getBoolean("load_comments_count",false)) {
                            String topic_source_id = "";
                            for (int i = 0; i < tempData.size(); i++) {
                                topic_source_id = topic_source_id + tempData.get(i).id + ",";
                            }
                            String src1 = "https://cm.gamersky.com/commentapi/count?" +
                                    "topic_source_id=" + topic_source_id;
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
                                JSONObject jsonObject1 = new JSONObject(result1).getJSONObject("result");
                                for (int i = 0; i < tempData.size(); i++) {
                                    String s1 = jsonObject1.getJSONObject(tempData.get(i).id).getString("comments");
                                    tempData.get(i).setCommentCount(s1);
                                }
                                connection.disconnect();
                            }else {
                                flag=lastFlag;
                            }
                        }
                    }
                    connection.disconnect();
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            newsList.addAll(tempData);
                            newsRecyclerViewAdapter.notifyItemRangeInserted(newsRecyclerViewAdapter.getItemCount(),tempData.size());
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                    flag=lastFlag;
                }
            }
        });
    }

    public void upTop(){
        recyclerView.smoothScrollToPosition(0);
    }






    public class RefreshHandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            if(msg.what==1){
                refreshLayout.setRefreshing(false);

                bannerData.clear();
                topData.clear();
                newsList.clear();
                bannerData.addAll(tempBannerData);
                topData.addAll(tempTopData);
                newsList.addAll(tempNewsList);
                System.out.println("更新ui");

                toptv1.setText(topData.get(0).title);
                toptv2.setText(topData.get(1).title);
                bannerAdapter.notifyDataSetChanged();
                newsRecyclerViewAdapter.notifyDataSetChanged();

                System.out.println("更新ui完毕");
                if(!firstRun){
                    AppUtil.getSnackbar(getContext(),recyclerView,getString(R.string.updata_successed),true,true).show();
                }
                firstRun=false;
                topiv.setVisibility(View.VISIBLE);
                progressBar.hide();
            }
            if (msg.what==0){
                AppUtil.getSnackbar(getContext(),recyclerView,getString(R.string.updata_failed),true,true).show();
                progressBar.hide();
                refreshLayout.setRefreshing(false);
            }
        }
    }

}
