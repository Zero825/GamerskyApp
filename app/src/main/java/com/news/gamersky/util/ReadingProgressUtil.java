package com.news.gamersky.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import static android.content.Context.MODE_PRIVATE;

public class ReadingProgressUtil {
    private static SharedPreferences sharedPreferences;
    private static String name="readingProgress";
    private static SharedPreferences sharedPreferences1;
    private static String name1="clickNewsList";
    private static SharedPreferences sharedPreferences2;
    private static String name2="clickSearchList";

    public static void putProgress(Context context,String key,int value){
        if(!PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("save_read_progress",false)){
            return;
        }
        if(sharedPreferences==null){
            sharedPreferences=context.getSharedPreferences(name,MODE_PRIVATE);
        }
        sharedPreferences.edit().putInt(key,value).apply();
    }

    public static int getProgress(Context context,String key){
        if(!PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("save_read_progress",false)){
            return -1;
        }
        if(sharedPreferences==null){
            sharedPreferences=context.getSharedPreferences(name,MODE_PRIVATE);
        }
        return sharedPreferences.getInt(key,-1);
    }

    public static void clearReadingProgress(Context context) {
        if(sharedPreferences==null){
            sharedPreferences=context.getSharedPreferences(name,MODE_PRIVATE);
        }
        sharedPreferences.edit().clear().apply();

    }

    public static void putNewsClick(Context context,String key,Boolean value){
        if(!PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("save_article_click",true)){
            return;
        }
        if(sharedPreferences1==null){
            sharedPreferences1=context.getSharedPreferences(name1,MODE_PRIVATE);
        }
        sharedPreferences1.edit().putBoolean(key,value).apply();
    }

    public static boolean getNewsClick(Context context, String key){
        if(!PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("save_article_click",true)){
            return false;
        }
        if(sharedPreferences1==null){
            sharedPreferences1=context.getSharedPreferences(name1,MODE_PRIVATE);
        }
        return sharedPreferences1.getBoolean(key,false);
    }

    public static void clearNewsClickList(Context context) {
        if(sharedPreferences1==null){
            sharedPreferences1=context.getSharedPreferences(name1,MODE_PRIVATE);
        }
        sharedPreferences1.edit().clear().apply();

    }

    public static void putSearchClick(Context context,String key,Boolean value){
        if(!PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("save_article_click",true)){
            return;
        }
        if(sharedPreferences2==null){
            sharedPreferences1=context.getSharedPreferences(name2,MODE_PRIVATE);
        }
        sharedPreferences2.edit().putBoolean(key,value).apply();
    }

    public static boolean getSearchClick(Context context, String key){
        if(!PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("save_article_click",true)){
            return false;
        }
        if(sharedPreferences2==null){
            sharedPreferences2=context.getSharedPreferences(name2,MODE_PRIVATE);
        }
        return sharedPreferences2.getBoolean(key,false);
    }

    public static void clearSearchClickList(Context context) {
        if(sharedPreferences2==null){
            sharedPreferences2=context.getSharedPreferences(name2,MODE_PRIVATE);
        }
        sharedPreferences2.edit().clear().apply();

    }



}
