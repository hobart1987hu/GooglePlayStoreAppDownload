package com.google.playstore.app.download;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.google.playstore.app.download.download.DownloadManagerFactory;
import com.google.playstore.app.download.download.DownloadManagerInterface;
import com.google.playstore.app.download.download.DownloadState;


/**
 * Created by huzy on 2017/6/7.
 */

public class DetailsDownloadReceiver extends BroadcastReceiver {

    private DownloadListener mCallback;

    public DetailsDownloadReceiver(Activity activity, DownloadListener callback) {
        mCallback = callback;
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManagerInterface.ACTION_DOWNLOAD_COMPLETE);
        activity.registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (null == extras) {
            return;
        }
        long id = extras.getLong(DownloadManagerInterface.EXTRA_DOWNLOAD_ID);
        DownloadState state = DownloadState.get(id);
        if (null == state) {
            return;
        }
        if (null != mCallback)
            mCallback.onDownloadFinished();

        state.setFinished(id);
        if (DownloadManagerFactory.get(context).success(id)) {
            state.setSuccessful(id);
        }
        if (!state.isEverythingFinished()) {
            return;
        }
    }
}
