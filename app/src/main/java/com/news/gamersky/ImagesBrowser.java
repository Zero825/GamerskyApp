package com.news.gamersky;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.ImageLoader;
import com.github.piasy.biv.view.BigImageView;
import com.github.piasy.biv.view.GlideImageViewFactory;
import com.google.android.material.snackbar.Snackbar;
import com.news.gamersky.fragment.ImageDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static androidx.core.content.FileProvider.getUriForFile;
import static java.security.AccessController.getContext;

public class ImagesBrowser extends AppCompatActivity implements ImageDialogFragment.ImageDialogListener {
    private ViewPager viewPager;
    private String imagesSrc;
    private int imagePosition;
    private  BigImageView bigImageViewTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.images_browser);
        init();
        loadData();
    }
    public void  init(){
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS|WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS|WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);

        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        viewPager=findViewById(R.id.images_viewpager);

        Intent intent = getIntent();
        imagesSrc = intent.getStringExtra("imagesSrc");
        imagePosition = intent.getIntExtra("imagePosition",0);
        System.out.println("位置"+imagePosition+"json"+imagesSrc);

    }


    public void loadData(){
        try {
            JSONArray jsonArray=new JSONArray(imagesSrc);

            viewPager.setAdapter(new AdapterViewpager(jsonArray));
            viewPager.setCurrentItem(imagePosition,false);
            //viewPager.setOffscreenPageLimit(2);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDownloadClick(DialogFragment dialog) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions={Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this,permissions, 1);
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(viewPager, "没有写入权限", Snackbar.LENGTH_LONG).show();
        }else {
            try {
                JSONArray jsonArray=new JSONArray(imagesSrc);
                JSONObject jsonObject=jsonArray.getJSONObject(viewPager.getCurrentItem());
                String url=jsonObject.getString("origin");
                String suffix=url.substring(url.indexOf(".",50));
                File path1 = bigImageViewTemp.getCurrentImageFile();
                System.out.println(path1.getName()+""+suffix);
                File path2=new File("/storage/emulated/0/Pictures/Gamersky/"+path1.getName().substring(0,10)+suffix);
                File path3=new File("/storage/emulated/0/Pictures/Gamersky");
                if(!path3.exists()){
                    path3.mkdir();
                }
                FileChannel inputChannel = new FileInputStream(path1).getChannel();
                FileChannel outputChannel = new FileOutputStream(path2).getChannel();
                outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
                inputChannel.close();
                outputChannel.close();
                Snackbar.make(viewPager, "图片下载成功", Snackbar.LENGTH_LONG).show();
            }
            catch (Exception e){
                Snackbar.make(viewPager, "图片下载失败", Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onShareClick(DialogFragment dialog) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions={Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this,permissions, 1);
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(viewPager, "没有写入权限", Snackbar.LENGTH_LONG).show();
        }else {
            try {

                File path = bigImageViewTemp.getCurrentImageFile();
                Uri contentUri = FileProvider.getUriForFile(this,
                        "com.news.gamersky.fileprovider", path);
                System.out.println(contentUri+"        "+path.getAbsolutePath());
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM,contentUri);
                shareIntent.setType("image/*");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "分享到"));

            }
            catch (Exception e){
                Snackbar.make(viewPager, "图片分享失败", Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }
    }


    public class MyViewpager2Adapter extends RecyclerView.Adapter<MyViewpager2Adapter.MyViewHolder> {
        private JSONArray jsonArray;


        public MyViewpager2Adapter(JSONArray jsonArray){
            this.jsonArray=jsonArray;
        }

        public  class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case

            public BigImageView imageView;
            public ProgressBar progressBar;
            public MyViewHolder(View v) {
                super(v);
                imageView=v.findViewById(R.id.imageView8);
                progressBar=v.findViewById(R.id.progressBar2);
            }
        }



        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.images_viewpager, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
            try{
                JSONObject jsonObject2=jsonArray.getJSONObject(position);
                System.out.println(jsonObject2.getString("origin"));

                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finishAfterTransition();
                        System.out.println("结束浏览图片");
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }



        }

        @Override
        public int getItemCount() {
            return jsonArray.length();
        }
    }

    public class AdapterViewpager extends PagerAdapter {
        private JSONArray jsonArray;

        public AdapterViewpager(JSONArray jsonArray) {
            this.jsonArray = jsonArray;
        }

        @Override
        public int getCount() {//必须实现
            return jsonArray.length();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {//必须实现
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.images_viewpager, container, false);
            final BigImageView imageView=v.findViewById(R.id.imageView8);
            final ProgressBar progressBar=v.findViewById(R.id.progressBar2);
            try{
                JSONObject jsonObject2=jsonArray.getJSONObject(position);
                System.out.println(jsonObject2.getString("origin"));
                imageView.setImageLoaderCallback(new ImageLoader.Callback() {
                    @Override
                    public void onCacheHit(int imageType, File image) {

                    }

                    @Override
                    public void onCacheMiss(int imageType, File image) {

                    }

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onProgress(int progress) {

                    }

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void onSuccess(File image) {
                        progressBar.setVisibility(View.GONE);
                       if(imageView.getSSIV()!=null) {
                           System.out.println(imageView.getSSIV());
                           imageView.getSSIV().setMaxScale(5.0f);
                           imageView.getSSIV().setDoubleTapZoomDuration(300);


                           imageView.getSSIV().setOnImageEventListener(new SubsamplingScaleImageView.OnImageEventListener() {
                               @Override
                               public void onReady() {
                                   final int LONG_IMAGE_SIZE_RATIO = 2;
                                   SubsamplingScaleImageView mImageView=imageView.getSSIV();
                                   float result = 0.5f;
                                   int imageWidth = mImageView.getSWidth();
                                   int imageHeight = mImageView.getSHeight();
                                   int viewWidth = mImageView.getWidth();
                                   int viewHeight = mImageView.getHeight();

                                   boolean hasZeroValue = false;
                                   if (imageWidth == 0 || imageHeight == 0 || viewWidth == 0 || viewHeight == 0) {
                                       result = 0.5f;
                                       hasZeroValue = true;
                                   }

                                   if (!hasZeroValue) {
                                       if (imageWidth <= imageHeight) {
                                           result = (float) viewWidth / imageWidth;
                                       } else {
                                           result = (float) viewHeight / imageHeight;
                                       }
                                   }

                                   if (!hasZeroValue && (float) imageHeight / imageWidth > LONG_IMAGE_SIZE_RATIO) {
                                       // scale at top
                                       mImageView
                                               .animateScaleAndCenter(result, new PointF(imageWidth / 2, 0))
                                               .withEasing(SubsamplingScaleImageView.EASE_OUT_QUAD)
                                               .start();

                                       // `对结果进行放大裁定，防止计算结果跟双击放大结果过于相近`
                                       if (Math.abs(result - 0.1) < 0.2f) {
                                           result += 0.2f;
                                       }
                                       mImageView.setDoubleTapZoomScale(result);
                                   }else {
                                       Display defaultDisplay = getWindowManager().getDefaultDisplay();
                                       Point point = new Point();
                                       defaultDisplay.getRealSize(point);
                                       float hh=(float) point.y/(float) imageView.getSSIV().getSHeight();
                                       imageView.getSSIV().setDoubleTapZoomScale(hh);
                                   }

                               }

                               @Override
                               public void onImageLoaded() {

                               }

                               @Override
                               public void onPreviewLoadError(Exception e) {

                               }

                               @Override
                               public void onImageLoadError(Exception e) {

                               }

                               @Override
                               public void onTileLoadError(Exception e) {

                               }

                               @Override
                               public void onPreviewReleased() {

                               }
                           });
                       }
                    }

                    @Override
                    public void onFail(Exception error) {

                    }
                });
                imageView.setImageViewFactory(new GlideImageViewFactory());
                imageView.showImage(Uri.parse(jsonObject2.getString("origin")));
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finishAfterTransition();
                        System.out.println("结束浏览图片");
                    }
                });
                imageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        try {
                            bigImageViewTemp=imageView;
                            DialogFragment newFragment = new ImageDialogFragment();
                            newFragment.show(getSupportFragmentManager(), "imageMenu");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        return false;
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }
            container.addView(v);

            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {//必须实现，销毁
            container.removeView((View) object);
        }
    }



}
