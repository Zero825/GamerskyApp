package com.news.gamersky.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.news.gamersky.R;
import com.news.gamersky.adapter.PictureRecyclerViewAdapter;
import com.news.gamersky.databean.PictureDataBean;
import com.news.gamersky.util.AppUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.security.auth.login.LoginException;

import static com.news.gamersky.util.AppUtil.is2s;

public class CommonGalleryFragment extends Fragment {
    private static final String TAG="CommonGalleryFragment";

    private boolean isFirstResume,isFirstLoad,isHomePage;
    private int page,flag,lastFlag;
    private String src,nodeId,generalId,sort;
    private ExecutorService executor;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private PictureRecyclerViewAdapter pictureRecyclerViewAdapter;
    private ArrayList<PictureDataBean> pictureData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_common_gallery,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle=getArguments();
        if(bundle!=null){
            src=bundle.getString("src");
            isHomePage=bundle.getBoolean("isHomePage");
            init();
            startListen();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        if(isFirstResume&&src!=null) {
            isFirstResume=false;
            loadData("time_desc");
        }

    }


    public void init(){
        Log.i(TAG, "init: "+"初始化");
        View view=getView();

        isFirstResume=true;
        isFirstLoad=true;
        flag=0;
        lastFlag=0;
        page=1;
        sort="time_desc";

        pictureData=new ArrayList<>();
        executor= Executors.newSingleThreadExecutor();

        recyclerView=view.findViewById(R.id.recyclerView);
        swipeRefreshLayout=view.findViewById(R.id.swipeRefreshLayout);

        recyclerView.setLayoutManager(staggeredGridLayoutManager=new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(pictureRecyclerViewAdapter=new PictureRecyclerViewAdapter(getContext(),pictureData));
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

    }

    public String findNodeId(String src){
        String nodeId=null;
        try {
            Document document= Jsoup.connect(src).get();
            Elements idElements1=document.getElementsByClass("pbllist pageContainer");
            if(idElements1.size()!=0){
                nodeId=idElements1.attr("data-nodeId");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return nodeId;
    }

    public String findGeneralId(String src){
        String generalId=null;
        String id1=AppUtil.urlToId(src);
        if(id1!=null){
            generalId=id1;
        }
        return generalId;
    }

    public synchronized void loadData(String sort){
        if(swipeRefreshLayout.isRefreshing()){
           return;
        }
        recyclerView.smoothScrollToPosition(0);
        swipeRefreshLayout.setRefreshing(true);
        flag=0;
        lastFlag=0;
        page=1;
        connect(sort,1).start();
    }

    public synchronized Thread loadMoreData(String sort){
        page++;
        return connect(sort,page);
    }

    public Thread connect(final String mSort, final int mPageIndex){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                int pageSize=50;
                if(isHomePage) {
                    if(nodeId==null) {
                        nodeId = findNodeId(src);
                    }
                }else {
                    if(generalId==null) {
                        generalId =findGeneralId(src);
                    }
                }
                sort=mSort;
                String src=null;
                if(isHomePage){
                    src = "http://pic.gamersky.com/home/getimagesindex?" +
                            "sort=" + sort + "&" +
                            "pageIndex=" + mPageIndex + "&" +
                            "pageSize=" + pageSize + "&" +
                            "nodeId=" + nodeId;
                }else {
                    src = "http://pic.gamersky.com/home/getimages?" +
                            "jsondata=" +
                            "{\"sort\":" +"\""+ sort +"\""+ "," +
                            "\"pageIndex\":" + mPageIndex + "," +
                            "\"pageSize\":" + pageSize + "," +
                            "\"tagId\":\"0\"," +
                            "\"gameId\":\"0\"," +
                            "\"generalId\":" +"\""+ generalId+"\""+ "}";
                }
                Log.i(TAG, "run: "+src);
                HttpURLConnection connection=null;
                try{
                    URL url = new URL(src);
                    //得到connection对象。
                    connection = (HttpURLConnection) url.openConnection();
                    //设置请求方式
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Referer",CommonGalleryFragment.this.src);
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
                        result=result.replace("\\","");
                        Log.i(TAG, "run: "+result.substring(0,100));
                        JSONObject dataJSONObject=new JSONObject(result);
                        JSONArray dataJSONArray=dataJSONObject.getJSONArray("body");
                        final ArrayList<PictureDataBean> tempData=new ArrayList<>();
                        for(int i=0;i<dataJSONArray.length();i++){
                            PictureDataBean tempPictureData=new PictureDataBean();
                            JSONObject pictureJSONObject=dataJSONArray.getJSONObject(i);
                            tempPictureData.id=pictureJSONObject.getString("id");
                            tempPictureData.title=pictureJSONObject.getString("title");
                            tempPictureData.generalId=pictureJSONObject.getString("generalId");
                            tempPictureData.nodeName=pictureJSONObject.getString("nodeName");
                            tempPictureData.itemTitle=pictureJSONObject.getString("itemTitle");
                            tempPictureData.hot=pictureJSONObject.getString("hot");
                            tempPictureData.tinyPictureUrl=pictureJSONObject.getString("tinyImg");
                            tempPictureData.smallPictureUrl=pictureJSONObject.getString("smallImg");
                            tempPictureData.originPictureUrl=pictureJSONObject.getString("originImg");
                            tempPictureData.itemUrl=pictureJSONObject.getString("itemUrl");
                            tempPictureData.originHeight=pictureJSONObject.getInt("height");
                            tempPictureData.originWidth=pictureJSONObject.getInt("width");
                            tempData.add(tempPictureData);
                        }
                        JSONArray imagesJsonArray = new JSONArray();
                        if(mPageIndex!=1) {
                            for (int i = 0; i < pictureData.size() + tempData.size(); i++) {
                                if (i < pictureData.size()) {
                                    imagesJsonArray.put(i, new JSONObject().put("origin", pictureData.get(i).originPictureUrl));
                                } else {
                                    imagesJsonArray.put(i, new JSONObject().put("origin", tempData.get(i - pictureData.size()).originPictureUrl));
                                }
                            }
                        }else {
                            for (int i = 0; i < tempData.size(); i++) {
                                imagesJsonArray.put(i, new JSONObject().put("origin", tempData.get(i).originPictureUrl));
                            }
                        }
                        for(int i=0;i<tempData.size();i++){
                            tempData.get(i).imagesJSON=imagesJsonArray.toString();
                        }
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                if(mPageIndex ==1) {
                                    pictureData.clear();
                                    pictureData.addAll(tempData);
                                    pictureRecyclerViewAdapter.notifyDataSetChanged();
                                }else {
                                    pictureData.addAll(tempData);
                                    pictureRecyclerViewAdapter.notifyItemRangeInserted(pictureData.size()-tempData.size(),tempData.size());
                                }
                                if(tempData.size()<50){
                                    pictureRecyclerViewAdapter.setMoreData(false);
                                    pictureRecyclerViewAdapter.notifyItemChanged(pictureRecyclerViewAdapter.getItemCount()-1);
                                }

                                swipeRefreshLayout.setRefreshing(false);
                                if(!isFirstLoad&&mPageIndex==1) {
                                    if (isHomePage) {
                                        AppUtil.getSnackbar(getContext(), recyclerView, getString(R.string.updata_successed), true, true).show();
                                    } else {
                                        AppUtil.getSnackbar(getContext(), recyclerView, getString(R.string.updata_successed), true, false).show();
                                    }
                                }else {
                                    isFirstLoad=false;
                                }
                            }
                        });
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    swipeRefreshLayout.setRefreshing(false);
                    if(!isFirstLoad&&mPageIndex==1) {
                        if (isHomePage) {
                            AppUtil.getSnackbar(getContext(), recyclerView, getString(R.string.updata_failed), true, true).show();
                        } else {
                            AppUtil.getSnackbar(getContext(), recyclerView, getString(R.string.updata_failed), true, false).show();
                        }
                    }else {
                        isFirstLoad=false;
                    }
                    if(page>1){
                        page--;
                        flag=lastFlag;
                    }
                } finally {
                    connection.disconnect();
                }

            }
        });
    }

    public void startListen(){
        recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int[] into=null;
                int[] span=staggeredGridLayoutManager.findLastVisibleItemPositions(into);
               // Log.i(TAG, "onScrollChange: "+span[0]+"\t"+span[1]);
                int line=pictureData.size()-5;

                if((span[0]==line&&span[0]!=flag)||(span[1]==line&&span[1]!=flag)){
                    lastFlag=flag;
                    flag=line;
                    executor.submit(loadMoreData(getSort()));
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                executor.submit(connect(getSort(),1));
            }
        });
    }

    public String getSort() {
        return sort;
    }

    public void upTop(){
        recyclerView.smoothScrollToPosition(0);
    }
}
