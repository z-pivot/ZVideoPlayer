package com.pivot.libvideocore.share;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * Created by KID on 2017/12/29.
 * 一切AppID和Key请在ShareSDK.xml中配置
 * ShareSDK分享工具类
 */
public class ShareUtils {
    //QQ好友分享
    public static void shareQQ(String title, String shareUrl, String text, String imageUrl, PlatformActionListener listener) {
        Platform qq = ShareSDK.getPlatform(QQ.NAME);
        QQ.ShareParams sp = new QQ.ShareParams();
        sp.setTitle(title);
        sp.setTitleUrl(shareUrl); // 标题的超链接
        sp.setText(text);
        sp.setImageUrl(imageUrl);
        qq.setPlatformActionListener(listener);
        qq.share(sp);
    }
    
    //QQ空间
    public static  void shareQQZone(String title, String shareUrl, String text, String imageUrl, PlatformActionListener listener) {
        Platform qqZone =ShareSDK.getPlatform(QZone.NAME);
        QZone.ShareParams sp = new QZone.ShareParams();
        sp.setShareType(Platform.SHARE_IMAGE);
        sp.setTitle(title);
        sp.setTitleUrl(shareUrl); // 标题的超链接
        sp.setSiteUrl(shareUrl);
        sp.setSite("QQ空间标题");
        sp.setText(text);
        sp.setImageUrl(imageUrl);
        qqZone.setPlatformActionListener(listener);
        qqZone.share(sp);
    }
    
    //新浪微博
    public static void shareWeiBo(String title, String shareUrl, String text, String imageUrl, PlatformActionListener listener) {
        Platform sina =ShareSDK.getPlatform(SinaWeibo.NAME);
        SinaWeibo.ShareParams sp = new SinaWeibo.ShareParams();
        sp.setTitle(title);
        sp.setTitleUrl(shareUrl); // 标题的超链接
        sp.setSiteUrl(shareUrl);
        sp.setText(text);
        sp.setImageUrl(imageUrl);
        sina.setPlatformActionListener(listener);
        sina.share(sp);
    }
    
    //微信
    public static void shareWeChat(String title, String shareUrl, String text, String imageUrl, PlatformActionListener listener) {
        Platform weixin =ShareSDK.getPlatform(Wechat.NAME);
        Wechat.ShareParams sp = new Wechat.ShareParams();
        sp.setShareType(Platform.SHARE_WEBPAGE);
        sp.setTitle(title);
        sp.setImageUrl(imageUrl);
        sp.setUrl(shareUrl);
        sp.setText(text);
//        sp.setSite("QQ空间标题");
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        weixin.setPlatformActionListener(listener);
        weixin.share(sp);
    }
    
    //微信朋友圈
    public static void sharePyq(String title, String shareUrl, String text, String imageUrl, PlatformActionListener listener) {
        Platform weixin =ShareSDK.getPlatform(WechatMoments.NAME);
        WechatMoments.ShareParams sp = new WechatMoments.ShareParams();
        sp.setShareType(Platform.SHARE_WEBPAGE);
        sp.setTitle(title);
        sp.setImageUrl(imageUrl);
        sp.setUrl(shareUrl);
        sp.setText(text);
//        sp.setSite("QQ空间标题");
        weixin.setPlatformActionListener(listener);
        weixin.share(sp);
    }

}
