package com.news.gamersky.databean;

import java.io.Serializable;

public class NewDataBean implements Serializable {
    public  String id;
    public String title;
    public String imageUrl;
    public String src;
    public String date;
    public String sort;
    public String commentCount;
    public String content;


    public NewDataBean(){}

    public NewDataBean(String imageUrl, String title, String src) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.src = src;

    }
    public NewDataBean(String title, String src) {
        this.title = title;
        this.src = src;

    }
    public NewDataBean(String id, String imageUrl, String title, String src, String date, String sort, String commentCount) {
        this.id=id;
        this.imageUrl = imageUrl;
        this.title = title;
        this.src = src;
        this.date=date;
        this.sort=sort;
        this.commentCount=commentCount;
    }

    public NewDataBean(String id, String title, String src, String date, String sort, String content) {
        this.id=id;
        this.title = title;
        this.src = src;
        this.date=date;
        this.sort=sort;
        this.content=content;
    }

    public void setCommentCount(String commentCount){
        this.commentCount=commentCount;
    }

}
