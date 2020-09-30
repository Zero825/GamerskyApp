package com.news.gamersky.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.widget.SearchView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.news.gamersky.ArticleActivity;
import com.news.gamersky.R;
import com.news.gamersky.SearchActivity;
import com.news.gamersky.customizeview.RoundImageView;
import com.news.gamersky.databean.NewDataBean;
import com.news.gamersky.setting.AppSetting;
import com.news.gamersky.util.AppUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HandBookFragment extends Fragment {
    private SwipeRefreshLayout refreshLayout;
    private NestedScrollView nestedScrollView;
    private ProgressBar progressBar;
    private SearchView searchView;
    private LinearLayout topList;
    private LinearLayout hotList;
    private String  dataSrc;
    private boolean firstRun;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_handbook, container, false);
        init(view);
        loadData();
        startListen();
        return view;
    }

    public void init(View view){
        searchView=view.findViewById(R.id.searchView);
        topList=view.findViewById(R.id.top);
        hotList=view.findViewById(R.id.hot_list);
        nestedScrollView=view.findViewById(R.id.nestedScrollView);
        progressBar=view.findViewById(R.id.progressBar5);
        refreshLayout=view.findViewById(R.id.refreshLayout);

        nestedScrollView.setVisibility(View.INVISIBLE);
        refreshLayout.setColorSchemeResources(R.color.colorAccent);

        dataSrc="https://www.gamersky.com/handbook/";
        firstRun=true;

    }

    public void loadData(){
        new Thread(){
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(dataSrc).get();
                    final Elements dataLi0=doc.getElementsByClass("Mid3img").get(0).getElementsByTag("li");
                    final Elements dataLi1=doc.getElementsByClass("LLBlist").get(0).getElementsByTag("li");
                    String pattern="/[0-9]*\\.";

                    final View[] topListViews=new View[dataLi0.size()];
                    for(int i=0;i<dataLi0.size();i=i+2){
                        Element element0=dataLi0.get(i);
                        Element element1=dataLi0.get(i+1);
                        final String imageUrl0=element0.getElementsByTag("img").get(0).attr("src");
                        final String title0=element0.getElementsByClass("txt").get(0).html();
                        String link0=element0.getElementsByTag("a").get(0).attr("href");
                        final String imageUrl1=element1.getElementsByTag("img").get(0).attr("src");
                        final String title1=element1.getElementsByClass("txt").get(0).html();
                        String link1=element1.getElementsByTag("a").get(0).attr("href");
                        Pattern r = Pattern.compile(pattern);
                        Matcher m = r.matcher(link0);
                        if(m.find()) {
                            link0="https://wap.gamersky.com/gl/Content-"+link0.substring(m.start()+1,m.end()-1)+".html";
                        }
                        m=r.matcher(link1);
                        if(m.find()){
                            link1="https://wap.gamersky.com/gl/Content-"+link1.substring(m.start()+1,m.end()-1)+".html";
                        }
                        Log.i("TAG", "run: "+link0+"\t"+link1);
                        final View vc = LayoutInflater.from(getContext())
                                .inflate(R.layout.linearlayout_handbook_top, null, false);
                        final RoundImageView roundImageView0=vc.findViewById(R.id.roundImageView0);
                        final TextView textView0=vc.findViewById(R.id.textView0);
                        final RoundImageView roundImageView1=vc.findViewById(R.id.roundImageView1);
                        final TextView textView1=vc.findViewById(R.id.textView1);
                        if(!AppSetting.isRoundCorner){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                roundImageView0.setForeground(getResources().getDrawable(R.drawable.pressed_image,null));
                                roundImageView1.setForeground(getResources().getDrawable(R.drawable.pressed_image,null));
                            }
                        }

                        final String finalLink0 = link0;
                        final String finalLink1 = link1;
                        roundImageView0.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent=new Intent(getContext(), ArticleActivity.class);
                                intent.putExtra("new_data",new NewDataBean(title0, finalLink0));
                                getContext().startActivity(intent);
                            }
                        });
                        roundImageView1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent=new Intent(getContext(), ArticleActivity.class);
                                intent.putExtra("new_data",new NewDataBean(title1, finalLink1));
                                getContext().startActivity(intent);
                            }
                        });
                        topList.post(new Runnable() {
                            @Override
                            public void run() {
                                textView0.setText(title0);
                                textView1.setText(title1);
                                Glide.with(roundImageView0)
                                        .load(imageUrl0)
                                        .transition(DrawableTransitionOptions.withCrossFade())
                                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(AppSetting.bigRoundCorner)))
                                        .into(roundImageView0);
                                Glide.with(roundImageView1)
                                        .load(imageUrl1)
                                        .transition(DrawableTransitionOptions.withCrossFade())
                                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(AppSetting.bigRoundCorner)))
                                        .into(roundImageView1);
                            }
                        });
                        topListViews[i]=vc;
                    }

                    final View[] hotListViews=new View[dataLi1.size()];
                    for(int i=0;i<dataLi1.size();i++){
                        Element element=dataLi1.get(i);
                        final String title=element.getElementsByClass("tit").get(0)
                                .getElementsByTag("a").html();
                        String link=element.getElementsByClass("tit").get(0)
                                .getElementsByTag("a").attr("href");
                        final SpannableString ss=new SpannableString("• "+(i+1)+"\t\t\t"+ Html.fromHtml(title));
                        ForegroundColorSpan foregroundColorSpan=new ForegroundColorSpan(getResources().getColor(R.color.colorAccent));
                        ss.setSpan(foregroundColorSpan,0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        Pattern r = Pattern.compile(pattern);
                        Matcher m = r.matcher(link);
                        if(m.find()) {
                            link="https://wap.gamersky.com/gl/Content-"+link.substring(m.start()+1,m.end()-1)+".html";
                        }
                        final View vc = LayoutInflater.from(getContext())
                                .inflate(R.layout.linearlayout_handbook_list, null, false);
                        final TextView textView=vc.findViewById(R.id.textView0);
                        final String finalLink = link;
                        textView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent=new Intent(getContext(), ArticleActivity.class);
                                intent.putExtra("new_data",new NewDataBean(title, finalLink));
                                getContext().startActivity(intent);
                            }
                        });
                        hotList.post(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(ss);
                            }
                        });
                        hotListViews[i]=vc;
                    }

                    nestedScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            topList.removeAllViews();
                            hotList.removeAllViews();
                            for(int i=0;i<dataLi0.size();i++){
                                if(topListViews[i]!=null)
                                topList.addView(topListViews[i]);
                            }
                            for(int i=0;i<dataLi1.size();i++){
                                if(hotListViews[i]!=null)
                                hotList.addView(hotListViews[i]);
                            }
                            progressBar.setVisibility(View.GONE);
                            nestedScrollView.setVisibility(View.VISIBLE);
                            refreshLayout.setRefreshing(false);
                            if(!firstRun){
                                AppUtil.getSnackbar(getContext(),nestedScrollView,getString(R.string.updata_successed),true,true).show();
                            }
                            firstRun=false;
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                    nestedScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);
                            AppUtil.getSnackbar(getContext(),nestedScrollView,getString(R.string.updata_failed),true,true).show();
                        }
                    });
                }
            }
        }.start();
    }

    public void startListen(){
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                Intent intent=new Intent(getContext(), SearchActivity.class);
                intent.putExtra("key",query);
                intent.putExtra("category","handbook");
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
    }

    public void upTop(){
        nestedScrollView.smoothScrollTo(0,0);
    }

}
