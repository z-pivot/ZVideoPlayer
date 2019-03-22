/*
 * *********************************************************
 *   author   fjm
 *   company  telchina
 *   email    fanjiaming@telchina.net
 *   date     19-3-22 上午10:48
 * ********************************************************
 */

/*
 * *********************************************************
 *   author   fjm
 *   company  telchina
 *   email    fanjiaming@telchina.net
 *   date     19-3-22 上午10:18
 * ********************************************************
 */

package com.pivot.zvideoplayer.listvideo.entity;

import android.view.View;

import java.util.List;

/**
 * 视频列表信息实体类
 */

public class ListVideoEntity {
    public String title;//标题
    public String fUrl;//初始化播放url
    public int bgImg;//封面背景图
    public List<String> urlList;//不同清晰度url集合
    public String discussCount;//讨论区条数
    public String playCount;//播放次数
    public String danMuJson;//弹幕json数据
    public String shareTitle = "百度一下";//分享的标题
    public String shareUrl = "http://blog.csdn.net/qq_31390699";//分享的链接
    public String shareImg = "http://img.zcool.cn/community/0183b855420c990000019ae98b9ce8.jpg@900w_1l_2o_100sh.jpg";//分享的封面图片
    public String shareDesc = "不懂你就百度啊";//分享的描述
    public View.OnClickListener discussClickListener;

    public ListVideoEntity(String title, String fUrl, int bgImg) {
        this.title = title;
        this.fUrl = fUrl;
        this.bgImg = bgImg;
    }
}
