package com.google.playstore.app.download;

import com.github.yeriomin.playstoreapi.AndroidCheckinRequest;
import com.github.yeriomin.playstoreapi.DeviceConfigurationProto;

/**
 * Created by huzy on 2017/6/7.
 */

public interface DeviceInfoProvider {
    AndroidCheckinRequest generateAndroidCheckinRequest();

    DeviceConfigurationProto getDeviceConfigurationProto();

    String getUserAgentString();

    int getSdkVersion();
}
