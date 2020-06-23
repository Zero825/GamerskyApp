package com.news.gamersky.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.news.gamersky.ImagesBrowserActivity;
import com.news.gamersky.R;
import com.news.gamersky.util.ReadingProgressUtil;
import com.news.gamersky.customizeview.MyWebView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;



public class ArticleFragment extends Fragment {
    private String  data_src;
    private MyWebView webView;
    private ProgressBar progressBar;
    private JSONArray jsonArray;
    private Thread loadThread;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_article, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        data_src=args.getString("data_src");
        progressBar=view.findViewById(R.id.progressBar);
        webView=view.findViewById(R.id.web);
        init();
        loadData();
    }

    public void init(){
        jsonArray=new JSONArray();
        webView.setBackgroundColor(0);
        //webView.setInitialScale(320);
        webView.setHorizontalScrollBarEnabled(false);//水平不显示
        webView.setVerticalScrollBarEnabled(true); //垂直不显示
        //webView.getSettings().setLoadsImagesAutomatically(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setTextZoom(110);
        //java回调js代码，不要忘了@JavascriptInterface这个注解，不然点击事件不起作用
        webView.addJavascriptInterface(new JsCallJavaObj() {
            @JavascriptInterface
            @Override
            public void showBigImg(int i) {
                System.out.println("我是第几张图片"+i);
                Intent intent=new Intent(getActivity(), ImagesBrowserActivity.class);
                intent.putExtra("imagesSrc",jsonArray.toString());
                intent.putExtra("imagePosition",i);
                startActivity(intent);
                //startActivity(intent,ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        },"jsCallJavaObj");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setWebImageClick(view);
                webView.scrollTo(0,ReadingProgressUtil.getProgress(getContext(),data_src));
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                Intent intent = new Intent();
                Uri content_url = Uri.parse(url);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(content_url);
                startActivity(intent);
                return true;
            }
        });



    }
    /**
     * 設置網頁中圖片的點擊事件
     * @param view
     */
    private  void setWebImageClick(WebView view) {
        String jsCode="javascript:(function(){" +
                "var imgs=document.getElementsByTagName(\"img\");" +
                "for(var i=0;i<imgs.length;i++){" +
                "imgs[i].pos = i;"+
                "imgs[i].onclick=function(){" +
                "window.jsCallJavaObj.showBigImg(this.pos);" +
                "}}})()";
        view.loadUrl(jsCode);
    }
    /**
     * Js調用Java接口
     */
    private interface JsCallJavaObj{
        void showBigImg(int i);
    }

    public void loadData(){

        loadThread=new Thread() {
            @Override
            public void run() {
                try {
                Document doc = Jsoup.connect(data_src).get();
                final Elements content = doc.getElementsByTag("article");
                final Elements content1 = doc.getElementsByClass("ymw-contxt-aside");
                Elements content4=doc.getElementsByClass("gsAreaContextArt");
                String srcUrl=content4.get(0).getElementsByTag("script").html();
                String a=content.html();
                boolean pinye=true;
                if(!srcUrl.equals("")){
                    pinye=false;
                    int i1=srcUrl.indexOf("h");
                    int i2=srcUrl.indexOf("\"",i1);
                    srcUrl=srcUrl.substring(i1,i2);
                    System.out.println("跳转链接"+srcUrl);
                    try {
                        Document doc1=Jsoup.connect(srcUrl).get();
                        Elements elements1=doc1.getElementsByClass("qzcmt-content");
                        Elements elements2=elements1.get(0).getElementsByTag("img");
                        Elements elements3=doc1.getElementsByClass("video-img");
                        if(elements3.attr("data-sitename").equals("bilibili")){
                            elements3.html(elements3.html()
                                    + "<a class=\"\" target=\"_blank\" href=\""+"https://www.bilibili.com/video/"
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

                String h= "<script src=\"file:///android_asset/js/echo.min.js\"></script>\n"+
//                        "<script src=\"file:///android_asset/js/jquery-3.5.0.min.js\"></script>\n"+
                        "<script>\n" +
                        "  echo.init({\n" +
                        "    offset: 2000,\n" +
                        "    throttle: 250,\n" +
                        "    unload: false,\n" +
                        "  });\n" +
//                        "   $(function() {\n" +
//                        "       $(\"div\").fadeIn(300);\n"+
//                        "   });\n"+
                        "</script>"+
                        "<b style=\"font-size:22px;margin:0;\">"+content1.get(0).getElementsByTag("h1").text()+"</b >"+
                        "<p class=\"author\" style=\"font-size:13px;margin:0;color:#808080\">"+"&nbsp;"+content1.get(0).getElementsByTag("span").text()+"</p >";

                String s=h+a;
                Elements elements1 = doc.getElementsByClass("yu-btnwrap");
                if(pinye) {
                    if (!elements1.toString().equals("")) {
                        Elements elements2 = elements1.get(0).getElementsByTag("span");
                        Elements elements3 = elements1.get(0).getElementsByTag("a");
                        Elements elements4 = content.get(0).getElementsByClass("gs_bot_author");
                        elements4.html("");

                        s = h + content.html();

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
                        //System.out.println(finalS);
                        //System.out.println(getNewContent(finalS));
                        webView.loadDataWithBaseURL("file:///android_asset", getNewContent(finalS), "text/html", "utf-8", null);
                        progressBar.setVisibility(View.GONE);
                        webView.setVisibility(View.VISIBLE);


                    }
                });





                }catch (Exception e){
                    System.out.println("载入失败");
                    e.printStackTrace();
                }
            }
        };
        loadThread.start();
    }


    /**
     * 将html文本内容中包含img标签的图片，宽度变为屏幕宽度，高度根据宽度比例自适应
     **/
    public String getNewContent(String htmltext) {
        try {

            Document doc = Jsoup.parse(htmltext);

            Elements elements2=doc.getElementsByTag("body");
            elements2.html("<div style=\"margin:0px 10px\">"+doc.body().children()+"</div>");
            Elements elements = doc.getElementsByTag("a");
            Elements elements1=doc.getElementsByTag("img");
            Elements elements3 = doc.getElementsByTag("span");
            Elements elements4=doc.getElementsByTag("p");
            Elements elements5=doc.getElementsByTag("script");
            Elements elements6 = doc.getElementsByClass("gs_bot_author");
            elements2.attr("href","");
            for (int i=0;i<elements1.size();i++) {
                Element element=elements1.get(i);
                element.attr("style", "border-radius: 0px")
                        .attr("width", "100%")
                        .attr("height", "auto")
                        .attr("data-echo",element.attr("src"))
                        .attr("_cke_saved_src","")
                        .attr("src","data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg");
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
                    element.attr("style","line-height:28px;");
                }
                if(!element.getElementsByTag("img").toString().equals("")){

                    element.getElementsByTag("a").attr("href","javascript:void(0);");

                }
            }
            for (int i=0;i<elements5.size();i++){
                Element element=elements5.get(i);
                if (element.attr("src").equals("//j.gamersky.com/g/gsVideo.js")){
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
                    s=s.substring(i1,i2);
                    System.out.println("视频链接"+s);
                    v.parent().html("<a class=\"\" target=\"_blank\" href=\""+s+"\" style=\"color:#D81B60;text-decoration:none;\">视频链接</a>");
                }

            }
            elements6.attr("style","color:#999;text-align:right;font-size:14px");
            return doc.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return htmltext;
        }
    }

    public void upTop(){
        webView.scrollTo(0,0);
    }

    @Override
    public void onDestroy() {
        ReadingProgressUtil.putProgress(getContext(),data_src,webView.getScrollY());
        webView.clearCache(true);
        webView.destroy();
        super.onDestroy();
    }


    public void resumeWebView() {
        webView.resumeTimers();
    }

    public void pauseWebView(){
        webView.pauseTimers();
    }
}
