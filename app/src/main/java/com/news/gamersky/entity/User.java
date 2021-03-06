package com.news.gamersky.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "users")
public class User {
    @PrimaryKey@NotNull@ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "password")
    public byte[] password;


}
