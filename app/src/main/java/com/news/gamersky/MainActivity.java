package com.news.gamersky;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;


import android.animation.ObjectAnimator;
import android.content.Intent;


import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.piasy.biv.BigImageViewer;
import com.news.gamersky.customizeview.LoadHeader;
import com.news.gamersky.customizeview.RoundImageView;
import com.news.gamersky.customizeview.ZoomOutPageTransformer;
import com.news.gamersky.databean.NewsDataBean;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity{
    private RefreshLayout refreshLayout;
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
    private Toast toast;
    private NestedScrollView nestedScrollView;
    private MyAdapter myAdapter;
    private MyViewpager2Adapter myViewpager2Adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        updata();
        startListen();
    }


    public void clearGlideDiskCache(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Glide.get(MainActivity.this).clearDiskCache();
                BigImageViewer.imageLoader().cancelAll();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(toast!=null) toast.cancel();
                        toast=showToast("缓存已清空",230,40);
                        toast.show();
                    }
                });
            }
        }).start();
    }

    public void init(){
        timer=new Timer();
        exitTime=0;
        bannerNum=5;
        bannerData=new ArrayList<>();
        topData = new ArrayList<>();
        newsList = new ArrayList<>();

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
        vp = findViewById(R.id.pager);
        vp.setPageTransformer(new ZoomOutPageTransformer());
        vp.setOffscreenPageLimit(4);
        myViewpager2Adapter=new MyViewpager2Adapter(bannerData);
        vp.setAdapter(myViewpager2Adapter);


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setVisibility(View.GONE);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        myAdapter=new MyAdapter(newsList);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setNestedScrollingEnabled(false);


        refreshLayout= (RefreshLayout)findViewById(R.id.refreshLayout1);
        refreshLayout.setRefreshHeader(new LoadHeader(this));
        refreshLayout.setEnableOverScrollDrag(true);
        refreshLayout.setDragRate(0.5f);
        refreshLayout.setDisableContentWhenRefresh(true);

    }


    public void bannerClick(View view){
        System.out.println("你点击了"+vp.getCurrentItem());
        Intent intent=new Intent(this,ArticleActivity.class);
        intent.putExtra("data_src",bannerData.get(vp.getCurrentItem()).src);
        startActivity(intent);
    }

    public void startListen(){
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                updata();
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
                        //System.out.println(vp.getCurrentItem());
                        // System.out.println("定时启动");
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
                clearGlideDiskCache();
                return true;
            case R.id.activity_main_menu2:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void updata(){

        bannerData.clear();
        topData.clear();
        newsList.clear();

        new Thread(){
            @Override
            public void run(){
                Document doc = null;
                try {
                    doc = Jsoup.connect("https://wap.gamersky.com/").get();

                    Elements content = doc.getElementsByAttributeValue("class","countHit");

                    for(int i=0;i<bannerNum;i++) {
                        Elements e1 = content.get(i).getElementsByTag("img");
                        Elements e2 = content.get(i).getElementsByTag("h5");
                        String imageUrl=e1.attr("src");
                        String title=e2.text();
                        String src=content.get(i).attr("href");
                        bannerData.add(new NewsDataBean(imageUrl,title,src));
                    }

                    Elements content1 = doc.getElementsByAttributeValue("class","ymw-todaytop");
                    Elements links = content1.get(0).getElementsByTag("a");

                    for (Element link : links) {
                        String linkHref = link.attr("href");
                        String linkTitle=link.text();
                        topData.add(new NewsDataBean(linkTitle,linkHref));
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
                        newsList.add(new NewsDataBean(s0,s1,s2,s3,s4,s5));
                    }

                    String topic_source_id = "";
                    for(int i=0;i<newsList.size();i++){
                        topic_source_id=topic_source_id+newsList.get(i).id+",";
                    }


                    String src = "https://cm.gamersky.com/commentapi/count?" +
                            "topic_source_id=" +topic_source_id;
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
                        System.out.println(jsonObject.getJSONObject("1291581").getString("comments"));
                        for(int i=0;i<newsList.size();i++){
                            String s=jsonObject.getJSONObject(newsList.get(i).id).getString("comments");
                            newsList.get(i).setCommentCount(s);
                        }
                        connection.disconnect();
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //此时已在主线程中，更新UI
                            System.out.println("更新ui");
                            toptv1.setText(topData.get(0).title);
                            toptv2.setText(topData.get(1).title);

                            myViewpager2Adapter.notifyDataSetChanged();
                            vp.setCurrentItem(bannerNum/2);

                            System.out.println("更新ui完毕");
                            recyclerView.setVisibility(VISIBLE);
                            myAdapter.notifyDataSetChanged();
                            if(toast!=null) toast.cancel();
                            toast=showToast("数据更新成功",230,40);
                            toast.show();
                            topiv.setVisibility(VISIBLE);
                            footv.setVisibility(VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            refreshLayout.finishRefresh(true);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(toast!=null) toast.cancel();
                            toast=showToast("数据更新失败",230,40);
                            toast.show();
                            refreshLayout.finishRefresh(false);
                            }
                    });


                }


            }
        }.start();
    }

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
        public void onBindViewHolder(MyViewHolder holder, final int position) {

            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.textView2.setText(mDataset.get(position).data);
            holder.textView.setText(mDataset.get(position).title);
            holder.textView3.setText(mDataset.get(position).sort);
            holder.textView4.setText(mDataset.get(position).commentCount+"评论");
            // holder.imageView.setImageResource(R.mipmap.ic_launcher);
            Glide.with(holder.imageView)
                    .load(mDataset.get(position).imageUrl)
                    .into(holder.imageView);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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

    public String is2s(InputStream inputStream){
        String str="";
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            str = result.toString(StandardCharsets.UTF_8.name());
        }catch (Exception e){
            e.printStackTrace();
        }
        return str;
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
        //System.out.println("定时取消");
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
