package com.news.gamersky.Util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.news.gamersky.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppUtil {

    public static Toast toast;

    public static Toast getToast(Activity activity, String message){
        toast = new Toast(activity);
        View toastview = activity.getLayoutInflater().inflate(R.layout.toast, null);
        toast.setView(toastview);
        TextView tv = toastview.findViewById(R.id.textView15);
        tv.setText(message);
        return toast;
    }
    public static void stopToast(){
        if (toast!=null) toast.cancel();
    }

    public static SpannableString keyTextColor(String text,String key,int color){
        SpannableString spannableString = new SpannableString(text);
        try {
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
            spannableString.setSpan(colorSpan, text.indexOf(key), text.indexOf(key)+key.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }catch (Exception e){
           // e.printStackTrace();
            System.out.println("着色失败");
        }
        return spannableString;
    }

    public static Snackbar getSnackbar(Context context,View view,String msg){
        Snackbar snackbar= Snackbar.make(view,msg,1000);
        View snackbarView = snackbar.getView();
        ((TextView) snackbarView.findViewById(R.id.snackbar_text)).setTextColor(Color.BLACK);
        snackbar.setBackgroundTint(context.getResources().getColor(R.color.colorPrimary));
        return snackbar;
    }

    public static String is2s(InputStream inputStream){
        String str="";
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            str = result.toString(StandardCharsets.UTF_8.name());
        }catch (Exception e){
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 根据毫秒时间戳来格式化字符串
     * 今天显示今天、昨天显示昨天、前天显示前天.
     * 早于前天的显示具体年-月-日，如2017-06-12；
     * @param timeStamp 毫秒值
     * @return 今天 昨天 前天 或者 yyyy-MM-dd HH:mm:ss类型字符串
     */
    public static String format(long timeStamp) {
        long curTimeMillis = System.currentTimeMillis();
        Date curDate = new Date(curTimeMillis);
        int todayHoursSeconds = curDate.getHours() * 60 * 60;
        int todayMinutesSeconds = curDate.getMinutes() * 60;
        int todaySeconds = curDate.getSeconds();
        int todayMillis = (todayHoursSeconds + todayMinutesSeconds + todaySeconds) * 1000;
        long todayStartMillis = curTimeMillis - todayMillis;
        SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
        if(timeStamp >= todayStartMillis) {
            return "今天 "+sdf1.format(new Date(timeStamp));
        }
        int oneDayMillis = 24 * 60 * 60 * 1000;
        long yesterdayStartMilis = todayStartMillis - oneDayMillis;
        if(timeStamp >= yesterdayStartMilis) {
            return "昨天 "+sdf1.format(new Date(timeStamp));
        }
        long yesterdayBeforeStartMilis = yesterdayStartMilis - oneDayMillis;
        if(timeStamp >= yesterdayBeforeStartMilis) {
            return "前天 "+sdf1.format(new Date(timeStamp));
        }
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return  sdf2.format(new Date(timeStamp));
    }
}
