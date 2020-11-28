package com.news.gamersky.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.news.gamersky.R;
import com.news.gamersky.SearchActivity;
import com.news.gamersky.adapter.ViewPagerFragmentAdapter;
import com.news.gamersky.customizeview.FixViewPager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_SET_USER_VISIBLE_HINT;

public class GalleryFragment extends Fragment {
    private final static String TAG="GalleryFragment";

    public final static String TIME_ASC="time_asc";
    public final static String TIME_DESC="time_desc";
    public final static String HOT_ASC="hot_asc";
    public final static String HOT_DESC="hot_desc";

    private boolean isHomePage;
    private String src,nowSort;
    private int nowPosition;
    private ArrayList<String> tabsName;
    private ArrayList<String> tabsSrc;
    private ArrayList<Fragment> fragments;
    private FixViewPager viewPager;
    private TabLayout tabLayout;
    private ConstraintLayout galleryContainer;
    private SearchView searchView;
    private ProgressBar progressBar;
    private TextView timeSortText,hotSortText;
    private ImageView timeSortImage,hotSortImage,logoGallery;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_gallery, container, false);
        init(view);
        loadTab();
        return view;
    }

    public void init(View view){
        Bundle bundle=getArguments();
        if(bundle!=null){
            src=bundle.getString("src");
            isHomePage=bundle.getBoolean("isHomePage");
        }else {
            src = "http://pic.gamersky.com/";
            isHomePage=true;
        }
        if(!isHomePage){
            view.findViewById(R.id.gallery_bar).setVisibility(View.GONE);
        }
        nowSort=TIME_DESC;
        nowPosition=0;
        tabsName=new ArrayList<>();
        tabsSrc=new ArrayList<>();
        fragments=new ArrayList<>();

        searchView=view.findViewById(R.id.searchView);
        viewPager=view.findViewById(R.id.container);
        tabLayout=view.findViewById(R.id.tabLayout);
        progressBar=view.findViewById(R.id.progressBar);
        galleryContainer=view.findViewById(R.id.galleryContainer);
        timeSortImage=view.findViewById(R.id.sortTimeImage);
        hotSortImage=view.findViewById(R.id.sortHotImage);
        timeSortText=view.findViewById(R.id.sortTime);
        hotSortText=view.findViewById(R.id.sortHot);
        logoGallery=view.findViewById(R.id.logoGallery);

    }

    public void loadTab(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document=Jsoup.connect(src).get();
                    Elements tabsElements=document.getElementsByClass("topnav").get(0)
                            .getElementsByTag("a");
                    if(isHomePage) {
                        for (Element element : tabsElements) {
                            tabsSrc.add(element.attr("href"));
                            tabsName.add(element.getElementsByTag("b").html());
                        }
                    }else {
                        for (Element element : tabsElements) {
                            String href=element.attr("href");
                            if(href.contains("pic.gamersky.com")) {
                                tabsSrc.add(href);
                                tabsName.add(element.getElementsByTag("b").html());
                            }
                        }
                    }
                    for(int i=0;i<tabsName.size();i++){
                        Bundle bundle=new Bundle();
                        bundle.putString("src",tabsSrc.get(i));
                        bundle.putBoolean("isHomePage",isHomePage);
                        CommonGalleryFragment commonGalleryFragment=new CommonGalleryFragment();
                        commonGalleryFragment.setArguments(bundle);
                        fragments.add(commonGalleryFragment);
                    }
                    viewPager.post(new Runnable() {
                        @Override
                        public void run() {
                            viewPager.setOffscreenPageLimit(fragments.size());
                            viewPager.setAdapter(new ViewPagerFragmentAdapter(getChildFragmentManager(),fragments,tabsName,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));
                            tabLayout.setupWithViewPager(viewPager);
                            galleryContainer.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            startListen();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void startListen(){
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                nowPosition=position;
                Fragment fragment=getFragment(position);
                if(fragment!=null){
                    String sort=((CommonGalleryFragment)fragment).getSort();
                    nowSort=sort;
                    changeSortBar(sort);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        timeSortText.setOnClickListener(new View.OnClickListener() {
            boolean isAsc=false;
            @Override
            public void onClick(View v) {
                if(nowSort.equals(TIME_ASC)||nowSort.equals(TIME_DESC)) {
                    if (!isAsc) {
                        isAsc = true;
                        nowSort=TIME_ASC;
                        changeSortBar(TIME_ASC);
                        ((CommonGalleryFragment) getFragment(nowPosition)).loadData(TIME_ASC);
                    } else {
                        isAsc = false;
                        nowSort=TIME_DESC;
                        changeSortBar(TIME_DESC);
                        ((CommonGalleryFragment) getFragment(nowPosition)).loadData(TIME_DESC);
                    }
                }else {
                    isAsc = false;
                    nowSort=TIME_DESC;
                    changeSortBar(TIME_DESC);
                    ((CommonGalleryFragment) getFragment(nowPosition)).loadData(TIME_DESC);
                }
            }
        });

        hotSortText.setOnClickListener(new View.OnClickListener() {
            boolean isAsc=false;
            @Override
            public void onClick(View v) {
                if(nowSort.equals(HOT_ASC)||nowSort.equals(HOT_DESC)) {
                    if (!isAsc) {
                        isAsc = true;
                        nowSort=HOT_ASC;
                        changeSortBar(HOT_ASC);
                        ((CommonGalleryFragment) getFragment(nowPosition)).loadData(HOT_ASC);

                    } else {
                        isAsc = false;
                        nowSort=HOT_DESC;
                        changeSortBar(HOT_DESC);
                        ((CommonGalleryFragment) getFragment(nowPosition)).loadData(HOT_DESC);

                    }
                }else {
                    isAsc = false;
                    nowSort=HOT_DESC;
                    changeSortBar(HOT_DESC);
                    ((CommonGalleryFragment) getFragment(nowPosition)).loadData(HOT_DESC);
                }
            }
        });

        logoGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upTop();
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

    private void changeSortBar(String sort){
        if(sort==TIME_ASC) {
            timeSortText.setText(R.string.sort_time_asc);
            timeSortText.setTextColor(getContext().getColor(R.color.colorAccent));
            hotSortText.setTextColor(getContext().getColor(R.color.defaultColor));
            timeSortImage.setImageDrawable(getContext().getDrawable(R.drawable.icon_sort_asc));
            hotSortImage.setImageDrawable(getContext().getDrawable(R.drawable.icon_sort_none));
        } else if(sort==TIME_DESC){
            timeSortText.setText(R.string.sort_time_desc);
            timeSortText.setTextColor(getContext().getColor(R.color.colorAccent));
            hotSortText.setTextColor(getContext().getColor(R.color.defaultColor));
            timeSortImage.setImageDrawable(getContext().getDrawable(R.drawable.icon_sort_desc));
            hotSortImage.setImageDrawable(getContext().getDrawable(R.drawable.icon_sort_none));
        }else if(sort==HOT_ASC){
            hotSortText.setText(R.string.sort_hot_asc);
            timeSortText.setTextColor(getContext().getColor(R.color.defaultColor));
            hotSortText.setTextColor(getContext().getColor(R.color.colorAccent));
            timeSortImage.setImageDrawable(getContext().getDrawable(R.drawable.icon_sort_none));
            hotSortImage.setImageDrawable(getContext().getDrawable(R.drawable.icon_sort_asc));
        }else if(sort==HOT_DESC){
            hotSortText.setText(R.string.sort_hot_desc);
            timeSortText.setTextColor(getContext().getColor(R.color.defaultColor));
            hotSortText.setTextColor(getContext().getColor(R.color.colorAccent));
            timeSortImage.setImageDrawable(getContext().getDrawable(R.drawable.icon_sort_none));
            hotSortImage.setImageDrawable(getContext().getDrawable(R.drawable.icon_sort_desc));
        }
    }

    private Fragment getFragment(int position){
        FragmentManager fragmentManager=getChildFragmentManager();
        return  fragmentManager.findFragmentByTag("android:switcher:"+viewPager.getId()+":"+position);
    }

    public void upTop(){
        Fragment fragment=getFragment(nowPosition);
        if(fragment!=null){
            ((CommonGalleryFragment)fragment).upTop();
        }
    }

}
