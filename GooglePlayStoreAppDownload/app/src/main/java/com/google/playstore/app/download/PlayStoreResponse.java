package com.google.playstore.app.download;

/**
 * Created by huzy on 2017/6/7.
 */

public interface PlayStoreResponse {

    void onIOError();

    void onAuthException();

    void needPurchase();

    void notSupportDownload();

    void onAccountNotExists();

    void onAccountOrPwdError();

    void onGoogleAccountLogin();

    void onCredentialsTaskFinished();
}
