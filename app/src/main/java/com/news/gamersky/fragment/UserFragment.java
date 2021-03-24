package com.news.gamersky.fragment;

import android.app.WallpaperColors;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.news.gamersky.LoginActivity;
import com.news.gamersky.R;
import com.news.gamersky.SettingsActivity;
import com.news.gamersky.UserFavoritesActivity;
import com.news.gamersky.customizeview.RoundImageView;
import com.news.gamersky.dialog.SignOutDialogFragment;
import com.news.gamersky.entity.User;
import com.news.gamersky.setting.AppSetting;
import com.news.gamersky.util.AppUtil;
import com.news.gamersky.util.UserMsgUtil;

public class UserFragment extends Fragment {
    private final static String TAG="UserFragment";
    public final static int LOGIN_REQUEST=0;
    public final static int GALLERY_REQUEST=2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        startListen();
    }

    public void init(){
        lodaUserMsg(getView());
    }

    public void startListen(){
        View view=getView();

        view.findViewById(R.id.myFavorites).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), UserFavoritesActivity.class));
            }
        });
        view.findViewById(R.id.mySettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), SettingsActivity.class));
            }
        });
        view.findViewById(R.id.signOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignOutDialog();
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
                if(UserMsgUtil.getUserName(getContext()).equals(getString(R.string.user_name))){
                    Intent intent=new Intent(getContext(), LoginActivity.class);
                    startActivityForResult(intent,LOGIN_REQUEST);
                }else {
                    Intent toGallery = new Intent(Intent.ACTION_PICK);
                    toGallery.setType("image/*");
                    startActivityForResult(toGallery, GALLERY_REQUEST);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==LOGIN_REQUEST&&resultCode==LoginActivity.LOGIN_SUCCESS){
            Log.i(TAG, "onActivityResult: "+"login_ok");
            AppUtil.getSnackbar(getContext(),getView(), getString(R.string.success_login)
                    ,false,true)
                    .show();
            if(getView()!=null) {
                lodaUserMsg(getView());
            }
        }
        if(requestCode==GALLERY_REQUEST){
            if(data!=null) {
                Log.i(TAG, "onActivityResult: " + data.getData());
                View view=getView();
                if(view!=null) {
                    UserMsgUtil.putUserAvatar(getContext(),UserMsgUtil.getUserName(getContext()),data.getData().toString());
                    lodaUserMsg(view);
                }
            }
        }
    }

    public void lodaUserMsg(View view){
        final TextView userName=view.findViewById(R.id.userName);
        final RoundImageView userAvatar=view.findViewById(R.id.userAvatar);
        final RoundImageView userMsgBg=view.findViewById(R.id.userMsgBg);
        String userAvatarPath=UserMsgUtil.getUserAvatar(getContext(), UserMsgUtil.getUserName(getContext()));
        userMsgBg.setRound(AppSetting.bigRoundCorner);
        userName.setText(UserMsgUtil.getUserName(getContext()));
        if(!userAvatarPath.isEmpty()) {
            Glide.with(userAvatar)
                    .asBitmap()
                    .load(userAvatarPath)
                    .centerCrop()
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @RequiresApi(api = Build.VERSION_CODES.O_MR1)
                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            int primaryColor= WallpaperColors.fromBitmap(resource).getPrimaryColor().toArgb();
                            userMsgBg.setImageDrawable(new ColorDrawable(primaryColor));
                            if(AppUtil.isDark(primaryColor)){
                                userName.setTextColor(getContext().getColor(R.color.textColorBanner));
                            }else {
                                userName.setTextColor(getContext().getColor(R.color.darkBackground));
                            }
                            return false;
                        }
                    })
                    .into(userAvatar);
        }else {
            Glide.with(userAvatar).load(getContext().getDrawable(R.drawable.avatar)).centerCrop().into(userAvatar);
        }
    }

    public void showSignOutDialog() {
        SignOutDialogFragment dialog = new SignOutDialogFragment();
        dialog.setSignOutDialogListener(new SignOutDialogFragment.SignOutDialogListener() {
            @Override
            public void onConfirmClick(DialogFragment dialog) {
                signOut();
            }

            @Override
            public void onCancelClick(DialogFragment dialog) {

            }
        });
        dialog.show(getChildFragmentManager(),"SignOutDialogFragment");
    }
    private void signOut(){
        if(!UserMsgUtil.getUserName(getContext()).equals(getString(R.string.user_name))) {
            UserMsgUtil.putUserName(getContext(), getString(R.string.user_name));
            lodaUserMsg(getView());
        }
        AppUtil.getSnackbar(getContext(),getView(),getString(R.string.sign_out_success),false,true).show();
    }

    public void upTop(){}
}
