package com.news.gamersky;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.news.gamersky.adapter.SearchAdapter;
import com.news.gamersky.databean.GameListDataBean;
import com.news.gamersky.setting.AppSetting;
import com.news.gamersky.util.AppUtil;
import com.news.gamersky.databean.NewDataBean;
import com.news.gamersky.util.ReadingProgressUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchActivity extends AppCompatActivity {
    private final static String TAG="SearchActivity";

    private SearchView searchView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private SearchAdapter searchAdapter;
    private ArrayList<NewDataBean> newsData;
    private ExecutorService executor;
    private  int page;
    private  int flag;
    private int lastFlag;
    private String key;
    private String category;
    //value "hot" or "time"
    private String type;
    //value "asc" or "des"
    private String sort;
    private String hotSort;
    private String timeSort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        init();
        startListen();
        autoSearch();
    }
    
    private void init(){
//        getWindow().getDecorView()
//                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);


        category="news";
        key="";
        page=1;
        flag=0;
        lastFlag=0;
        type="hot";
        sort="des";
        hotSort="des";
        timeSort="des";
        if(getIntent().getStringExtra("category")!=null) {
            category = getIntent().getStringExtra("category");
        }

        progressBar=findViewById(R.id.progressBar4);
        searchView=findViewById(R.id.view_search);

        searchView.setIconifiedByDefault(false);
        searchView.findViewById(R.id.search_plate).setBackgroundResource(R.color.tc);
        searchView.requestFocus();
        recyclerView=findViewById(R.id.list_search);
        linearLayoutManager=new LinearLayoutManager(this);
        gridLayoutManager=new GridLayoutManager(this,3);
        if(category.equals("ku")){
            recyclerView.setLayoutManager(gridLayoutManager);
        }else {
            recyclerView.setLayoutManager(linearLayoutManager);
        }

        newsData=new ArrayList<>();
        searchAdapter=new SearchAdapter(this,newsData,category);
        recyclerView.setAdapter(searchAdapter);
        executor= Executors.newSingleThreadExecutor();


    }

    private void startListen(){
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                key=query;
                page=1;
                flag=0;
                lastFlag=0;
                searchAdapter.setNoMore(false);
                searchAdapter.setKey(query);
                progressBar.setVisibility(View.VISIBLE);
                search(query);
                searchView.clearFocus();
                recyclerView.scrollToPosition(0);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastItem;
                if(category.equals("ku")){
                    lastItem = gridLayoutManager.findLastVisibleItemPosition();
                }else {
                    lastItem = linearLayoutManager.findLastVisibleItemPosition();
                }
                int dataNum=newsData.size();
                int line=dataNum-10;

                //System.out.println(lastItem+"      "+flag+"       "+line);
                if(lastItem>19&&lastItem!=flag&&lastItem==line){
                    lastFlag=flag;
                    flag=lastItem;
                    System.out.println("加载搜索数据");
                    executor.submit(moreData());
                }
            }
        });

        final TextView hotSortBtn=findViewById(R.id.textView25);
        final TextView timeSortBtn=findViewById(R.id.textView26);
        hotSortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(type.equals("hot")) {
                    if (hotSort.equals("des")) {
                        sort = "asc";
                        hotSort = "asc";
                        hotSortBtn.setText(R.string.sort_hot_asc);
                    } else {
                        sort = "des";
                        hotSort = "des";
                        hotSortBtn.setText(R.string.sort_hot_desc);
                    }
                }else {
                    type="hot";
                    sort=hotSort;
                    hotSortBtn.setTextColor(getResources().getColor(R.color.colorAccent));
                    timeSortBtn.setTextColor(getResources().getColor(R.color.defaultColor));
                }
                searchView.setQuery(key,true);
            }
        });
        timeSortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(type.equals("time")) {

                    if (timeSort.equals("des")) {
                        sort = "asc";
                        timeSort = "asc";
                        timeSortBtn.setText(R.string.sort_time_asc);
                    } else {
                        sort = "des";
                        timeSort = "des";
                        timeSortBtn.setText(R.string.sort_time_desc);
                    }

                }else {
                    type = "time";
                    sort=timeSort;
                    timeSortBtn.setTextColor(getResources().getColor(R.color.colorAccent));
                    hotSortBtn.setTextColor(getResources().getColor(R.color.defaultColor));
                }
                searchView.setQuery(key,true);
            }
        });

    }

    public void autoSearch(){
        Log.i("TAG", "autoSearch: "+category);
        if(getIntent().getStringExtra("key")!=null){
            key=getIntent().getStringExtra("key");
            searchView.setQuery(key,true);
            searchView.clearFocus();
        }
        if(category.equals("news")){
            findViewById(R.id.type_sort).setVisibility(View.GONE);
        }
        if(category.equals("ku")){
            findViewById(R.id.type_sort).setVisibility(View.GONE);
        }
    }

    private void search(final String query){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Document doc = null;
                try {
                    final ArrayList<NewDataBean> tempData=new ArrayList<>();
                    doc= Jsoup.connect("http://so.gamersky.com/all/"
                            +category+"?s="+query
                            +"&type="+type+"&sort="+sort+"&p="+page).get();
                    if(category.equals("ku")){
                        final Elements es1=doc.getElementsByClass("ImgY contentpaging")
                                .get(0).getElementsByTag("li");
                        for(int i=0;i<es1.size();i++){
                            Element e1=es1.get(i);
                            String title=e1.getElementsByTag("img").get(0).attr("title");
                            String src=e1.getElementsByTag("a").get(0).attr("href");
                            String imageUrl=e1.getElementsByTag("img").get(0).attr("src");
                            tempData.add(new NewDataBean(
                                    imageUrl,
                                    title,
                                    src
                            ));
                        }
                    }else {
                        final Elements es1=doc.getElementsByClass("txtlist contentpaging")
                                .get(0).getElementsByTag("li");
                        for(int i=0;i<es1.size();i++){
                            Element e1=es1.get(i);
                            String id=e1.getElementsByTag("a").get(0).attr("href");
                            id=new StringBuffer(id).reverse().toString();
                            id=id.substring(id.indexOf(".")+1,id.indexOf("/"));
                            id=new StringBuffer(id).reverse().toString();
                            String title=e1.getElementsByTag("a").get(0).html();
                            String src="";
                            String date=e1.getElementsByClass("time").get(0).html();
                            String sort=e1.getElementsByTag("span").get(0).html();
                            String content=e1.getElementsByClass("con").get(0).html();
                            tempData.add(new NewDataBean(
                                    id,
                                    title,
                                    src,
                                    date,
                                    sort,
                                    content
                            ));
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            newsData.clear();
                            newsData.addAll(tempData);
                            if(tempData.size()<30){
                                searchAdapter.setNoMore(true);
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                            searchAdapter.notifyDataSetChanged();
                            if(tempData.size()==0){
                                AppUtil.getSnackbar(SearchActivity.this,recyclerView,getResources().getString(R.string.no_search_result),true,false).show();
                            }
                        }
                    });

                }catch (Exception e){
                    e.printStackTrace();
                    progressBar.setVisibility(View.INVISIBLE);
                    AppUtil.getSnackbar(SearchActivity.this,recyclerView,getResources().getString(R.string.search_faild),true,false).show();
                }
            }
        }).start();
    }

    private Thread moreData(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                Document doc = null;
                page++;
                try {
                    final ArrayList<NewDataBean> tempData=new ArrayList<>();
                    doc= Jsoup.connect("http://so.gamersky.com/all/"
                            +category+"?s="+key+"&type="+type+"&sort="+sort+"&p="+page).get();
                    if(category.equals("ku")){
                        final Elements es1=doc.getElementsByClass("ImgY contentpaging")
                                .get(0).getElementsByTag("li");
                        for(int i=0;i<es1.size();i++){
                            Element e1=es1.get(i);
                            String title=e1.getElementsByTag("img").get(0).attr("title");
                            String src=e1.getElementsByTag("a").get(0).attr("href");
                            String imageUrl=e1.getElementsByTag("img").get(0).attr("src");
                            tempData.add(new NewDataBean(
                                    imageUrl,
                                    title,
                                    src
                            ));
                        }
                    }else {
                        final Elements es1=doc.getElementsByClass("txtlist contentpaging")
                                .get(0).getElementsByTag("li");
                        for(int i=0;i<es1.size();i++){
                            Element e1=es1.get(i);
                            String id=AppUtil.urlToId(e1.getElementsByTag("a").get(0).attr("href"));
                            String title=e1.getElementsByTag("a").get(0).html();
                            String src="";
                            String date=e1.getElementsByClass("time").get(0).html();
                            String sort=e1.getElementsByTag("span").get(0).html();
                            String content=e1.getElementsByClass("con").get(0).html();
                            tempData.add(new NewDataBean(
                                    id,
                                    title,
                                    src,
                                    date,
                                    sort,
                                    content
                            ));
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            newsData.addAll(tempData);
                            searchAdapter.notifyItemRangeInserted(newsData.size()-tempData.size(),tempData.size());
                            if(tempData.size()==0||tempData.size()<30){
                                searchAdapter.setNoMore(true);
                                searchAdapter.notifyItemChanged(searchAdapter.getItemCount()-1);
                            }
                        }
                    });

                }catch (Exception e){
                    e.printStackTrace();
                    page--;
                    flag=lastFlag;
                }
            }
        });
    }


}