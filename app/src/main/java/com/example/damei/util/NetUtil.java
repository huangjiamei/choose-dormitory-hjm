package com.example.damei.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by damei on 17/12/30.
 * util包用于存放公共的工具类
 * NetUtil类用于检查网络状态
 */

public class NetUtil {
    public static final int NETWORK_NONE = 0;
    public static final int NETWORK_WIFI = 1;
    public static final int NETWORK_MOBILE = 2;

    public static int getNetworkState(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null)
            return NETWORK_NONE;
        else {
            int nType = networkInfo.getType();
            if (nType == ConnectivityManager.TYPE_MOBILE)
                return NETWORK_MOBILE;
            else
                return NETWORK_WIFI;
        }
    }
}
