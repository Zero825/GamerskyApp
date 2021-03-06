package com.news.gamersky.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.news.gamersky.LoginActivity;
import com.news.gamersky.R;
import com.news.gamersky.UserFavoritesActivity;
import com.news.gamersky.customizeview.RoundImageView;
import com.news.gamersky.entity.User;
import com.news.gamersky.util.UserMsgUtil;

public class UserFragment extends Fragment {
    private final static String TAG="UserFragment";
    public final static int LOGIN_REQUEST=0;
    public final static int GALLERY_REQUEST=2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_user,container,false);
        init(view);
        startListen(view);
        return view;
    }

    public void init(View view){
        lodaUserMsg(view);
    }

    public void startListen(View view){
        view.findViewById(R.id.myFavorites).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), UserFavoritesActivity.class));
            }
        });
        view.findViewById(R.id.userName).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(), LoginActivity.class);
                startActivityForResult(intent,LOGIN_REQUEST);
            }
        });
        view.findViewById(R.id.userAvatar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toGallery = new Intent(Intent.ACTION_PICK);
                toGallery.setType("image/*");
                //toGallery.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(toGallery, GALLERY_REQUEST);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==LOGIN_REQUEST&&resultCode==LoginActivity.LOGIN_SUCCESS){
            Log.i(TAG, "onActivityResult: "+"login_ok");
            if(getView()!=null) {
                lodaUserMsg(getView());
            }
        }
        if(requestCode==GALLERY_REQUEST){
            if(data!=null) {
                Log.i(TAG, "onActivityResult: " + data.getData());
                View view=getView();
                if(view!=null) {
                    RoundImageView userAvatar=(RoundImageView)view.findViewById(R.id.userAvatar);
                    UserMsgUtil.putUserAvatar(getContext(),UserMsgUtil.getUserName(getContext()),data.getData().toString());
                    Glide.with(userAvatar).load(data.getData()).centerCrop().into(userAvatar);
                }
            }
        }
    }

    public void lodaUserMsg(View view){
        ((TextView)view.findViewById(R.id.userName)).setText(UserMsgUtil.getUserName(getContext()));
        RoundImageView userAvatar=view.findViewById(R.id.userAvatar);
        String userAvatarPath=UserMsgUtil.getUserAvatar(getContext(), UserMsgUtil.getUserName(getContext()));
        if(userAvatarPath.isEmpty()) {
            Glide.with(userAvatar).load(getContext().getDrawable(R.drawable.avatar)).centerCrop().into(userAvatar);
        }else {
            Glide.with(userAvatar).load(userAvatarPath).centerCrop().into(userAvatar);
        }
    }

    public void upTop(){}
}
