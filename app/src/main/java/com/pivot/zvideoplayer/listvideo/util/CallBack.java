/*
 * *********************************************************
 *   author   fjm
 *   company  telchina
 *   email    fanjiaming@telchina.net
 *   date     19-3-22 上午10:48
 * ********************************************************
 */

package com.pivot.zvideoplayer.listvideo.util;

import android.view.View;

public interface CallBack {

    //当前的item 滚动结束调用
    void activeOnScrolled(View activeView, int position);

    //当前的item 滚动中调用
    void activeOnScrolling(View activeView, int position);

    //销毁的item
    void deactivate(View currentView, int position);
}