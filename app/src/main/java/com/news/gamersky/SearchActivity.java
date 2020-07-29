package com.news.gamersky;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.news.gamersky.util.AppUtil;
import com.news.gamersky.databean.NewsDataBean;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayoutManager linearLayoutManager;
    private SearchAdapter searchAdapter;
    private ArrayList<NewsDataBean> newsData;
    private ExecutorService executor;
    private  int page;
    private  int flag;
    private int lastFlag;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        init();
        setListen();
    }
    
    private void init(){
        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        progressBar=findViewById(R.id.progressBar4);
        searchView=findViewById(R.id.view_search);
        searchView.setIconifiedByDefault(false);
        searchView.findViewById(R.id.search_plate).setBackgroundResource(R.color.tc);
        searchView.requestFocus();
        recyclerView=findViewById(R.id.list_search);
        linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        newsData=new ArrayList<>();
        searchAdapter=new SearchAdapter(newsData);
        recyclerView.setAdapter(searchAdapter);

        key="";
        page=1;
        flag=0;
        lastFlag=0;
        executor= Executors.newSingleThreadExecutor();
    }

    private void setListen(){
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
                int lastItem=linearLayoutManager.findLastVisibleItemPosition();
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
    }

    private void search(final String query){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Document doc = null;
                try {
                    final ArrayList<NewsDataBean> tempData=new ArrayList<>();
                    doc= Jsoup.connect("http://so.gamersky.com/all/news?s="+query).get();
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
                        tempData.add(new NewsDataBean(
                                id,
                                title,
                                src,
                                date,
                                sort,
                                content
                        ));
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
                            if(es1.size()==0){
                                AppUtil.getSnackbar(SearchActivity.this,recyclerView,getResources().getString(R.string.no_search_result),true,false).show();
                            }
                        }
                    });

                }catch (Exception e){
                    e.printStackTrace();
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
                    final ArrayList<NewsDataBean> tempData=new ArrayList<>();
                    doc= Jsoup.connect("http://so.gamersky.com/all/news?s="+key+"&p="+page).get();
                    final Elements es1=doc.getElementsByClass("txtlist contentpaging")
                            .get(0).getElementsByTag("li");
                    System.out.println("加载成功"+doc.toString());
                    for(int i=0;i<es1.size();i++){
                        Element e1=es1.get(i);
                        String id=AppUtil.urlToId(e1.getElementsByTag("a").get(0).attr("href"));
                        String title=e1.getElementsByTag("a").get(0).html();
                        String src="";
                        String date=e1.getElementsByClass("time").get(0).html();
                        String sort=e1.getElementsByTag("span").get(0).html();
                        String content=e1.getElementsByClass("con").get(0).html();
                        tempData.add(new NewsDataBean(
                                id,
                                title,
                                src,
                                date,
                                sort,
                                content
                        ));
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

    public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        ArrayList<NewsDataBean> mData;
        private String key;
        private boolean moreData;

        public SearchAdapter(ArrayList<NewsDataBean> mData){
            this.mData=mData;
            moreData=true;
        }

        public void setKey(String key){
            this.key=key;
        }

        public void setNoMore(boolean b){
            moreData=!b;
        }

        @Override
        public int getItemViewType(int position){
            if(position==mData.size()){
                return 0;
            }else {
                return 1;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v=null;
            if(viewType==0){
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_header, parent, false);
                return new FooterViewHolder(v);
            }
            if(viewType==1){
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_search, parent, false);
                return new SearchViewHolder(v);
            }
            return new SearchViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            int vt=holder.getItemViewType();
            if(vt==0){
                ((FooterViewHolder)holder).bindView(position);
            }
            if(vt==1){
                ((SearchViewHolder)holder).bindView(position);
            }
        }

        @Override
        public int getItemCount() {
            return mData.size()+1;
        }

        public  class SearchViewHolder extends RecyclerView.ViewHolder {

            public TextView textView1;
            public TextView textView2;
            public TextView textView3;
            public TextView textView4;

            public SearchViewHolder(View v) {
                super(v);
                textView1=v.findViewById(R.id.textView19);
                textView2=v.findViewById(R.id.textView21);
                textView3=v.findViewById(R.id.textView22);
                textView4=v.findViewById(R.id.textView23);
            }

            public void bindView(final int position){
                textView1.setText(mData.get(position).sort);
                textView2.setText(AppUtil.keyTextColor(Html.fromHtml(mData.get(position).title).toString(),key, Color.parseColor("#F01A21")));
                textView3.setText(Html.fromHtml(mData.get(position).content));
                textView4.setText(mData.get(position).date);
                this.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        textView2.setTextColor(getResources().getColor(R.color.defaultColor));
                        textView3.setTextColor(getResources().getColor(R.color.defaultColor));
                        NewsDataBean newData=mData.get(position);
                        newData.title=Html.fromHtml(mData.get(position).title).toString();
                        newData.src="https://wap.gamersky.com/news/Content-"+mData.get(position).id+".html";
                        Intent intent=new Intent(SearchActivity.this,ArticleActivity.class);
                        intent.putExtra("new_data",mData.get(position));
                        startActivity(intent);
                    }
                });
            }
        }

        public  class FooterViewHolder extends RecyclerView.ViewHolder {

            public TextView textView;

            public FooterViewHolder(View v) {
                super(v);
                textView=v.findViewById(R.id.textView8);
            }

            public void bindView(int position){

                if(mData.size()==0){
                    textView.setVisibility(View.GONE);
                }
                else {
                    textView.setVisibility(View.VISIBLE);
                    if(moreData) {
                        textView.setText("请稍等");
                    }else {
                        textView.setText("没有了，没有奇迹了");
                    }
                }
            }
        }
    }
}