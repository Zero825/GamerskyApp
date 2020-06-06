package com.news.gamersky.databean;

import java.io.Serializable;
import java.util.ArrayList;

public class CommentDataBean implements Serializable {
    public String commentId;
    public String replyId;
    public String clubContentId;
    public String userImage;
    public String userName;
    public String objectUserName;
    public String time;
    public String likeNum;
    public String content;
    public String floor;
    public String imagesJson;
    public ArrayList<String> images;
    public ArrayList<CommentDataBean> replies;
    public String repliesCount;


    public CommentDataBean(String commentId,String userImage, String userName, String time, String likeNum, String content, String floor, ArrayList<String> images,String imagesJson,ArrayList<CommentDataBean> replies,String repliesCount) {
        this.commentId=commentId;
        this.userImage = userImage;
        this.userName = userName;
        this.time = time;
        this.likeNum = likeNum;
        this.content = content;
        this.floor = floor;
        this.images = images;
        this.imagesJson=imagesJson;
        this.replies=replies;
        this.repliesCount=repliesCount;
    }
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


    public CommentDataBean(String commentId, String clubContentId, String userImage, String userName, String time, String likeNum, String content, String floor, ArrayList<String> images, String imagesJson, ArrayList<CommentDataBean> replies, String repliesCount) {
        this.commentId=commentId;
        this.clubContentId=clubContentId;
        this.userImage = userImage;
        this.userName = userName;
        this.time = time;
        this.likeNum = likeNum;
        this.content = content;
        this.floor = floor;
        this.images = images;
        this.imagesJson=imagesJson;
        this.replies=replies;
        this.repliesCount=repliesCount;
    }

    public CommentDataBean(String userImage, String userName, String time, String likeNum, String content,String objectUserName) {
        this.userImage = userImage;
        this.userName = userName;
        this.time = time;
        this.likeNum = likeNum;
        this.content = content;
        this.objectUserName=objectUserName;
    }

    public CommentDataBean(String replyId,String userImage, String userName, String time, String likeNum, String content,String objectUserName) {
        this.replyId=replyId;
        this.userImage = userImage;
        this.userName = userName;
        this.time = time;
        this.likeNum = likeNum;
        this.content = content;
        this.objectUserName=objectUserName;
    }


    @Override
    public String toString() {
        return "CommentDataBean{" +
                "commentId='" + commentId + '\'' +
                ", clubContentId='" + clubContentId + '\'' +
                ", userImage='" + userImage + '\'' +
                ", userName='" + userName + '\'' +
                ", objectUserName='" + objectUserName + '\'' +
                ", time='" + time + '\'' +
                ", likeNum='" + likeNum + '\'' +
                ", content='" + content + '\'' +
                ", floor='" + floor + '\'' +
                ", imagesJson='" + imagesJson + '\'' +
                ", images=" + images +
                ", replies=" + replies +
                ", repliesCount='" + repliesCount + '\'' +
                '}';
    }

    public void setLikeNum(String likeNum){
        this.likeNum=likeNum;
    }

}
