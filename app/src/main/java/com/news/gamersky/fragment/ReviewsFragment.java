package com.news.gamersky.fragment;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.news.gamersky.R;
import com.news.gamersky.SearchActivity;
import com.news.gamersky.adapter.GameRecyclerViewAdapter;
import com.news.gamersky.customizeview.LineFeedRadioGroup;
import com.news.gamersky.databean.GameDataBean;
import com.news.gamersky.util.AppUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.news.gamersky.util.AppUtil.is2s;

public class ReviewsFragment extends Fragment {
    private static final String TAG="ReviewsFragment";

    private ObjectAnimator arrowAnimator;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;
    private ProgressBar progressBar;
    private ProgressBar smallProgressBar;
    private ImageView btnLogo;

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private GameRecyclerViewAdapter gameRecyclerViewAdapter;

    private ConstraintLayout gameGenresSelectTip;
    private LineFeedRadioGroup gameGenresSelect;

    private ConstraintLayout gamePlatformSelectTip;
    private LineFeedRadioGroup gamePlatformSelect;

    private ConstraintLayout saleDateSelectTip;
    private LineFeedRadioGroup saleDateSelect;

    private ConstraintLayout gameCompanySelectTip;
    private LineFeedRadioGroup gameCompanySelect;

    private ConstraintLayout officialChineseSelectTip;
    private LineFeedRadioGroup officialChineseSelect;

    private ConstraintLayout gameTagSelectTip;
    private LineFeedRadioGroup gameTagSelect;

    private ArrayList<GameDataBean> gameDataList;
    private HashMap<RadioButton,String> tagUrlHashMap;
    private String tagSrc;
    private String rootNodeId;
    private int pageIndex;
    private int pageSize;
    private String sort;

    private String gameGenresSelectKey;
    private String gamePlatformSelectKey;
    private String saleDateSelectKey;
    private String gameCompanySelectKey;
    private String officialChineseSelectKey;
    private String gameTagSelectKey;
    private String listedSelectKey;

    private TextView btnSortHot;
    private TextView btnSortTime;
    private TextView btnSortEvaluation;

