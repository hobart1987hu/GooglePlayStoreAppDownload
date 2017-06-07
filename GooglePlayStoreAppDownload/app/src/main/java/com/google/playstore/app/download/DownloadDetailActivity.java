package com.google.playstore.app.download;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;


import com.google.playstore.app.download.download.Downloader;
import com.google.playstore.app.download.model.App;

import java.io.File;

/**
 * Created by huzy on 2017/6/7.
 */

public class DownloadDetailActivity extends Activity {

    static public final int PERMISSIONS_REQUEST_CODE = 828;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE
                && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
            download(mApp);
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    DownloadDetailActivity.PERMISSIONS_REQUEST_CODE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_papa_download_detail);
        Intent intent = getIntent();
        final String packageName = intent.getStringExtra("packageName");
        if (TextUtils.isEmpty(packageName)) {
            finish();
            return;
        }

        registerReceivers();

        DetailsTask task = getDetailsTask(packageName);
//        task.setTaskClone(getDetailsTask(packageName));
        task.execute();
    }

    private DetailsDownloadReceiver downloadReceiver;

    private final DownloadListener mDownloadListener = new DownloadListener() {
        @Override
        public void onStartDownload() {
            printLog("开始下载");
        }

        @Override
        public void onDownloading() {
            printLog("正在下载中...");
        }

        @Override
        public void onDownloadFinished() {
            printLog("下载完成 path:" + downloadPath);
            finish();
        }
    };

    public void registerReceivers() {
        if (null == downloadReceiver) {
            downloadReceiver = new DetailsDownloadReceiver(this, mDownloadListener);
        }
    }

    public void unregisterReceivers() {
        if (null != downloadReceiver) {
            unregisterReceiver(downloadReceiver);
            downloadReceiver = null;
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceivers();
        super.onDestroy();
    }

    private App mApp;

    private DetailsTask getDetailsTask(String packageName) {
        DetailsTask task = new DetailsTask() {
            @Override
            protected void onPostExecute(Throwable e) {
                super.onPostExecute(e);
                if (this.app != null) {
                    mApp = app;
                    printLog("DetailsTask app info :" + app.toString());
                    if (checkPermission()) {
                        download(this.app);
                    } else {
                        requestPermission();
                    }
                } else {
                    printLog("DetailsTask exception:" + e.getMessage());
                }
            }
        };
        task.setPaPaFetchAppCallback(mCallback);
        task.setPackageName(packageName);
        task.setContext(this);
        return task;
    }

    private void download(App app) {
        printLog("start to download app...");
        if (prepareDownloadsDir(app)) {
            printLog(" download app...");
            mDownloadListener.onStartDownload();
            getPurchaseTask(app).execute();
        }
    }

    private String downloadPath;

    private boolean prepareDownloadsDir(App app) {
        File path = Downloader.getApkPath(app.getPackageName(), app.getVersionCode());
        File dir = path.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.downloadPath = path.getAbsolutePath();
        return dir.exists() && dir.isDirectory() && dir.canWrite();
    }

    private PurchaseTask getPurchaseTask(App app) {
        PurchaseTask purchaseTask = new PurchaseTask() {
            @Override
            protected void onPostExecute(Throwable e) {
                super.onPostExecute(e);
                if (null == e) {
                    //下载中
                    mDownloadListener.onDownloading();
                    printLog("----downloading----");
                } else {
                    printLog("PurchaseTask exception:" + e.getMessage());
                }
            }
        };
        purchaseTask.setPaPaFetchAppCallback(mCallback);
        purchaseTask.setApp(app);
        purchaseTask.setContext(getApplicationContext());
        return purchaseTask;
    }

    private final PlayStoreResponse mCallback = new PlayStoreResponse() {
        @Override
        public void onIOError() {
            //异常处理，下载失败
            ToastUtils.showLongTaost("下载失败");
            finish();
        }

        @Override
        public void onAuthException() {
            //需要重新授权
            ToastUtils.showLongTaost("需要重新授权");
            finish();
        }

        @Override
        public void needPurchase() {
            //需要购买，通知服务器，不可以去下载
            ToastUtils.showLongTaost("此APP需要购买，无法下载");
            finish();
        }

        @Override
        public void notSupportDownload() {
            //通支服务器，不可以去下载
            ToastUtils.showLongTaost("不支持此APP下载");
            finish();
        }

        @Override
        public void onGoogleAccountLogin() {

        }

        @Override
        public void onCredentialsTaskFinished() {

        }

        @Override
        public void onAccountNotExists() {

        }

        @Override
        public void onAccountOrPwdError() {

        }
    };

    void printLog(String info) {
        Log.d("PaPaDownloadService", info);
    }
}
