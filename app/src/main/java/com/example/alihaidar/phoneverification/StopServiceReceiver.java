package com.example.alihaidar.phoneverification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Ahmad Alibrahim on 5/25/2018.
 */

public class StopServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MapsService.service.unregisterReceiver(this);
        MapsService.service.stopSelf();
        if(MapsFragmentDriver.fabStartService!=null)
            MapsFragmentDriver.fabStartService.setImageResource(R.drawable.icon_start);
    }
}
