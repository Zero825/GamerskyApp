package com.news.gamersky.databean;

import java.util.ArrayList;
import java.util.List;

public class NewsDataBean {
    public  String id;
    public String title;
    public String imageUrl;
    public String src;
    public String data;
    public String sort;
    public String commentCount;


    public NewsDataBean(String imageUrl, String title, String src) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.src = src;

    }
    public NewsDataBean(String title, String src) {
        this.title = title;
        this.src = src;

    }
    public NewsDataBean(String id,String imageUrl, String title, String src, String data, String sort,String commentCount) {
        this.id=id;
        this.imageUrl = imageUrl;
        this.title = title;
        this.src = src;
        this.data=data;
        this.sort=sort;
        this.commentCount=commentCount;
    }

    public void setCommentCount(String commentCount){
        this.commentCount=commentCount;
    }

    public static List<NewsDataBean> getTestData() {
        List<NewsDataBean> list = new ArrayList<>();

        list.add(new NewsDataBean("https://imgs.gamersky.com/pic/2020/20200409_yy_461_2.jpg", "相信自己,你努力的样子真的很美", ""));
        list.add(new NewsDataBean("https://image.gamersky.com/gameshd/2020/20200407_gd_340_11.jpg", "极致简约,梦幻小屋", ""));
        list.add(new NewsDataBean("https://image.gamersky.com/gameshd/2020/20200407_gd_340_12.jpg", "超级卖梦人", ""));
        list.add(new NewsDataBean("https://image.gamersky.com/gameshd/2020/20200407_gd_340_13.jpg", "夏季新搭配", ""));
        list.add(new NewsDataBean("https://imgs.gamersky.com/pic/2020/20200402_yy_461_2.jpg", "绝美风格搭配", ""));
        return list;
    }
}
