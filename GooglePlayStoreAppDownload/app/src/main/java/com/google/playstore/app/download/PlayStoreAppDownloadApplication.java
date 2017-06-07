package com.google.playstore.app.download;

import android.app.Application;

/**
 * Created by huzy on 2017/6/7.
 */

public class PlayStoreAppDownloadApplication extends Application {

    private static PlayStoreAppDownloadApplication instance;

    public static PlayStoreAppDownloadApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
