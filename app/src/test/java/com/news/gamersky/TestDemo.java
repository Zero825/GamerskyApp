package com.news.gamersky;
import android.util.Log;

import androidx.room.Room;

import com.news.gamersky.database.AppDatabase;
import com.news.gamersky.util.AppUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.news.gamersky.util.AppUtil.is2s;

public class TestDemo {
    private final static String TAG="TestDemo";

    @Test
    public void test(){

//        String password1="123456";
//        String password2="12345";
//        try {
//            MessageDigest messageDigest=MessageDigest.getInstance("SHA-256");
//            messageDigest.update(password1.getBytes());
//            byte[] bytesResult1=messageDigest.digest();
//            messageDigest.update(password2.getBytes());
//            byte[] bytesResult2=messageDigest.digest();
//            System.out.println(AppUtil.compereByteArray(bytesResult1,bytesResult2));
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }



    }


}
