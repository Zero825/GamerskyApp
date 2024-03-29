package com.news.gamersky.util;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.ColorUtils;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;
import com.news.gamersky.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AppUtil {

    private final static String TAG="AppUtil";


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

    //游民星空链接获取id
    public static String urlToId(String url){
        String s=null;
        try {
            String id=new StringBuffer(url).reverse().toString();
            id=id.substring(id.indexOf(".")+1,id.indexOf("/"));
            id=new StringBuffer(id).reverse().toString();
            s=id;
        }catch (Exception e){
            e.printStackTrace();
        }
        return s;
    }

    public static Snackbar getSnackbar(Context context,View view,String msg,boolean primaryColor,boolean setAnchorView){
        Snackbar snackbar= Snackbar.make(view,msg,1500);
        if(primaryColor){
            snackbar.setBackgroundTint(context.getColor(R.color.colorPrimary));
            snackbar.setTextColor(context.getColor(R.color.textColorPrimary));
        }
        //snackbar.getView().setElevation(0f);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(!sharedPreferences.getBoolean("no_bottombar",false)&&setAnchorView){
            snackbar.setAnchorView(R.id.nav_view);
        }
//        snackbar.setAnchorView(R.id.nav_view);
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
        Calendar calendar=Calendar.getInstance();
        int todayHoursSeconds = calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60;
        int todayMinutesSeconds = calendar.get(Calendar.MINUTE) * 60;
        int todaySeconds = calendar.get(Calendar.SECOND);
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

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


    public static ObjectAnimator rockObjectAnimator(View view){
        float f1=0,f2=10,f3=-10,f4=10,f5=0;
        ObjectAnimator objectAnimator=ObjectAnimator.ofFloat(view,"translationX",f1,f2,f3,f4,f5);
        objectAnimator.setDuration(100);
        return objectAnimator;
    }

    //判断图片亮色还是暗色
    public static boolean isDark(int color) {
        return ColorUtils.calculateLuminance(color) < 0.5;
    }

    /**
     * 获取状态栏高度
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    //高斯模糊
    public static Bitmap rsBlur(Context context, Bitmap source, int radius){

        Bitmap inputBmp = source;
        //(1)
        RenderScript renderScript =  RenderScript.create(context);

        Log.i(TAG,"scale size:"+inputBmp.getWidth()+"*"+inputBmp.getHeight());

        // Allocate memory for Renderscript to work with
        //(2)
        final Allocation input = Allocation.createFromBitmap(renderScript,inputBmp);
        final Allocation output = Allocation.createTyped(renderScript,input.getType());
        //(3)
        // Load up an instance of the specific script that we want to use.
        ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        //(4)
        scriptIntrinsicBlur.setInput(input);
        //(5)
        // Set the blur radius
        scriptIntrinsicBlur.setRadius(radius);
        //(6)
        // Start the ScriptIntrinisicBlur
        scriptIntrinsicBlur.forEach(output);
        //(7)
        // Copy the output to the blurred bitmap
        output.copyTo(inputBmp);
        //(8)
        renderScript.destroy();

        return inputBmp;
    }

    public static int getDisplayWidth(Context context){
        Point point=new Point();
        ((Activity)context).getWindowManager().getDefaultDisplay().getSize(point);
        return point.x;
    }

    public static boolean compereByteArray(byte[] b1, byte[] b2) {
        if(b1.length == 0 || b2.length == 0 ){
            return false;
        }
        if (b1.length != b2.length) {
            return false;
        }
        boolean isEqual = true;
        for (int i = 0; i < b1.length; i++) {
            if (b1[i] != b2[i]) {
                isEqual = false;
                break;
            }
        }
        return isEqual;
    }

    //sha-256加密
    public static byte[] getSHA256Bytes(String str,int times){
        MessageDigest messageDigest;
        byte[] bytesResult = new byte[256];
        byte[] strBytes=str.getBytes();
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            for(int i=0;i<times;i++) {
                messageDigest.update(strBytes);
            }
            bytesResult = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return bytesResult;
    }
}
