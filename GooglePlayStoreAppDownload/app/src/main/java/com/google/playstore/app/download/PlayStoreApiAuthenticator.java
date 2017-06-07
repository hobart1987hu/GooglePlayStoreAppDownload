package com.google.playstore.app.download;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.github.yeriomin.playstoreapi.ApiBuilderException;
import com.github.yeriomin.playstoreapi.DeviceInfoProvider;
import com.github.yeriomin.playstoreapi.GooglePlayAPI;
import com.github.yeriomin.playstoreapi.PropertiesDeviceInfoProvider;

import java.io.IOException;
import java.util.Locale;

/**
 * Created by huzy on 2017/6/7.
 */

public class PlayStoreApiAuthenticator {

    private Context context;

    private static GooglePlayAPI api;

    public PlayStoreApiAuthenticator(Context context) {
        this.context = context;
    }

    public GooglePlayAPI getApi() throws IOException {
        if (api == null) {
            api = build();
        }
        return api;
    }

    public void login(String email) throws IOException {
        build(email);
    }

    public void login(String email, String password) throws IOException {
        build(email, password);
    }

    public void logout() {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.remove(PreferenceConstants.PREFERENCE_EMAIL);
        prefs.remove(PreferenceConstants.PREFERENCE_GSF_ID);
        prefs.remove(PreferenceConstants.PREFERENCE_AUTH_TOKEN);
        prefs.commit();
        api = null;
    }

    private GooglePlayAPI build() throws IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String email = prefs.getString(PreferenceConstants.PREFERENCE_EMAIL, "");
        return build(email);
    }

    private GooglePlayAPI build(String email) throws IOException {
        return build(email, null);
    }

    private GooglePlayAPI build(String email, String password) throws IOException {
        if (TextUtils.isEmpty(email)) {
            throw new CredentialsEmptyException();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String locale = prefs.getString(PreferenceConstants.PREFERENCE_REQUESTED_LANGUAGE, "");
        String gsfId = prefs.getString(PreferenceConstants.PREFERENCE_GSF_ID, "");
        String token = prefs.getString(PreferenceConstants.PREFERENCE_AUTH_TOKEN, "");

        com.github.yeriomin.playstoreapi.PlayStoreApiBuilder builder = new com.github.yeriomin.playstoreapi.PlayStoreApiBuilder()
                .setHttpClient(new NativeHttpClientAdapter())
                .setDeviceInfoProvider(getDeviceInfoProvider())
                .setLocale(TextUtils.isEmpty(locale) ? Locale.getDefault() : new Locale(locale))
                .setEmail(email)
                .setPassword(password)
                .setGsfId(gsfId)
                .setToken(token);
        try {
            api = builder.build();
        } catch (ApiBuilderException e) {
            // Should not happen
        }

        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(PreferenceConstants.PREFERENCE_EMAIL, email);
        prefsEditor.putString(PreferenceConstants.PREFERENCE_GSF_ID, api.getGsfId());
        prefsEditor.putString(PreferenceConstants.PREFERENCE_AUTH_TOKEN, api.getToken());
        prefsEditor.commit();
        return api;
    }

    private DeviceInfoProvider getDeviceInfoProvider() {
        DeviceInfoProvider deviceInfoProvider;
        String spoofDevice = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PreferenceConstants.PREFERENCE_DEVICE_TO_PRETEND_TO_BE, "");
        if (TextUtils.isEmpty(spoofDevice)) {
            deviceInfoProvider = new NativeDeviceInfoProvider();
            ((NativeDeviceInfoProvider) deviceInfoProvider).setContext(context);
            ((NativeDeviceInfoProvider) deviceInfoProvider).setLocaleString(Locale.getDefault().toString());
        } else {
            deviceInfoProvider = new PropertiesDeviceInfoProvider();
            ((PropertiesDeviceInfoProvider) deviceInfoProvider).setProperties(new SpoofDeviceManager(context).getProperties(spoofDevice));
            ((PropertiesDeviceInfoProvider) deviceInfoProvider).setLocaleString(Locale.getDefault().toString());
        }
        return deviceInfoProvider;
    }
}
