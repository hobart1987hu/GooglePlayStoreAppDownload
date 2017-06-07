package com.google.playstore.app.download.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;


import com.google.playstore.app.download.R;

import java.lang.reflect.Method;

public class NotificationManagerWrapperLegacy extends NotificationManagerWrapper {

    public NotificationManagerWrapperLegacy(Context context) {
        super(context);
    }

    @Override
    protected Notification get(Intent intent, String title, String message) {
        Notification notification = new Notification(R.mipmap.ic_launcher, "", System.currentTimeMillis());
        try {
            // try to call "setLatestEventInfo" if available
            Method m = notification.getClass().getMethod("setLatestEventInfo", Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
            m.invoke(notification, context, title, message, getPendingIntent(intent));
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
        } catch (Exception e) {
            // do nothing
        }
        return notification;
    }
}
