package com.betasolutions.cameraimage;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * Created by mohammedalichherawalla on 12/07/16.
 */
@SuppressLint("NewApi")
public class NLService extends NotificationListenerService {
    private String TAG = this.getClass().getSimpleName();
    public static final String NOT_TAG = "com.betasolutions.NOTIFICATION_LISTENER";
    public static final String NOT_POSTED = "POSTED";
    public static final String NOT_REMOVED = "REMOVED";
    public static final String NOT_EVENT_KEY = "not_key";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "**********  onNotificationPosted");
        Intent i = new Intent(NOT_TAG);
        i.putExtra(NOT_EVENT_KEY, NOT_POSTED);
        sendBroadcast(i);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "********** onNOtificationRemoved");
        Intent i = new Intent(NOT_TAG);
        i.putExtra(NOT_EVENT_KEY, NOT_REMOVED);
        sendBroadcast(i);
    }

}
