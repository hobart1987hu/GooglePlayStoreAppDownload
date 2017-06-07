package com.google.playstore.app.download;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

public class LoginActivity extends AppCompatActivity {

    EditText et_googleAccount, et_googlePwd,
            et_downloadAppPkg;
    String email, password;
    LinearLayout ll_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);

        et_googleAccount = (EditText) findViewById(R.id.et_googleAccount);
        et_googlePwd = (EditText) findViewById(R.id.et_googlePwd);
        et_downloadAppPkg = (EditText) findViewById(R.id.et_downloadAppPkg);

        ll_login = (LinearLayout) findViewById(R.id.ll_login);


        FirstLaunchChecker firstLaunchChecker = new FirstLaunchChecker(this);
        if (firstLaunchChecker.isFirstLogin()) {
            ll_login.setVisibility(View.VISIBLE);
            findViewById(R.id.btnOAuth).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String account = et_googleAccount.getText().toString();
                    if (TextUtils.isEmpty(account)) {
                        ToastUtils.showLongTaost("请输入Google账号");
                        return;
                    }
                    email = account.trim();

                    String pwd = et_googlePwd.getText().toString();
                    if (TextUtils.isEmpty(account)) {
                        ToastUtils.showLongTaost("请输入Google账号密码");
                        return;
                    }
                    email = pwd.trim();

                    UserProvidedAccountDialogBuilder builder = new UserProvidedAccountDialogBuilder(LoginActivity.this);
                    builder.setPaPaFetchAppCallback(mCallback);
                    builder.logInWithPredefinedAccount();
                }
            });
        } else {
            ll_login.setVisibility(View.GONE);
        }
        findViewById(R.id.btnStartDownload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appPkg = et_downloadAppPkg.getText().toString();
                if (TextUtils.isEmpty(appPkg)) {
                    ToastUtils.showLongTaost("请输入需要下载应用的包名");
                    return;
                }
                appPkg = appPkg.trim();
                Intent intent = new Intent(LoginActivity.this, DownloadDetailActivity.class);
                intent.putExtra("packageName", appPkg);
                startActivity(intent);
            }
        });
    }


    private final PlayStoreResponse mCallback = new PlayStoreResponse() {
        @Override
        public void onIOError() {
            ToastUtils.showLongTaost("网络异常，请重试");
        }

        @Override
        public void onAuthException() {
            ToastUtils.showLongTaost("授权异常，请重试");
        }

        @Override
        public void needPurchase() {

        }

        @Override
        public void notSupportDownload() {

        }

        @Override
        public void onAccountNotExists() {
            ToastUtils.showLongTaost("账户不存在");
        }

        @Override
        public void onAccountOrPwdError() {
            ToastUtils.showLongTaost("账户或者密码不正确");
        }

        @Override
        public void onGoogleAccountLogin() {
            UserProvidedAccountDialogBuilder builder = new UserProvidedAccountDialogBuilder(LoginActivity.this);
            builder.setPaPaFetchAppCallback(mCallback);
            builder.startCredentialsTask(email, password);
        }

        @Override
        public void onCredentialsTaskFinished() {
            ll_login.setVisibility(View.GONE);
            ToastUtils.showLongTaost("授权成功，现在可以开始去下载APP啦！");
        }
    };
}
