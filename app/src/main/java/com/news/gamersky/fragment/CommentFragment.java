package com.news.gamersky.fragment;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.news.gamersky.ImagesBrowser;
import com.news.gamersky.R;
import com.news.gamersky.customizeview.LoadHeader;
import com.news.gamersky.customizeview.MyRecyclerView;
import com.news.gamersky.databean.CommentDataBean;

import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnMultiPurposeListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;


public class CommentFragment extends Fragment {
    private String  data_src;
    private ImageView loadimageView;
    private TextView loadtextView;
    private MyRecyclerView recyclerView;
    private LinearLayout mask;
    private LinearLayoutManager layoutManager;
    private ArrayList<CommentDataBean> hotCommentData;
    private ArrayList<CommentDataBean> allCommentData;
    private CommentAdapter commentAdapter;
    private LinearLayout commentHeader;
    private int mSuspensionHeight;
    private int mCurrentPosition;
    private RefreshLayout refreshLayout;
    private  Document doc;
    private  String srcUrl;
    private  int page;
    private  String sid;
    private  int flag;
    private ExecutorService executor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.comment_show, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        data_src = args.getString("data_src");
        System.out.println("评论片接收到的链接"+data_src);
        init(view);
        loadComment();
        startListener();
    }

    public void init(View view){
        loadimageView=view.findViewById(R.id.imageView9);
        loadtextView=view.findViewById(R.id.textView7);
        recyclerView=view.findViewById(R.id.comment_recycler_view);
        mask=view.findViewById(R.id.mask);
        commentHeader=view.findViewById(R.id.comment_head);
        refreshLayout=view.findViewById(R.id.refreshLayout2);
        hotCommentData=new ArrayList<>();
        allCommentData=new ArrayList<>();
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        commentAdapter=new CommentAdapter(hotCommentData,allCommentData);
        recyclerView.setAdapter(commentAdapter);

        refreshLayout.setRefreshHeader(new LoadHeader(getContext()));
        refreshLayout.setEnableOverScrollDrag(true);
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setDragRate(0.5f);
        refreshLayout.setDisableContentWhenRefresh(true);

        mCurrentPosition = 0;
        page=1;
        flag=0;
        executor= Executors.newSingleThreadExecutor();

    }
    @SuppressLint("ClickableViewAccessibility")
    public void startListener(){
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mSuspensionHeight = commentHeader.getHeight();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (commentAdapter.getItemViewType(mCurrentPosition + 1) == 0
                        ||commentAdapter.getItemViewType(mCurrentPosition + 1) ==1) {
                    View view = layoutManager.findViewByPosition(mCurrentPosition + 1);
                    if (view != null) {
                        if (view.getTop() <= mSuspensionHeight) {
                            commentHeader.setY(-(mSuspensionHeight - view.getTop()));
                        }
                        else {
                            commentHeader.setY(0);
                        }
                    }
                }

                if (mCurrentPosition != layoutManager.findFirstVisibleItemPosition()) {
                    mCurrentPosition = layoutManager.findFirstVisibleItemPosition();
                    if ((commentAdapter.getItemViewType(mCurrentPosition + 1) == 0
                            ||commentAdapter.getItemViewType(mCurrentPosition + 1) ==1)) {
                        View view = layoutManager.findViewByPosition(mCurrentPosition + 1);
                        if (view != null) {
                            if (view.getTop() <= mSuspensionHeight) {
                                commentHeader.setY(-mSuspensionHeight);
                            }
                        }

                    } else {
                        commentHeader.setY(0);
                    }
                    updateSuspensionBar();
                }

               int lastItem=layoutManager.findLastVisibleItemPosition();
               int dataNum=allCommentData.size()+hotCommentData.size();
               int line=dataNum;
               if(lastItem>dataNum){
                   line=dataNum+1;
               }
                //System.out.println(lastItem+"      "+flag+"       "+line);
                if(lastItem>10&&lastItem!=flag&&lastItem==line){
                    flag=lastItem;
                    System.out.println("加载评论");
                    executor.submit(loadMoreComment());
                }

            }
        });
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                loadComment();
            }
        });
        refreshLayout.setOnMultiPurposeListener(new OnMultiPurposeListener() {
            @Override
            public void onHeaderMoving(RefreshHeader header, boolean isDragging, float percent, int offset, int headerHeight, int maxDragHeight) {

            }

            @Override
            public void onHeaderReleased(RefreshHeader header, int headerHeight, int maxDragHeight) {
                mask.setVisibility(View.VISIBLE);
            }

            @Override
            public void onHeaderStartAnimator(RefreshHeader header, int headerHeight, int maxDragHeight) {

            }

            @Override
            public void onHeaderFinish(RefreshHeader header, boolean success) {
                Timer timer = new Timer();
                TimerTask timerTask=new TimerTask() {
                    @Override
                    public void run() {
                        mask.post(new Runnable() {
                            @Override
                            public void run() {
                                mask.setVisibility(View.GONE);
                            }
                        });

                    }
                };
                timer.schedule(timerTask,800);
            }

            @Override
            public void onFooterMoving(RefreshFooter footer, boolean isDragging, float percent, int offset, int footerHeight, int maxDragHeight) {

            }

            @Override
            public void onFooterReleased(RefreshFooter footer, int footerHeight, int maxDragHeight) {

            }

            @Override
            public void onFooterStartAnimator(RefreshFooter footer, int footerHeight, int maxDragHeight) {

            }

            @Override
            public void onFooterFinish(RefreshFooter footer, boolean success) {

            }

            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

            }

            @Override
            public void onStateChanged(@NonNull RefreshLayout refreshLayout, @NonNull RefreshState oldState, @NonNull RefreshState newState) {

            }
        });

        mask.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ViewGroup viewGroup = (ViewGroup) v.getParent();
                viewGroup.requestDisallowInterceptTouchEvent(true);
                return true;
            }
        });

    }

    public void updateSuspensionBar(){
        TextView textView=commentHeader.findViewById(R.id.textView16);
        if(allCommentData.size()==0){
            textView.setText("暂时还没有评论");
        }
        else {
            if (mCurrentPosition <= hotCommentData.size()) {
                textView.setText("热门评论");
            } else {
                textView.setText("全部评论");
            }
        }
    }


    public  void loadComment(){
        ((AnimationDrawable) loadimageView.getDrawable()).start();
        loadtextView.setText("正在加载...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                hotCommentData.clear();
                allCommentData.clear();
                page=1;
                flag=0;
                try {
                    doc = Jsoup.connect(data_src).get();
                    Elements content = doc.getElementsByClass("gsAreaContextArt");
                    srcUrl = content.get(0).getElementsByTag("script").html();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (srcUrl != null && srcUrl.indexOf("https://club") != -1) {
                    System.out.println("特殊处理");
                    try {
                        int i1 = srcUrl.indexOf("h");
                        int i2 = srcUrl.indexOf("\"", i1);
                        srcUrl = srcUrl.substring(i1, i2);
                        System.out.println("特殊处理链接" + srcUrl);
                        i1 = srcUrl.indexOf("activity");
                        i2 = srcUrl.indexOf("?");
                        if(i2==-1){
                            sid = srcUrl.substring(i1 + 9);
                        }
                        else {
                            sid = srcUrl.substring(i1 + 9, i2);
                        }
                        System.out.println("特殊处理sid" + sid);
                        String pageIndex = "1";
                        String pageSize = "10"; //最多条数
                        String sorts = "0"; //排序
                        String isShowHot = "true"; //显示热门
                        String src = "https://club.gamersky.com/club/api/getclubactivity?" +
                                "jsondata=" +
                                "{\"hotSize\":" + "2" + "," +
                                "\"sp\":" + "5" + "," +
                                "\"clubContentId\":" + sid + "," +
                                "\"pageIndex\":" + pageIndex + "," +
                                "\"pageSize\":" + pageSize + "," +
                                "\"sorts\":" + sorts + "," +
                                "\"isShowHot\":" + isShowHot + "}";
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
                            System.out.println(result);
                            final JSONObject jsonObject = new JSONObject(result);
                            JSONArray jsonArray1 = jsonObject.getJSONArray("hotContent");
                            JSONArray jsonArray3 = jsonObject.getJSONArray("content");
                            for (int i = 0; i < jsonArray1.length(); i++) {
                                JSONObject jsonObject1 = jsonArray1.getJSONObject(i);
                                String s1 = jsonObject1.getString("content");
                                Document doc = Jsoup.parse(s1);
                                Elements es1 = doc.getElementsByClass("content");
                                Elements es2 = doc.getElementsByClass("uname");
                                Elements es3 = doc.getElementsByClass("ccmt_time");
                                Elements es4 = doc.getElementsByClass("floor");
                                final Elements es5 = doc.getElementsByClass("digg-btn");
                                Elements es6 = doc.getElementsByClass("userlink")
                                        .get(0).getElementsByTag("img");
                                final JSONArray jsonArray2 = new JSONArray();
                                ArrayList<String> images = new ArrayList<>();
                                try{
                                    Elements es7 = doc.getElementsByClass("qzcmt-picdiv").get(0)
                                            .getElementsByTag("img");

                                    for (int j = 0; j < es7.size(); j++) {
                                        Element element = es7.get(j);
                                        JSONObject jsonObject2 = new JSONObject();
                                        jsonObject2.put("tinysquare", element.attr("src"));
                                        jsonObject2.put("origin", element.attr("src").replace("tinysquare", "origin"));
                                        jsonArray2.put(j, jsonObject2);
                                        images.add(element.attr("src"));
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                String src1 = "https://club.gamersky.com/club/api/getcommentlike?" +
                                        "jsondata=" +
                                        "{\"commentIds\":" + es5.attr("cmtid") + "}";
                                URL url1 = new URL(src1);
                                //得到connection对象。
                                HttpURLConnection connection1 = (HttpURLConnection) url1.openConnection();
                                //设置请求方式
                                connection1.setRequestMethod("GET");
                                //连接
                                connection1.connect();
                                //得到响应码
                                int responseCode1 = connection1.getResponseCode();
                                String s2="0";
                                if (responseCode1 == HttpURLConnection.HTTP_OK) {
                                    //得到响应流
                                    InputStream inputStream1 = connection1.getInputStream();
                                    //将响应流转换成字符串
                                    String result1 = is2s(inputStream1);//将流转换为字符串。
                                    result1 = result1.substring(1, result1.length() - 1);
                                    System.out.println(result1);
                                    JSONObject jsonObject2 = new JSONObject(result1);
                                    String s = jsonObject2.getString("body");
                                    s = s.substring(1, s.length() - 1);
                                    JSONObject jsonObject3 = new JSONObject(s);
                                    System.out.println(jsonObject3.getString("digg"));
                                    s2 = jsonObject3.getString("digg");
                                }
                                connection1.disconnect();
                                hotCommentData.add(new CommentDataBean(
                                        es6.attr("src"),
                                        es2.html(),
                                        es3.html(),
                                        "赞:"+s2,
                                        es1.html(),
                                        es4.html() + "楼",
                                        images, jsonArray2.toString()
                                ));
                            }
                            for (int i = 0; i < jsonArray3.length(); i++) {
                                JSONObject jsonObject1 = jsonArray3.getJSONObject(i);
                                String s1 = jsonObject1.getString("content");
                                Document doc = Jsoup.parse(s1);
                                Elements es1 = doc.getElementsByClass("content");
                                Elements es2 = doc.getElementsByClass("uname");
                                Elements es3 = doc.getElementsByClass("ccmt_time");
                                Elements es4 = doc.getElementsByClass("floor");
                                final Elements es5 = doc.getElementsByClass("digg-btn");
                                Elements es6 = doc.getElementsByClass("userlink")
                                        .get(0).getElementsByTag("img");
                                final JSONArray jsonArray2 = new JSONArray();
                                ArrayList<String> images = new ArrayList<>();
                                try{
                                    Elements es7 = doc.getElementsByClass("qzcmt-picdiv").get(0)
                                            .getElementsByTag("img");

                                    for (int j = 0; j < es7.size(); j++) {
                                        Element element = es7.get(j);
                                        JSONObject jsonObject2 = new JSONObject();
                                        jsonObject2.put("tinysquare", element.attr("src"));
                                        jsonObject2.put("origin", element.attr("src").replace("tinysquare", "origin"));
                                        jsonArray2.put(j, jsonObject2);
                                        images.add(element.attr("src"));
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                String src1 = "https://club.gamersky.com/club/api/getcommentlike?" +
                                        "jsondata=" +
                                        "{\"commentIds\":" + es5.attr("cmtid") + "}";
                                URL url1 = new URL(src1);
                                //得到connection对象。
                                HttpURLConnection connection1 = (HttpURLConnection) url1.openConnection();
                                //设置请求方式
                                connection1.setRequestMethod("GET");
                                //连接
                                connection1.connect();
                                //得到响应码
                                int responseCode1 = connection1.getResponseCode();
                                String s2="0";
                                if (responseCode1 == HttpURLConnection.HTTP_OK) {
                                    //得到响应流
                                    InputStream inputStream1 = connection1.getInputStream();
                                    //将响应流转换成字符串
                                    String result1 = is2s(inputStream1);//将流转换为字符串。
                                    result1 = result1.substring(1, result1.length() - 1);
                                    System.out.println(result1);
                                    JSONObject jsonObject2 = new JSONObject(result1);
                                    String s = jsonObject2.getString("body");
                                    s = s.substring(1, s.length() - 1);
                                    JSONObject jsonObject3 = new JSONObject(s);
                                    System.out.println(jsonObject3.getString("digg"));
                                    s2 = jsonObject3.getString("digg");
                                }
                                connection1.disconnect();
                                allCommentData.add(new CommentDataBean(
                                        es6.attr("src"),
                                        es2.html(),
                                        es3.html(),
                                        "赞:"+s2,
                                        es1.html(),
                                        es4.html() + "楼",
                                        images, jsonArray2.toString()
                                ));
                            }
                        }
                        connection.disconnect();



                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                commentAdapter.notifyDataSetChanged();
                                recyclerView.scheduleLayoutAnimation();
                                loadtextView.setText("加载成功");
                                loadtextView.setVisibility(View.GONE);
                                loadimageView.setVisibility(View.GONE);
                                commentHeader.setVisibility(View.VISIBLE);
                                ((AnimationDrawable) loadimageView.getDrawable()).stop();
                                if(allCommentData.size()!=0&&
                                        (allCommentData.get(allCommentData.size()-1).floor.equals("1楼")||allCommentData.size()<10)){
                                    commentAdapter.setNoMore();
                                }
                                refreshLayout.setEnableRefresh(true);
                                refreshLayout.finishRefresh(true);

                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                ((AnimationDrawable) loadimageView.getDrawable()).stop();
                                loadimageView.setImageResource(R.drawable.load_animation);
                                loadtextView.setText("加载失败");
                                refreshLayout.setEnableRefresh(true);
                                refreshLayout.finishRefresh(false);

                            }
                        });
                    }

                } else {
                    System.out.println("正常处理");

                    try {
                        String pageIndex = "1";
                        String pageSize = "5"; //最多条数
                        String minCount = "5"; //最少赞数
                        String maxCount = "0"; //回复条数
                        sid = doc.getElementsByTag("div").attr("sid");
                        System.out.println("sid=" + sid);
                        String src = "https://cm.gamersky.com/appapi/GetArticleCommentWithClubStyle?" +
                                "request=" +
                                "{\"articleId\":" + sid + "," +
                                "\"minPraisesCount\":" + minCount + "," +
                                "\"repliesMaxCount\":" + maxCount + "," +
                                "\"pageIndex\":" + pageIndex + "," +
                                "\"pageSize\":" + pageSize + "," +
                                "\"order\":\"praiseDESC\"}";
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
                            JSONObject jsonObject = new JSONObject(result);
                            JSONArray jsonArray1 = jsonObject.getJSONObject("result").getJSONArray("comments");
                            for (int i = 0; i < jsonArray1.length(); i++) {
                                JSONObject jsonObject1 = jsonArray1.getJSONObject(i);
                                JSONArray jsonArray2 = jsonObject1.getJSONArray("imageInfes");
                                ArrayList<String> images = new ArrayList<>();
                                if (jsonArray2.length() != 0) {
                                    for (int j = 0; j < jsonArray2.length(); j++) {
                                        JSONObject jsonObject2 = jsonArray2.getJSONObject(j);
                                        images.add(jsonObject2.getString("tinysquare"));
                                    }
                                }
                                hotCommentData.add(new CommentDataBean(
                                        jsonObject1.getString("img_url"),
                                        jsonObject1.getString("nickname"),
                                        format(jsonObject1.getLong("create_time")),
                                        "赞:" + jsonObject1.getString("support_count"),
                                        jsonObject1.getString("content"),
                                        jsonObject1.getString("floorNumber") + "楼",
                                        images, jsonArray2.toString()
                                ));

                            }
                        }
                        connection.disconnect();
                        minCount="0";
                        pageSize="10";
                        String src1 = "https://cm.gamersky.com/appapi/GetArticleCommentWithClubStyle?" +
                                "request=" +
                                "{\"articleId\":" + sid + "," +
                                "\"minPraisesCount\":" + minCount + "," +
                                "\"repliesMaxCount\":" + maxCount + "," +
                                "\"pageIndex\":" + pageIndex + "," +
                                "\"pageSize\":" + pageSize + "," +
                                "\"order\":\"createTimeDESC\"}";
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
                            InputStream inputStream = connection1.getInputStream();
                            //将响应流转换成字符串
                            String result = is2s(inputStream);//将流转换为字符串。
                            JSONObject jsonObject = new JSONObject(result);
                            JSONArray jsonArray1 = jsonObject.getJSONObject("result").getJSONArray("comments");
                            for (int i = 0; i < jsonArray1.length(); i++) {
                                JSONObject jsonObject1 = jsonArray1.getJSONObject(i);
                                JSONArray jsonArray2 = jsonObject1.getJSONArray("imageInfes");
                                ArrayList<String> images = new ArrayList<>();
                                if (jsonArray2.length() != 0) {
                                    for (int j = 0; j < jsonArray2.length(); j++) {
                                        JSONObject jsonObject2 = jsonArray2.getJSONObject(j);
                                        images.add(jsonObject2.getString("tinysquare"));
                                    }
                                }
                                allCommentData.add(new CommentDataBean(
                                        jsonObject1.getString("img_url"),
                                        jsonObject1.getString("nickname"),
                                        format(jsonObject1.getLong("create_time")),
                                        "赞:" + jsonObject1.getString("support_count"),
                                        jsonObject1.getString("content"),
                                        jsonObject1.getString("floorNumber") + "楼",
                                        images, jsonArray2.toString()
                                ));

                            }
                        }
                        connection1.disconnect();
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                commentAdapter.notifyDataSetChanged();
                                recyclerView.scheduleLayoutAnimation();
                                loadtextView.setText("加载成功");
                                loadtextView.setVisibility(View.GONE);
                                loadimageView.setVisibility(View.GONE);
                                commentHeader.setVisibility(View.VISIBLE);
                                ((AnimationDrawable) loadimageView.getDrawable()).stop();
                                if(allCommentData.size()!=0&&
                                        (allCommentData.get(allCommentData.size()-1).floor.equals("1楼")||allCommentData.size()<10)){
                                    commentAdapter.setNoMore();
                                }
                                refreshLayout.setEnableRefresh(true);
                                refreshLayout.finishRefresh(true);

                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                ((AnimationDrawable) loadimageView.getDrawable()).stop();
                                loadimageView.setImageResource(R.drawable.load_animation);
                                loadtextView.setText("加载失败");
                                refreshLayout.setEnableRefresh(true);
                                refreshLayout.finishRefresh(false);
                            }
                        });
                    }
                }

            }
        }).start();
    }

    public  Thread loadMoreComment(){


           return new  Thread(new Runnable() {
                @Override
                public void run() {

                    final String lastCommentFloor1=allCommentData.get(allCommentData.size()-1).floor;
                    page++;
                    if (srcUrl != null && srcUrl.indexOf("https://club") != -1) {
                        try {
                            System.out.println("特殊处理sid" + sid);
                            String pageSize = "10"; //最多条数
                            String sorts = "0"; //排序
                            String isShowHot = "true"; //显示热门
                            String src = "https://club.gamersky.com/club/api/getclubactivity?" +
                                    "jsondata=" +
                                    "{\"hotSize\":" + "2" + "," +
                                    "\"sp\":" + "5" + "," +
                                    "\"clubContentId\":" + sid + "," +
                                    "\"pageIndex\":" + page + "," +
                                    "\"pageSize\":" + pageSize + "," +
                                    "\"sorts\":" + sorts + "," +
                                    "\"isShowHot\":" + isShowHot + "}";
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
                                System.out.println(result);
                                final JSONObject jsonObject = new JSONObject(result);
                                JSONArray jsonArray3 = jsonObject.getJSONArray("content");

                                for (int i = 0; i < jsonArray3.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray3.getJSONObject(i);
                                    String s1 = jsonObject1.getString("content");
                                    Document doc = Jsoup.parse(s1);
                                    Elements es1 = doc.getElementsByClass("content");
                                    Elements es2 = doc.getElementsByClass("uname");
                                    Elements es3 = doc.getElementsByClass("ccmt_time");
                                    Elements es4 = doc.getElementsByClass("floor");
                                    final Elements es5 = doc.getElementsByClass("digg-btn");
                                    Elements es6 = doc.getElementsByClass("userlink")
                                            .get(0).getElementsByTag("img");
                                    final JSONArray jsonArray2 = new JSONArray();
                                    ArrayList<String> images = new ArrayList<>();
                                    try{
                                        Elements es7 = doc.getElementsByClass("qzcmt-picdiv").get(0)
                                                .getElementsByTag("img");

                                        for (int j = 0; j < es7.size(); j++) {
                                            Element element = es7.get(j);
                                            JSONObject jsonObject2 = new JSONObject();
                                            jsonObject2.put("tinysquare", element.attr("src"));
                                            jsonObject2.put("origin", element.attr("src").replace("tinysquare", "origin"));
                                            jsonArray2.put(j, jsonObject2);
                                            images.add(element.attr("src"));
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    String src1 = "https://club.gamersky.com/club/api/getcommentlike?" +
                                            "jsondata=" +
                                            "{\"commentIds\":" + es5.attr("cmtid") + "}";
                                    URL url1 = new URL(src1);
                                    //得到connection对象。
                                    HttpURLConnection connection1 = (HttpURLConnection) url1.openConnection();
                                    //设置请求方式
                                    connection1.setRequestMethod("GET");
                                    //连接
                                    connection1.connect();
                                    //得到响应码
                                    int responseCode1 = connection1.getResponseCode();
                                    String s2="0";
                                    if (responseCode1 == HttpURLConnection.HTTP_OK) {
                                        //得到响应流
                                        InputStream inputStream1 = connection1.getInputStream();
                                        //将响应流转换成字符串
                                        String result1 = is2s(inputStream1);//将流转换为字符串。
                                        result1 = result1.substring(1, result1.length() - 1);
                                        System.out.println(result1);
                                        JSONObject jsonObject2 = new JSONObject(result1);
                                        String s = jsonObject2.getString("body");
                                        s = s.substring(1, s.length() - 1);
                                        JSONObject jsonObject3 = new JSONObject(s);
                                        System.out.println(jsonObject3.getString("digg"));
                                        s2 = jsonObject3.getString("digg");
                                    }
                                    connection1.disconnect();
                                    allCommentData.add(new CommentDataBean(
                                            es6.attr("src"),
                                            es2.html(),
                                            es3.html(),
                                            "赞:"+s2,
                                            es1.html(),
                                            es4.html() + "楼",
                                            images, jsonArray2.toString()
                                    ));
                                }
                            }
                            connection.disconnect();



                            recyclerView.post(new Runnable() {
                                @Override
                                public void run() {
                                    commentAdapter.notifyDataSetChanged();
                                    String lastCommentFloor2=allCommentData.get(allCommentData.size()-1).floor;
                                    if(lastCommentFloor2.equals("1楼")||lastCommentFloor1.equals(lastCommentFloor2)){
                                       commentAdapter.setNoMore();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        try {
                            String pageSize = "10"; //最多条数
                            String minCount = "0"; //最少赞数
                            String maxCount = "0"; //回复条数
                            String src1 = "https://cm.gamersky.com/appapi/GetArticleCommentWithClubStyle?" +
                                    "request=" +
                                    "{\"articleId\":" + sid + "," +
                                    "\"minPraisesCount\":" + minCount + "," +
                                    "\"repliesMaxCount\":" + maxCount + "," +
                                    "\"pageIndex\":" + page + "," +
                                    "\"pageSize\":" + pageSize + "," +
                                    "\"order\":\"createTimeDESC\"}";
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
                                InputStream inputStream = connection1.getInputStream();
                                //将响应流转换成字符串
                                String result = is2s(inputStream);//将流转换为字符串。
                                JSONObject jsonObject = new JSONObject(result);
                                JSONArray jsonArray1 = jsonObject.getJSONObject("result").getJSONArray("comments");
                                for (int i = 0; i < jsonArray1.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray1.getJSONObject(i);
                                    JSONArray jsonArray2 = jsonObject1.getJSONArray("imageInfes");
                                    ArrayList<String> images = new ArrayList<>();
                                    if (jsonArray2.length() != 0) {
                                        for (int j = 0; j < jsonArray2.length(); j++) {
                                            JSONObject jsonObject2 = jsonArray2.getJSONObject(j);
                                            images.add(jsonObject2.getString("tinysquare"));
                                        }
                                    }
                                    allCommentData.add(new CommentDataBean(
                                            jsonObject1.getString("img_url"),
                                            jsonObject1.getString("nickname"),
                                            format(jsonObject1.getLong("create_time")),
                                            "赞:" + jsonObject1.getString("support_count"),
                                            jsonObject1.getString("content"),
                                            jsonObject1.getString("floorNumber") + "楼",
                                            images, jsonArray2.toString()
                                    ));

                                }
                            }
                            connection1.disconnect();
                            recyclerView.post(new Runnable() {
                                @Override
                                public void run() {
                                    System.out.println("加载评论成功");
                                    commentAdapter.notifyDataSetChanged();
                                    updateSuspensionBar();
                                    String lastCommentFloor2=allCommentData.get(allCommentData.size()-1).floor;
                                    if(lastCommentFloor2.equals("1楼")||lastCommentFloor1.equals(lastCommentFloor2)){
                                        commentAdapter.setNoMore();
                                    }
                                }
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }
            });


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

    /**
     * 根据毫秒时间戳来格式化字符串
     * 今天显示今天、昨天显示昨天、前天显示前天.
     * 早于前天的显示具体年-月-日，如2017-06-12；
     * @param timeStamp 毫秒值
     * @return 今天 昨天 前天 或者 yyyy-MM-dd HH:mm:ss类型字符串
     */
    public static String format(long timeStamp) {
        long curTimeMillis = System.currentTimeMillis();
        Date curDate = new Date(curTimeMillis);
        int todayHoursSeconds = curDate.getHours() * 60 * 60;
        int todayMinutesSeconds = curDate.getMinutes() * 60;
        int todaySeconds = curDate.getSeconds();
        int todayMillis = (todayHoursSeconds + todayMinutesSeconds + todaySeconds) * 1000;
        long todayStartMillis = curTimeMillis - todayMillis;
        SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
        if(timeStamp >= todayStartMillis) {
            return "今天 "+sdf1.format(new Date(timeStamp));
        }
        int oneDayMillis = 24 * 60 * 60 * 1000;
        long yesterdayStartMilis = todayStartMillis - oneDayMillis;
        if(timeStamp >= yesterdayStartMilis) {
            return "昨天 "+sdf1.format(new Date(timeStamp));
        }
        long yesterdayBeforeStartMilis = yesterdayStartMilis - oneDayMillis;
        if(timeStamp >= yesterdayBeforeStartMilis) {
            return "前天 "+sdf1.format(new Date(timeStamp));
        }
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return  sdf2.format(new Date(timeStamp));
    }

    public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {
        private ArrayList<CommentDataBean> hotData;
        private ArrayList<CommentDataBean> allData;
        private boolean moreData;


        public CommentAdapter(ArrayList<CommentDataBean> hotData,ArrayList<CommentDataBean> allData){

            this.hotData=hotData;
            this.allData=allData;
            moreData=true;
        }

        public  class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case


            public TextView textView;
            public TextView textView1;
            public TextView textView2;
            public TextView textView3;
            public TextView textView4;
            public TextView textView5;
            public CircleImageView imageView;
            public GridLayout gridLayout;
            public MyViewHolder(View v) {
                super(v);
                textView=v.findViewById(R.id.textView8);
                textView1=v.findViewById(R.id.textView9);
                textView2=v.findViewById(R.id.textView11);
                textView3=v.findViewById(R.id.textView12);
                textView4=v.findViewById(R.id.textView13);
                textView5=v.findViewById(R.id.textView14);
                imageView=v.findViewById(R.id.imageView6);
                gridLayout=v.findViewById(R.id.imagecontainer);

            }
        }

        @Override
        public int getItemViewType(int position){
            int i=2;
            if(position==0){
                i=0;
            }
            if(position==hotData.size()+1){
                i=1;
            }
            if(position==hotData.size()+allData.size()+2){
                i=3;
            }
            return i;
        }


        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v=null;
            if(viewType==0){
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.comment_header, parent, false);
            }
            if(viewType==1){
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.comment_header, parent, false);
            }
            if(viewType==2){
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.comment_view, parent, false);
            }
            if(viewType==3){
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.comment_header, parent, false);
            }
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
            int vt=holder.getItemViewType();
            if(vt==0){
                if(hotData.size()==0){
                    holder.textView.setVisibility(View.GONE);
                }
                else {
                    holder.textView.setVisibility(View.VISIBLE);
                    holder.textView.setText("热门评论");
                }
            }
            if(vt==1){
                if(allData.size()==0){
                    holder.textView.setVisibility(View.GONE);
                }
                else {
                    holder.textView.setVisibility(View.VISIBLE);
                    holder.textView.setText("全部评论");
                }
            }
            if(vt==2){
                if(position<=hotData.size()){
                    final int p=position-1;
                    holder.textView1.setText(hotData.get(p).userName);
                    if(hotData.get(p).content.equals("")){
                        holder.textView2.setVisibility(View.GONE);
                    }else {
                        holder.textView2.setVisibility(View.VISIBLE);
                        holder.textView2.setText(hotData.get(p).content);
                    }
                    holder.textView3.setText(hotData.get(p).time);
                    holder.textView4.setText(hotData.get(p).floor);
                    holder.textView5.setText(hotData.get(p).likeNum);
                    Glide.with(holder.imageView)
                    .load(hotData.get(p).userImage)
                    .centerCrop()
                    .into(holder.imageView);

                    holder.gridLayout.removeAllViews();
                    final ImageView[] imageViews=new ImageView[hotData.get(p).images.size()];
                    for (int i=0;i<hotData.get(p).images.size();i++){
                        View ic = LayoutInflater.from(getContext())
                                .inflate(R.layout.images_container, null, false);
                        imageViews[i]=ic.findViewById(R.id.imageView7);
                        Glide.with(imageViews[i])
                                .load(hotData.get(p).images.get(i))
                                .centerCrop()
                                .into(imageViews[i]);
                        final int finalI = i;
                        imageViews[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getContext(), ImagesBrowser.class);
                                intent.putExtra("imagesSrc", hotData.get(p).imagesJson);
                                intent.putExtra("imagePosition", finalI);
                                //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                                startActivity(intent);
                            }
                        });
                        holder.gridLayout.addView(ic);
                    }
                }else {
                    final int p=position-2-hotData.size();
                    holder.textView1.setText(allData.get(p).userName);
                    if(allData.get(p).content.equals("")){
                        holder.textView2.setVisibility(View.GONE);
                    }else {
                        holder.textView2.setVisibility(View.VISIBLE);
                        holder.textView2.setText(allData.get(p).content);
                    }
                    holder.textView3.setText(allData.get(p).time);
                    holder.textView4.setText(allData.get(p).floor);
                    holder.textView5.setText(allData.get(p).likeNum);
                    Glide.with(holder.imageView)
                            .load(allData.get(p).userImage)
                            .centerCrop()
                            .into(holder.imageView);
                    holder.gridLayout.removeAllViews();
                    for (int i=0;i<allData.get(p).images.size();i++){
                        View ic = LayoutInflater.from(getContext())
                                .inflate(R.layout.images_container, null, false);
                        final ImageView imageView1=ic.findViewById(R.id.imageView7);
                        Glide.with(imageView1)
                                .load(allData.get(p).images.get(i))
                                .centerCrop()
                                .into(imageView1);
                        holder.gridLayout.addView(ic);
                        imageView1.setTransitionName("image"+i);
                        final int finalI = i;
                        ic.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getContext(), ImagesBrowser.class);
                                intent.putExtra("imagesSrc", allData.get(p).imagesJson);
                                intent.putExtra("imagePosition", finalI);
                                //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                                startActivity(intent);

                            }
                        });
                    }

                }

            }
            if(vt==3){
                if(allData.size()==0){
                    holder.textView.setVisibility(View.GONE);
                }
                else {
                    holder.textView.setVisibility(View.VISIBLE);
                    if(moreData) {
                        holder.textView.setText("请稍等");
                    }else {
                        holder.textView.setText("没有了，没有奇迹了");
                    }
                }
            }

        }

        @Override
        public int getItemCount() {
                return hotData.size()+allData.size()+3;

        }
        public void setNoMore(){
            moreData=false;
        }
    }



}
