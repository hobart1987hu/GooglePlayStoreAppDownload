package com.google.playstore.app.download;

import android.widget.Toast;

/**
 * Created by huzeyin on 2017/6/5.
 */

public class ToastUtils {


    public static void showLongTaost(String info) {
        Toast.makeText(PlayStoreAppDownloadApplication.getInstance().getApplicationContext(), info, Toast.LENGTH_LONG).show();
    }

    public static void showShortTaost(String info) {
        Toast.makeText(PlayStoreAppDownloadApplication.getInstance().getApplicationContext(), info, Toast.LENGTH_SHORT).show();
    }
}
