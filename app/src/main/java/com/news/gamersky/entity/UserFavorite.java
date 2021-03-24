package com.news.gamersky.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Entity(tableName = "user_favorites",primaryKeys = {"user_name","href"})
public class UserFavorite {
    public final static int TYPE_NEW=0;
    public final static int TYPE_HANDBOOK=1;
    public final static int TYPE_GAME=2;


    @ColumnInfo(name = "user_name")
    @NonNull
    public String userName;

    @ColumnInfo(name = "href")
    @NonNull
    public String href;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "time")
    public String time;

    @ColumnInfo(name = "type")
    public int type;

    @ColumnInfo(name = "pic_url")
    public String picUrl;

    public UserFavorite(){}

    @Ignore
    public UserFavorite(@NotNull String userName, @NotNull String href, String title, String time, int type) {
        this.userName=userName;
        this.href = href;
        this.title = title;
        this.time = time;
        this.type = type;
    }

    public static ArrayList<UserFavorite> getTestData(){
        ArrayList<UserFavorite> data=new ArrayList<>();
        data.add(new UserFavorite("用户名","","ssss","1111",TYPE_NEW));
        data.add(new UserFavorite("用户名","","ssss","2222",TYPE_NEW));
        data.add(new UserFavorite("用户名","","ssss","3333",TYPE_NEW));
        return data;
    }


}
