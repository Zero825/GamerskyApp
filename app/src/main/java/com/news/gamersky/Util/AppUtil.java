package com.news.gamersky.Util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppUtil {
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
