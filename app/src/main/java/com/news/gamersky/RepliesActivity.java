package com.news.gamersky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.news.gamersky.databean.CommentDataBean;

public class RepliesActivity extends AppCompatActivity {
    private String commentId;
    private String clubContentId;
    private ConstraintLayout constraintLayout;
    private ImageButton imageButton;
    private CommentDataBean comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replies);
        init();
        loadReplies();
        startListen();
    }

    public void init(){
        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        TextView textView=findViewById(R.id.textView20);
        constraintLayout=findViewById(R.id.constraintLayout);
        imageButton=findViewById(R.id.imageButton);
        Intent intent = getIntent();
        commentId = intent.getStringExtra("commentId");
        clubContentId = intent.getStringExtra("clubContentId");
        comment= (CommentDataBean) intent.getSerializableExtra("comment");
        textView.setText(comment.userName+"的回复");
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(constraintLayout, "translationY", 500f, 0f);
        objectAnimator.setDuration(300);
        objectAnimator.start();
    }

    public void loadReplies(){
        if(clubContentId!=null){

        }else {

        }
    }

    public void startListen(){
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(constraintLayout, "translationY", 0f, 500f);
        objectAnimator.setDuration(300);
        objectAnimator.start();

    }
}