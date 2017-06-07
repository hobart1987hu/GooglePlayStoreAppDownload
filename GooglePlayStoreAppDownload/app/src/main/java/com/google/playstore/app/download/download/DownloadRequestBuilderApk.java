package com.google.playstore.app.download.download;

import com.github.yeriomin.playstoreapi.AndroidAppDeliveryData;
import com.google.playstore.app.download.model.App;

import java.io.File;

public class DownloadRequestBuilderApk extends DownloadRequestBuilder {

    public DownloadRequestBuilderApk(App app, AndroidAppDeliveryData deliveryData) {
        super(app, deliveryData);
    }

    @Override
    protected String getDownloadUrl() {
        return deliveryData.getDownloadUrl();
    }

    @Override
    protected File getDestinationFile() {
        return Downloader.getApkPath(app.getPackageName(), app.getVersionCode());
    }

    @Override
    protected String getNotificationTitle() {
        return app.getDisplayName();
    }
}
