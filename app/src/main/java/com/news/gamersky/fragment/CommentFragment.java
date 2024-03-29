package com.news.gamersky.fragment;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.news.gamersky.ImagesBrowserActivity;
import com.news.gamersky.R;
import com.news.gamersky.RepliesActivity;
import com.news.gamersky.adapter.CommentAdapter;
import com.news.gamersky.customizeview.RoundImageView;
import com.news.gamersky.util.AppUtil;
import com.news.gamersky.util.CommentEmojiUtil;
import com.news.gamersky.databean.CommentDataBean;

import org.json.JSONArray;
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


import static com.news.gamersky.util.AppUtil.format;
import static com.news.gamersky.util.AppUtil.is2s;


public class CommentFragment extends Fragment {
    private static final String TAG="CommentFragment";

    private String  data_src;
    private ImageView loadimageView;
    private TextView loadtextView;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ArrayList<CommentDataBean> hotCommentData;
    private ArrayList<CommentDataBean> allCommentData;
    private CommentAdapter commentAdapter;
    private ConstraintLayout commentHeader;
    private int mSuspensionHeight;
    private int mCurrentPosition;
    private SwipeRefreshLayout refreshLayout;
    private Document doc;
    private String srcUrl;
    private String order;
    private int page;
    private String sid;
    private int flag;
    private int lastFlag;
    private boolean isFirst;
    private ExecutorService executor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            data_src = args.getString("data_src");
            Log.i("TAG", "init: 评论片接收到的链接"+data_src);
            init();
            loadComment();
            startListener();
        }
    }

    public void init(){
        View view=getView();

        loadimageView=view.findViewById(R.id.imageView9);
        loadtextView=view.findViewById(R.id.textView7);
        recyclerView=view.findViewById(R.id.comment_recycler_view);
        commentHeader=view.findViewById(R.id.comment_head);
        refreshLayout=view.findViewById(R.id.refreshLayout2);
        refreshLayout.setColorSchemeResources(R.color.colorAccent);
        hotCommentData=new ArrayList<>();
        allCommentData=new ArrayList<>();
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        commentAdapter=new CommentAdapter(getContext(),hotCommentData,allCommentData);
        recyclerView.setAdapter(commentAdapter);

        //createTimeDESC or createTimeASC
        order="createTimeDESC";
        mCurrentPosition = 0;
        page=1;
        flag=0;
        lastFlag=0;
        isFirst=true;
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
                int line=commentAdapter.getItemCount()-1;
                //System.out.println(lastItem+"      "+lastFlag+"      "+flag+"       "+line);
                if(lastItem!=flag&&lastItem==line&&!isFirst){
                    lastFlag=flag;
                    flag=line;
                    executor.submit(loadMoreComment());
                }

            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadComment();

            }
        });

        commentHeader.findViewById(R.id.order).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(order.equals("createTimeDESC")){
                    order="createTimeASC";
                }else {
                    order="createTimeDESC";
                }
                refreshLayout.setRefreshing(true);
                loadComment();
            }
        });

    }

    public void updateSuspensionBar(){
        TextView title=commentHeader.findViewById(R.id.textView16);
        TextView orderTitle=commentHeader.findViewById(R.id.order);
        if(allCommentData.size()==0){
            title.setText(getString(R.string.no_comment));
            orderTitle.setVisibility(View.INVISIBLE);
        }
        else {
            if (mCurrentPosition <= hotCommentData.size()) {
                if(isAdded()) {
                    title.setText(getString(R.string.hot_comment));
                    orderTitle.setVisibility(View.INVISIBLE);
                }
            } else {
                if(isAdded()) {
                    title.setText(getString(R.string.all_comment));
                    if (order.equals("createTimeDESC")) {
                        orderTitle.setText(getString(R.string.newest));
                    } else {
                        orderTitle.setText(getString(R.string.earliest));
                    }
                    orderTitle.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public  void loadComment(){
        ((AnimationDrawable) loadimageView.getDrawable()).start();
        loadtextView.setText(getString(R.string.loading));
        commentAdapter.setNoMore(false);
        final ArrayList<CommentDataBean> tempHotCommentData=new ArrayList<>();
        final ArrayList<CommentDataBean> tempAllCommentData=new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {

                page=1;
                flag=0;
                lastFlag=0;
                try {
                    doc = Jsoup.connect(data_src).get();
                    Elements content = doc.getElementsByClass("gsAreaContextArt");
                    srcUrl = content.get(0).getElementsByTag("script").html();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (srcUrl != null && (srcUrl.contains("https://club")||srcUrl.contains("http://club"))) {
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
                        if(order.equals("createTimeASC")) {
                            sorts = "1";
                        }
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
                            final JSONObject jsonObject = new JSONObject(result);
                            JSONArray jsonArray1 = jsonObject.getJSONArray("hotContent");
                            JSONArray jsonArray2 = jsonObject.getJSONArray("content");

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
                                String contentComment=es1.html();
                                try {
                                    String temp=doc.getElementsByClass("ccmt_all").attr("data-content");
                                    if (!temp.equals("")) contentComment=temp;
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                final JSONArray jsonArray = new JSONArray();
                                ArrayList<String> images = new ArrayList<>();
                                if(doc.getElementsByClass("qzcmt-picdiv").size()!=0) {
                                    Elements es7 = doc.getElementsByClass("qzcmt-picdiv").get(0)
                                            .getElementsByTag("li");
                                    for (int j = 0; j < es7.size(); j++) {
                                        Element element1 = es7.get(j).getElementsByTag("img").get(0);
                                        Element element2 = es7.get(j).getElementsByTag("i").get(0);
                                        JSONObject jsonObject2 = new JSONObject();
                                        jsonObject2.put("tinysquare", element1.attr("src"));
                                        String origin = element1.attr("src").replace("tinysquare", "origin")
                                                .replace("small", "origin");
                                        if (element2.attr("class").equals("gif")) {
                                            origin = origin.replace("jpg", "gif");
                                        }
                                        jsonObject2.put("origin", origin);
                                        jsonArray.put(j, jsonObject2);
                                        images.add(element1.attr("src"));
                                    }
                                }
                                JSONArray jsonArray3 = jsonObject1.getJSONArray("replyContent");
                                ArrayList<CommentDataBean> replies=new ArrayList<>();
                                StringBuilder repliesCommentId= new StringBuilder();
                                for(int j=0;j<5&&j<jsonArray3.length();j++){
                                    Document doc1 = Jsoup.parse(jsonArray3.get(j).toString());
                                    String userImage=doc1.getElementsByTag("img").get(0).attr("src");
                                    String userName=doc1.getElementsByClass("uname").get(0).html();
                                    String objectUserName=es2.html();
                                    String time=doc1.getElementsByClass("ccmt_time").get(0).html();
                                    String likeNum=doc1.getElementsByClass("digg-btn").get(0).html();
                                    String content=doc1.getElementsByClass("content").get(0).html();
                                    if(doc1.getElementsByClass("uname").size()>1){
                                        objectUserName=doc1.getElementsByClass("uname").get(1).html();
                                    }
                                    try {
                                        String temp=doc1.getElementsByClass("ccmt_all").attr("data-content");
                                        if (!temp.equals("")) content=temp;
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    replies.add(new CommentDataBean(
                                            userImage,
                                            userName,
                                            time,
                                            likeNum,
                                            content,
                                            objectUserName
                                    ));
                                    repliesCommentId.append(doc1.getElementsByClass("ccmt_reply_cont").get(0).attr("cmtid")).append(",");
                                }
                                String src1 = "https://club.gamersky.com/club/api/getcommentlike?" +
                                        "jsondata=" +
                                        "{\"commentIds\":" +"\""+ es5.attr("cmtid")+","+repliesCommentId +"\""+ "}";
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
                                    JSONObject jsonObject2 = new JSONObject(result1);
                                    String s = jsonObject2.getString("body");
                                   JSONArray jsonArray4=new JSONArray(s);
                                    JSONObject jsonObject3 = jsonArray4.getJSONObject(0);
                                    s2 = jsonObject3.getString("digg");
                                    for(int j=0;j<5&&j<jsonArray3.length();j++){
                                        JSONObject jsonObject4 = jsonArray4.getJSONObject(j+1);
                                        String s3 = jsonObject4.getString("digg");
                                        replies.get(j).setLikeNum(s3);
                                    }
                                }
                                connection1.disconnect();
                                tempHotCommentData.add(new CommentDataBean(
                                        es5.attr("cmtid"),
                                        sid,
                                        es6.attr("src"),
                                        es2.html(),
                                        es3.html(),
                                        "赞:"+s2,
                                        contentComment,
                                        es4.html() + "楼",
                                        images,
                                        jsonArray.toString(),
                                        replies,
                                        jsonObject1.getInt("replyCount")

                                ));
                            }
                            for (int i = 0; i < jsonArray2.length(); i++) {
                                JSONObject jsonObject1 = jsonArray2.getJSONObject(i);
                                String s1 = jsonObject1.getString("content");
                                Document doc = Jsoup.parse(s1);
                                Elements es1 = doc.getElementsByClass("content");
                                Elements es2 = doc.getElementsByClass("uname");
                                Elements es3 = doc.getElementsByClass("ccmt_time");
                                Elements es4 = doc.getElementsByClass("floor");
                                final Elements es5 = doc.getElementsByClass("digg-btn");
                                Elements es6 = doc.getElementsByClass("userlink")
                                        .get(0).getElementsByTag("img");
                                String contentComment=es1.html();
                                try {
                                    String temp=doc.getElementsByClass("ccmt_all").attr("data-content");
                                    if (!temp.equals("")) contentComment=temp;
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                final JSONArray jsonArray = new JSONArray();
                                ArrayList<String> images = new ArrayList<>();
                                if(doc.getElementsByClass("qzcmt-picdiv").size()!=0){
                                    Elements es7 = doc.getElementsByClass("qzcmt-picdiv").get(0)
                                            .getElementsByTag("li");

                                    for (int j = 0; j < es7.size(); j++) {
                                        Element element1 = es7.get(j).getElementsByTag("img").get(0);
                                        Element element2 = es7.get(j).getElementsByTag("i").get(0);
                                        JSONObject jsonObject2 = new JSONObject();
                                        jsonObject2.put("tinysquare", element1.attr("src"));
                                        String origin=element1.attr("src").replace("tinysquare", "origin")
                                                .replace("small", "origin");
                                        if(element2.attr("class").equals("gif")){
                                            origin=origin.replace("jpg","gif");
                                        }
                                        jsonObject2.put("origin",origin);
                                        jsonArray.put(j, jsonObject2);
                                        images.add(element1.attr("src"));
                                    }
                                }
                                JSONArray jsonArray3 = jsonObject1.getJSONArray("replyContent");
                                ArrayList<CommentDataBean> replies=new ArrayList<>();
                                StringBuilder repliesCommentId= new StringBuilder();
                                for(int j=0;j<5&&j<jsonArray3.length();j++){
                                    Document doc1 = Jsoup.parse(jsonArray3.get(j).toString());
                                    String userImage=doc1.getElementsByTag("img").get(0).attr("src");
                                    String userName=doc1.getElementsByClass("uname").get(0).html();
                                    String objectUserName=es2.html();
                                    String time=doc1.getElementsByClass("ccmt_time").get(0).html();
                                    String likeNum=doc1.getElementsByClass("digg-btn").get(0).html();
                                    String content=doc1.getElementsByClass("content").get(0).html();
                                    if(doc1.getElementsByClass("uname").size()>1){
                                        objectUserName=doc1.getElementsByClass("uname").get(1).html();
                                    }
                                    try {
                                        String temp=doc1.getElementsByClass("ccmt_all").attr("data-content");
                                        if (!temp.equals("")) content=temp;
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    replies.add(new CommentDataBean(
                                            userImage,
                                            userName,
                                            time,
                                            likeNum,
                                            content,
                                            objectUserName
                                    ));
                                    repliesCommentId.append(doc1.getElementsByClass("ccmt_reply_cont").get(0).attr("cmtid")).append(",");
                                }

                                String src1 = "https://club.gamersky.com/club/api/getcommentlike?" +
                                        "jsondata=" +
                                        "{\"commentIds\":" +"\""+ es5.attr("cmtid")+","+repliesCommentId +"\""+ "}";
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
                                    JSONObject jsonObject2 = new JSONObject(result1);
                                    String s = jsonObject2.getString("body");
                                    JSONArray jsonArray4=new JSONArray(s);
                                    JSONObject jsonObject3 = jsonArray4.getJSONObject(0);
                                    s2 = jsonObject3.getString("digg");
                                    for(int j=0;j<5&&j<jsonArray3.length();j++){
                                        JSONObject jsonObject4 = jsonArray4.getJSONObject(j+1);
                                        String s3 = jsonObject4.getString("digg");
                                        replies.get(j).setLikeNum(s3);
                                    }
                                }
                                connection1.disconnect();
                                tempAllCommentData.add(new CommentDataBean(
                                        es5.attr("cmtid"),
                                        sid,
                                        es6.attr("src"),
                                        es2.html(),
                                        es3.html(),
                                        "赞:"+s2,
                                        contentComment,
                                        es4.html() + "楼",
                                        images,
                                        jsonArray.toString(),
                                        replies,
                                        jsonObject1.getInt("replyCount")
                                ));
                            }
                        }
                        connection.disconnect();

                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                refreshLayout.setRefreshing(false);
                                hotCommentData.clear();
                                allCommentData.clear();
                                hotCommentData.addAll(tempHotCommentData);
                                allCommentData.addAll(tempAllCommentData);
                                commentAdapter.notifyDataSetChanged();
                                loadtextView.setText("加载成功");
                                loadtextView.setVisibility(View.GONE);
                                loadimageView.setVisibility(View.GONE);
                                commentHeader.setVisibility(View.VISIBLE);
                                ((AnimationDrawable) loadimageView.getDrawable()).stop();
                                if(allCommentData.size()!=0&&
                                        (allCommentData.get(allCommentData.size()-1).floor.equals("1楼")||allCommentData.size()<10)){
                                    commentAdapter.setNoMore(true);
                                }
                                updateSuspensionBar();
                                if(!isFirst) {
                                    AppUtil.getSnackbar(getContext(), recyclerView, getResources().getString(R.string.succeed_comment_load),true,false).show();
                                }
                                isFirst=false;
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
                                refreshLayout.setRefreshing(false);
                                AppUtil.getSnackbar(getContext(),recyclerView,getResources().getString(R.string.error_comment_load),true,false).show();

                            }
                        });
                    }

                } else {
                    System.out.println("正常处理");

                    try {
                        String pageIndex = "1";
                        String pageSize = "5"; //最多条数
                        String minCount = "5"; //最少赞数
                        String maxCount = "5"; //回复条数
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
                                JSONArray jsonArray3 = jsonObject1.getJSONArray("replies");
                                ArrayList<String> images = new ArrayList<>();
                                ArrayList<CommentDataBean> replies=new ArrayList<>();
                                if (jsonArray2.length() != 0) {
                                    for (int j = 0; j < jsonArray2.length(); j++) {
                                        JSONObject jsonObject2 = jsonArray2.getJSONObject(j);
                                        images.add(jsonObject2.getString("tinysquare"));
                                    }
                                }
                                if(jsonArray3.length()!=0){
                                    for (int j = 0; j < jsonArray3.length(); j++) {
                                        JSONObject jsonObject3 = jsonArray3.getJSONObject(j);
                                        replies.add(new CommentDataBean(
                                                jsonObject3.getString("userHeadImageURL"),
                                                jsonObject3.getString("userName"),
                                                format(jsonObject3.getLong("createTime")),
                                                jsonObject3.getString("praisesCount"),
                                                jsonObject3.getString("replyContent"),
                                                jsonObject3.getString("objectUserName")
                                        ));
                                    }
                                }
                                tempHotCommentData.add(new CommentDataBean(
                                        jsonObject1.getString("comment_id"),
                                        jsonObject1.getString("img_url"),
                                        jsonObject1.getString("nickname"),
                                        format(jsonObject1.getLong("create_time")),
                                        "赞:" + jsonObject1.getString("support_count"),
                                        jsonObject1.getString("content"),
                                        jsonObject1.getString("floorNumber") + "楼",
                                        images,
                                        jsonArray2.toString(),
                                        replies,
                                        jsonObject1.getInt("repliesCount")

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
                                "\"order\":"+"\""+order+"\""+"}";
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
                                JSONArray jsonArray3 = jsonObject1.getJSONArray("replies");
                                ArrayList<String> images = new ArrayList<>();
                                ArrayList<CommentDataBean> replies=new ArrayList<>();
                                if (jsonArray2.length() != 0) {
                                    for (int j = 0; j < jsonArray2.length(); j++) {
                                        JSONObject jsonObject2 = jsonArray2.getJSONObject(j);
                                        images.add(jsonObject2.getString("tinysquare"));
                                    }
                                }
                                if(jsonArray3.length()!=0){
                                    for (int j = 0; j < jsonArray3.length(); j++) {
                                        JSONObject jsonObject3 = jsonArray3.getJSONObject(j);
                                        replies.add(new CommentDataBean(
                                                jsonObject3.getString("userHeadImageURL"),
                                                jsonObject3.getString("userName"),
                                                format(jsonObject3.getLong("createTime")),
                                                jsonObject3.getString("praisesCount"),
                                                jsonObject3.getString("replyContent"),
                                                jsonObject3.getString("objectUserName")
                                        ));
                                    }
                                }
                                tempAllCommentData.add(new CommentDataBean(
                                        jsonObject1.getString("comment_id"),
                                        jsonObject1.getString("img_url"),
                                        jsonObject1.getString("nickname"),
                                        format(jsonObject1.getLong("create_time")),
                                        "赞:" + jsonObject1.getString("support_count"),
                                        jsonObject1.getString("content"),
                                        jsonObject1.getString("floorNumber") + "楼",
                                        images,
                                        jsonArray2.toString(),
                                        replies,
                                        jsonObject1.getInt("repliesCount")

                                ));

                            }
                        }
                        connection1.disconnect();
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                refreshLayout.setRefreshing(false);
                                hotCommentData.clear();
                                allCommentData.clear();
                                hotCommentData.addAll(tempHotCommentData);
                                allCommentData.addAll(tempAllCommentData);
                                commentAdapter.notifyDataSetChanged();
                                loadtextView.setText("加载成功");
                                loadtextView.setVisibility(View.GONE);
                                loadimageView.setVisibility(View.GONE);
                                commentHeader.setVisibility(View.VISIBLE);
                                ((AnimationDrawable) loadimageView.getDrawable()).stop();
                                if(allCommentData.size()!=0&&
                                        (allCommentData.get(allCommentData.size()-1).floor.equals("1楼")||allCommentData.size()<10)){
                                    commentAdapter.setNoMore(true);
                                }
                                updateSuspensionBar();
                                if(!isFirst) {
                                    AppUtil.getSnackbar(getContext(), recyclerView, getResources().getString(R.string.succeed_comment_load),true,false).show();
                                }
                                isFirst=false;
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
                                refreshLayout.setRefreshing(false);
                                AppUtil.getSnackbar(getContext(), recyclerView, getResources().getString(R.string.error_comment_load),true,false).show();
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
                    Log.i(TAG, "run: "+"加载评论"+page);
                    final String lastCommentFloor1=allCommentData.get(allCommentData.size()-1).floor;
                    page++;
                    if (srcUrl != null && (srcUrl.contains("https://club")||srcUrl.contains("http://club"))) {
                        try {
                            System.out.println("特殊处理sid" + sid);
                            String pageSize = "10"; //最多条数
                            String sorts = "0"; //排序
                            if(order.equals("createTimeASC")) {
                                sorts = "1";
                            }
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
                            int loadCommentNum=0;
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                //得到响应流
                                InputStream inputStream = connection.getInputStream();
                                //将响应流转换成字符串
                                String result = is2s(inputStream);//将流转换为字符串。
                                result = result.substring(1, result.length() - 1);
                                final JSONObject jsonObject = new JSONObject(result);
                                JSONArray jsonArray2 = jsonObject.getJSONArray("content");
                                loadCommentNum=jsonArray2.length();
                                for (int i = 0; i < jsonArray2.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray2.getJSONObject(i);
                                    String s1 = jsonObject1.getString("content");
                                    Document doc = Jsoup.parse(s1);
                                    Elements es1 = doc.getElementsByClass("content");
                                    Elements es2 = doc.getElementsByClass("uname");
                                    Elements es3 = doc.getElementsByClass("ccmt_time");
                                    Elements es4 = doc.getElementsByClass("floor");
                                    final Elements es5 = doc.getElementsByClass("digg-btn");
                                    Elements es6 = doc.getElementsByClass("userlink")
                                            .get(0).getElementsByTag("img");
                                    String contentComment=es1.html();
                                    try {
                                        String temp=doc.getElementsByClass("ccmt_all").attr("data-content");
                                        if (!temp.equals("")) contentComment=temp;
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    final JSONArray jsonArray = new JSONArray();
                                    ArrayList<String> images = new ArrayList<>();
                                    try{
                                        Elements es7 = doc.getElementsByClass("qzcmt-picdiv").get(0)
                                                .getElementsByTag("li");

                                        for (int j = 0; j < es7.size(); j++) {
                                            Element element1 = es7.get(j).getElementsByTag("img").get(0);
                                            Element element2 = es7.get(j).getElementsByTag("i").get(0);
                                            JSONObject jsonObject2 = new JSONObject();
                                            jsonObject2.put("tinysquare", element1.attr("src"));
                                            String origin=element1.attr("src").replace("tinysquare", "origin")
                                                    .replace("small", "origin");
                                            if(element2.attr("class").equals("gif")){
                                                origin=origin.replace("jpg","gif");
                                            }
                                            jsonObject2.put("origin",origin);
                                            jsonArray.put(j, jsonObject2);
                                            images.add(element1.attr("src"));
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }

                                    JSONArray jsonArray3 = jsonObject1.getJSONArray("replyContent");
                                    ArrayList<CommentDataBean> replies=new ArrayList<>();
                                    String repliesCommentId="";
                                    for(int j=0;j<5&&j<jsonArray3.length();j++){
                                        Document doc1 = Jsoup.parse(jsonArray3.get(j).toString());
                                        String userImage=doc1.getElementsByTag("img").get(0).attr("src");
                                        String userName=doc1.getElementsByClass("uname").get(0).html();
                                        String objectUserName=es2.html();
                                        String time=doc1.getElementsByClass("ccmt_time").get(0).html();
                                        String likeNum=doc1.getElementsByClass("digg-btn").get(0).html();
                                        String content=doc1.getElementsByClass("content").get(0).html();
                                        try{
                                            objectUserName=doc1.getElementsByClass("uname").get(1).html();
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        try {
                                            String temp=doc1.getElementsByClass("ccmt_all").attr("data-content");
                                            if (!temp.equals("")) content=temp;
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        replies.add(new CommentDataBean(
                                                userImage,
                                                userName,
                                                time,
                                                likeNum,
                                                content,
                                                objectUserName
                                        ));
                                        repliesCommentId+=
                                                doc1.getElementsByClass("ccmt_reply_cont").get(0).attr("cmtid")+",";
                                    }
                                    String src1 = "https://club.gamersky.com/club/api/getcommentlike?" +
                                            "jsondata=" +
                                            "{\"commentIds\":" +"\""+ es5.attr("cmtid")+","+repliesCommentId +"\""+ "}";
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
                                        JSONObject jsonObject2 = new JSONObject(result1);
                                        String s = jsonObject2.getString("body");
                                        JSONArray jsonArray4=new JSONArray(s);
                                        JSONObject jsonObject3 = jsonArray4.getJSONObject(0);
                                        s2 = jsonObject3.getString("digg");
                                        for(int j=0;j<5&&j<jsonArray3.length();j++){
                                            JSONObject jsonObject4 = jsonArray4.getJSONObject(j+1);
                                            String s3 = jsonObject4.getString("digg");
                                            replies.get(j).setLikeNum(s3);
                                        }
                                    }
                                    connection1.disconnect();

                                    allCommentData.add(new CommentDataBean(
                                            es5.attr("cmtid"),
                                            sid,
                                            es6.attr("src"),
                                            es2.html(),
                                            es3.html(),
                                            "赞:"+s2,
                                            contentComment,
                                            es4.html() + "楼",
                                            images,
                                            jsonArray.toString(),
                                            replies,
                                            jsonObject1.getInt("replyCount")
                                    ));
                                }
                            }
                            connection.disconnect();


                            final int finalLoadCommentNum=loadCommentNum;
                            recyclerView.post(new Runnable() {
                                @Override
                                public void run() {
                                    commentAdapter.notifyItemRangeInserted(commentAdapter.getItemCount(),finalLoadCommentNum);
                                    String lastCommentFloor2=allCommentData.get(allCommentData.size()-1).floor;
                                    if(lastCommentFloor2.equals("1楼")||lastCommentFloor1.equals(lastCommentFloor2)){
                                       commentAdapter.setNoMore(true);
                                       commentAdapter.notifyItemChanged(commentAdapter.getItemCount()-1);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            flag=lastFlag;
                            page--;
                        }
                    }
                    else {
                        try {
                            String pageSize = "10"; //最多条数
                            String minCount = "0"; //最少赞数
                            String maxCount = "5"; //回复条数
                            String src1 = "https://cm.gamersky.com/appapi/GetArticleCommentWithClubStyle?" +
                                    "request=" +
                                    "{\"articleId\":" + sid + "," +
                                    "\"minPraisesCount\":" + minCount + "," +
                                    "\"repliesMaxCount\":" + maxCount + "," +
                                    "\"pageIndex\":" + page + "," +
                                    "\"pageSize\":" + pageSize + "," +
                                    "\"order\":"+"\""+order+"\""+"}";
                            URL url1 = new URL(src1);
                            //得到connection对象。
                            HttpURLConnection connection1 = (HttpURLConnection) url1.openConnection();
                            //设置请求方式
                            connection1.setRequestMethod("GET");
                            //连接
                            connection1.connect();
                            //得到响应码
                            int responseCode1 = connection1.getResponseCode();
                            int loadCommentNum=0;
                            if (responseCode1 == HttpURLConnection.HTTP_OK) {
                                //得到响应流
                                InputStream inputStream = connection1.getInputStream();
                                //将响应流转换成字符串
                                String result = is2s(inputStream);//将流转换为字符串。
                                JSONObject jsonObject = new JSONObject(result);
                                JSONArray jsonArray1 = jsonObject.getJSONObject("result").getJSONArray("comments");
                                loadCommentNum=jsonArray1.length();
                                for (int i = 0; i < jsonArray1.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray1.getJSONObject(i);
                                    JSONArray jsonArray2 = jsonObject1.getJSONArray("imageInfes");
                                    JSONArray jsonArray3 = jsonObject1.getJSONArray("replies");
                                    ArrayList<String> images = new ArrayList<>();
                                    ArrayList<CommentDataBean> replies=new ArrayList<>();
                                    if (jsonArray2.length() != 0) {
                                        for (int j = 0; j < jsonArray2.length(); j++) {
                                            JSONObject jsonObject2 = jsonArray2.getJSONObject(j);
                                            images.add(jsonObject2.getString("tinysquare"));
                                        }
                                    }
                                    if(jsonArray3.length()!=0){
                                        for (int j = 0; j < jsonArray3.length(); j++) {
                                            JSONObject jsonObject3 = jsonArray3.getJSONObject(j);
                                            replies.add(new CommentDataBean(
                                                    jsonObject3.getString("userHeadImageURL"),
                                                    jsonObject3.getString("userName"),
                                                    format(jsonObject3.getLong("createTime")),
                                                    jsonObject3.getString("praisesCount"),
                                                    jsonObject3.getString("replyContent"),
                                                    jsonObject3.getString("objectUserName")
                                            ));
                                        }
                                    }
                                    allCommentData.add(new CommentDataBean(
                                            jsonObject1.getString("comment_id"),
                                            jsonObject1.getString("img_url"),
                                            jsonObject1.getString("nickname"),
                                            format(jsonObject1.getLong("create_time")),
                                            "赞:" + jsonObject1.getString("support_count"),
                                            jsonObject1.getString("content"),
                                            jsonObject1.getString("floorNumber") + "楼",
                                            images,
                                            jsonArray2.toString(),
                                            replies,
                                            jsonObject1.getInt("repliesCount")

                                    ));

                                }
                            }
                            connection1.disconnect();
                            final int finalLoadCommentNum=loadCommentNum;
                            recyclerView.post(new Runnable() {
                                @Override
                                public void run() {
                                    commentAdapter.notifyItemRangeInserted(commentAdapter.getItemCount(),finalLoadCommentNum);
                                    updateSuspensionBar();
                                    String lastCommentFloor2=allCommentData.get(allCommentData.size()-1).floor;
                                    if(lastCommentFloor2.equals("1楼")||lastCommentFloor1.equals(lastCommentFloor2)){
                                        commentAdapter.setNoMore(true);
                                        commentAdapter.notifyItemChanged(commentAdapter.getItemCount()-1);
                                    }
                                }
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                            flag=lastFlag;
                            page--;
                        }

                    }
                }
            });


    }

    public void upTop(){
        recyclerView.smoothScrollToPosition(0);
    }


}
