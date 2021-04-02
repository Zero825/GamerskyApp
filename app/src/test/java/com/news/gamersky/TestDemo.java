package com.news.gamersky;
import android.util.Log;

import androidx.room.Room;

import com.news.gamersky.database.AppDatabase;
import com.news.gamersky.util.AppUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
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
    }

}
