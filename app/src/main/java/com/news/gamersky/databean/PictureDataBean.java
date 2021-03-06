package com.news.gamersky.databean;

import android.net.Uri;

import java.net.URI;
import java.util.ArrayList;

public class PictureDataBean {
    public String id;
    public String title;
    public String generalId;
    public String nodeName;
    public String itemTitle;
    public String hot;
    public String tinyPictureUrl;
    public String smallPictureUrl;
    public String originPictureUrl;
    public String itemUrl;
    public int originHeight;
    public int originWidth;
    public String imagesJSON;
    public Uri uri;

    public PictureDataBean() {
    }

    public PictureDataBean(String smallPictureUrl) {
        this.smallPictureUrl = smallPictureUrl;
    }

    public PictureDataBean(String smallPictureUrl,int originWidth,int originHeight) {
        this.smallPictureUrl = smallPictureUrl;
        this.originHeight = originHeight;
        this.originWidth = originWidth;
    }


    public static ArrayList<PictureDataBean> getTestData(){
        ArrayList<PictureDataBean> data=new ArrayList<>();
        data.add(new PictureDataBean("https://img1.gamersky.com/upimg/pic/2020/11/02/202011021645035905_tiny.jpg",1024,1024));
        data.add(new PictureDataBean("https://img1.gamersky.com/upimg/pic/2020/11/03/202011031412524266_tiny.jpg",2560,1440));
        data.add(new PictureDataBean("https://img1.gamersky.com/upimg/pic/2020/11/02/202011021516516269_tiny.jpg",1820,1024));
        data.add(new PictureDataBean("https://img1.gamersky.com/upimg/pic/2020/09/30/202009301121585783_tiny.jpg",1080,1536));
        data.add(new PictureDataBean("https://img1.gamersky.com/upimg/pic/2020/07/13/202007130512258764_tiny.jpg",1920,2715));
        data.add(new PictureDataBean("https://img1.gamersky.com/upimg/pic/2020/03/19/202003191833392750_tiny.jpg",960,960));
        data.add(new PictureDataBean("https://img1.gamersky.com/upimg/pic/2019/12/26/201912261723319905_tiny.jpg",550,778));
        return data;
    }
}
