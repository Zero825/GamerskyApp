package com.news.gamersky.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.news.gamersky.dao.UserDao;
import com.news.gamersky.entity.User;

@Database(entities = {User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
}