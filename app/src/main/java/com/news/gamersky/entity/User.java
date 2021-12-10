package com.news.gamersky.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey@NonNull@ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "password")
    public byte[] password;

}
