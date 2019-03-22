/*
 * *********************************************************
 *   author   fjm
 *   company  telchina
 *   email    fanjiaming@telchina.net
 *   date     19-3-21 下午4:04
 * ********************************************************
 */

package com.pivot.libvideocore.share;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.pivot.libvideocore.R;

/**
 * 自定义分享弹出框
 * @author fjm
 */
public class ShareDialog implements View.OnClickListener {
    private Context context;
    private AlertDialog alertDialog;
    private ShareClickListener shareClickListener;

    public ShareDialog(Context context) {
        this.context = context;
    }
    
    public ShareDialog builder() {
        alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.mycustom_dialog)).create();
        alertDialog.show();
        Window win = alertDialog.getWindow();
        win.setWindowAnimations(R.style.mystyle);
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        win.setAttributes(lp);
        win.setContentView(R.layout.dialog_share);

        RelativeLayout rlMenuCancel = win.findViewById(R.id.rl_menu_cancel);
        LinearLayout llShareWeChat = win.findViewById(R.id.ll_share_wechat);
        LinearLayout llSharePyq = win.findViewById(R.id.ll_share_pyq);
        LinearLayout llShareQq = win.findViewById(R.id.ll_share_qq);
        LinearLayout llShareQQZone = win.findViewById(R.id.ll_share_qzone);
        LinearLayout llShareWeiBo = win.findViewById(R.id.ll_share_weibo);

        rlMenuCancel.setOnClickListener(this);
        llShareWeChat.setOnClickListener(this);
        llSharePyq.setOnClickListener(this);
        llShareQq.setOnClickListener(this);
        llShareQQZone.setOnClickListener(this);
        llShareWeiBo.setOnClickListener(this);

        return this;
    }
    
    public void show(){
        alertDialog.show();
    }
    
    public void cancel(){
        alertDialog.cancel();
    }
    
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.rl_menu_cancel) {
            cancel();
        } else if (i == R.id.ll_share_wechat) {
            cancel();
            if (shareClickListener != null) {
                shareClickListener.shareWeChat();
            }
        } else if (i == R.id.ll_share_pyq) {
            cancel();
            if (shareClickListener != null) {
                shareClickListener.sharePyq();
            }
        } else if (i == R.id.ll_share_qq) {
            cancel();
            if (shareClickListener != null) {
                shareClickListener.shareQQ();
            }
        } else if (i == R.id.ll_share_qzone) {
            cancel();
            if (shareClickListener != null) {
                shareClickListener.shareQQZone();
            }
        } else if (i == R.id.ll_share_weibo) {
            cancel();
            if (shareClickListener != null) {
                shareClickListener.shareWeiBo();
            }
        }
    }
    
    public interface ShareClickListener{
        /**
         * 微信分享
         */
        void shareWeChat();

        /**
         * 微信朋友圈分享
         */
        void sharePyq();

        /**
         * QQ分享
         */
        void shareQQ();

        /**
         * QQ空间分享
         */
        void shareQQZone();

        /**
         * 微博分享
         */
        void shareWeiBo();
    }
    
    public void setShareClickListener(ShareClickListener shareClickListener){
        this.shareClickListener=shareClickListener;
    }
}
