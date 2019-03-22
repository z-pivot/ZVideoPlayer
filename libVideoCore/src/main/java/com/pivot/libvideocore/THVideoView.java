/*
 * *********************************************************
 *   author   fjm
 *   company  telchina
 *   email    fanjiaming@telchina.net
 *   date     19-3-21 下午4:04
 * ********************************************************
 */

package com.pivot.libvideocore;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pivot.libvideocore.danmuku.DanmakuConfig;
import com.pivot.libvideocore.danmuku.DanmakuControl;
import com.pivot.libvideocore.danmuku.QSDanmakuParser;
import com.pivot.libvideocore.io.FileUtil;
import com.pivot.libvideocore.media.BaseMedia;
import com.pivot.libvideocore.media.IjkMedia;
import com.pivot.libvideocore.share.ShareDialog;
import com.pivot.libvideocore.share.ShareUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import master.flame.danmaku.danmaku.model.BaseDanmaku;

/**
 * 播放器View
 * @author fjm
 */
public class THVideoView extends QSVideoViewHelp implements View.OnClickListener{
    private String title;//标题文本
    private String danMuJson;//弹幕json数据
    private String shareTitle = "百度一下";//分享的标题
    private String shareUrl = "http://blog.csdn.net/qq_31390699";//分享的链接
    private String shareImg = "http://img.zcool.cn/community/0183b855420c990000019ae98b9ce8.jpg@900w_1l_2o_100sh.jpg";//分享的封面图片
    private String shareDesc = "不懂你就百度啊";//分享的描述
    private boolean isShowWifiDialog = true;//是否显示移动网络提示框
    private List<String> sharpnessUrlList = new ArrayList<>();//不同清晰度视频的url集合 默认有标清、高清、超清
    private Class<? extends BaseMedia> decodeMediaStyle = IjkMedia.class;//视频解码方式
    
    //控件
    private ImageView coverImageView;//封面
    private ImageView btnDanMuOpen;//弹幕开按钮
    private ImageView btnDanMuClose;//弹幕关按钮
    private ImageView btnDanMuWrite;//弹幕写按钮
    private TextView titleTextView;//标题
    private TextView btnSharpness;//清晰度更换按钮
    private EditText danMuText;//弹幕文本编辑框
    private ViewGroup bottomContainer;//底部栏
    private ViewGroup topContainer;//顶部栏
    private ViewGroup loadingContainer;//初始化
    private ViewGroup errorContainer;//出错了显示的 重试
    private ViewGroup bufferingContainer;//缓冲
    private List<View> changeViews;//根据状态隐藏显示的view集合
    private DanmakuControl danmakuControl;//弹幕控制器
    private ViewGroup settingContainer;//设置详情栏
    private AlertDialog dialog;//清晰度切换

    //分享回调
    private PlatformActionListener platformActionListener = new PlatformActionListener() {
        @Override
        public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
            Log.e("kid", "分享成功");
        }
        @Override
        public void onError(Platform platform, int i, Throwable throwable) {
            Log.e("kid", "分享失败");
        }

