package com.news.gamersky;

import android.content.ContentUris;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.news.gamersky.databean.PictureDataBean;

import java.util.ArrayList;

public class UserFavoritesActivity extends AppCompatActivity {
    private final static String TAG="UserFavoritesActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_favorites);
        init();
        startListen();
    }

    public void init(){

    }

    synchronized public ArrayList<PictureDataBean> loadData(){
        ArrayList<PictureDataBean> tempData=new ArrayList<>();

        String[] paths=new String[]{MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPath()};
        String[] mimeTypes=new String[]{"image/jpeg","image/png","image/bmp"};
        MediaScannerConnection.scanFile(this,paths,mimeTypes,null);

        String[] projection=new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME
        };
        String selection=new String(MediaStore.Images.Media.DATA+" LIKE ?");
        String[] selectionArgs=new String[]{
                "%Gamersky%"
        };

        Cursor cursor=this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projection,selection,selectionArgs,null);

        int idColumn=cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        int nameColumn=cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
        while (cursor.moveToNext()) {
            long id = cursor.getLong(idColumn);
            String name = cursor.getString(nameColumn);
            Log.i(TAG, "loadData: "+id+"\t"+name);
            Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id);
            PictureDataBean pictureDataBean=new PictureDataBean();
            pictureDataBean.title=name;
            pictureDataBean.uri=contentUri;
            tempData.add(pictureDataBean);
        }
        cursor.close();
        return tempData;
    }

    synchronized public void bindData(ArrayList<PictureDataBean> data){

    }

    public void startListen(){
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
