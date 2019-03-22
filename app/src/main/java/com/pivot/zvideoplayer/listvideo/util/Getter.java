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

public interface Getter {
    View getChildAt(int position);

    int indexOfChild(View view);

    int getChildCount();

    int getLastVisiblePosition();

    int getFirstVisiblePosition();
}
