package com.news.gamersky.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.news.gamersky.R;
import com.news.gamersky.adapter.NewsRecyclerViewAdapter;
import com.news.gamersky.databean.NewDataBean;
import com.news.gamersky.util.AppUtil;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.news.gamersky.util.AppUtil.is2s;

public class CommonNewsFragment extends Fragment {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout midSwipeRefreshLayout;
    private NewsRecyclerViewAdapter newsAdapter;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<NewDataBean> newsData;
    private String nodeId;
    private SharedPreferences sharedPreferences;
    private ExecutorService executor;
    private  int page;
    private  int flag;
    private int lastFlag;
    private boolean firstRun;
    private boolean firstVisible;
    private String src;
    private int nodeIdPos;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_common_news, container, false);
        Bundle args = getArguments();
        if (args != null) {
            src=args.getString("src");
            nodeIdPos=args.getInt("nodeIdPos");
            init(view);
            //loadNews();
            startListen();
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(firstVisible){
            firstVisible=false;
            loadNews();
        }
    }


    public void init(View view){
        newsData=new ArrayList<>();
        newsAdapter=new NewsRecyclerViewAdapter(newsData,getActivity(),null);
        recyclerView=view.findViewById(R.id.recyclerView);
        linearLayoutManager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(newsAdapter);
        recyclerView.setHasFixedSize(true);
        midSwipeRefreshLayout=view.findViewById(R.id.refreshLayout);
        midSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        firstRun=true;
        firstVisible=true;
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
        midSwipeRefreshLayout.setRefreshing(true);
        newsAdapter.setNoMore(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ArrayList<NewDataBean> tempData=new ArrayList<>();
                    Document doc= Jsoup.connect(src).get();
                    Element e=doc.getElementsByClass("pictxt contentpaging")
                            .get(nodeIdPos);
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
                                String id= AppUtil.urlToId(e1.getElementsByTag("a").get(0).attr("href"));
                                String title=e1.getElementsByTag("a").get(1).html();
                                String src1="https://wap.gamersky.com/news/Content-"+id;
                                String date=e1.getElementsByClass("time").get(0).html();
                                String imageUrl=e1.getElementsByTag("img").get(0).attr("src");
                                String sort="娱乐";
                                String commentCount="";
                                tempData.add(new NewDataBean(id,imageUrl,title,src1,date,sort,commentCount));
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

                        }
                        connection.disconnect();
                    }

                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            newsData.clear();
                            newsData.addAll(tempData);
                            newsAdapter.notifyDataSetChanged();
                            midSwipeRefreshLayout.setRefreshing(false);
                            if(!firstRun) {
                                AppUtil.getSnackbar(getContext(), recyclerView, getString(R.string.updata_successed),true,true).show();
                            }
                            firstRun=false;
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            midSwipeRefreshLayout.setRefreshing(false);
                            AppUtil.getSnackbar(getContext(),recyclerView,getString(R.string.updata_failed),true,true).show();
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
                    final ArrayList<NewDataBean> tempData=new ArrayList<>();
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
                                String sort="娱乐";
                                String commentCount="";
                                tempData.add(new NewDataBean(id,imageUrl,title,src1,date,sort,commentCount));
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

                    recyclerView.post(new Runnable() {
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
                    executor.submit(loadMoreNews());
                }
            }
        });
    }

    public void upTop(){
        recyclerView.smoothScrollToPosition(0);
    }

}
