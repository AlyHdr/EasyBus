package com.example.alihaidar.phoneverification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class MyFireBaseMessagingService extends FirebaseMessagingService{
    public MyFireBaseMessagingService() {

    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(this);
        if(!preferences.getBoolean("disable_notif",false)) {
            DataBaseHandler handler=new DataBaseHandler(getApplication());
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            Date date = new Date();
            handler.addNotification(new Message(remoteMessage.getData().get("title"),remoteMessage.getData().get("message"),dateFormat.format(date)));
            handler.close();
            sendNotification(remoteMessage.getData().get("message"));
        }
    }

    private void sendNotification(String messageBody){
        Intent intent=new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,0);
        Uri defaultUriRingtone= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_action_name);
        builder.setContentTitle("EasyBus");
        builder.setContentText(messageBody);
        builder.setAutoCancel(true);
        builder.setSound(defaultUriRingtone);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Random random=new Random();
        int id=random.nextInt();
        notificationManager.notify(id,builder.build());

    }
}
