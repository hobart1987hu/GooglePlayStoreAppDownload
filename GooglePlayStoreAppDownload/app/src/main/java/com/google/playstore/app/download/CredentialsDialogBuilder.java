package com.google.playstore.app.download;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.github.yeriomin.playstoreapi.AuthException;
import com.github.yeriomin.playstoreapi.TokenDispenserException;

import java.io.IOException;

abstract public class CredentialsDialogBuilder {

    protected Context context;
    protected PlayStoreResponse mCallback;

    public void setPaPaFetchAppCallback(PlayStoreResponse callback) {
        mCallback = callback;
    }

    public CredentialsDialogBuilder(Context context) {
        this.context = context;
    }

    abstract protected class CheckCredentialsTask extends GoogleApiAsyncTask {

        static private final String APP_PASSWORDS_URL = "https://security.google.com/settings/security/apppasswords";

        @Override
        protected void onPostExecute(Throwable e) {
            if (null != this.progressDialog) {
                this.progressDialog.dismiss();
            }
            if (null != e) {
                handleException(e);
                if (e instanceof AuthException && null != ((AuthException) e).getTwoFactorUrl()) {
                    return;
                }
                if (null != mCallback) {
                    mCallback.onGoogleAccountLogin();
                }
            } else {
                new FirstLaunchChecker(context).setLoggedIn();
                if (null != mCallback)
                    mCallback.onCredentialsTaskFinished();
//                if (null != this.taskClone) {
//                    this.taskClone.execute();
//                } else {
//                    Log.i(getClass().getName(), "No task clone provided");
//                }
            }
        }

        private void handleException(Throwable e) {
            if (e instanceof CredentialsEmptyException) {
                if (null != mCallback)
                    mCallback.onIOError();
            } else if (e instanceof TokenDispenserException) {
                e.getCause().printStackTrace();
                if (null != mCallback)
                    mCallback.onAccountNotExists();
            } else if (e instanceof AuthException) {
                if (null != ((AuthException) e).getTwoFactorUrl()) {
                    getTwoFactorAuthDialog().show();
                } else {
                    if (null != mCallback)
                        mCallback.onAccountOrPwdError();
                }
            } else if (e instanceof IOException) {
                if (null != mCallback)
                    mCallback.onIOError();
            } else {
                if (null != mCallback)
                    mCallback.onIOError();
                e.printStackTrace();
            }
        }

        private AlertDialog getTwoFactorAuthDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            return builder
                    .setMessage(R.string.dialog_message_two_factor)
                    .setTitle(R.string.dialog_title_two_factor)
                    .setPositiveButton(
                            R.string.dialog_two_factor_create_password,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse(APP_PASSWORDS_URL));
                                    context.startActivity(i);
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                }
                            }
                    )
                    .setNegativeButton(
                            R.string.dialog_two_factor_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                }
                            }
                    )
                    .create();
        }
    }
}
