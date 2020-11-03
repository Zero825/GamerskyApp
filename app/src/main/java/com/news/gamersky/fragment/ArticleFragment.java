package com.news.gamersky.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.FutureTarget;
import com.news.gamersky.ArticleActivity;
import com.news.gamersky.ImagesBrowserActivity;
import com.news.gamersky.R;
import com.news.gamersky.databean.NewDataBean;
import com.news.gamersky.setting.AppSetting;
import com.news.gamersky.util.AppUtil;
import com.news.gamersky.util.NightModeUtil;
import com.news.gamersky.util.ReadingProgressUtil;
import com.news.gamersky.customizeview.ArticleWebView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import okhttp3.Request;


public class ArticleFragment extends Fragment {
    private final static String TAG="ArticleFragment";

    private String  data_src;
    private ArticleWebView webView;
    private ConstraintLayout navListView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ConstraintLayout navView;
    private JSONArray jsonArray;
    private boolean pinye;
    private int page;
    private int maxPage;
    private List<NewDataBean> listData;
    private boolean listShowed;
    private AnimatorSet listAnimator;;
    private String newTitle;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_article, container, false);
        Bundle args = getArguments();
        if (args != null) {
            data_src=args.getString("data_src");
            Log.i("TAG", "onCreateView: "+data_src);
            init(view);
            startListen();
            loadData(data_src);
        }
        return view;
    }


    @SuppressLint("ClickableViewAccessibility")
    public void init(View view){
        progressBar=view.findViewById(R.id.progressBar);
        webView=view.findViewById(R.id.web);
        navView=view.findViewById(R.id.article_nav);
        recyclerView=view.findViewById(R.id.recyclerView);
        navListView=view.findViewById(R.id.nav_list);

        listShowed=false;
        listAnimator=new AnimatorSet();
        page=0;
        pinye=true;
        jsonArray=new JSONArray();
        listData=new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.hasFixedSize();
        recyclerView.setAdapter(new RecyclerViewAdapter(getContext(),listData));

        webView.setBackgroundColor(0);
        //webView.setInitialScale(320);
        webView.setHorizontalScrollBarEnabled(false);//水平不显示
        webView.setVerticalScrollBarEnabled(true); //垂直不显示
        //webView.getSettings().setLoadsImagesAutomatically(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setTextZoom(110);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        //java回调js代码，不要忘了@JavascriptInterface这个注解，不然点击事件不起作用
        webView.addJavascriptInterface(new JsCallJavaObj() {
            @JavascriptInterface
            @Override
            public void showBigImg(int i) {
                Intent intent=new Intent(getActivity(), ImagesBrowserActivity.class);
                intent.putExtra("imagesSrc",jsonArray.toString());
                intent.putExtra("imagePosition",i);
                if(!listAnimator.isRunning()){
                    startActivity(intent);
                }
                //startActivity(intent,ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        },"jsCallJavaObj");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setWebImageClick(view);
                //webView.scrollTo(0,ReadingProgressUtil.getProgress(getContext(),data_src));
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                Log.i("TAG", "shouldOverrideUrlLoading: "+url);
                if(url.contains("gamersky.com/news")){
                    String wapUrl= "https://wap.gamersky.com/news/Content-" + AppUtil.urlToId(url)+ ".html";
                    Intent intent=new Intent(getContext(), ArticleActivity.class);
                    intent.putExtra("new_data",new NewDataBean("",wapUrl));
                    startActivity(intent);
                }else if(url.contains("http")){
                    Intent intent = new Intent();
                    Uri contentUrl = Uri.parse(url);
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(contentUrl);
                    startActivity(intent);
                }
                return true;
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                //Log.i(TAG, "shouldInterceptRequest: " + request.getUrl());
                HttpURLConnection connection = null;
                String url=request.getUrl().toString();
                if(!url.contains("http")){
                    return super.shouldInterceptRequest(view, request);
                }
                try {
                    connection= (HttpURLConnection) new URL(url).openConnection();
                    connection.setConnectTimeout(60000);
                    connection.setReadTimeout(60000);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(connection!=null){
                    List<String> contentTypeList=connection.getHeaderFields().get("Content-Type");
                    if(contentTypeList!=null&&contentTypeList.get(0).startsWith("image")){
                        String contentType=contentTypeList.get(0);
                        //Log.i(TAG, "shouldInterceptRequest: "+contentType);
                        byte[] bytes=null;
                        if(contentType.contains("gif")){
                            try {
                                FutureTarget<byte[]> target = Glide.with(getContext())
                                        .as(byte[].class)
                                        .timeout(60000)
                                        .load(url)
                                        .decode(GifDrawable.class)
                                        .submit();
                                bytes=target.get();
                            } catch (ExecutionException|InterruptedException|NullPointerException e) {
                                e.printStackTrace();
                            }
                        }else {
                            try {
                                FutureTarget<Bitmap> target = Glide.with(getContext())
                                        .asBitmap()
                                        .timeout(60000)
                                        .load(url)
                                        .submit();
                                Bitmap bitmap = target.get();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                                bytes=baos.toByteArray();
                            } catch (ExecutionException|InterruptedException|NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                        if(bytes!=null) {
                            connection.disconnect();
                            Log.i(TAG, "shouldInterceptRequest: "+"return image");
                            return new WebResourceResponse(contentType, "utf-8", new ByteArrayInputStream(bytes));
                        }
                    }

                }
                return super.shouldInterceptRequest(view, request);
            }


        });
        webView.loadDataWithBaseURL("file:///android_asset", "<div></div>", "text/html", "utf-8", null);

    }

    public String getContentType(String url){

        try {
            HttpURLConnection connection= (HttpURLConnection) new URL(url).openConnection();
            return connection.getHeaderFields().get("Content-Type").get(0);
        } catch (IOException|NullPointerException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 設置網頁中圖片的點擊事件
     * @param view
     */
    private  void setWebImageClick(WebView view) {
        String jsCode= "javascript:(function(){" +
                    "var imgs=document.getElementsByTagName(\"img\");" +
                    "for(var i=0;i<imgs.length;i++){" +
                        "imgs[i].pos = i;"+
                        "imgs[i].onclick=function(){" +
                            "window.jsCallJavaObj.showBigImg(this.pos);" +
                        "};" +
                        "imgs[i].onerror=function(){"+
                            "this.src=\"file:///android_asset/pic/placeholders_pic_null.png\";"+
                        "};"+
                    "}" +
                "})()";
        view.loadUrl(jsCode);
    }
    /**
     * Js調用Java接口
     */
    private interface JsCallJavaObj{
        void showBigImg(int i);
    }

    public void loadData(final String link){

        new Thread() {
            @Override
            public void run() {
                try {
                Document doc = Jsoup.connect(link).get();
                final Elements content = doc.getElementsByTag("article");
                final Elements content1 = doc.getElementsByClass("ymw-contxt-aside");

                Elements content4=doc.getElementsByClass("gsAreaContextArt");
                String srcUrl="";
                if(content4.size()!=0&&content4.get(0).getElementsByTag("script").size()!=0) {
                    srcUrl = content4.get(0).getElementsByTag("script").html();
                }

                String a=content.html();
                //Log.i("TAG", "run: "+srcUrl);
                pinye=true;
                int i1=srcUrl.indexOf("http");
                int i2=srcUrl.indexOf("\"",i1);
                if(i1!=-1&&i2!=-1&&!srcUrl.equals("")){
                    pinye=false;
                    srcUrl=srcUrl.substring(i1,i2);
                    System.out.println("跳转链接"+srcUrl);
                    try {
                        Document doc1=Jsoup.connect(srcUrl).get();
                        Elements elements1=doc1.getElementsByClass("qzcmt-content");
                        Elements elements2=elements1.get(0).getElementsByTag("img");
                        Elements elements3=doc1.getElementsByClass("video-img");
                        if(elements3.attr("data-sitename").equals("bilibili")){
                            elements3.html("<p></p>"+elements3.html()
                                    + "<a class=\"\" target=\"_blank\" href=\""+"https://www.bilibili.com/video/"
                                    +elements3.attr("data-vid")
                                    +"\" style=\"color:#D81B60;text-decoration:none;\">视频链接</a>");
                            elements3.attr("style","text-align:center;");
                        }
                        if(elements3.attr("data-sitename").equals("youku")){
                            elements3.html("<p></p>"+elements3.html()
                                    + "<a class=\"\" target=\"_blank\" href=\""+"https://v.youku.com/v_show/id_"
                                    +elements3.attr("data-vid")
                                    +"\" style=\"color:#D81B60;text-decoration:none;\">视频链接</a>");
                            elements3.attr("style","text-align:center;");
                        }
                        for(Element element:elements2){
                            if(!element.attr("data-origin").equals(""))
                            element.attr("src",element.attr("data-origin"));

                        }
                        a=elements1.html();
                    }catch (Exception e){
                        e.printStackTrace();
                        pinye=true;
                    }

                }else {
                    System.out.println("不用跳转");
                }
                newTitle=content1.get(0).getElementsByTag("h1").text();


                String h= "<script src=\"file:///android_asset/js/echo.min.js\"></script>\n"+
//                        "<script src=\"file:///android_asset/js/jquery-3.5.0.min.js\"></script>\n"+
                        "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/css/oldstyle.css\" />"+
                        "<script>\n" +
                        "  echo.init({\n" +
                        "    offset: 1000,\n" +
                        "    throttle: 250,\n" +
                        "    unload: false,\n" +
                        "  });\n" +
                        "</script>"+
                        "<b style=\"font-size:22px;margin:0;\">"+newTitle+"</b >"+
                        "<p class=\"author\" style=\"font-size:13px;margin:0;color:#808080\">"+"&nbsp;"+content1.get(0).getElementsByTag("span").text()+"</p >";

                if(doc.getElementById("ymwTopVideoInfos")!=null){
                    h=h+doc.getElementById("ymwTopVideoInfos").toString();
                }


                String s=h+a;

                Elements elements1 = doc.getElementsByClass("yu-btnwrap");
                Elements articleNav = doc.getElementsByClass("ymw-article-nav-in");
                if(!articleNav.toString().equals("")){
                    Element eSelected=articleNav.get(0)
                            .getElementById("SelectPage")
                            .getElementsByAttributeValue("selected","selected").get(0);
                    Elements eOptions=articleNav.get(0)
                            .getElementById("SelectPage").getElementsByTag("option");
                    listData.clear();
                    for(int i=0;i<eOptions.size();i++){
                        Element eOption=eOptions.get(i);
                        String title=eOption.html();
                        String page=eOption.attr("value");
                        title="第"+page+"页:\t\t"+title;
                        String link;
                        if(i==0) {
                            link= data_src;
                        }else {
                            link= data_src.substring(0, data_src.indexOf(".html")) + "_" + (i + 1) + ".html";
                        }
                        listData.add(new NewDataBean(title,link));
                    }

                    final String title=eSelected.html();
                    final String sPage=eSelected.attr("value");
                    navView.post(new Runnable() {
                        @Override
                        public void run() {
                            TextView textView=navView.findViewById(R.id.textView6);
                            textView.setText("第"+sPage+"页:\t\t"+title);
                            recyclerView.getAdapter().notifyDataSetChanged();
                            navView.setVisibility(View.VISIBLE);
                            navListView.setVisibility(View.VISIBLE);
                        }
                    });
                    maxPage=articleNav.get(0)
                            .getElementById("SelectPage").getElementsByTag("option").size();
                    page=Integer.parseInt(sPage);
                    pinye=false;
                }else {
                    navView.post(new Runnable() {
                        @Override
                        public void run() {
                            navView.setVisibility(View.INVISIBLE);
                            navListView.setVisibility(View.INVISIBLE);
                        }
                    });
                    pinye=true;
                }

                if(pinye) {
                    if (!elements1.toString().equals("")) {
                        Elements elements2 = elements1.get(0).getElementsByTag("span");
                        Elements elements3 = elements1.get(0).getElementsByTag("a");
                        Elements elements4 = content.get(0).getElementsByClass("gs_bot_author");
                        elements4.html("");

                        //s=h+scriptClear(content.html());
                        s=h+content.html();

                        int n = Integer.parseInt(elements2.text().substring(2));
                        String s1 = elements3.get(1).attr("href");
                        String s2 = s1.substring(0, s1.indexOf("_"));

                        for (int i = 2; i <= n; i++) {
                            String s3 = s2 + "_" + i+".html";
                            Document doc1 = null;
                            System.out.println(s3);
                            try {
                                doc1 = Jsoup.connect(s3).get();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Elements content2 = doc1.getElementsByTag("article");
                            if (i != n) {
                                Elements content3 = content2.get(0).getElementsByClass("gs_bot_author");
                                for (Element element : content3) {
                                    element.html("");
                                }
                            }
                            s = s + content2.html();
                        }
                    }
                }



                final String finalS = s;
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("正文载入");
                        //Log.i("TAG", "run: "+getNewContent(finalS));
                        webView.loadDataWithBaseURL("file:///android_asset", getNewContent(finalS), "text/html", "utf-8", null);
                        progressBar.setVisibility(View.GONE);


                    }
                });





                }catch (Exception e){
                    System.out.println("载入失败");
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void startListen(){
        navView.findViewById(R.id.imageView14).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrHideList();
            }
        });
        navView.findViewById(R.id.imageView15).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrHideList();
            }
        });
        navView.findViewById(R.id.imageView16).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(page>1){
                    if(page==2){
                        loadData(data_src);
                    }else {
                        String nextLink = data_src.substring(0, data_src.indexOf(".html")) + "_" + (page - 1) + ".html";
                        Log.i("TAG", "onClick: " + nextLink);
                        loadData(nextLink);
                        ((RecyclerViewAdapter)recyclerView.getAdapter()).setCurrentSelected(page);
                    }
                }
            }
        });
        navView.findViewById(R.id.imageView17).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(page<maxPage){
                    String nextLink=data_src.substring(0,data_src.indexOf(".html"))+"_"+(page+1)+".html";
                    Log.i("TAG", "onClick: "+nextLink);
                    loadData(nextLink);
                    ((RecyclerViewAdapter)recyclerView.getAdapter()).setCurrentSelected(page);
                }
            }
        });
        navView.findViewById(R.id.textView6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrHideList();
            }
        });

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        if(sharedPreferences.getBoolean("swpie_back",true)){
            final float dis=sharedPreferences.getInt("swipe_back_distance",10)*8;
            final float stc=sharedPreferences.getInt("swipe_sides_sensitivity",50)*0.01f;

            webView.setOnTouchListener(new View.OnTouchListener() {
                float x1 = 0;
                float x2 = 0;
                float y1 = 0;
                float y2 = 0;
                float k = 0;
                boolean back=true;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    //Log.i("TAG", "onTouch: "+event.toString());
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            x1=event.getX();
                            y1=event.getY();
                            x2 = 0;
                            y2 = 0;
                            back=true;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if(x2>event.getX()){
                                //Log.i("TAG", "onTouch: "+icon_back+"\t"+x2+"\t"+event.getX());
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                back=false;
                            }else {
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                            }
                            x2=event.getX();
                            break;
                        case MotionEvent.ACTION_UP:
                            x2=event.getX();
                            y2=event.getY();
                            k=(y2-y1)/(x2-x1);
                            //Log.i("TAG", "onTouch: "+x2+"\t"+x1+"\t"+dis+"\t"+k+"\t"+stc+"\t"+back);
                            if(x2-x1>dis&&Math.abs(k)<stc&&back){
                                getActivity().finish();
                            }
                            if(listShowed){
                                showOrHideList();
                            }
                            break;
                    }
                    return false;
                }
            });
        }
    }

    public void showOrHideList(){
        if(!listAnimator.isRunning()) {
            ObjectAnimator objectAnimator1=new ObjectAnimator();
            ObjectAnimator objectAnimator2 = new ObjectAnimator();
            if(!listShowed) {
                objectAnimator1 = ObjectAnimator.ofFloat(navListView, "translationY", 0f,-navListView.getHeight());
                objectAnimator2 = ObjectAnimator.ofFloat(navView.findViewById(R.id.imageView15), "rotation", 0f,180f);
                listShowed = true;
            }else if(listShowed){
                objectAnimator1 = ObjectAnimator.ofFloat(navListView, "translationY", -navListView.getHeight(), 0f);
                objectAnimator2 = ObjectAnimator.ofFloat(navView.findViewById(R.id.imageView15), "rotation", 180f, 0f);
                listShowed=false;
            }
            listAnimator.playTogether(objectAnimator1, objectAnimator2);
            listAnimator.setDuration(300);
            listAnimator.start();
        }
    }


    /**
     * 格式化
     **/
    public String getNewContent(String htmltext) {
        try {

            Document doc = Jsoup.parse(htmltext);
            String textColor="#"+Integer.toHexString(getResources().getColor(R.color.textColorPrimary)).substring(2);
            Elements elements2=doc.getElementsByTag("body");
            if(pinye) {
                elements2.html("<div style=\"color:" + textColor + ";margin:0px 10px;word-wrap:break-word;max-width:100%;\">" + doc.body().children() + "</div>");
            }else {
                elements2.html("<div style=\"color:" + textColor + ";margin:0px 10px 50px;word-wrap:break-word;max-width:100%;\">" + doc.body().children() + "</div>");
            }
            Elements elements = doc.getElementsByTag("a");
            Elements elements1=doc.getElementsByTag("img");
            Elements elements3 = doc.getElementsByTag("span");
            Elements elements4=doc.getElementsByTag("p");
            Elements elements5=doc.getElementsByTag("script");
            Elements elements6 = doc.getElementsByClass("gs_bot_author");
            Elements elements7 = doc.getElementsByTag("iframe");
            Elements elements8 = doc.getElementsByClass("recommend-app-btn-wrap");

            elements2.attr("href","");
            for (int i=0;i<elements1.size();i++) {
                Element element=elements1.get(i);
                element.attr("style", "border-radius: 2px;max-width:100%;")
                        //.attr("width", "100%")
                        .attr("height", "auto")
                        .attr("data-echo",element.attr("src"))
                        .attr("_cke_saved_src","");
                if(NightModeUtil.isNightMode(getContext())){
                    element.attr("src","file:///android_asset/pic/placeholders_pic_dark.png");
                }else {
                    element.attr("src","file:///android_asset/pic/placeholders_pic_light.png");
                }

                String s=element.parent().getElementsByTag("a").attr("href");
                if(s.equals("")){
                    jsonArray.put(i,new JSONObject().put("origin",element.attr("data-echo")));
                }else {
                    s=s.substring(s.indexOf("?")+1);
                    jsonArray.put(i,new JSONObject().put("origin",s));
                }

            }

            for (Element element : elements) {

                element.attr("style","color:#D81B60;text-decoration:none;-webkit-tap-highlight-color:rgba(255,0,0,0);");
            }

            for(Element element:elements3){
                if(element.attr("id").equals("pe100_page_contentpage"))
                element.html("");
            }
            for (Element element:elements4){
                if (!element.attr("class").equals("author")){
                    element.attr("style","line-height:28px;word-wrap:break-word;")
                            .attr("width", "100%");
                }
                if(!element.getElementsByTag("img").toString().equals("")){
                    element.getElementsByTag("a").attr("href","javascript:void(0);");
                }
            }
            for (int i=0;i<elements5.size();i++){
                Element element=elements5.get(i);
                if (element.attr("src").contains("//j.gamersky.com/g/gsVideo.js")){
                    Element v=elements5.get(i+1);
                    String s=v.html();
                    int i1=s.indexOf("http");
                    int i2=0;
                    if(s.indexOf("\"",i1)==-1){
                        i2=s.indexOf("'",i1);
                    }
                    if(s.indexOf("'",i1)==-1){
                        i2=s.indexOf("\"",i1);
                    }
                    if(s.indexOf("'",i1)!=-1&&s.indexOf("'",i1)!=-1) {
                        i2 = (s.indexOf("\"", i1) < s.indexOf("'", i1)) ? s.indexOf("\"", i1) : s.indexOf("'", i1);
                    }
                    try{
                        s=s.substring(i1,i2);
                        v.parent().html("<p style=\"text-align: center;\">"+"<a class=\"\" target=\"_blank\" href=\""+s+"\" style=\"color:#D81B60;text-decoration:none;\">视频链接</a>"+"</p>");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            elements6.attr("style","color:#999;text-align:right;font-size:14px");
            for(Element element:elements7){
                element.attr("width", "100%")
                        .attr("height", "auto");
            }
            elements8.html("");
            return doc.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return htmltext;
        }
    }

    public void upTop(){
        webView.scrollTo(0,0);
    }

    public void webViewResume(){
        if(webView!=null){
            webView.resumeTimers();
        }
    }

    public void webViewPause(){
        if(webView!=null){
            webView.pauseTimers();
        }
    }

    @Override
    public void onDestroy() {
        //ReadingProgressUtil.putProgress(getContext(),data_src,webView.getScrollY());
        webView.destroy();
        super.onDestroy();
    }

    public String getNewTitle() {
        if(newTitle==null) {
            return "";
        }else {
            return newTitle;
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter{
        private List<NewDataBean> data;
        private Context context;
        private int currentSelected;

        public class VH extends RecyclerView.ViewHolder{
            private  TextView textView;

            public VH(@NonNull View itemView) {
                super(itemView);
                textView=itemView.findViewById(R.id.textView28);
            }
        }

        public RecyclerViewAdapter(Context context,List<NewDataBean> data){
            this.context=context;
            this.data=data;
            currentSelected=0;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view=LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_nav_list,parent,false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            ((VH)holder).textView.setText(data.get(position).title);
            if(position==currentSelected){
                ((VH)holder).textView.setTextColor(getResources().getColor(R.color.colorAccent));
            }else {
                ((VH)holder).textView.setTextColor(getResources().getColor(R.color.textColorPrimary));
            }
            ((VH)holder).textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadData(data.get(position).src);
                    setCurrentSelected(position);
                    showOrHideList();
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public void setCurrentSelected(int currentSelected) {
            this.currentSelected = currentSelected;
            notifyDataSetChanged();
        }
    }

}
