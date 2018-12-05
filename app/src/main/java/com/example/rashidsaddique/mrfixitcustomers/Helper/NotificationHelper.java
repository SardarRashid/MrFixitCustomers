package com.example.rashidsaddique.mrfixitcustomers.Helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.example.rashidsaddique.mrfixitcustomers.R;

public class NotificationHelper extends ContextWrapper {
    private static final String FIX_IT_ID = "com.example.rashidsaddique.mrfixitcustomers.FIXIT";
    private static final String FIX_IT_NAME = "FIXIT customer";

    private NotificationManager manager;


    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannels();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {
        NotificationChannel FixitChannals = new NotificationChannel(FIX_IT_ID,
                FIX_IT_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        FixitChannals.enableLights(true);
        FixitChannals.enableVibration(true);
        FixitChannals.setLightColor(Color.GRAY);
        FixitChannals.setLockscreenVisibility(android.app.Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(FixitChannals);
    }

    public NotificationManager getManager() {
        if (manager == null)
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public android.app.Notification.Builder getFixItNotification(String title, String content, PendingIntent contentIntent,
                                                                 Uri soundUri)
    {
        return new android.app.Notification.Builder(getApplicationContext(), FIX_IT_ID)
                .setContentText(content)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_man);

    }
}
