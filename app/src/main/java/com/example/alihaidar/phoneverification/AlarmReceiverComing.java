package com.example.alihaidar.phoneverification;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import static android.app.NotificationManager.IMPORTANCE_DEFAULT;

/**
 * Created by Ahmad Alibrahim on 5/24/2018.
 */

public class AlarmReceiverComing extends BroadcastReceiver {
    Context context;
    boolean tracking;
    SharedPreferences defaultPreferences;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        defaultPreferences=PreferenceManager.getDefaultSharedPreferences(context);
        tracking=intent.getExtras().getBoolean("tracking");
        NotifyDriver();
    }


    private void NotifyDriver() {
        if (!tracking && defaultPreferences.getBoolean("preference_switch_afternoonNotif",true))
        {
            Intent notificationIntent = new Intent(context, MainActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            //stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(notificationIntent);

            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_main)
                    .setContentTitle("Easy Bus")
                    .setContentText("Reminding you to start service for coming")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
            mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));


            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(5, mBuilder.build());
        }
    }
}
