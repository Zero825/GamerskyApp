package com.news.gamersky;

import androidx.annotation.NonNull;;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.piasy.biv.BigImageViewer;
import com.news.gamersky.customizeview.RoundImageView;
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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.news.gamersky.Util.AppUtil.is2s;


public class MainActivity extends AppCompatActivity{
    private SwipeRefreshLayout refreshLayout;
    private Timer timer;
    private long exitTime;
    private int bannerNum;
    private ProgressBar progressBar;
    private ViewPager2 vp;
    private TextView toptv1;
    private TextView toptv2;
    private ImageView topiv;
    private ImageView logo;
    private TextView footv;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ArrayList<NewsDataBean> bannerData;
    private ArrayList<NewsDataBean> topData ;
    private ArrayList<NewsDataBean> newsList ;
    private ArrayList<NewsDataBean> tempBannerData;
    private ArrayList<NewsDataBean> tempTopData;
    private ArrayList<NewsDataBean> tempNewsList;
    private Toast toast;
    private NestedScrollView nestedScrollView;
    private MyAdapter myAdapter;
    private MyViewpager2Adapter myViewpager2Adapter;
    private SharedPreferences sharedPreferences;
    private ArrayList<String> alreadyRead;
    private String nodeId;
    private MyHandler myHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        loadNews();
        startListen();

    }


    public void init(){
        timer=new Timer();
        exitTime=0;
        bannerNum=5;
        bannerData=new ArrayList<>();
        topData = new ArrayList<>();
        newsList = new ArrayList<>();
        tempBannerData=new ArrayList<>();
        tempTopData = new ArrayList<>();
        tempNewsList = new ArrayList<>();
        alreadyRead=new ArrayList<>();

        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        progressBar=findViewById(R.id.progressBar3);
        nestedScrollView=findViewById(R.id.nestedScrollView);
        logo=findViewById(R.id.imageView4);
        toptv1=findViewById(R.id.textView2);
        toptv2=findViewById(R.id.textView3);
        topiv=findViewById(R.id.imageView2);
        footv=findViewById(R.id.textView6);
        topiv.setVisibility(View.INVISIBLE);
        footv.setVisibility(View.INVISIBLE);
        vp = findViewById(R.id.pager);
        vp.setPageTransformer(new ZoomOutPageTransformer());
        vp.setOffscreenPageLimit(4);
        myViewpager2Adapter=new MyViewpager2Adapter(bannerData);
        vp.setAdapter(myViewpager2Adapter);


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        myAdapter=new MyAdapter(newsList);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setNestedScrollingEnabled(false);


        refreshLayout= findViewById(R.id.refreshLayout1);
        refreshLayout.setColorSchemeResources(R.color.colorAccent);
        sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        clearGlideDiskCache(sharedPreferences.getBoolean("auto_clear_cache",true));

        myHandler=new MyHandler();
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


    public void bannerClick(View view){
        System.out.println("你点击了"+vp.getCurrentItem());
        Intent intent=new Intent(this,ArticleActivity.class);
        intent.putExtra("data_src",bannerData.get(vp.getCurrentItem()).src);
        startActivity(intent);
    }

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
                Intent intent=new Intent(MainActivity.this,ArticleActivity.class);
                intent.putExtra("data_src",topData.get(0).src);
                startActivity(intent);
            }
        });
        toptv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("你点击的是第二条头条");
                Intent intent=new Intent(MainActivity.this,ArticleActivity.class);
                intent.putExtra("data_src",topData.get(1).src);
                startActivity(intent);
            }
        });

        vp.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                timer.cancel();
                timer=new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        final int tp=vp.getCurrentItem();
                        runOnUiThread(new Runnable() {
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

        });

        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nestedScrollView.smoothScrollTo(0,0);
            }
        });

        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

            }
        });




    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (toast != null) toast.cancel();
        return super.onMenuOpened(featureId, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.activity_main_menu1:
                Intent intent=new Intent(this,SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.activity_main_menu2:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void loadNews(){
        tempBannerData.clear();
        tempTopData.clear();
        tempNewsList.clear();

        new Thread(){
            @Override
            public void run(){

                Document doc = null;
                try {

                    doc = Jsoup.connect("https://wap.gamersky.com/").get();

                    Elements content = doc.getElementsByAttributeValue("class","countHit");

                    nodeId=doc.getElementsByAttribute("data-nodeId").attr("data-nodeId");
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
                        String s2=e.get(0).attr("alt");
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

    //请求太慢了，放弃
//    public void loadMoreNews(){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String id=newsList.get(newsList.size()-1).id;
//                String page="1";
//                try{
//                    String src = "https://db2.gamersky.com/LabelJsonpAjax.aspx?" +
//                            "jsondata=" +
//                            "{\"type\":\"getwaplabelpage\"," +
//                            "\"isCache\":\"true\"," +
//                            "\"cacheTime\":\"60\"," +
//                            "\"templatekey\":\"newshot\"," +
//                            "\"id\":" + id + "," +
//                            "\"nodeId\":" + nodeId + "," +
//                            "\"page\":" + page + "}";
//                    URL url = new URL(src);
//                    //得到connection对象。
//                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                    //设置请求方式
//                    connection.setRequestMethod("GET");
//                    //连接
//                    connection.connect();
//                    //得到响应码
//                    int responseCode = connection.getResponseCode();
//                    if (responseCode == HttpURLConnection.HTTP_OK) {
//                        //得到响应流
//                        InputStream inputStream = connection.getInputStream();
//                        //将响应流转换成字符串
//                        String result = is2s(inputStream);//将流转换为字符串。
//                        result=result.substring(1,result.length()-1);
//                        JSONObject jsonObject=new JSONObject(result);
//                        String s = jsonObject.getString("body");
//                        Document document=Jsoup.parse(s);
//                        Elements content2 = document.getElementsByTag("li");
//                        for(int i=0;i<content2.size();i++){
//                            Elements e=content2.get(i).getElementsByTag("img");
//                            Elements e1=content2.get(i).getElementsByTag("p");
//                            Elements e2=content2.get(i).getElementsByTag("h5");
//                            String s0=content2.get(i).attr("data-id");
//                            String s1=e.get(0).attr("src");
//                            String s2=e.get(0).attr("alt");
//                            String s3=content2.get(i).getElementsByTag("a").attr("href");
//                            String s4=e1.get(0).getElementsByTag("time").text();
//                            String s5=e2.get(0).getElementsByTag("strong").text();
//                            newsList.add(new NewsDataBean(s0,s1,s2,s3,s4,s5,""));
//
//                        }
//                    }
//                    connection.disconnect();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            //此时已在主线程中，更新UI
//                            refreshLayout.finishLoadMore(true);
//                            //myAdapter.notifyDataSetChanged();
//                        }
//                    });
//                }catch (Exception e){
//                    e.printStackTrace();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            refreshLayout.finishLoadMore(false);
//                        }
//                    });
//                }
//            }
//        }).start();
//    }
    public Toast showToast(final String message,int x,int y){
        final Toast toast = new Toast(MainActivity.this);
        View toastview = getLayoutInflater().inflate(R.layout.toast, null);
        toast.setView(toastview);
        TextView tv = toastview.findViewById(R.id.textView15);
        tv.setText(message);
        toast.setGravity(Gravity.TOP, x, y);
        return toast;
    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private List<NewsDataBean> mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public  class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView textView;
            public TextView textView2;
            public TextView textView3;
            public TextView textView4;
            public RoundImageView imageView;
            public MyViewHolder(View v) {
                super(v);
                textView = v.findViewById(R.id.textView4);
                textView2 = v.findViewById(R.id.textView5);
                textView3 = v.findViewById(R.id.textView10);
                textView4=v.findViewById(R.id.textView17);
                imageView=v.findViewById(R.id.imageView3);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(List<NewsDataBean> myDataset) {
            mDataset = myDataset;
        }


        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.news_list, parent, false);
            return new MyViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {

            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.textView2.setText(mDataset.get(position).date);
            holder.textView.setText(mDataset.get(position).title);
            boolean b=false;
            for(String s:alreadyRead){
                if(s.equals(mDataset.get(position).id)){
                    b=true;
                }
            }
            if(b){
                holder.textView.setTextColor(getResources().getColor(R.color.defaultColor));
            }
            holder.textView3.setText(mDataset.get(position).sort);
            if (!mDataset.get(position).commentCount.equals("")) {
                holder.textView4.setText(mDataset.get(position).commentCount + "评论");
            }else {
                holder.textView4.setText("");
            }

            Glide.with(holder.imageView)
                        .load(mDataset.get(position).imageUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(holder.imageView);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.textView.setTextColor(getResources().getColor(R.color.defaultColor));
                        alreadyRead.add(mDataset.get(position).id);
                        System.out.println("我是第"+position);
                        Intent intent=new Intent(MainActivity.this,ArticleActivity.class);
                        intent.putExtra("data_src",newsList.get(position).src);
                        startActivity(intent);
                    }
                });


        }



        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }


    }

    public class MyViewpager2Adapter extends RecyclerView.Adapter<MyViewpager2Adapter.MyViewHolder> {
        private ArrayList<NewsDataBean> myData;


        public MyViewpager2Adapter(ArrayList<NewsDataBean> myData){
            this.myData=myData;
        }

        public  class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case

            public RoundImageView imageView;
            public TextView textView;
            public MyViewHolder(View v) {
                super(v);
                imageView=v.findViewById(R.id.imageView);
                textView=v.findViewById(R.id.textView);
            }
        }

        @Override
        public int getItemViewType(int position){
            return position;
        }


        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.banner_change, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
            holder.textView.setText(myData.get(position).title);
            Glide.with(holder.imageView)
                    .load(myData.get(position).imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(holder.imageView);

        }

        @Override
        public int getItemCount() {
            return myData.size();
        }
    }

    public class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            if(msg.what==1){
                bannerData.clear();
                topData.clear();
                newsList.clear();
                bannerData.addAll(tempBannerData);
                topData.addAll(tempTopData);
                newsList.addAll(tempNewsList);
                System.out.println("更新ui");
                toptv1.setText(topData.get(0).title);
                toptv2.setText(topData.get(1).title);
                myViewpager2Adapter.notifyDataSetChanged();
                vp.setCurrentItem(bannerNum/2);

                System.out.println("更新ui完毕");
                myAdapter.notifyDataSetChanged();
                if(toast!=null) toast.cancel();
                toast=showToast("数据更新成功",230,40);
                toast.show();
                topiv.setVisibility(View.VISIBLE);
                footv.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                refreshLayout.setRefreshing(false);
            }
            if (msg.what==0){
                if(toast!=null) toast.cancel();
                toast=showToast("数据更新失败",230,40);
                toast.show();
                progressBar.setVisibility(View.GONE);
                refreshLayout.setRefreshing(false);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //System.out.println("开始定时");

        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
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
    protected void onPause() {
        super.onPause();
        if(toast!=null) {
            toast.cancel();
        }
        timer.cancel();
    }

    @Override
    public void onBackPressed() {
        exitApp();
    }

    private void exitApp() {
        if(toast!=null) toast.cancel();
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            toast=showToast("再按一次退出应用",200,40);
            toast.show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }
}
