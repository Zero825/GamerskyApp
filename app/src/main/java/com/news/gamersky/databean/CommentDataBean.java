package com.news.gamersky.databean;

import java.util.ArrayList;

public class CommentDataBean {
    public String userImage;
    public String userName;
    public String time;
    public String likeNum;
    public String content;
    public String floor;
    public String imagesJson;
    public ArrayList<String> images;


    public CommentDataBean(String userImage, String userName, String time, String likeNum, String content, String floor, ArrayList<String> images,String imagesJson) {
        this.userImage = userImage;
        this.userName = userName;
        this.time = time;
        this.likeNum = likeNum;
        this.content = content;
        this.floor = floor;
        this.images = images;
        this.imagesJson=imagesJson;
    }




}
