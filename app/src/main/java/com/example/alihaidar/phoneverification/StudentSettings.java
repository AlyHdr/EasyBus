package com.example.alihaidar.phoneverification;

import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;


import java.util.concurrent.ExecutionException;


/**
 * Created by Ali Haidar on 5/20/2018.
 */

public class StudentSettings extends PreferenceFragment{

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1 ;
    SeekBar seekBar;
    TextView textView;
    ListPreference preferenceListMapType;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.student_settings_xml);

        preferenceListMapType = (ListPreference) getPreferenceManager().findPreference("preference_list_mapType");
        preferenceListMapType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                preferenceListMapType.setSummary((String) o);
                return true;
            }
        });

        Preference change_location=findPreference("changeLocation");
        Preference seekbar_radius=findPreference("seekbar_radius");
        seekbar_radius.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder alert=new AlertDialog.Builder(getContext());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.layout_alert_adjust_radius, null);
                seekBar=alertLayout.findViewById(R.id.seekbar_radius);
                seekBar.setProgress(MainActivity.student_user.fenceRaduis);
                textView=alertLayout.findViewById(R.id.text_seek);
                textView.setText(String.valueOf(MainActivity.student_user.fenceRaduis));
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        textView.setText(String.valueOf(i));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlertDialog alert = new AlertDialog.Builder(getActivity()).create();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View alertLayout = inflater.inflate(R.layout.layout_loading, null);
                        TextView text_load=alertLayout.findViewById(R.id.text_loading);
                        text_load.setText("Updating radius please wait..");
                        alert.setView(alertLayout);
                        alert.setCancelable(false);
                        alert.show();
                        MainActivity.student_user.setRadius(seekBar.getProgress());
                        UpdateRadiusSizeTask task=new UpdateRadiusSizeTask(getActivity().getApplicationContext(),alert);
                        task.execute(String.valueOf(MainActivity.student_user.id),String.valueOf(seekBar.getProgress()));
                    }
                });

                alert.setView(alertLayout);
                alert.setNeutralButton("Cancel",null);
                alert.create().show();
                return false;
            }
        });
        change_location.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                final CharSequence[] items=new CharSequence[2];
                items[0]="Auto Detect";
                items[1]="Pick From Map";
                alert.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0)
                            takeLocation();
                        else
                        {
                            FragmentManager manager=getFragmentManager();
                            manager.beginTransaction().replace(R.id.content_navigation,new FragmentChangeLocationMap()).commit();
                        }
                    }
                });
                alert.setNeutralButton("Cancel",null);
                alert.create().show();
                return false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==2)
            takeLocation();
    }
    final int REQUEST_CHECK_SETTINGS=2;
    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context).addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        break;
                }
            }
        });
    }

    boolean loc_first_time=false;
    public void takeLocation()
    {
        LocationManager manager=(LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        LocationRequest request = new LocationRequest();
        request.setInterval(1000);
        request.setFastestInterval(1000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getActivity());
        int permission = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {

            if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
              displayLocationSettingsRequest(getActivity());
            }
            else {
                final AlertDialog alert = new AlertDialog.Builder(getActivity()).create();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.layout_loading, null);
                alert.setView(alertLayout);
                alert.setCancelable(false);
                alert.show();
                client.requestLocationUpdates(request, new LocationCallback() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (!loc_first_time) {
                            loc_first_time = true;
                            Location location = locationResult.getLastLocation();
                            if (location != null) {
                                alert.dismiss();
                                UpdateLocationTask task = new UpdateLocationTask(getContext());
                                task.execute(String.valueOf(MainActivity.student_user.id), String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
                            }
                        }
                    }
                }, null);
            }
        }
        else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},1);
            takeLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                takeLocation();
            } else {

            }
            return;

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }
}
