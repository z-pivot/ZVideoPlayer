/*
 * *********************************************************
 *   author   fjm
 *   company  telchina
 *   email    fanjiaming@telchina.net
 *   date     19-3-22 下午5:03
 * ********************************************************
 */

package com.pivot.zvideoplayer;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pivot.libvideocore.IVideoPlayer;
import com.pivot.libvideocore.PlayListener;
import com.pivot.libvideocore.THVideoView;
import com.pivot.libvideocore.io.FileUtil;
import com.pivot.libvideocore.media.IjkMedia;
import com.pivot.zvideoplayer.listvideo.entity.ListVideoEntity;
import com.pivot.zvideoplayer.listvideo.util.CallBack;
import com.pivot.zvideoplayer.listvideo.util.ListCalculator;
import com.pivot.zvideoplayer.listvideo.util.RecyclerViewGetter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 视频列表activity
 * @author fjm
 */
public class ListVideoActivity extends AppCompatActivity implements CallBack {
    
    private static String mp4Demo = "http://videos.kpie.com.cn/videos/20170526/037DCE54-EECE-4520-AA92-E4002B1F29B0.mp4";

    RecyclerView recyclerView;
    List<ListVideoEntity> data = new ArrayList<>();
    ListCalculator calculator;
    THVideoView commentVideoView;//当前播放视图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recy_video);
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        for (int i = 0; i < 10; i++) {//模拟视频列表数据
            ListVideoEntity videoEntity = new ListVideoEntity("标题" + i, mp4Demo, R.mipmap.cover);
            videoEntity.urlList = Arrays.asList(mp4Demo, mp4Demo, mp4Demo);
            videoEntity.discussCount = "54";
            videoEntity.playCount = "176次播放";
            videoEntity.danMuJson = FileUtil.readAssets("danmu.json", getBaseContext());
            videoEntity.shareTitle = "视频" + i;
            videoEntity.shareDesc = "视频描述" + i;
            videoEntity.shareImg = "http://blog.csdn.net/qq_31390699";
            videoEntity.shareImg = "http://img.zcool.cn/community/0183b855420c990000019ae98b9ce8.jpg@900w_1l_2o_100sh.jpg";
            videoEntity.discussClickListener = v -> {
                //TODO 点击评论数量后执行的操作
            };
            data.add(videoEntity);
        }
        recyclerView.setAdapter(new Adapter(data));
        calculator = new ListCalculator(new RecyclerViewGetter((LinearLayoutManager) recyclerView.getLayoutManager(), recyclerView), this);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int newState = 0;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                this.newState = newState;
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    return;
                }
                calculator.onScrolled(300);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                calculator.onScrolling(newState);
            }
        });
    }

    @Override
    public void activeOnScrolled(View newActiveView, int position) {
        commentVideoView = newActiveView.findViewById(R.id.th_video_view);
        if (commentVideoView != null) {
            commentVideoView.play();
        }
        Log.d("activeOnScrolled", "" + position);
    }

    @Override
    public void activeOnScrolling(View newActiveView, int position) {
        Log.d("activeOnScrolled", "" + position);
        ObjectAnimator animator = ObjectAnimator.ofFloat(newActiveView, "alpha", 0.3f, 1);
        animator.setDuration(300);
        animator.start();
    }

    @Override
    public void deactivate(View currentView, int position) {
        final THVideoView demoQSVideoView = currentView.findViewById(R.id.th_video_view);
        if (demoQSVideoView != null) {
            demoQSVideoView.pause();
        }
        Log.d("deactivate", "" + position);
        ObjectAnimator animator = ObjectAnimator.ofFloat(currentView, "alpha", 1, 0.3f);
        animator.setDuration(300);
        animator.start();
    }

    @Override
    public void onBackPressed() {
        if (commentVideoView != null && commentVideoView.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (commentVideoView != null) {
            commentVideoView.release();
        }
    }

    private class Adapter extends RecyclerView.Adapter<Holder> {
        List<ListVideoEntity> data;

        Adapter(List<ListVideoEntity> data) {
            this.data = data;
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(View.inflate(ListVideoActivity.this, R.layout.item_video, null));
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            holder.bindData(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private class Holder extends RecyclerView.ViewHolder {
        THVideoView videoItemView;
        TextView videoTitle;
        TextView videoPlayCount;
        TextView videoDiscussCount;

        Holder(View itemView) {
            super(itemView);
            videoTitle = itemView.findViewById(R.id.video_list_title);
            videoPlayCount = itemView.findViewById(R.id.video_list_play_count);
            videoDiscussCount = itemView.findViewById(R.id.video_list_discuss_count);
            videoItemView = itemView.findViewById(R.id.th_video_view);
            videoItemView.setDecodeMedia(IjkMedia.class);
            videoItemView.setPlayListener(new PlayListener() {
                @Override
                public void onStatus(int status) {}

                @Override
                public void onMode(int mode) {}

                @Override
                public void onEvent(int what, Integer... extra) {
                    if (what == IVideoPlayer.EVENT_PREPARE_START) {
                        calculator.setCurrentActiveItem(getLayoutPosition());
                    }
                }
            });
            videoItemView.setShowWifiDialog(false);
        }

        private void bindData(ListVideoEntity entity) {
            videoTitle.setText(entity.title);
            videoDiscussCount.setText(entity.discussCount);
            videoDiscussCount.setOnClickListener(entity.discussClickListener);
            videoPlayCount.setText(entity.playCount);
            videoItemView.setTitle(entity.title);
            videoItemView.setSharpnessUrlList(entity.urlList);
            videoItemView.getCoverImageView().setImageResource(entity.bgImg);
            videoItemView.setDanMuJson(entity.danMuJson);
            videoItemView.setShareTitle(entity.shareTitle);
            videoItemView.setShareDesc(entity.shareDesc);
            videoItemView.setShareImg(entity.shareImg);
            videoItemView.setShareUrl(entity.shareUrl);
            videoItemView.setUp(entity.fUrl, entity.title);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, (getResources().getDisplayMetrics().widthPixels * 3 / 4));
            videoItemView.setLayoutParams(layoutParams);
            itemView.setAlpha(0.3f);
        }
    }
}
