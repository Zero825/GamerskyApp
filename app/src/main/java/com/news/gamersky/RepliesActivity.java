package com.news.gamersky;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.news.gamersky.customizeview.RoundImageView;
import com.news.gamersky.util.CommentEmojiUtil;
import com.news.gamersky.databean.CommentDataBean;
import com.news.gamersky.util.NightModeUtil;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import static com.news.gamersky.util.AppUtil.format;
import static com.news.gamersky.util.AppUtil.is2s;

public class RepliesActivity extends AppCompatActivity {
    private String commentId;
    private String clubContentId;
    private ConstraintLayout constraintLayout;
    private TextView headTextView;
    private ImageView mask;
    private ImageButton imageButton;
    private CommentDataBean comment;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ArrayList<CommentDataBean> repliesData;
    private RepliesAdapter repliesAdapter;
    private Point point;
    private  int page;
    private  int flag;
    private int lastFlag;
    private ExecutorService executor;
    private AnimatorSet animSetIn;
    private AnimatorSet animSetOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replies);
        init();
        //loadReplies();
        startListen();
    }

    public void init(){

        if(!NightModeUtil.isNightMode(this)){
            getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            |View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }else {
            getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        headTextView=findViewById(R.id.textView20);
        constraintLayout=findViewById(R.id.constraintLayout);
        imageButton=findViewById(R.id.imageButton);
        mask=findViewById(R.id.imageView13);
        Intent intent = getIntent();
        commentId = intent.getStringExtra("commentId");
        clubContentId = intent.getStringExtra("clubContentId");
        System.out.println(commentId+"    "+clubContentId);
        comment= (CommentDataBean) intent.getSerializableExtra("comment");
        headTextView.setText(comment.userName+"的回复");
        point = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(point);
        constraintLayout.setY(point.y);
        mask.setAlpha(0f);
        constraintLayout.setTranslationY(point.y);
        repliesData=new ArrayList<>();

        recyclerView=findViewById(R.id.replies_recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        repliesAdapter=new RepliesAdapter(comment,repliesData);
        recyclerView.setAdapter(repliesAdapter);

        page=1;
        flag=0;
        lastFlag=0;
        executor= Executors.newSingleThreadExecutor();
        animSetIn = new AnimatorSet();
        animSetOut = new AnimatorSet();
        animSetIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                loadReplies();
                animSetIn.removeAllListeners();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        startAnimator();


    }

    public void loadReplies(){
        page=1;
        flag=0;
        lastFlag=0;
        repliesAdapter.setNoMore(false);
        if(clubContentId!=null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        String pageIndex="1";
                        String pageSize="10";
                        String src = "https://club.gamersky.com/club/api/getclubitemactivity?" +
                                "jsondata=" +
                                "{\"commentId\":" + commentId + "," +
                                "\"clubContentId\":" + clubContentId + "," +
                                "\"pageIndex\":" + pageIndex + "," +
                                "\"pageSize\":" + pageSize + "," +
                                "}";
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
                            String result = is2s(inputStream);//将流转换为字符串
                            result=result.substring(1,result.length()-1);
                            System.out.println(result);
                            JSONObject jsonObject = new JSONObject(result);
                            String s = jsonObject.getString("body");
                            Document doc = Jsoup.parse(s);
                            Elements es1=doc.getElementsByClass("ccmt_reply_cont");
                            String repliesCommentId="";
                            for (int i = 0; i < es1.size(); i++) {
                                Element e1=es1.get(i);
                                String objectUserName=comment.userName;
                                try {
                                    objectUserName=e1.getElementsByClass("uname").get(1).html();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                String contentReply=e1.getElementsByClass("content").get(0).html();
                                try {
                                    String temp=e1.getElementsByClass("ccmt_all").attr("data-content");
                                    if (!temp.equals("")) contentReply=temp;
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                repliesData.add(new CommentDataBean(
                                        e1.attr("cmtid"),
                                        e1.getElementsByTag("img").get(0).attr("src"),
                                        e1.getElementsByClass("uname").get(0).html(),
                                        e1.getElementsByClass("ccmt_time").get(0).html(),
                                        "赞:"+e1.getElementsByClass("digg-btn").get(0).html(),
                                        contentReply,
                                        objectUserName

                                ));

                                repliesCommentId += e1.attr("cmtid") + ",";

                            }
                            System.out.println(repliesCommentId);
                            String src1 = "https://club.gamersky.com/club/api/getcommentlike?" +
                                    "jsondata=" +
                                    "{\"commentIds\":" +"\""+repliesCommentId +"\""+ "}";
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
                                JSONObject jsonObject1=new JSONObject(result1);
                                JSONArray jsonArray=new JSONArray(jsonObject1.getString("body"));
                                for (int j=0;j<jsonArray.length();j++){
                                    JSONObject jsonObject2=jsonArray.getJSONObject(j);
                                    repliesData.get(j).setLikeNum("赞:"+jsonObject2.getString("digg"));
                                }
                            }
                            connection.disconnect();
                            connection1.disconnect();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    repliesAdapter.notifyDataSetChanged();
                                    if(repliesData.size()<10){
                                        repliesAdapter.setNoMore(true);
                                    }
                                }
                            });
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        String pageIndex="1";
                        String pageSize="10";
                        String src = "https://cm.gamersky.com/appapi/getCommentRepliesListWithClubStyle?" +
                                "request=" +
                                "{\"commentId\":" + commentId + "," +
                                "\"pageIndex\":" + pageIndex + "," +
                                "\"pageSize\":" + pageSize + "," +
                                "\"order\":\"timeASC\"}";
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
                            String result = is2s(inputStream);//将流转换为字符串
                            JSONObject jsonObject = new JSONObject(result);
                            JSONArray jsonArray1 = jsonObject.getJSONObject("result").getJSONArray("replies");
                            for (int i = 0; i < jsonArray1.length(); i++) {
                                JSONObject jsonObject1 = jsonArray1.getJSONObject(i);
                                repliesData.add(new CommentDataBean(
                                        jsonObject1.getString("replyId"),
                                        jsonObject1.getString("userHeadImageURL"),
                                        jsonObject1.getString("userName"),
                                        format(jsonObject1.getLong("createTime")),
                                        "赞:" + jsonObject1.getString("praisesCount"),
                                        jsonObject1.getString("replyContent"),
                                        jsonObject1.getString("objectUserName")

                                ));

                            }
                            connection.disconnect();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    repliesAdapter.notifyDataSetChanged();
                                    if(repliesData.size()<10){
                                        repliesAdapter.setNoMore(true);
                                    }
                                    startAnimator();
                                }
                            });
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startAnimator();
                            }
                        });
                    }
                }
            }).start();
        }
    }

    public Thread loadMoreReplies(){
        final String lastReplyId=repliesData.get(repliesData.size()-1).replyId;
        if(clubContentId!=null){
            return new Thread(new Runnable() {
                @Override
                public void run() {
                    try{

                        page++;
                        String pageSize="10";
                        String src = "https://club.gamersky.com/club/api/getclubitemactivity?" +
                                "jsondata=" +
                                "{\"commentId\":" + commentId + "," +
                                "\"clubContentId\":" + clubContentId + "," +
                                "\"pageIndex\":" + page + "," +
                                "\"pageSize\":" + pageSize + "," +
                                "}";
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
                            String result = is2s(inputStream);//将流转换为字符串
                            result=result.substring(1,result.length()-1);
                            System.out.println(result);
                            JSONObject jsonObject = new JSONObject(result);
                            String s = jsonObject.getString("body");
                            Document doc = Jsoup.parse(s);
                            Elements es1=doc.getElementsByClass("ccmt_reply_cont");
                            String repliesCommentId="";
                            int loadRepliesNum=es1.size();
                            for (int i = 0; i < es1.size(); i++) {
                                Element e1=es1.get(i);
                                String objectUserName=comment.userName;
                                try {
                                    objectUserName=e1.getElementsByClass("uname").get(1).html();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                String contentReply=e1.getElementsByClass("content").get(0).html();
                                try {
                                    String temp=e1.getElementsByClass("ccmt_all").attr("data-content");
                                    if (!temp.equals("")) contentReply=temp;
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                repliesData.add(new CommentDataBean(
                                        e1.attr("cmtid"),
                                        e1.getElementsByTag("img").get(0).attr("src"),
                                        e1.getElementsByClass("uname").get(0).html(),
                                        e1.getElementsByClass("ccmt_time").get(0).html(),
                                        "赞:"+e1.getElementsByClass("digg-btn").get(0).html(),
                                        contentReply,
                                        objectUserName

                                ));

                                repliesCommentId += e1.attr("cmtid") + ",";

                            }
                            System.out.println(repliesCommentId);
                            if(!repliesCommentId.equals("")){
                                String src1 = "https://club.gamersky.com/club/api/getcommentlike?" +
                                        "jsondata=" +
                                        "{\"commentIds\":" +"\""+repliesCommentId +"\""+ "}";
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
                                    JSONObject jsonObject1=new JSONObject(result1);
                                    JSONArray jsonArray=new JSONArray(jsonObject1.getString("body"));
                                    for (int j=0;j<jsonArray.length();j++){
                                        JSONObject jsonObject2=jsonArray.getJSONObject(j);
                                        repliesData.get(j).setLikeNum("赞:"+jsonObject2.getString("digg"));
                                    }
                                }
                                connection1.disconnect();
                            }
                            connection.disconnect();
                            final int finalLoadRepliesNum=loadRepliesNum;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    repliesAdapter.notifyItemRangeInserted(repliesAdapter.getItemCount(),finalLoadRepliesNum);
                                    String nowReplyId=repliesData.get(repliesData.size()-1).replyId;
                                    if(lastReplyId.equals(nowReplyId)){
                                        repliesAdapter.setNoMore(true);
                                        repliesAdapter.notifyItemChanged(repliesAdapter.getItemCount()-1);
                                    }
                                }
                            });
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        flag=lastFlag;
                    }
                }
            });

        }else {
            return new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        page++;
                        String pageSize="10";
                        String src = "https://cm.gamersky.com/appapi/getCommentRepliesListWithClubStyle?" +
                                "request=" +
                                "{\"commentId\":" + commentId + "," +
                                "\"pageIndex\":" + page + "," +
                                "\"pageSize\":" + pageSize + "," +
                                "\"order\":\"timeASC\"}";
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
                            String result = is2s(inputStream);//将流转换为字符串
                            JSONObject jsonObject = new JSONObject(result);
                            JSONArray jsonArray1 = jsonObject.getJSONObject("result").getJSONArray("replies");
                            int loadRepliesNum=jsonArray1.length();
                            for (int i = 0; i < jsonArray1.length(); i++) {
                                JSONObject jsonObject1 = jsonArray1.getJSONObject(i);
                                repliesData.add(new CommentDataBean(
                                        jsonObject1.getString("replyId"),
                                        jsonObject1.getString("userHeadImageURL"),
                                        jsonObject1.getString("userName"),
                                        format(jsonObject1.getLong("createTime")),
                                        "赞:" + jsonObject1.getString("praisesCount"),
                                        jsonObject1.getString("replyContent"),
                                        jsonObject1.getString("objectUserName")

                                ));

                            }
                            connection.disconnect();
                            final int finalLoadRepliesNum=loadRepliesNum;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    repliesAdapter.notifyItemRangeInserted(repliesAdapter.getItemCount(),finalLoadRepliesNum);
                                    String nowReplyId=repliesData.get(repliesData.size()-1).replyId;
                                    if(lastReplyId.equals(nowReplyId)){
                                        repliesAdapter.setNoMore(true);
                                        repliesAdapter.notifyItemChanged(repliesAdapter.getItemCount()-1);
                                    }
                                }
                            });
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        flag=lastFlag;
                        page--;
                    }
                }
            });
        }

    }

    public void upTop(){
        recyclerView.smoothScrollToPosition(0);
    }

    public void startAnimator(){
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(constraintLayout, "translationY", constraintLayout.getTranslationY(), 0f);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(mask, "alpha", mask.getAlpha(), 1f);
        objectAnimator1.setInterpolator(new AccelerateDecelerateInterpolator());
        objectAnimator2.setInterpolator(new AccelerateDecelerateInterpolator());
        animSetIn.play(objectAnimator1).with(objectAnimator2);
        animSetIn.setDuration(300);
        animSetIn.start();
    }

    public void endAnimator(){
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(constraintLayout, "translationY", constraintLayout.getTranslationY(), point.y);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(mask, "alpha", mask.getAlpha(), 0f);
        objectAnimator1.setInterpolator(new AccelerateDecelerateInterpolator());
        objectAnimator2.setInterpolator(new AccelerateDecelerateInterpolator());
        animSetOut.play(objectAnimator1).with(objectAnimator2);
        animSetOut.setDuration(300);
        animSetOut.start();
        animSetOut.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }



    @SuppressLint("ClickableViewAccessibility")
    public void startListen(){
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastItem=layoutManager.findLastVisibleItemPosition();
                int dataNum=repliesData.size();
                int line=dataNum;
                if(lastItem>dataNum){
                    line=dataNum+1;
                }
                //System.out.println(lastItem+"      "+flag+"       "+line);
                if(lastItem>10&&lastItem!=flag&&lastItem==line){
                    lastFlag=flag;
                    flag=lastItem;
                    System.out.println("加载评论");
                    executor.submit(loadMoreReplies());
                }

            }

        });
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            int yVelocity=0;
            int lastYVelocity=0;
            float ry=0;
            float dis=0;
            boolean canScrollVertically=false;
            boolean isCanScrollVertically=false;
            boolean consumed=false;
            VelocityTracker velocityTracker;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Log.i("TAG", event.toString());
                velocityTracker = VelocityTracker.obtain();
                velocityTracker.addMovement(event);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;

                    case MotionEvent.ACTION_MOVE:
                        consumed=false;
                        if(animSetIn.isRunning()||animSetOut.isRunning()) break;
                        if(ry==0f){
                            ry=event.getY();
                            isCanScrollVertically=recyclerView.canScrollVertically(-1);
                        }
                        dis = constraintLayout.getTranslationY() + event.getY() - ry;
                        if(!recyclerView.canScrollVertically(-1)&&dis > 0) {
                            if(recyclerView.canScrollVertically(-1)==canScrollVertically) {
                                constraintLayout.setTranslationY(dis);
                                consumed=true;
                            }else {
                                ry=event.getY();
                            }
                        }else if(!recyclerView.canScrollVertically(-1)&&dis < 0){
                            constraintLayout.setTranslationY(0);
                        }
                        if(recyclerView.canScrollVertically(-1)){
                            canScrollVertically=true;
                        }else {
                            canScrollVertically=false;
                        }
                        velocityTracker.computeCurrentVelocity(1000);
                        if(velocityTracker.getYVelocity()>0){
                            lastYVelocity=(int) velocityTracker.getYVelocity();
                        }
                        if(yVelocity<(int) velocityTracker.getYVelocity()){
                            yVelocity = (int) velocityTracker.getYVelocity();
                        }
                        return consumed;
                    case MotionEvent.ACTION_UP:
                        ry=0f;
                        consumed=false;
                        //Log.i("TAG", "onTouch: "+yVelocity+"\t"+lastYVelocity);
                        velocityTracker.recycle();
                        if(yVelocity>3000&&lastYVelocity>3000&&!isCanScrollVertically){
                            onBackPressed();
                            yVelocity=0;
                            lastYVelocity=0;
                            break;
                        }else {
                            yVelocity=0;
                            lastYVelocity=0;
                        }
                        if (constraintLayout.getTranslationY() > constraintLayout.getHeight() / 3f) {
                            onBackPressed();
                        } else {
                            startAnimator();
                        }
                        break;
                }
                return consumed;
            }
        });

        headTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upTop();
            }
        });
        headTextView.setOnTouchListener(new View.OnTouchListener() {
            float ry=0;
            float dis=0;
            int yVelocity=0;
            int lastYVelocity=0;
            boolean consumed=false;
            VelocityTracker velocityTracker;
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                velocityTracker = VelocityTracker.obtain();
                velocityTracker.addMovement(event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ry=event.getY();
                        consumed=false;
                        yVelocity=0;
                        lastYVelocity=0;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        consumed=true;
                        dis=constraintLayout.getTranslationY()+event.getY()-ry;
                        if(dis>0)
                            constraintLayout.setTranslationY(dis);
                        velocityTracker.computeCurrentVelocity(1000);
                        if(velocityTracker.getYVelocity()>0){
                            lastYVelocity=(int) velocityTracker.getYVelocity();
                        }
                        if(yVelocity<(int) velocityTracker.getYVelocity()){
                            yVelocity = (int) velocityTracker.getYVelocity();
                        }

                        //Log.i("TAG", "onTouch: "+velocityTracker.getYVelocity());
                        break;

                    case MotionEvent.ACTION_UP:
                        consumed=false;
                        velocityTracker.getYVelocity();
                        //Log.i("TAG", "onTouch: "+yVelocity+"\t"+velocityTracker.getYVelocity());
                        velocityTracker.recycle();
                        if(yVelocity>3000&&lastYVelocity>3000){
                            onBackPressed();
                            break;
                        }
                        if(constraintLayout.getTranslationY()>constraintLayout.getHeight()/3f){
                            onBackPressed();
                        }else {
                            startAnimator();
                        }
                        if(constraintLayout.getTranslationY()!=0f){
                            consumed=true;
                        }
                        break;
                }

                return consumed;
            }
        });

    }

    @Override
    public void onBackPressed() {
        endAnimator();
        //super.onBackPressed();
    }

    public class RepliesAdapter extends RecyclerView.Adapter {
        private CommentDataBean commentData;
        private ArrayList<CommentDataBean> repliesData;
        private boolean moreData;


        public RepliesAdapter(CommentDataBean commentData,ArrayList<CommentDataBean> repliesData){

            this.commentData=commentData;
            this.repliesData=repliesData;
            moreData=true;
        }

        public  class CommentViewHolder extends RecyclerView.ViewHolder {

            public TextView textView1;
            public TextView textView2;
            public TextView textView3;
            public TextView textView4;
            public TextView textView5;
            public TextView textView6;
            public RoundImageView imageView;
            public GridLayout gridLayout;
            public LinearLayout linearLayout;

            public CommentViewHolder(View v) {
                super(v);
                textView1=v.findViewById(R.id.textView9);
                textView2=v.findViewById(R.id.textView11);
                textView3=v.findViewById(R.id.textView12);
                textView4=v.findViewById(R.id.textView13);
                textView5=v.findViewById(R.id.textView14);
                imageView=v.findViewById(R.id.imageView6);
                gridLayout=v.findViewById(R.id.imageContainer);
                textView6=v.findViewById(R.id.textView18);
                linearLayout=v.findViewById(R.id.repliesContainer);
            }

            public void bindView(int position){
                textView1.setText( commentData.userName);
                if( commentData.content.equals("")){
                    textView2.setVisibility(View.GONE);
                }else {
                    textView2.setVisibility(View.VISIBLE);
                    textView2.setText(CommentEmojiUtil.getEmojiString( commentData.content));
                }
                textView6.setVisibility(View.GONE);

                textView3.setText( commentData.time);
                textView4.setText( commentData.floor);
                textView5.setText( commentData.likeNum);
                if(! commentData.userImage.equals("")) {
                    Glide.with( imageView)
                            .load( commentData.userImage)
                            .centerCrop()
                            .into( imageView);
                }
                gridLayout.removeAllViews();
                final ImageView[] imageViews=new ImageView[ commentData.images.size()];
                for (int i=0;i< commentData.images.size();i++){
                    View ic = LayoutInflater.from(RepliesActivity.this)
                            .inflate(R.layout.gridlayout_comment_image, null, false);
                    imageViews[i]=ic.findViewById(R.id.imageView7);
                    Glide.with(imageViews[i])
                            .load( commentData.images.get(i))
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .centerCrop()
                            .into(imageViews[i]);
                    final int finalI = i;
                    final CommentDataBean finalTempData= commentData;
                    imageViews[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(RepliesActivity.this, ImagesBrowserActivity.class);
                            intent.putExtra("imagesSrc", finalTempData.imagesJson);
                            intent.putExtra("imagePosition", finalI);
                            //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                            startActivity(intent);
                        }
                    });
                    gridLayout.addView(ic);
                }
                if(commentData.images.size()==0){
                    gridLayout.setVisibility(View.GONE);
                }else {
                    gridLayout.setVisibility(View.VISIBLE);
                }
            }
        }

        public  class RepliesViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case


            public TextView textView1;
            public TextView textView2;
            public TextView textView3;
            public TextView textView4;
            public TextView textView5;
            public TextView textView6;
            public RoundImageView imageView;

            public RepliesViewHolder(View v) {
                super(v);
                imageView=v.findViewById(R.id.imageView6_2);
                textView1=v.findViewById(R.id.textView9_2);
                textView2=v.findViewById(R.id.textView20_2);
                textView3=v.findViewById(R.id.textView11_2);
                textView4=v.findViewById(R.id.textView12_2);
                textView5=v.findViewById(R.id.textView14_2);
                textView6=v.findViewById(R.id.textView19_2);
            }

            public void bindView(int position){
                int p=position-1;
//                imageView.getLayoutParams().width=100;
////                imageView.getLayoutParams().height=100;
                CommentDataBean tempCommentDataBean=repliesData.get(p);
                if(!tempCommentDataBean.userImage.equals("")) {
                    Glide.with(imageView)
                            .load(tempCommentDataBean.userImage)
                            .centerCrop()
                            .into(imageView);
                }

                textView1.setText(tempCommentDataBean.userName);
                if(tempCommentDataBean.objectUserName.equals(commentData.userName)){
                    textView2.setText("");
                    textView6.setVisibility(View.INVISIBLE);
                    textView1.setMaxEms(16);
                }else {
                    textView2.setText(tempCommentDataBean.objectUserName);
                    textView6.setVisibility(View.VISIBLE);
                    textView1.setMaxEms(8);
                }

                textView3.setText(CommentEmojiUtil.getEmojiString(tempCommentDataBean.content));
                textView4.setText(tempCommentDataBean.time);
                textView5.setText(tempCommentDataBean.likeNum);
            }
        }

        public  class FooterViewHolder extends RecyclerView.ViewHolder {

            public TextView textView;

            public FooterViewHolder(View v) {
                super(v);
                textView=v.findViewById(R.id.textView8);
            }

            public void bindView(int position){

                if(repliesData.size()==0){
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

        @Override
        public int getItemViewType(int position){
            int i=1;
            if(position==0){
                i=0;
            }
            if(position==repliesData.size()+1){
                i=2;
            }
            return i;
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v=null;
            if(viewType==0){
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_comment, parent, false);
                return new RepliesAdapter.CommentViewHolder(v);
            }
            if(viewType==1){
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_item_reply, parent, false);
                return new RepliesAdapter.RepliesViewHolder(v);
            }
            if(viewType==2){
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_footer, parent, false);
                return new RepliesAdapter.FooterViewHolder(v);
            }

            return new RepliesAdapter.RepliesViewHolder(v);

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            int vt=holder.getItemViewType();
            if(vt==0){
                ((CommentViewHolder)holder).bindView(position);
            }
            if(vt==1){
                ((RepliesViewHolder)holder).bindView(position);
            }

            if(vt==2){
                ((FooterViewHolder)holder).bindView(position);
            }

        }

        @Override
        public int getItemCount() {
            return repliesData.size()+2;

        }
        public void setNoMore(boolean b){
            moreData=!b;
        }
    }

}