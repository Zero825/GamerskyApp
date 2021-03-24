package com.news.gamersky.database;

import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.news.gamersky.ThisApp;

public class AppDataBaseSingleton {
    private static final String DATABASE_NAME="UserDatabase";

    private static final AppDatabase DATABASE =Room.databaseBuilder(ThisApp.getContext(),
            AppDatabase.class, DATABASE_NAME).build();

    public static AppDatabase getAppDatabase(){
        return DATABASE;
    }
}
