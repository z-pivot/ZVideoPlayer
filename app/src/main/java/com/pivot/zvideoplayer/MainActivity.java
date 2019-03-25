/*
 * *********************************************************
 *   author   fjm
 *   company  telchina
 *   email    fanjiaming@telchina.net
 *   date     19-3-22 下午5:03
 * ********************************************************
 */

package com.pivot.zvideoplayer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pivot.libvideocore.IVideoPlayer;
import com.pivot.libvideocore.THVideoView;
import com.pivot.libvideocore.floatwindow.FloatParams;
import com.pivot.libvideocore.media.IjkMedia;

import java.util.Arrays;

/**
 * @author fjm
 */
public class MainActivity extends AppCompatActivity {
    THVideoView videoView;

    String mp4 = "https://media.w3.org/2010/05/sintel/trailer.mp4";//初始化播放地址
    String otherMp4 = "http://sinacloud.net/sakaue/shelter.mp4";//切换播放地址
    String m3u8 = "http://ivi.bupt.edu.cn/hls/cctv6hd.m3u8";//直播地址 cctv6
    boolean flag;//记录退出时播放状态 回来的时候继续播放
    int position;//记录销毁时的进度 回来继续盖进度播放
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 19) {//透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_main);
        
        videoView = findViewById(R.id.th_video_view);
        videoView.getCoverImageView().setImageResource(R.mipmap.cover);//设置视频未播放时的背景图
        videoView.setLayoutParams(new LinearLayout.LayoutParams(-1, getResources().getDisplayMetrics().widthPixels * 9 / 16));
        videoView.enterFullMode = 3;//进入全屏的模式 -1什么都不做 0横屏 1竖屏 2传感器自动横竖屏 3根据视频比例自动确定横竖屏
        videoView.isWindowGesture = true;//是否非全屏下也可以手势调节进度
        videoView.setSharpnessUrlList(Arrays.asList(mp4, mp4, mp4));//设置视频url集合 标清 高清 超清 这里不同清晰度的地址用的是同一个，因为没找到
        videoView.startVideo(mp4, "视频123", IjkMedia.class);//初始化播放
        findViewById(R.id.btn_url).setOnClickListener(v -> changeUrl());//点击切换视频
    }
    
    //返回键
    @Override
    public void onBackPressed() {
        //全屏和系统浮窗不finish
        if (videoView.onBackPressed()) {
            if (videoView.isSystemFloatMode()) {
                //系统浮窗返回上一界面 android:launchMode="singleTask"
                moveTaskToBack(true);
            }
            return;
        }
        super.onBackPressed();
    }
    
    @Override
    public void onResume() {//页面第一次或恢复显示时执行
        super.onResume();
        if (flag) {
            videoView.play();
        }
        handler.removeCallbacks(runnable);
        if (position > 0) {
            videoView.seekTo(position);
            position = 0;
        }
    }
    
    @Override
    public void onPause() {//页面跳转或退出时执行 与onResume()对应
        super.onPause();
        if (videoView.isSystemFloatMode()) {
            return;
        }
        flag = videoView.isPlaying();//暂停视频播放
        videoView.pause();
    }
    
    @Override
    public void onStop() {
        super.onStop();
        if (videoView.isSystemFloatMode()) {
            return;
        }
        //不马上销毁 延时15秒
        handler.postDelayed(runnable, 1000 * 15);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();//销毁
        if (videoView.isSystemFloatMode()) {
            videoView.quitWindowFloat();
        }
        videoView.release();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 & resultCode == RESULT_OK) {
            mp4 = data.getData().toString();
            videoView.startVideo(mp4, "视频1", IjkMedia.class);//初始化播放
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("text", mp4));
        }
    }
    
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (videoView.getCurrentState() != IVideoPlayer.STATE_AUTO_COMPLETE) {
                position = videoView.getPosition();
            }
            videoView.release();
        }
    };
    
    //切换视频地址
    public void changeUrl() {
        final EditText editText = new EditText(this);
        editText.setText(otherMp4);
        new AlertDialog.Builder(this).setView(editText).setTitle("网络视频地址").setNegativeButton("确定", (dialog, which) -> {
            mp4 = editText.getText().toString();
            videoView.setSharpnessUrlList(Arrays.asList(mp4, mp4, mp4));
            videoView.startVideo(mp4, "视频1", IjkMedia.class);//初始化播放
        }).setPositiveButton("本地视频", (dialog, which) -> {
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, 1);
        }).create().show();
    }
    
    //进入浮窗
    private void enterFloat(boolean isSystemFloat) {
        FloatParams floatParams = videoView.getFloatParams();
        if (floatParams == null) {
            floatParams = new FloatParams();
            floatParams.x = 0;
            floatParams.y = 0;
            floatParams.w = getResources().getDisplayMetrics().widthPixels * 3 / 4;
            floatParams.h = floatParams.w * 9 / 16;
            floatParams.round = 30;
            floatParams.fade = 0.8f;
            floatParams.canMove = true;
            floatParams.canCross = false;
        }
        floatParams.systemFloat = isSystemFloat;
        if (videoView.isWindowFloatMode()) {
            videoView.quitWindowFloat();
        } else {
            if (!videoView.enterWindowFloat(floatParams)) {
                Toast.makeText(this, "没有浮窗权限", Toast.LENGTH_LONG).show();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, 0);
                }
            }
        }
        if (videoView.isSystemFloatMode()) {
            onBackPressed();
        }
    }

    //视频列表
    public void videoList(View v) {
        startActivity(new Intent(this, ListVideoActivity.class));
    }

    //现场直播
    public void liveBroadcast(View v) {
        videoView.setSharpnessUrlList(Arrays.asList(m3u8, m3u8, m3u8));
        videoView.startVideo(m3u8, "m3u8直播", IjkMedia.class);//初始化播放
    }

    //android:launchMode="singleTask" 根据自己需求设置
    //系统浮窗
    public void systemFloatWindow(View v) {
        if (videoView.getCurrentMode() == IVideoPlayer.MODE_WINDOW_FLOAT_ACT) {
            return;
        }
        enterFloat(true);
    }

    //界面内浮窗
    public void localFloatWindow(View v) {
        if (videoView.getCurrentMode() == IVideoPlayer.MODE_WINDOW_FLOAT_SYS) {
            return;
        }
        enterFloat(false);
    }

    //销毁
    public void stopPlayer(View v) {
        videoView.release();
    }
}