    private  int flag;
    private int lastFlag;
    private ExecutorService executor;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_reviews, container, false);
        View headerView=inflater.inflate(R.layout.recyclerview_reviews_header, container, false);
        init(view,headerView);
        loadTag();
        loadData(tagSrc,false);
        return view;
    }

    public void init(View view,View headerView){

        recyclerView=view.findViewById(R.id.recyclerView);
        swipeRefreshLayout=view.findViewById(R.id.swipeRefreshLayout);
        progressBar=view.findViewById(R.id.progressBar6);
        btnLogo=view.findViewById(R.id.imageView18);
        searchView=view.findViewById(R.id.searchView);

        smallProgressBar=headerView.findViewById(R.id.progressBar7);
        gameGenresSelectTip=headerView.findViewById(R.id.gameGenresSelectTip);
        gameGenresSelect=headerView.findViewById(R.id.radioGroup1);

        gamePlatformSelectTip=headerView.findViewById(R.id.gamePlatformSelectTip);
        gamePlatformSelect=headerView.findViewById(R.id.radioGroup2);

        saleDateSelectTip=headerView.findViewById(R.id.saleDateSelectTip);
        saleDateSelect=headerView.findViewById(R.id.radioGroup3);

        gameCompanySelectTip=headerView.findViewById(R.id.gameCompanySelectTip);
        gameCompanySelect=headerView.findViewById(R.id.radioGroup4);

        officialChineseSelectTip=headerView.findViewById(R.id.officialChineseSelectTip);
        officialChineseSelect=headerView.findViewById(R.id.radioGroup5);

        gameTagSelectTip=headerView.findViewById(R.id.gameTagSelectTip);
        gameTagSelect=headerView.findViewById(R.id.radioGroup6);

        btnSortHot=headerView.findViewById(R.id.textView30);
        btnSortTime=headerView.findViewById(R.id.textView31);
        btnSortEvaluation=headerView.findViewById(R.id.textView32);

        flag=0;
        lastFlag=0;
        executor= Executors.newSingleThreadExecutor();

        tagSrc="https://ku.gamersky.com/sp/";
        rootNodeId="20039";
        pageIndex=1;
        pageSize=36;
        sort="00";

        gameGenresSelectKey="/sp/";
        gamePlatformSelectKey="0";
        saleDateSelectKey="0";
        gameCompanySelectKey="0";
        officialChineseSelectKey="0";
        gameTagSelectKey="0";
        listedSelectKey="0";

        arrowAnimator=new ObjectAnimator();
        tagUrlHashMap=new HashMap<>();
        gameDataList=new ArrayList<>();

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setVisibility(View.INVISIBLE);
        linearLayoutManager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        gameRecyclerViewAdapter=new GameRecyclerViewAdapter(gameDataList,headerView,getContext());
        recyclerView.setAdapter(gameRecyclerViewAdapter);
        recyclerView.hasFixedSize();



    }

    public void loadTag(){
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect(tagSrc).get();
                    final Elements tagList=document.getElementsByClass("Menu").get(0).getElementsByClass("cont");
                    for(int i=0;i<tagList.size();i++) {
                        Elements aTagList =tagList.get(i).getElementsByTag("a");
                        for (int j = 0; j <aTagList.size();j++){
                            final RadioButton radioButton =new RadioButton(getContext());
                            radioButton.setText(aTagList.get(j).html());
                            radioButton.setButtonDrawable(null);
                            radioButton.setBackgroundResource(R.drawable.bg_selected);
                            radioButton.setTextColor(getResources().getColorStateList(R.color.text_color_selector));
                            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
                            radioButton.setPaddingRelative(AppUtil.dip2px(getContext(),6f),AppUtil.dip2px(getContext(),0f),
                                    AppUtil.dip2px(getContext(),6f),AppUtil.dip2px(getContext(),0f));
                            final ViewGroup.MarginLayoutParams marginLayoutParams=new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, AppUtil.dip2px(getContext(),26f));
                            marginLayoutParams.setMargins(AppUtil.dip2px(getContext(),4f),AppUtil.dip2px(getContext(),4f),
                                    AppUtil.dip2px(getContext(), 4f),AppUtil.dip2px(getContext(),0f));
                            tagUrlHashMap.put(radioButton,aTagList.get(j).attr("href"));
                            if(i==0) {
                                swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        gameGenresSelect.addView(radioButton,marginLayoutParams);
                                    }
                                });
                            }else if(i==1) {
                                swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        gamePlatformSelect.addView(radioButton, marginLayoutParams);
                                    }
                                });
                            }else if(i==2) {
                                swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        saleDateSelect.addView(radioButton, marginLayoutParams);
                                    }
                                });
                            }else if(i==3) {
                                swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        gameCompanySelect.addView(radioButton, marginLayoutParams);
                                    }
                                });
                            }else if(i==4) {
                                swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        officialChineseSelect.addView(radioButton, marginLayoutParams);
                                    }
                                });
                            }else if(i==5) {
                                swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        gameTagSelect.addView(radioButton, marginLayoutParams);
                                    }
                                });
                            }
                            final int finalJ = j;
                            swipeRefreshLayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(finalJ ==0) {
                                        radioButton.setChecked(true);
                                    }
                                }
                            });
                        }
                    }
                    swipeRefreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.getAdapter().notifyDataSetChanged();
                            hideAll();
                            startListen();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }

    public void startListen(){

        gameGenresSelectTip.setOnClickListener(new View.OnClickListener() {
            boolean show=true;

            @Override
            public void onClick(View v) {
                if(!arrowAnimator.isRunning()) {
                    gameGenresSelect.showOrHide(show);
                    tipAnimator(gameGenresSelectTip.findViewById(R.id.imageView1), show);
                    show = !show;
                }
            }
        });
        gameGenresSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            private TextView gameGenresSelected=gameGenresSelectTip.findViewById(R.id.gameGenresSelected);

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton tempRadioButton=((RadioButton)group.findViewById(checkedId));
                if(tempRadioButton!=null) {
                    String nowKey = getKeyFromUrl(tagUrlHashMap.get(tempRadioButton), 0);
                    Log.i(TAG, "onCheckedChanged: " + nowKey);
                    if (!gameGenresSelectKey.equals(nowKey)) {
                        gameGenresSelectKey=nowKey;
                        gameGenresSelected.setText(tempRadioButton.getText());
                        loadData(getReferer(),false);
                        gameGenresSelectTip.performClick();
                    }
                }
            }
        });

        gamePlatformSelectTip.setOnClickListener(new View.OnClickListener() {
            boolean show=true;

            @Override
            public void onClick(View v) {
                if(!arrowAnimator.isRunning()) {
                    gamePlatformSelect.showOrHide(show);
                    tipAnimator(gamePlatformSelectTip.findViewById(R.id.imageView2), show);
                    show = !show;
                }
            }
        });
        gamePlatformSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            private TextView gamePlatformSelected=gamePlatformSelectTip.findViewById(R.id.gamePlatformSelected);

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton tempRadioButton=((RadioButton)group.findViewById(checkedId));
                if(tempRadioButton!=null) {
                    String nowKey = getKeyFromUrl(tagUrlHashMap.get(tempRadioButton), 1);
                    Log.i(TAG, "onCheckedChanged: " + nowKey);
                    if (!gamePlatformSelectKey.equals(nowKey)) {
                        gamePlatformSelectKey=nowKey;
                        gamePlatformSelected.setText(tempRadioButton.getText());
                        loadData(getReferer(),false);
                        gamePlatformSelectTip.performClick();
                    }
                }
            }
        });

        saleDateSelectTip.setOnClickListener(new View.OnClickListener() {
            boolean show=true;

            @Override
            public void onClick(View v) {
                if(!arrowAnimator.isRunning()) {
                    saleDateSelect.showOrHide(show);
                    tipAnimator(saleDateSelectTip.findViewById(R.id.imageView3), show);
                    show = !show;
                }
            }
        });
        saleDateSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            private TextView saleDateSelected=saleDateSelectTip.findViewById(R.id.saleDateSelected);

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton tempRadioButton=((RadioButton)group.findViewById(checkedId));
                if(tempRadioButton!=null) {
                    String nowKey = getKeyFromUrl(tagUrlHashMap.get(tempRadioButton), 2);
                    Log.i(TAG, "onCheckedChanged: " + nowKey);
                    if (!saleDateSelectKey.equals(nowKey)) {
                        saleDateSelectKey=nowKey;
                        saleDateSelected.setText(tempRadioButton.getText());
                        loadData(getReferer(),false);
                        saleDateSelectTip.performClick();
                    }
                }
            }
        });

        gameCompanySelectTip.setOnClickListener(new View.OnClickListener() {
            boolean show=true;

            @Override
            public void onClick(View v) {
                if(!arrowAnimator.isRunning()) {
                    gameCompanySelect.showOrHide(show);
                    tipAnimator(gameCompanySelectTip.findViewById(R.id.imageView4), show);
                    show = !show;
                }
            }
        });
        gameCompanySelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            private TextView gameCompanySelected=gameCompanySelectTip.findViewById(R.id.gameCompanySelected);

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton tempRadioButton=((RadioButton)group.findViewById(checkedId));
                if(tempRadioButton!=null) {
                    String nowKey = getKeyFromUrl(tagUrlHashMap.get(tempRadioButton), 3);
                    Log.i(TAG, "onCheckedChanged: " + nowKey);
                    if (!gameCompanySelectKey.equals(nowKey)) {
                        gameCompanySelectKey=nowKey;
                        gameCompanySelected.setText(tempRadioButton.getText());
                        loadData(getReferer(),false);
                        gameCompanySelectTip.performClick();
                    }
                }
            }
        });

        officialChineseSelectTip.setOnClickListener(new View.OnClickListener() {
            boolean show=true;

            @Override
            public void onClick(View v) {
                if(!arrowAnimator.isRunning()) {
                    officialChineseSelect.showOrHide(show);
                    tipAnimator(officialChineseSelectTip.findViewById(R.id.imageView5), show);
                    show = !show;
                }
            }
        });
        officialChineseSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            private TextView officialChineseSelected=officialChineseSelectTip.findViewById(R.id.officialChineseSelected);

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton tempRadioButton=((RadioButton)group.findViewById(checkedId));
                if(tempRadioButton!=null) {
                    String nowKey = getKeyFromUrl(tagUrlHashMap.get(tempRadioButton), 4);
                    Log.i(TAG, "onCheckedChanged: " + nowKey);
                    if (!officialChineseSelectKey.equals(nowKey)) {
                        officialChineseSelectKey=nowKey;
                        officialChineseSelected.setText(tempRadioButton.getText());
                        loadData(getReferer(),false);
                        officialChineseSelectTip.performClick();
                    }
                }
            }
        });

        gameTagSelectTip.setOnClickListener(new View.OnClickListener() {
            boolean show=true;

            @Override
            public void onClick(View v) {
                if(!arrowAnimator.isRunning()) {
                    gameTagSelect.showOrHide(show);
                    tipAnimator(gameTagSelectTip.findViewById(R.id.imageView6), show);
                    show = !show;
                }
            }
        });
        gameTagSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            private TextView gameTagSelected=gameTagSelectTip.findViewById(R.id.gameTagSelected);

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton tempRadioButton=((RadioButton)group.findViewById(checkedId));
                if(tempRadioButton!=null) {
                    String nowKey = getKeyFromUrl(tagUrlHashMap.get(tempRadioButton), 5);
                    Log.i(TAG, "onCheckedChanged: " + nowKey);
                    if (gameTagSelectKey != nowKey) {
                        gameTagSelectKey=nowKey;
                        gameTagSelected.setText(tempRadioButton.getText());
                        loadData(getReferer(),false);
                        gameTagSelectTip.performClick();
                    }
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(getReferer(),true);
            }
        });

        btnSortHot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSortHot.setTextColor(getContext().getResources().getColor(R.color.colorAccent));
                btnSortTime.setTextColor(getContext().getResources().getColor(R.color.textColorPrimary));
                btnSortEvaluation.setTextColor(getContext().getResources().getColor(R.color.textColorPrimary));
                if(sort.equals("00")){
                    sort="01";
                    btnSortHot.setText(getString(R.string.sort_hot_asc));
                }else if(sort.equals("01")){
                    sort="00";
                    btnSortHot.setText(getString(R.string.sort_hot_des));
                }else {
                    sort="00";
                    btnSortHot.setText(getString(R.string.sort_hot_des));
                }
                loadData(getReferer(),false);
            }
        });
        btnSortTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSortHot.setTextColor(getContext().getResources().getColor(R.color.textColorPrimary));
                btnSortTime.setTextColor(getContext().getResources().getColor(R.color.colorAccent));
                btnSortEvaluation.setTextColor(getContext().getResources().getColor(R.color.textColorPrimary));
                if(sort.equals("10")){
                    sort="11";
                    btnSortTime.setText(getString(R.string.sort_time_asc));
                }else if(sort.equals("11")){
                    sort="10";
                    btnSortTime.setText(getString(R.string.sort_time_des));
                }else {
                    sort="10";
                    btnSortTime.setText(getString(R.string.sort_time_des));
                }
                loadData(getReferer(),false);
            }
        });
        btnSortEvaluation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSortHot.setTextColor(getContext().getResources().getColor(R.color.textColorPrimary));
                btnSortTime.setTextColor(getContext().getResources().getColor(R.color.textColorPrimary));
                btnSortEvaluation.setTextColor(getContext().getResources().getColor(R.color.colorAccent));
                if(sort.equals("20")){
                    sort="21";
                    btnSortEvaluation.setText(getString(R.string.sort_evaluation_asc));
                }else if(sort.equals("21")){
                    sort="20";
                    btnSortEvaluation.setText(getString(R.string.sort_evaluation_des));
                }else {
                    sort="20";
                    btnSortEvaluation.setText(getString(R.string.sort_evaluation_des));
                }
                loadData(getReferer(),false);
            }
        });

        btnLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upTop();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastItem=linearLayoutManager.findLastVisibleItemPosition();
                int dataNum=gameDataList.size();
                int line=dataNum-5;

                //Log.i(TAG, "onScrolled: "+dataNum);
                if(dataNum>=pageSize&&lastItem!=flag&&lastItem==line){
                    lastFlag=flag;
                    flag=lastItem;
                    executor.submit(loadMoreData());
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                Intent intent=new Intent(getContext(), SearchActivity.class);
                intent.putExtra("key",query);
                intent.putExtra("category","ku");
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    public void hideAll(){
        gameGenresSelect.hide();
        gamePlatformSelect.hide();
        saleDateSelect.hide();
        gameCompanySelect.hide();
        officialChineseSelect.hide();
        gameTagSelect.hide();
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
    }

    public void loadData(final String referer, final boolean showTip){
        pageIndex=1;
        gameRecyclerViewAdapter.setNoMore(false);
        flag=0;
        lastFlag=0;
        smallProgressBar.setVisibility(View.VISIBLE);
        Log.i(TAG, "loadData: "+referer+"\t"+sort);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String src = "https://ku.gamersky.com/SearchGameLibAjax.aspx?"+
                            "jsondata=" +
                            "{\"rootNodeId\":" + rootNodeId + "," +
                            "\"pageIndex\":" + pageIndex + "," +
                            "\"pageSize\":" + pageSize + "," +
                            "\"sort\":"+"'"+sort+"'"+"}";
                    URL url = new URL(src);
                    //得到connection对象。
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    //设置请求方式
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Referer",referer);
                    //连接
                    connection.connect();
                    //得到响应码
                    int responseCode = connection.getResponseCode();
                    Log.i(TAG, "run: "+responseCode+connection.getHeaderFields().toString());
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        //得到响应流
                        InputStream inputStream = connection.getInputStream();
                        //将响应流转换成字符串
                        String result = is2s(inputStream);//将流转换为字符串。
                        result = result.substring(1, result.length() - 2);
                        //Log.i(TAG, "run: "+result);
                        final ArrayList<GameDataBean> tempGameDataList=new ArrayList<>();
                        JSONObject resultJsonObject=new JSONObject(result);
                        JSONArray resultJsonArray=resultJsonObject.getJSONArray("result");
                        for(int i=0;i<resultJsonArray.length();i++){
                            JSONObject tempJsonObject=(JSONObject)resultJsonArray.get(i);
                            tempGameDataList.add(new GameDataBean(
                                    tempJsonObject.getString("id"),
                                    tempJsonObject.getString("title"),
                                    tempJsonObject.getString("enTitle"),
                                    tempJsonObject.getString("defaultPicUrl"),
                                    tempJsonObject.getString("allTimeT"),
                                    tempJsonObject.getString("gameMake"),
                                    tempJsonObject.getString("officialChinese"),
                                    tempJsonObject.getString("itemUrl"),
                                    tempJsonObject.getString("ratingAverage")
                            ));
                            //Log.i(TAG, "run:"+tempJsonObject.getString("title"));
                        }
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                gameDataList.clear();
                                gameDataList.addAll(tempGameDataList);
                                recyclerView.getAdapter().notifyDataSetChanged();
                                swipeRefreshLayout.setRefreshing(false);
                                if(showTip){
                                    AppUtil.getSnackbar(getContext(),recyclerView,getString(R.string.updata_successed),true,true).show();
                                }
                                if(gameDataList.size()<pageSize){
                                    gameRecyclerViewAdapter.setNoMore(true);
                                }
                                smallProgressBar.setVisibility(View.GONE);
                            }
                        });

                    }
                    connection.disconnect();
                }catch (IOException | JSONException e){
                    e.printStackTrace();
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                            AppUtil.getSnackbar(getContext(),recyclerView,getString(R.string.updata_failed),true,true).show();
                            smallProgressBar.setVisibility(View.GONE);
                        }
                    });
                }

            }
        }).start();

    }

    public Thread loadMoreData(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                pageIndex++;
                try {
                    String src = "https://ku.gamersky.com/SearchGameLibAjax.aspx?"+
                            "jsondata=" +
                            "{\"rootNodeId\":" + rootNodeId + "," +
                            "\"pageIndex\":" + pageIndex + "," +
                            "\"pageSize\":" + pageSize + "," +
                            "\"sort\":"+"'"+sort+"'"+"}";
                    URL url = new URL(src);
                    //得到connection对象。
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    //设置请求方式
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Referer",getReferer());
                    //连接
                    connection.connect();
                    //得到响应码
                    int responseCode = connection.getResponseCode();
                    Log.i(TAG, "run: "+responseCode+connection.getHeaderFields().toString());
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        //得到响应流
                        InputStream inputStream = connection.getInputStream();
                        //将响应流转换成字符串
                        String result = is2s(inputStream);//将流转换为字符串。
                        result = result.substring(1, result.length() - 2);
                        //Log.i(TAG, "run: "+result);
                        final ArrayList<GameDataBean> tempGameDataList=new ArrayList<>();
                        JSONObject resultJsonObject=new JSONObject(result);
                        JSONArray resultJsonArray=resultJsonObject.getJSONArray("result");
                        for(int i=0;i<resultJsonArray.length();i++){
                            JSONObject tempJsonObject=(JSONObject)resultJsonArray.get(i);
                            tempGameDataList.add(new GameDataBean(
                                    tempJsonObject.getString("id"),
                                    tempJsonObject.getString("title"),
                                    tempJsonObject.getString("enTitle"),
                                    tempJsonObject.getString("defaultPicUrl"),
                                    tempJsonObject.getString("allTimeT"),
                                    tempJsonObject.getString("gameMake"),
                                    tempJsonObject.getString("officialChinese"),
                                    tempJsonObject.getString("itemUrl"),
                                    tempJsonObject.getString("ratingAverage")
                            ));
                            //Log.i(TAG, "run:"+tempJsonObject.getString("title"));
                        }
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {

                                gameDataList.addAll(tempGameDataList);
                                gameRecyclerViewAdapter.notifyItemRangeInserted(gameDataList.size() - tempGameDataList.size(),
                                        tempGameDataList.size());
                                if(tempGameDataList.size()<pageSize){
                                    gameRecyclerViewAdapter.setNoMore(true);
                                }
                            }
                        });

                    }else {
                        pageIndex--;
                        flag=lastFlag;
                    }
                    connection.disconnect();
                }catch (IOException | JSONException e){
                    e.printStackTrace();
                    pageIndex--;
                    flag=lastFlag;
                }

            }
        });
    }

    public String getReferer(){
        return "https://ku.gamersky.com"+gameGenresSelectKey
                +gamePlatformSelectKey+"-"+gameCompanySelectKey+"-"
                +saleDateSelectKey+"-"+gameTagSelectKey+"-"
                +listedSelectKey+"-"+officialChineseSelectKey
                +".html"+"?sort="+sort;
    }

    public void tipAnimator(View view, boolean show){

        if(arrowAnimator.getTarget()!=view||!arrowAnimator.isRunning()){
            if(show){
                arrowAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f,180f);
            }else {
                arrowAnimator = ObjectAnimator.ofFloat(view, "rotation", 180f, 0f);
            }
            arrowAnimator.setDuration(300);
            arrowAnimator.start();
        }
    }

    //从链接中取得链接tag关键词
    public String getKeyFromUrl(String url,int pos){
        String tempString="";
        if(pos==0){
            return url;
        }
        if(url.equals("/sp/")){
            tempString="0";
        }else if(pos==1){
            tempString=url.substring(4,url.indexOf("-",4));
        }else if(pos==2){
            tempString=url.substring(8,url.indexOf("-",8));
        }else if(pos==3){
            tempString=url.substring(6,url.indexOf("-",6));
        }else if(pos==4){
            tempString=url.substring(14,url.indexOf(".",14));
        }else if(pos==5){
            tempString=url.substring(10,url.indexOf("-",10));
        }
        return tempString;
    }

    public void upTop(){
        recyclerView.smoothScrollToPosition(0);
    }

}
