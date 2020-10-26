package com.news.gamersky.databean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class GameDetailDataBean {
    public String id;
    public String title;
    public String enTitle;
    public ArrayList<GamePlatformAndTime> gamePlatformAndTimeArrayList;
    public ArrayList<Review> reviews;
    public String gameTime;
    public String gameType;
    public String supportChinese;
    public String issue;
    public String shortIntroduction;
    public String introduction;

    public GameDetailDataBean() {
    }

    public static class GamePlatformAndTime{
        public String gamePlatform;
        public String issueDate;

        public GamePlatformAndTime() {
        }

        public GamePlatformAndTime(String gamePlatform, String issueDate) {
            this.gamePlatform = gamePlatform;
            this.issueDate = issueDate;
        }
    }

    public static class Review{
        public String mediaName;
        public String score;
        public String content;
        public String url;

        public Review(){}

        public Review(String mediaName, String score, String content, String url) {
            this.mediaName = mediaName;
            this.score = score;
            this.content = content;
            this.url = url;
        }
    }

}
