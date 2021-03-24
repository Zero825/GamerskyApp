package com.news.gamersky;

import android.app.Activity;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.news.gamersky.database.AppDataBaseSingleton;
import com.news.gamersky.entity.User;
import com.news.gamersky.util.AppUtil;
import com.news.gamersky.util.UserMsgUtil;

import java.lang.ref.WeakReference;

public class LoginActivity extends AppCompatActivity {
    private final static String TAG="LoginActivity";

    public final static int LOGIN_SUCCESS=1;
    public final static int LOGIN_FAIL=0;
    private final static int LOGIN_MODE=1;
    private final static int REGISTER_MODE=0;
    private final static int SHA256_TIMES=38;
    private int mode=LOGIN_MODE;
    public View layoutView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        startListen();
    }

    private void init(){
        layoutView=findViewById(R.id.loginLayout);
    }

    private void startListen(){
        final TextView nameText=findViewById(R.id.userName);
        final EditText passwordText=findViewById(R.id.userPassword);
        final EditText comfirmPasswordText=findViewById(R.id.comfirmPassword);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=nameText.getText().toString();
                String password=passwordText.getText().toString();
                String comfirmPassword=comfirmPasswordText.getText().toString();
                if(mode==REGISTER_MODE) {
                    if(name.isEmpty()){
                        AppUtil.getSnackbar(LoginActivity.this,LoginActivity.this.layoutView,
                                LoginActivity.this.getString(R.string.please_input_user_name)
                                ,false,false)
                                .show();
                    }else if (password.isEmpty()){
                        AppUtil.getSnackbar(LoginActivity.this,LoginActivity.this.layoutView,
                                LoginActivity.this.getString(R.string.please_input_user_password)
                                ,false,false)
                                .show();
                    }else if(comfirmPassword.isEmpty()){
                        AppUtil.getSnackbar(LoginActivity.this,LoginActivity.this.layoutView,
                                LoginActivity.this.getString(R.string.please_confirm_user_password)
                                ,false,false)
                                .show();
                    }else {
                        if (password.equals(comfirmPassword)) {
                            User user = new User();
                            user.name = name;
                            user.password = AppUtil.getSHA256Bytes(passwordText.getText().toString(), SHA256_TIMES);
                            RegisterAsyncTask registerAsyncTask
                                    = new RegisterAsyncTask(new WeakReference<LoginActivity>(LoginActivity.this));
                            registerAsyncTask.execute(user);
                        } else {
                            AppUtil.getSnackbar(LoginActivity.this, LoginActivity.this.layoutView,
                                    LoginActivity.this.getString(R.string.error_password_inconsistent)
                                    , false, false)
                                    .show();
                        }
                    }
                }else if(mode==LOGIN_MODE){
                    if(name.isEmpty()){
                        AppUtil.getSnackbar(LoginActivity.this,LoginActivity.this.layoutView,
                                LoginActivity.this.getString(R.string.please_input_user_name)
                                ,false,false)
                                .show();
                    }else if (password.isEmpty()){
                        AppUtil.getSnackbar(LoginActivity.this,LoginActivity.this.layoutView,
                                LoginActivity.this.getString(R.string.please_input_user_password)
                                ,false,false)
                                .show();
                    }else {
                        LoginAsyncTask loginAsyncTask = new LoginAsyncTask(new WeakReference<LoginActivity>(LoginActivity.this));
                        loginAsyncTask.execute(name,password);
                    }
                }
            }
        });
        findViewById(R.id.changeMode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mode==LOGIN_MODE){
                    ((TextView)findViewById(R.id.title)).setText(R.string.register);
                    ((TextView)findViewById(R.id.changeMode)).setText(R.string.login);
                    ((TextView)findViewById(R.id.login)).setText(R.string.register);
                    findViewById(R.id.comfirmPassword).setVisibility(View.VISIBLE);
                    mode=REGISTER_MODE;
                }else if(mode==REGISTER_MODE){
                    ((TextView)findViewById(R.id.title)).setText(R.string.login);
                    ((TextView)findViewById(R.id.changeMode)).setText(R.string.register);
                    ((TextView)findViewById(R.id.login)).setText(R.string.login);
                    findViewById(R.id.comfirmPassword).setVisibility(View.GONE);
                    mode=LOGIN_MODE;
                }

            }
        });

    }

    private static class RegisterAsyncTask extends AsyncTask<User, Void, Boolean>{
        private WeakReference<LoginActivity> activityWeakReference;

        public RegisterAsyncTask(WeakReference<LoginActivity> activityWeakReference) {
            this.activityWeakReference = activityWeakReference;
        }

        @Override
        protected Boolean doInBackground(User... users) {
            try {
                AppDataBaseSingleton.getAppDatabase().userDao().insertUser(users[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            LoginActivity loginActivity=activityWeakReference.get();
            if(!success){
                AppUtil.getSnackbar(loginActivity,loginActivity.layoutView,
                        loginActivity.getString(R.string.error_register)
                        ,false,false)
                        .show();
            }else {
                AppUtil.getSnackbar(loginActivity,loginActivity.layoutView,
                        loginActivity.getString(R.string.success_register)
                        ,false,false)
                        .show();
                loginActivity.findViewById(R.id.changeMode).callOnClick();
            }
        }

    }

    private static class LoginAsyncTask extends AsyncTask<String, Void, String>{
        private WeakReference<LoginActivity> activityWeakReference;

        public LoginAsyncTask(WeakReference<LoginActivity> activityWeakReference) {
            this.activityWeakReference = activityWeakReference;
        }

        @Override
        protected String doInBackground(String... msg) {
            User user=AppDataBaseSingleton.getAppDatabase().userDao().findByNameAndPassword(msg[0],AppUtil.getSHA256Bytes(msg[1], SHA256_TIMES));
            if(user==null){
                return null;
            }else {
                return user.name;
            }
        }

        @Override
        protected void onPostExecute(String userName) {
            final LoginActivity loginActivity=activityWeakReference.get();
            if(userName==null){
                AppUtil.getSnackbar(loginActivity,loginActivity.layoutView,
                        loginActivity.getString(R.string.error_login)
                        ,false,false)
                        .show();
            }else {
                loginActivity.setResult(LOGIN_SUCCESS);
                UserMsgUtil.putUserName(loginActivity,userName);
                loginActivity.finish();
            }

        }

    }


}
