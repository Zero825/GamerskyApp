package com.news.gamersky.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.news.gamersky.ArticleActivity;
import com.news.gamersky.R;
import com.news.gamersky.adapter.NewsAdapter;
import com.news.gamersky.customizeview.BannerViewPager;
import com.news.gamersky.util.AppUtil;
import com.news.gamersky.customizeview.ZoomOutPageTransformer;
import com.news.gamersky.databean.NewsDataBean;

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
    private SwipeRefreshLayout refreshLayout;
    private Timer timer;
    private int bannerNum;
    private BannerViewPager vp;
    private TextView toptv1;
    private TextView toptv2;
    private ImageView topiv;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ConstraintLayout constraintLayout;
    private LinearLayoutManager layoutManager;
    private ArrayList<NewsDataBean> bannerData;
    private ArrayList<NewsDataBean> topData ;
    private ArrayList<NewsDataBean> newsList ;
    private ArrayList<NewsDataBean> tempBannerData;
    private ArrayList<NewsDataBean> tempTopData;
    private ArrayList<NewsDataBean> tempNewsList;
    private NewsAdapter myAdapter;
    private MyViewpagerAdapter myViewpagerAdapter;
    private String nodeId;
    private MyHandler myHandler;
    private ExecutorService executor;
    private int flag;
    private int lastFlag;
    private boolean firstRun;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_homepage, container, false);
        View headerView=inflater.inflate(R.layout.recyclerview_home_header, container, false);
        init(view,headerView);
        loadNews();
        startListen();
        return view;
    }


    public void init(View view,View headerView){

        bannerNum=5;
        flag=0;
        lastFlag=0;
        firstRun=true;

        timer=new Timer();
        bannerData=new ArrayList<>();
        topData = new ArrayList<>();
        newsList = new ArrayList<>();
        tempBannerData=new ArrayList<>();
        tempTopData = new ArrayList<>();
        tempNewsList = new ArrayList<>();

        progressBar=view.findViewById(R.id.progressBar3);

        constraintLayout=headerView.findViewById(R.id.hc);
        vp =headerView.findViewById(R.id.pager);
        vp.setPageTransformer(true,new ZoomOutPageTransformer());
        vp.setOffscreenPageLimit(bannerNum);
        myViewpagerAdapter=new MyViewpagerAdapter(bannerData);
        vp.setAdapter(myViewpagerAdapter);
        toptv1=headerView.findViewById(R.id.textView2);
        toptv2=headerView.findViewById(R.id.textView3);
        topiv=headerView.findViewById(R.id.imageView2);
        topiv.setVisibility(View.INVISIBLE);


        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        myAdapter= new NewsAdapter(newsList, getActivity(),headerView);
        recyclerView.setAdapter(myAdapter);


        refreshLayout= view.findViewById(R.id.refreshLayout1);
        refreshLayout.setColorSchemeResources(R.color.colorAccent);

        myHandler=new MyHandler();
        executor= Executors.newSingleThreadExecutor();
        sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        if(sharedPreferences.getBoolean("page_both_sides",true)){
            constraintLayout.setClipChildren(false);
        }else {
            constraintLayout.setClipChildren(true);
        }
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

        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                timer.cancel();
                timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        final int tp=vp.getCurrentItem();
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                if(vp.getCurrentItem()==bannerNum-1){
                                    vp.setCurrentItem(0);
                                }else {
                                    vp.setCurrentItem(tp + 1);
                                }
                            }
                        });
                    }
                },5000,5000);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastItem=layoutManager.findLastVisibleItemPosition();
                int dataNum=newsList.size();
                int line=dataNum-5;
                
                if(lastItem>100&&lastItem!=flag&&lastItem==line){
                    lastFlag=flag;
                    flag=lastItem;
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

                    Elements content = doc.getElementsByAttributeValue("class","countHit");
                    for(int i=0;i<bannerNum;i++) {
                        Elements e1 = content.get(i).getElementsByTag("img");
                        Elements e2 = content.get(i).getElementsByTag("h5");
                        String imageUrl=e1.attr("src");
                        String title=e2.text();
                        String src=content.get(i).attr("href");
                        tempBannerData.add(new NewsDataBean(imageUrl,title,src));
                    }

                    Elements content1 = doc.getElementsByAttributeValue("class","ymw-todaytop");
                    Elements links = content1.get(0).getElementsByTag("a");

                    for (Element link : links) {
                        String linkHref = link.attr("href");
                        String linkTitle=link.text();
                        tempTopData.add(new NewsDataBean(linkTitle,linkHref));
                    }
                    //System.out.println(content1.toString());

                    Element es=doc.getElementById("listDataArea");
                    //System.out.println(es.toString());

                    Elements content2 = es.getElementsByTag("li");
                    //System.out.println(content2.toString());

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
                        tempNewsList.add(new NewsDataBean(s0,s1,s2,s3,s4,s5,""));
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
                    myHandler.sendMessage(message);


                } catch (Exception e) {
                    e.printStackTrace();
                    Message message=Message.obtain();
                    message.what=0;
                    myHandler.sendMessage(message);

                }


            }
        }.start();
    }

    //这个请求有点慢
    public Thread loadMoreNews(){
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
                    final ArrayList<NewsDataBean> tempData=new ArrayList<>();
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
                            tempData.add(new NewsDataBean(s0,s1,s2,s3,s4,s5,""));

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
                            }
                        }
                    }
                    connection.disconnect();
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            newsList.addAll(tempData);
                            myAdapter.notifyItemRangeInserted(myAdapter.getItemCount(),tempData.size());
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
        recyclerView.scrollToPosition(0);
    }


    public class  MyViewpagerAdapter extends PagerAdapter{
        private ArrayList<NewsDataBean> myData;


        public MyViewpagerAdapter(ArrayList<NewsDataBean> myData){
            this.myData=myData;
        }
        @Override
        public int getCount() {
            return myData.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view==object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View v = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.viewpager_banner, container, false);
            TextView textView=v.findViewById(R.id.textView);
            final RoundedImageView imageView=v.findViewById(R.id.imageView);
            if(!sharedPreferences.getBoolean("corner",false)){
                imageView.setCornerRadius(0);
            }
            textView.setText(myData.get(position).title);
            if(firstRun&&position==0){
                Timer timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        imageView.post(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(imageView)
                                        .load(myData.get(position).imageUrl)
                                        //.transition(DrawableTransitionOptions.withCrossFade())
                                        .centerCrop()
                                        .into(imageView);
                            }
                        });
                    }
                },200);
            }else {
                Glide.with(imageView)
                        .load(myData.get(position).imageUrl)
                        //.transition(DrawableTransitionOptions.withCrossFade())
                        .centerCrop()
                        .into(imageView);
            }
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(getActivity(), ArticleActivity.class);
                    intent.putExtra("new_data",myData.get(position));
                    startActivity(intent);
                }
            });
            container.addView(v);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            if(!bannerData.get(0).title.equals(myData.get(0).title)){
                return POSITION_NONE;
            }else {
                return POSITION_UNCHANGED;
            }

        }
    }



    public class MyHandler extends Handler{
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
                myViewpagerAdapter.notifyDataSetChanged();
                if(firstRun) {
                    vp.setCurrentItem(1);
                }

                System.out.println("更新ui完毕");
                myAdapter.notifyDataSetChanged();
                if(!firstRun){
                    AppUtil.getSnackbar(getContext(),recyclerView,"首页刷新成功",true,true).show();
                }
                firstRun=false;
                topiv.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
            if (msg.what==0){
                AppUtil.getSnackbar(getContext(),recyclerView,"首页加载失败",true,true).show();
                progressBar.setVisibility(View.GONE);
                refreshLayout.setRefreshing(false);
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        //System.out.println("开始定时");

        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        int tp=vp.getCurrentItem();
                        //System.out.println(vp.getCurrentItem());
                        // System.out.println("定时启动");
                        if(vp.getCurrentItem()==bannerNum-1){
                            vp.setCurrentItem(0);
                        }else {
                            vp.setCurrentItem(tp + 1);
                        }
                    }
                });
            }
        },5000,5000);
    }

    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
    }

}
