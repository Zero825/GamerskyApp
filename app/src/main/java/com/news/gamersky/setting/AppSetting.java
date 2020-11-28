package com.news.gamersky.setting;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.news.gamersky.util.AppUtil;
import com.news.gamersky.util.NightModeUtil;

public class AppSetting {
    public static boolean isRoundCorner;
    public static int smallRoundCorner;
    public static int bigRoundCorner;
    public static int nightMode;


    public static void init(Context context){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        isRoundCorner=sharedPreferences.getBoolean("corner",true);
        if(isRoundCorner) {
            smallRoundCorner = AppUtil.dip2px(context, 4f);
            bigRoundCorner = AppUtil.dip2px(context, 8f);
        }else {
            smallRoundCorner = 1;
            bigRoundCorner = 1;
        }

        nightMode=Integer.parseInt(sharedPreferences.getString("night_mode","2"));
        NightModeUtil.changeNightMode(AppSetting.nightMode);
    }

}
