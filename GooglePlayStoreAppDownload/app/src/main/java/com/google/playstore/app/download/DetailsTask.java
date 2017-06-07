package com.google.playstore.app.download;

import android.content.pm.PackageManager;

import com.github.yeriomin.playstoreapi.GooglePlayException;
import com.google.playstore.app.download.model.App;

import java.io.IOException;

public class DetailsTask extends GoogleApiAsyncTask {

    protected App app;
    protected String packageName;

    public DetailsTask setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    @Override
    protected void processIOException(IOException e) {
        if (null != e && e instanceof GooglePlayException && ((GooglePlayException) e).getCode() == 404) {
            if (null != mCallback) {
                mCallback.onIOError();
            }
        }
    }

    @Override
    protected Throwable doInBackground(String... params) {
        PlayStoreApiWrapper wrapper = new PlayStoreApiWrapper(this.context);
        try {
            app = wrapper.getDetails(packageName);
        } catch (Throwable e) {
            return e;
        }
        try {
            app.getPackageInfo().applicationInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
            app.setInstalled(true);
        } catch (PackageManager.NameNotFoundException e) {
            // App is not installed
        }
        return null;
    }
}
