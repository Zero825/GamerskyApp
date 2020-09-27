package com.news.gamersky.databean;



public class GameDataBean {
    public String id;
    public String title;
    public String enTitle;
    public String picUrl;
    public String listedTime;
    public String gameMake;
    public String officialChinese;
    public String itemUrl;
    public String ratingAverage;

    public GameDataBean() {}

    public GameDataBean(String id, String title, String enTitle, String picUrl, String listedTime, String gameMake, String officialChinese,String itemUrl,String ratingAverage) {
        this.id = id;
        this.title = title;
        this.enTitle = enTitle;
        this.picUrl = picUrl;
        this.listedTime = listedTime;
        this.gameMake = gameMake;
        this.officialChinese = officialChinese;
        this.itemUrl=itemUrl;
        this.ratingAverage=ratingAverage;
    }
}
