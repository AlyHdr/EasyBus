package com.example.alihaidar.phoneverification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Ali Haidar on 5/23/2018.
 */

public class AbsenceTomorrowReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences=context.getSharedPreferences("absence",Context.MODE_PRIVATE);
        int morning=preferences.getInt("morning",0);
        int after=preferences.getInt("afternoon",0);
        UpdateAbsenceTask task=new UpdateAbsenceTask(context,null);
        task.execute(MainActivity.student_user.id,morning,after);
    }
    private void sendNotification(Context c,String messageBody){
        Intent intent=new Intent(c,NavigationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(c,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultUriRingtone= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(c);
        builder.setSmallIcon(R.drawable.ic_action_name);
        builder.setContentTitle("EasyBus");
        builder.setContentText(messageBody);
        builder.setAutoCancel(true);
        builder.setSound(defaultUriRingtone);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager=(NotificationManager)c.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,builder.build());

    }
}
