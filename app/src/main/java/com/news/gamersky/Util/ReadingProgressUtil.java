package com.news.gamersky.Util;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class ReadingProgressUtil {
    private static SharedPreferences sharedPreferences;
    private static String name="readingProgress";
    private static SharedPreferences sharedPreferences1;
    private static String name1="clickList";

    public static void putProgress(Context context,String key,int value){
        if(sharedPreferences==null){
            sharedPreferences=context.getSharedPreferences(name,MODE_PRIVATE);
        }
        sharedPreferences.edit().putInt(key,value).apply();
    }

    public static int getProgress(Context context,String key){
        if(sharedPreferences==null){
            sharedPreferences=context.getSharedPreferences(name,MODE_PRIVATE);
        }
        return sharedPreferences.getInt(key,0);
    }

    public static void clearReadingProgress(Context context) {
        if(sharedPreferences==null){
            sharedPreferences=context.getSharedPreferences(name,MODE_PRIVATE);
        }
        sharedPreferences.edit().clear();

    }

    public static void putClick(Context context,String key,Boolean value){
        if(sharedPreferences1==null){
            sharedPreferences1=context.getSharedPreferences(name1,MODE_PRIVATE);
        }
        sharedPreferences1.edit().putBoolean(key,value).apply();
    }

    public static boolean getClick(Context context, String key){
        if(sharedPreferences1==null){
            sharedPreferences1=context.getSharedPreferences(name1,MODE_PRIVATE);
        }
        return sharedPreferences1.getBoolean(key,false);
    }

    public static void clearClickList(Context context) {
        if(sharedPreferences1==null){
            sharedPreferences1=context.getSharedPreferences(name1,MODE_PRIVATE);
        }
        sharedPreferences1.edit().clear();

    }



}
