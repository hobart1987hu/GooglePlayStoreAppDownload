package com.google.playstore.app.download;

import android.content.Context;
import android.text.TextUtils;

import com.github.yeriomin.playstoreapi.AuthException;

public class UserProvidedAccountDialogBuilder extends CredentialsDialogBuilder {

    protected GoogleApiAsyncTask taskClone;

    public UserProvidedAccountDialogBuilder(Context context) {
        super(context);
    }

    public void setTaskClone(GoogleApiAsyncTask taskClone) {
        this.taskClone = taskClone;
    }

    static public final String APP_PROVIDED_EMAIL = "yalp.store.user.one@gmail.com";

    public void logInWithPredefinedAccount() {
        AppProvidedCredentialsTask task = new AppProvidedCredentialsTask();
//        task.setTaskClone(taskClone);
        task.setContext(context);
        task.prepareDialog(R.string.dialog_message_logging_in_predefined, R.string.dialog_title_logging_in);
        task.execute(APP_PROVIDED_EMAIL);
    }

    public void startCredentialsTask(String email, String password) {
        UserProvidedCredentialsTask task = new UserProvidedCredentialsTask();
//        task.setTaskClone(taskClone);
        task.setContext(context);
        task.prepareDialog(R.string.dialog_message_logging_in_provided_by_user, R.string.dialog_title_logging_in);
        task.execute(email, password);
    }

    private class AppProvidedCredentialsTask extends CredentialsDialogBuilder.CheckCredentialsTask {

        @Override
        protected Throwable doInBackground(String[] params) {
            try {
                new PlayStoreApiAuthenticator(context).login(params[0]);
            } catch (Throwable e) {
                return e;
            }
            return null;
        }
    }

    private class UserProvidedCredentialsTask extends CredentialsDialogBuilder.CheckCredentialsTask {

        @Override
        protected Throwable doInBackground(String[] params) {
            if (params.length < 2
                    || params[0] == null
                    || params[1] == null
                    || TextUtils.isEmpty(params[0])
                    || TextUtils.isEmpty(params[1])
                    ) {
                return new CredentialsEmptyException();
            }
            try {
                new PlayStoreApiAuthenticator(context).login(params[0], params[1]);
            } catch (Throwable e) {
                if (e instanceof AuthException && null != ((AuthException) e).getTwoFactorUrl()) {
                    //TODO:
                }
                return e;
            }
            return null;
        }
    }
}
