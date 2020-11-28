package com.news.gamersky.adapter;


import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.news.gamersky.ImagesBrowserActivity;
import com.news.gamersky.R;
import com.news.gamersky.setting.AppSetting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class GameHeaderViewpager2Adapter extends RecyclerView.Adapter {
    private final static String TAG="GHViewpager2Adapter";

    public ArrayList<String> dataList;
    public int size;

    public GameHeaderViewpager2Adapter(ArrayList<String> dataList) {
        this.dataList = dataList;
        this.size=8;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.viewpager2_image,parent,false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final ImageViewHolder imageViewHolder= (ImageViewHolder) holder;
        Log.i(TAG, "onBindViewHolder: "+position+dataList.get(position));
        Glide.with(imageViewHolder.imageView)
                .load(dataList.get(position))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageViewHolder.imageView);
        imageViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONArray jsonArray=new JSONArray();
                for(int i=0;i<dataList.size();i++) {
                    try {
                        jsonArray.put(i, new JSONObject().put("origin", dataList.get(i)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Intent intent=new Intent(imageViewHolder.imageView.getContext(), ImagesBrowserActivity.class);
                intent.putExtra("imagesSrc",jsonArray.toString());
                intent.putExtra("imagePosition",position);
                imageViewHolder.imageView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return Math.min(size,dataList.size());
    }

    public void setSize(int size){
        this.size=size;
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.imageView20);
        }
    }
}
