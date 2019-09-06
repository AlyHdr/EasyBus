package com.example.alihaidar.phoneverification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * Created by Ali Haidar on 6/13/2018.
 */

public class ResetAbsenceReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        UpdateAbsenceTask task=new UpdateAbsenceTask(context,null);
        SharedPreferences preferences=context.getSharedPreferences("authenticated",Context.MODE_PRIVATE);
        int UID=preferences.getInt("userId",0);
        task.execute(UID,1,1);
    }
}
