package com.google.playstore.app.download;

/**
 * Created by huzy on 2017/6/7.
 */

public interface DownloadListener {

    void onStartDownload();

    void onDownloading();

    void onDownloadFinished();
    
}
