package com.news.gamersky.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.news.gamersky.ArticleActivity;
import com.news.gamersky.R;
import com.news.gamersky.customizeview.EndSwipeRefreshLayout;
import com.news.gamersky.databean.NewsDataBean;
import com.news.gamersky.util.AppUtil;
import com.news.gamersky.util.ReadingProgressUtil;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.news.gamersky.util.AppUtil.is2s;

public class InterestingImagesFragment extends Fragment {
    private RecyclerView recyclerView;
    private EndSwipeRefreshLayout midSwipeRefreshLayout;
    private NewsAdapter newsAdapter;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<NewsDataBean> newsData;
    private String nodeId;
    private SharedPreferences sharedPreferences;
    private ExecutorService executor;
    private  int page;
    private  int flag;
    private int lastFlag;
    private boolean firstRun;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_interesting_images, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        init(view);
        loadNews();
        startListen();
    }



    public void init(View view){
        newsData=new ArrayList<>();
        newsAdapter=new NewsAdapter(newsData,getActivity());
        recyclerView=view.findViewById(R.id.recyclerView);
        linearLayoutManager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(newsAdapter);
        midSwipeRefreshLayout=view.findViewById(R.id.refreshLayout);
        midSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        midSwipeRefreshLayout.setRefreshing(true);

        firstRun=true;
        page=1;
        flag=0;
        lastFlag=0;
        executor= Executors.newSingleThreadExecutor();
        sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    public void loadNews(){
        page=1;
        flag=0;
        lastFlag=0;
        newsAdapter.setNoMore(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ArrayList<NewsDataBean> tempData=new ArrayList<>();
                    Document doc= Jsoup.connect("https://www.gamersky.com/ent/qw").get();
                    Element e=doc.getElementsByClass("pictxt contentpaging")
                            .get(0);
                    nodeId=e.attr("data-nodeId");
                    if(nodeId!=null) {
                        String src = "https://db2.gamersky.com/LabelJsonpAjax.aspx?" +
                                "jsondata=" +
                                "{\"type\":\"updatenodelabel\"," +
                                "\"isCache\":\"true\"," +
                                "\"cacheTime\":\"60\"," +
                                "\"nodeId\":" + nodeId + "," +
                                "\"isNodeId\":\"true\"," +
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
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            //得到响应流
                            InputStream inputStream = connection.getInputStream();
                            //将响应流转换成字符串
                            String result = is2s(inputStream);//将流转换为字符串。
                            result = result.substring(1, result.length() - 1);
                            JSONObject jsonObject = new JSONObject(result);
                            String s = jsonObject.getString("body");
                            Document document = Jsoup.parse(s);
                            final Elements es1=document.getElementsByTag("li");
                            for(int i=0;i<es1.size();i++){
                                Element e1=es1.get(i);
                                String id=AppUtil.urlToId(e1.getElementsByTag("a").get(0).attr("href"));
                                String title=e1.getElementsByTag("a").get(1).html();
                                String src1="https://wap.gamersky.com/news/Content-"+id;
                                String date=e1.getElementsByClass("time").get(0).html();
                                String imageUrl=e1.getElementsByTag("img").get(0).attr("src");
                                String sort="囧图";
                                String commentCount="";
                                tempData.add(new NewsDataBean(id,imageUrl,title,src1,date,sort,commentCount));
                            }
                        }
                    }

                    if(sharedPreferences.getBoolean("load_comments_count",false)) {
                        String topic_source_id = "";
                        for (int i = 0; i < tempData.size(); i++) {
                            topic_source_id = topic_source_id + tempData.get(i).id + ",";
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
                            for (int i = 0; i < tempData.size(); i++) {
                                String s = jsonObject.getJSONObject(tempData.get(i).id).getString("comments");
                                tempData.get(i).setCommentCount(s);
                            }
                            connection.disconnect();
                        }
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            newsData.clear();
                            newsData.addAll(tempData);
                            newsAdapter.notifyDataSetChanged();
                            midSwipeRefreshLayout.setRefreshing(false);
                            if(!firstRun) {
                                AppUtil.getSnackbar(getContext(), recyclerView, "数据刷新成功").show();
                            }
                            firstRun=false;
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            midSwipeRefreshLayout.setRefreshing(false);
                            AppUtil.getSnackbar(getContext(),recyclerView,"数据加载失败").show();
                        }
                    });

                }
            }
        }).start();
    }

    public Thread loadMoreNews(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                page++;
                try{
                    final ArrayList<NewsDataBean> tempData=new ArrayList<>();
                    if(nodeId!=null) {
                        String src = "https://db2.gamersky.com/LabelJsonpAjax.aspx?" +
                                "jsondata=" +
                                "{\"type\":\"updatenodelabel\"," +
                                "\"isCache\":\"true\"," +
                                "\"cacheTime\":\"60\"," +
                                "\"nodeId\":" + nodeId + "," +
                                "\"isNodeId\":\"true\"," +
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
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            //得到响应流
                            InputStream inputStream = connection.getInputStream();
                            //将响应流转换成字符串
                            String result = is2s(inputStream);//将流转换为字符串。
                            result = result.substring(1, result.length() - 1);
                            JSONObject jsonObject = new JSONObject(result);
                            String s = jsonObject.getString("body");
                            Document document = Jsoup.parse(s);
                            final Elements es1=document.getElementsByTag("li");
                            for(int i=0;i<es1.size();i++){
                                Element e1=es1.get(i);
                                String id=AppUtil.urlToId(e1.getElementsByTag("a").get(0).attr("href"));
                                String title=e1.getElementsByTag("a").get(1).html();
                                String src1="https://wap.gamersky.com/news/Content-"+id;
                                String date=e1.getElementsByClass("time").get(0).html();
                                String imageUrl=e1.getElementsByTag("img").get(0).attr("src");
                                String sort="囧图";
                                String commentCount="";
                                tempData.add(new NewsDataBean(id,imageUrl,title,src1,date,sort,commentCount));
                            }
                        }
                    }

                    if(sharedPreferences.getBoolean("load_comments_count",false)) {
                        String topic_source_id = "";
                        for (int i = 0; i < tempData.size(); i++) {
                            topic_source_id = topic_source_id + tempData.get(i).id + ",";
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
                            for (int i = 0; i < tempData.size(); i++) {
                                String s = jsonObject.getJSONObject(tempData.get(i).id).getString("comments");
                                tempData.get(i).setCommentCount(s);
                            }
                            connection.disconnect();
                        }
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            newsData.addAll(tempData);
                            newsAdapter.notifyItemRangeInserted(newsData.size()-tempData.size(),tempData.size());
                            if(tempData.size()==0||tempData.size()<14){
                                newsAdapter.setNoMore(true);
                                newsAdapter.notifyItemChanged(newsAdapter.getItemCount()-1);
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

    public void startListen(){
        midSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNews();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastItem=linearLayoutManager.findLastVisibleItemPosition();
                int dataNum=newsData.size();
                int line=dataNum-5;

                //System.out.println(lastItem+"      "+flag+"       "+line);
                if(lastItem>8&&lastItem!=flag&&lastItem==line){
                    lastFlag=flag;
                    flag=lastItem;
                    System.out.println("加载搜索数据");
                    executor.submit(loadMoreNews());
                }
            }
        });
    }

    public void upTop(){
        recyclerView.smoothScrollToPosition(0);
    }

    public class NewsAdapter extends RecyclerView.Adapter {
        private List<NewsDataBean> mDataset;
        private Activity mActivity;
        private boolean moreData;

        public  class NewsListViewHolder extends RecyclerView.ViewHolder {
            public TextView textView;
            public TextView textView2;
            public TextView textView3;
            public TextView textView4;
            public RoundedImageView imageView;

            public NewsListViewHolder(View v) {
                super(v);
                textView = v.findViewById(R.id.textView4);
                textView2 = v.findViewById(R.id.textView5);
                textView3 = v.findViewById(R.id.textView10);
                textView4=v.findViewById(R.id.textView17);
                imageView=v.findViewById(R.id.imageView3);
            }

            public void bindView(final int position){

                textView2.setText(mDataset.get(position).date);
               textView.setText(Html.fromHtml(mDataset.get(position).title));
                if(ReadingProgressUtil.getClick(mActivity,mDataset.get(position).id)){
                    textView.setTextColor(mActivity.getResources().getColor(R.color.defaultColor));
                }else {
                    textView.setTextColor(Color.BLACK);
                }
                textView3.setText(mDataset.get(position).sort);
                if (!mDataset.get(position).commentCount.equals("")) {
                    textView4.setText(mDataset.get(position).commentCount + "评论");
                }else {
                    textView4.setText("");
                }
                if(!sharedPreferences.getBoolean("corner",true)){
                    imageView.setCornerRadius(0);
                }
                Glide.with(imageView)
                        .load(mDataset.get(position).imageUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       textView.setTextColor(mActivity.getResources().getColor(R.color.defaultColor));
                        System.out.println("我是第"+position);
                        ReadingProgressUtil.putClick(mActivity,mDataset.get(position).id,true);
                        Intent intent=new Intent(mActivity, ArticleActivity.class);
                        intent.putExtra("data_src",mDataset.get(position).src);
                        mActivity.startActivity(intent);
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

                if(mDataset.size()==0){
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

        public NewsAdapter(List<NewsDataBean> dataset,Activity activity) {
            mDataset = dataset;
            mActivity = activity;
            moreData=true;
        }

        @Override
        public int getItemViewType(int position){
            int i=0;
            if(position==mDataset.size()){
                i=1;
            }
            return i;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v=null;
            if(viewType==0){
                if(sharedPreferences.getBoolean("new_image_side",false)){
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.recyclerview_new_left, parent, false);
                }else {
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.recyclerview_new, parent, false);
                }
                return new NewsListViewHolder(v);
            }
            if(viewType==1){
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_header, parent, false);
                return new FooterViewHolder(v);
            }
            return new NewsListViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            int vt=holder.getItemViewType();
            if(vt==0){
                ((NewsListViewHolder)holder).bindView(position);
            }
            if(vt==1){
                ((FooterViewHolder)holder).bindView(position);
            }


        }

        @Override
        public int getItemCount() {
            return mDataset.size()+1;
        }

        public void setNoMore(boolean b){
            moreData=!b;
        }

    }

}
