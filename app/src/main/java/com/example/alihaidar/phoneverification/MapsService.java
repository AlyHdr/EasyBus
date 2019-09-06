package com.example.alihaidar.phoneverification;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.Manifest;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

public class MapsService extends Service {

    private static final String TAG = MapsService.class.getSimpleName();
    String phone = "";
    public static Service service;
    public static Location globalLocation;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        service = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null)
        {
            phone = intent.getExtras().getString("phone");
            buildNotification();
            requestLocationUpdates();
        }
        return super.onStartCommand(intent, flags, startId);
    }
    public static StopServiceReceiver stopServiceReceiver=new StopServiceReceiver();

    private void buildNotification() {
        String stop = "stop";
        registerReceiver(stopServiceReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);
        // Create the persistent notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("tracking, tap to cancel")
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.drawable.ic_bus);
        startForeground(1, builder.build());
    }

    boolean puttingLiveLocation= true;

    @Override
    public void onDestroy() {
        super.onDestroy();
        puttingLiveLocation= false;
        SharedPreferences.Editor editor=NavigationActivity.finish.getPreferences(MODE_PRIVATE).edit();
        editor.putBoolean("tracking",false);
        editor.apply();
    }


    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(1000);
        request.setFastestInterval(1000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        final String path1 = "Locations/bus" + phone;
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (puttingLiveLocation)
                    {
                        DatabaseReference refForStudentsUpdate = FirebaseDatabase.getInstance().getReference(path1);
                        Location location = locationResult.getLastLocation();
                        globalLocation = location;
                        if (location != null) {
                            Log.d(TAG, "location update " + location);
                            refForStudentsUpdate.setValue(location);
                        }
                    }
                }
            }, null);
        }
    }

}
