package com.google.playstore.app.download;

import com.github.yeriomin.playstoreapi.AuthException;
import com.google.playstore.app.download.download.DownloadState;
import com.google.playstore.app.download.download.Downloader;
import com.google.playstore.app.download.model.App;

public class PurchaseTask extends GoogleApiAsyncTask {

    protected App app;

    private DownloadState.TriggeredBy triggeredBy = DownloadState.TriggeredBy.DOWNLOAD_BUTTON;

    protected PlayStoreResponse mCallback;

    public void setPaPaFetchAppCallback(PlayStoreResponse callback) {
        mCallback = callback;
    }

    public void setApp(App app) {
        this.app = app;
    }

    @Override
    protected Throwable doInBackground(String... params) {
        PlayStoreApiWrapper wrapper = new PlayStoreApiWrapper(context);
        try {
            DownloadState.get(app.getPackageName()).setTriggeredBy(triggeredBy);
            new Downloader(context).download(app, wrapper.purchaseOrDeliver(app));
        } catch (Throwable e) {
            return e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Throwable e) {
        super.onPostExecute(e);
        if (e instanceof NotPurchasedException) {
            if (null != mCallback)
                mCallback.needPurchase();
        }
    }

    @Override
    protected void processAuthException(AuthException e) {
        if (e.getCode() == 403) {
            if (null != mCallback)
                mCallback.notSupportDownload();
        } else {
            super.processAuthException(e);
        }
    }
}