        @Override
        public void onCancel(Platform platform, int i) {
            Log.e("kid", "分享取消");
        }
    };
    
    public THVideoView(Context context) {
        this(context, null);
    }

    public THVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public THVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        setUIWithStateAndMode(STATE_NORMAL, currentMode);
    }

    //提交xml 需要父类替你完成某个控件的逻辑 控件的id按照values/ids.xml去定义 如播放按钮id定义为(android:id="@id/help_start") @id 不是 @+id
    @Override
    protected int getLayoutId() {
        return R.layout.video_view;
    }

    public void initView() {
        topContainer = findViewById(R.id.layout_top);
        bottomContainer = findViewById(R.id.layout_bottom);
        bufferingContainer = findViewById(R.id.buffering_container);
        loadingContainer = findViewById(R.id.loading_container);
        errorContainer = findViewById(R.id.error_container);
        coverImageView = findViewById(R.id.cover);
        titleTextView = findViewById(R.id.title);
        btnDanMuOpen = findViewById(R.id.danmu_open);
        btnDanMuClose = findViewById(R.id.danmu_close);
        btnDanMuWrite = findViewById(R.id.danmu_write);
        danMuText = findViewById(R.id.danmu_text);
        btnSharpness = findViewById(R.id.sharpness);
        settingContainer = findViewById(R.id.layout_right);
        settingContainer.setVisibility(GONE);
        
        findViewById(R.id.tv_speed_normal).setOnClickListener(this);
        findViewById(R.id.tv_speed_slow).setOnClickListener(this);
        findViewById(R.id.tv_speed_fast).setOnClickListener(this);
        findViewById(R.id.tv_speed_big_fast).setOnClickListener(this);
        findViewById(R.id.tv_prop_old).setOnClickListener(this);
        findViewById(R.id.tv_prop_16_9).setOnClickListener(this);
        findViewById(R.id.tv_prop_fit).setOnClickListener(this);
        findViewById(R.id.tv_prop_4_3).setOnClickListener(this);
        
        danmakuControl = DanmakuControl.bind(//初始化弹幕控制器
                this, new QSDanmakuParser(danMuJson == null ? FileUtil.readAssets("danmu.json", getContext()): danMuJson), DanmakuConfig.getDefaultContext()
        );
        danmakuControl.hide();

        findViewById(R.id.setting).setOnClickListener(v -> {//设置按钮点击监听
            if (settingContainer.getVisibility() == GONE) {
                settingContainer.setVisibility(VISIBLE);
            } else {
                settingContainer.setVisibility(GONE);
            }
        });

        findViewById(R.id.share).setOnClickListener(v -> {//分享按钮点击监听
            ShareDialog shareDialog = new ShareDialog(getContext());
            shareDialog.builder().show();
            shareDialog.setShareClickListener(new ShareDialog.ShareClickListener() {
                @Override
                public void shareWeChat() {
                    ShareUtils.shareWeChat(shareTitle, shareUrl, shareDesc, shareImg, platformActionListener);
                }
                @Override
                public void sharePyq() {
                    ShareUtils.sharePyq(shareTitle, shareUrl, shareDesc, shareImg, platformActionListener);
                }
                @Override
                public void shareQQ() {
                    ShareUtils.shareQQ(shareTitle, shareUrl, shareDesc, shareImg, platformActionListener);
                }
                @Override
                public void shareQQZone() {
                    ShareUtils.shareQQZone(shareTitle, shareUrl, shareDesc, shareImg, platformActionListener);
                }
                @Override
                public void shareWeiBo() {
                    ShareUtils.shareWeiBo(shareTitle, shareUrl, shareDesc, shareImg, platformActionListener);
                }
            });
        });
        
        danMuText.setImeOptions(EditorInfo.IME_ACTION_DONE);//弹幕编辑框被点击时的响应类型
        btnDanMuOpen.setOnClickListener(v -> {//关闭弹幕
            btnDanMuClose.setVisibility(VISIBLE);
            btnDanMuOpen.setVisibility(GONE);
            btnDanMuWrite.setVisibility(GONE);
            danmakuControl.hide();
        });
        btnDanMuClose.setOnClickListener(v -> {//打开弹幕
            btnDanMuOpen.setVisibility(VISIBLE);
            btnDanMuClose.setVisibility(GONE);
            btnDanMuWrite.setVisibility(VISIBLE);
            danmakuControl.show();
        });
        btnDanMuWrite.setOnClickListener(v -> {//写弹幕
            danMuText.setVisibility(VISIBLE);
            InputMethodManager imm = (InputMethodManager) danMuText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);        
            imm.showSoftInput(danMuText, 0);
        });
        danMuText.setOnEditorActionListener((v, actionId, event) -> {//获取写入的弹幕文本
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                danMuText.setVisibility(GONE);
                addDanMu(v.getText().toString(), false);
            }
            return false;
        });
        btnSharpness.setOnClickListener(v -> {//切换清晰度
            final String[] items = {"标 清", "高 清", "超 清"};
            AlertDialog.Builder listDialog = new AlertDialog.Builder(getContext());
            listDialog.setAdapter(new ArrayAdapter<>(getContext(),R.layout.dialog_item, items), (dialog, which) -> {
                btnSharpness.setText(items[which]);
                titleTextView.setText(title);
                setUp(sharpnessUrlList.get(which), title);
                setDecodeMedia(IjkMedia.class);
                play();
            });
            dialog = listDialog.create();
            dialog.show();
            Window dialogWindow = dialog.getWindow();
            dialogWindow.setBackgroundDrawableResource(R.drawable.radius_3_no_stroke_black88_bg_shape);
            dialogWindow.setGravity(Gravity.END | Gravity.BOTTOM);
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.dimAmount = 0;//屏幕上其他区域阴影程度
            lp.x = 55;//显示的x坐标
            lp.y = 80;//显示的y坐标
            lp.width = 180;//dialog的宽,高度自适应
            dialogWindow.setAttributes(lp);
        });

        setPlayListener(new PlayListener() {//播放事件监听
            @Override
            public void onStatus(int status) {//播放状态
                if (status == IVideoPlayer.STATE_AUTO_COMPLETE) {
                    quitWindowFullscreen();//播放完成退出全屏
                }
            }

            @Override//全屏/普通/浮窗
            public void onMode(int mode) {}

            @Override
            public void onEvent(int what, Integer... extra) {
                if (what == THVideoView.EVENT_CONTROL_VIEW & Build.VERSION.SDK_INT >= 19 & !isWindowFloatMode()) {
                    if (extra[0] == 0) {//状态栏隐藏/显示
                        Util.CLEAR_FULL(getContext());
                    } else {
                        Util.SET_FULL(getContext());
                    }
                }
                if (what == THVideoView.EVENT_CLICK_VIEW && extra[0] == R.id.help_float_close) {//系统浮窗点击退出activity
                    if (isSystemFloatMode()) {
                        Util.SET_FULL(getContext());
                    }
                }
            }
        });
        
        //会根据播放器状态而改变的view加进去
        changeViews = new ArrayList<>();
        changeViews.add(topContainer);
        changeViews.add(bottomContainer);
        changeViews.add(loadingContainer);
        changeViews.add(errorContainer);
        changeViews.add(coverImageView);
        changeViews.add(startButton);
        changeViews.add(progressBar);
    }

    @Override
    public void setUp(String url, Object... objects) {
        super.setUp(url, objects);
        titleTextView.setText(title);
    }

    //根据状态设置ui显示/隐藏 用方法內的参数,不要用currentStatus,currentMode
    @Override
    protected void changeUiWithStateAndMode(int status, int mode) {
        switch (status) {
            case STATE_NORMAL:
                showChangeViews(coverImageView, startButton);//普通状态显示封面和播放按钮
                break;
            case STATE_PREPARING:
                showChangeViews(loadingContainer);//初始化状态显示loading
                break;
            case STATE_PLAYING:
            case STATE_PAUSE:
            case STATE_AUTO_COMPLETE://显示 播放按钮  [底部] [顶部]
                showChangeViews(startButton,
                        mode >= MODE_WINDOW_FLOAT_SYS ? null : bottomContainer,
                        mode == MODE_WINDOW_FULLSCREEN ? topContainer : null);
                settingContainer.setVisibility(GONE);
                break;
            case STATE_ERROR://出错显示errorContainer
                showChangeViews(errorContainer);
                break;
            default:
                break;
        }
        startButton.setImageResource(status == STATE_PLAYING ? R.drawable.jc_click_pause_selector : R.drawable.jc_click_play_selector);
        fullscreenButton.setImageResource(mode == MODE_WINDOW_FULLSCREEN ? R.drawable.jc_shrink : R.drawable.jc_enlarge);
        floatCloseView.setVisibility(mode >= MODE_WINDOW_FLOAT_SYS ? View.VISIBLE : View.INVISIBLE);
        floatBackView.setVisibility(mode >= MODE_WINDOW_FLOAT_SYS ? View.VISIBLE : View.INVISIBLE);
    }

    //播放时隐藏的view
    @Override
    protected void dismissControlView(int status, int mode) {
        bottomContainer.setVisibility(View.INVISIBLE);
        topContainer.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        if (status != STATE_AUTO_COMPLETE) {
            startButton.setVisibility(View.INVISIBLE);
        }
        if (mode >= MODE_WINDOW_FLOAT_SYS) {
            floatCloseView.setVisibility(View.INVISIBLE);
        }
        if (mode >= MODE_WINDOW_FLOAT_SYS) {
            floatBackView.setVisibility(View.INVISIBLE);
        }
    }
    
    //缓冲
    @Override
    protected void onBuffering(boolean isBuffering) {
        bufferingContainer.setVisibility(isBuffering ? VISIBLE : INVISIBLE);
    }

    //设置框点击响应
    @Override
    public void onClick(View v) {
        settingContainer.setVisibility(GONE);
        int i = v.getId();
        if (i == R.id.tv_speed_normal) {//正常速度
            setSpeed(1f);
        } else if (i == R.id.tv_speed_slow) {//慢速0.5倍
            setSpeed(0.5f);
        } else if (i == R.id.tv_speed_fast) {//快速1.5倍
            setSpeed(1.5f);
        } else if (i == R.id.tv_speed_big_fast) {//极快2.0倍
            setSpeed(2f);
        } else if (i == R.id.tv_prop_old) {//原尺寸
            setAspectRatio(2);
        } else if (i == R.id.tv_prop_fit) {//自适应
            setAspectRatio(0);
        } else if (i == R.id.tv_prop_16_9) {//16:9
            setAspectRatio(4);
        } else if (i == R.id.tv_prop_4_3) {//4:3
            setAspectRatio(5);
        }
    }
    
    //移动网络提示框
    @Override
    protected boolean showWifiDialog() {
        if (!isShowWifiDialog) {
            return false;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getString(R.string.tips_not_wifi));
        builder.setPositiveButton(getResources().getString(R.string.tips_not_wifi_confirm), (dialog, which) -> {
            dialog.dismiss();
            prepareMediaPlayer();
        });
        builder.setNegativeButton(getResources().getString(R.string.tips_not_wifi_cancel), (dialog, which) -> dialog.dismiss());
        builder.create().show();
        return true;
    }

    /**
     调节视频进度框
     */
    private PopupWindow mProgressDialog;//进度弹出框
    private ProgressBar mDialogProgressBar;//中心进度条
    private TextView tvCurrent;//当前进度时间点
    private TextView tvDuration;//视频总时间长度
    private TextView tvDelta;//快进或后退的秒数
    private ImageView mDialogIcon;//代表进或退的图标
    
    @Override
    protected boolean showProgressDialog(int delta, int position, int duration) {
        if (mProgressDialog == null) {
            View localView = LayoutInflater.from(getContext()).inflate(R.layout.jc_dialog_progress, null);
            mDialogProgressBar = localView.findViewById(R.id.duration_progressbar);
            tvCurrent = localView.findViewById(R.id.tv_current);
            tvDuration = localView.findViewById(R.id.tv_duration);
            tvDelta = localView.findViewById(R.id.tv_delta);
            mDialogIcon = localView.findViewById(R.id.duration_image_tip);
            mProgressDialog = getPopupWindow(localView);

        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.showAtLocation(this, Gravity.CENTER, 0, 0);
        }
        tvDelta.setText((delta > 0 ? "+" : "") + delta / 1000 + "秒");
        tvCurrent.setText(Util.stringForTime(position + delta) + "/");
        tvDuration.setText(Util.stringForTime(duration));
        mDialogProgressBar.setProgress((position + delta) * 100 / duration);
        if (delta > 0) {
            mDialogIcon.setBackgroundResource(R.drawable.jc_forward_icon);
        } else {
            mDialogIcon.setBackgroundResource(R.drawable.jc_backward_icon);
        }
        return true;
    }

    @Override
    protected boolean dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        return true;
    }
    
    /**
     * 调节音量框
     */
    private PopupWindow mVolumeDialog;//音量调节弹出框
    private ProgressBar mDialogVolumeProgressBar;//音量进度条
    private TextView mDialogVolumeTextView;//音量文本 0-15
    private ImageView mDialogVolumeImageView;//音量图标

    @Override
    protected boolean showVolumeDialog(int nowVolume, int maxVolume) {
        if (mVolumeDialog == null) {
            View localView = LayoutInflater.from(getContext()).inflate(R.layout.jc_dialog_volume, null);
            mDialogVolumeImageView = localView.findViewById(R.id.volume_image_tip);
            mDialogVolumeTextView = localView.findViewById(R.id.tv_volume);
            mDialogVolumeProgressBar = localView.findViewById(R.id.volume_progressbar);
            mDialogVolumeProgressBar.setMax(maxVolume);
            mVolumeDialog = getPopupWindow(localView);
        }
        if (!mVolumeDialog.isShowing()) {
            mVolumeDialog.showAtLocation(this, Gravity.TOP, 0, Util.dp2px(getContext(), currentMode == MODE_WINDOW_NORMAL ? 25 : 50));
        }

        mDialogVolumeTextView.setText(nowVolume + "");
        mDialogVolumeProgressBar.setProgress(nowVolume);
        return true;
    }

    @Override
    protected boolean dismissVolumeDialog() {
        if (mVolumeDialog != null) {
            mVolumeDialog.dismiss();
        }
        return true;
    }

    /**
     * 调节亮度框
     */
    private PopupWindow mBrightnessDialog;//亮度调节弹出框
    private ProgressBar mDialogBrightnessProgressBar;//亮度进度条
    private TextView mDialogBrightnessTextView;//亮度文本 0-100
    
    @Override
    protected boolean showBrightnessDialog(int brightnessPercent, int max) {
        if (mBrightnessDialog == null) {
            View localView = LayoutInflater.from(getContext()).inflate(R.layout.jc_dialog_brightness, null);
            mDialogBrightnessTextView = localView.findViewById(R.id.tv_brightness);
            mDialogBrightnessProgressBar = localView.findViewById(R.id.brightness_progressbar);
            mDialogBrightnessProgressBar.setMax(max);
            mBrightnessDialog = getPopupWindow(localView);
        }
        if (!mBrightnessDialog.isShowing()) {
            mBrightnessDialog.showAtLocation(this, Gravity.TOP, 0, Util.dp2px(getContext(), currentMode == MODE_WINDOW_NORMAL ? 25 : 50));
        }

        mDialogBrightnessTextView.setText(brightnessPercent + "");
        mDialogBrightnessProgressBar.setProgress(brightnessPercent);
        return true;
    }

    @Override
    protected boolean dismissBrightnessDialog() {
        if (mBrightnessDialog != null) {
            mBrightnessDialog.dismiss();
        }
        return true;
    }
    
    @Override
    public void enterWindowFullscreen() {
        super.enterWindowFullscreen();
        if (Util.PaserUrl(url) != -1) {
            btnSharpness.setVisibility(VISIBLE);
        }
    }

    @Override
    public void quitWindowFullscreen() {
        super.quitWindowFullscreen();
        if (Util.PaserUrl(url) != -1) {
            btnSharpness.setVisibility(GONE);
            settingContainer.setVisibility(GONE);
        }
    }

    /**
     * 初始化播放视频
     */
    public void startVideo(String urlNow, String title, Class<? extends BaseMedia> media) {
        this.title = title;
        this.decodeMediaStyle = media;
        titleTextView.setText(title);
        setUp(urlNow, title == null ? "视频" : title);
        setDecodeMedia(media);
        openCache();
        play();
        if(Util.PaserUrl(urlNow) == -1) {
            btnDanMuOpen.setVisibility(GONE);
            btnDanMuClose.setVisibility(GONE);
            btnDanMuWrite.setVisibility(GONE);
            danMuText.setVisibility(GONE);
            btnSharpness.setVisibility(GONE);
        } else {
            btnDanMuClose.setVisibility(VISIBLE);
        }
    }
    
    /**
     * 根据播放器状态显示view
     */
    private void showChangeViews(View... views) {
        for (int i = 0; i < changeViews.size(); i++) {
            View v = changeViews.get(i);
            if (v != null) {
                v.setVisibility(INVISIBLE);
            }
        }
        for (View v : views) {
            if (v != null) {
                v.setVisibility(VISIBLE);
            }
        }
    }
    
    /**
     * 获取弹出框对象
     */
    private PopupWindow getPopupWindow(View popupView) {
        PopupWindow mPopupWindow = new PopupWindow(popupView, FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0));
        mPopupWindow.setAnimationStyle(R.style.jc_popup_toast_anim);
        return mPopupWindow;
    }

    /**
     * 发弹幕
     */
    private void addDanMu(String content, boolean islive) {
        BaseDanmaku danMu = DanmakuConfig.getDefaultContext().mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (danMu == null || danmakuControl == null) {
            return;
        }
        danMu.text = content;
        danMu.padding = 5;
        danMu.priority = 10;  // 弹幕优先级 0最低
        danMu.isLive = islive;
        danMu.setTime(this.getPosition() + 1200);
        danMu.textSize = 40;
        danMu.textColor = Color.RED;
        danMu.textShadowColor = Color.WHITE;
        danMu.borderColor = Color.GREEN;
        danmakuControl.add(danMu);
    }
    
    public ImageView getCoverImageView() {
        return coverImageView;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDanMuJson(String danMuJson) {
        this.danMuJson = danMuJson;
    }

    public void setShowWifiDialog(boolean showWifiDialog) {
        this.isShowWifiDialog = showWifiDialog;
    }

    public void setSharpnessUrlList(List<String> sharpnessUrlList) {
        this.sharpnessUrlList = sharpnessUrlList;
    }

    public void setShareTitle(String shareTitle) {
        this.shareTitle = shareTitle;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public void setShareImg(String shareImg) {
        this.shareImg = shareImg;
    }

    public void setShareDesc(String shareDesc) {
        this.shareDesc = shareDesc;
    }

    public void setPlatformActionListener(PlatformActionListener platformActionListener) {
        this.platformActionListener = platformActionListener;
    }
}
