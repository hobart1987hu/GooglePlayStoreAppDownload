package com.google.playstore.app.download;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.github.yeriomin.playstoreapi.AuthException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLHandshakeException;

abstract class GoogleApiAsyncTask extends AsyncTask<String, Void, Throwable> {

    protected ProgressDialog progressDialog;
    protected Context context;
    //    protected GoogleApiAsyncTask taskClone;
    protected PlayStoreResponse mCallback;

    public void prepareDialog(int messageId, int titleId) {
        this.progressDialog = Util.prepareProgressDialog(context, messageId, titleId);
    }

    public void setContext(Context context) {
        this.context = context;
    }

//    public void setTaskClone(GoogleApiAsyncTask taskClone) {
//        this.taskClone = taskClone;
//    }

    public void setPaPaFetchAppCallback(PlayStoreResponse callback) {
        mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        if (null != this.progressDialog) {
            this.progressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(Throwable result) {
        if (null != this.progressDialog && isContextUiCapable()) {
            this.progressDialog.dismiss();
        }

        Throwable e = result;
        if (result instanceof RuntimeException && null != result.getCause()) {
            e = result.getCause();
        }
        if (e != null) {
            processException(e);
        }
    }

    protected void processException(Throwable e) {
        Log.d(getClass().getName(), e.getClass().getName() + " caught during a google api request: " + e.getMessage());
        if (e instanceof AuthException) {
            processAuthException((AuthException) e);
        } else if (e instanceof IOException) {
            processIOException((IOException) e);
        } else {
            if (null != mCallback) {
                mCallback.onIOError();
            }
            Log.e(getClass().getName(), "Unknown exception " + e.getClass().getName() + " " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void processIOException(IOException e) {
        if (null != mCallback) {
            mCallback.onIOError();
        }
    }

    protected void processAuthException(AuthException e) {
        if (!isContextUiCapable()) {
            Log.e(getClass().getName(), "AuthException happened and the provided context is not ui capable");
            return;
        }
        new PlayStoreApiAuthenticator(context).logout();
        if (null != mCallback) {
            mCallback.onAuthException();
        }
    }

    protected boolean isContextUiCapable() {
        if (!(context instanceof Activity)) {
            return false;
        }
        Activity activity = (Activity) context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return !activity.isDestroyed();
        } else {
            return !activity.isFinishing();
        }
    }

    static public boolean noNetwork(Throwable e) {
        return e instanceof UnknownHostException
                || e instanceof SSLHandshakeException
                || e instanceof ConnectException
                || e instanceof SocketException
                || e instanceof SocketTimeoutException;
    }
}
