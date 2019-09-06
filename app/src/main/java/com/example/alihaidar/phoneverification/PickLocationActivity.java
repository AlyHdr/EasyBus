package com.example.alihaidar.phoneverification;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class PickLocationActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_location);
        MapFragment fragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map_change_location);
        fragment.getMapAsync(this);
    }
    boolean location_enabled=false;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==1)
        {
            location_enabled=true;
            takeLocation();
        }
    }
    Location currentLocation;
    boolean loc_first_time=false;
    public void takeLocation() {
        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationRequest request = new LocationRequest();
        request.setInterval(1000);
        request.setFastestInterval(1000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {

            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                displayLocationSettingsRequest(this);
            }
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (!loc_first_time) {
                        loc_first_time = true;
                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            currentLocation=location;
                            marker=myMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude())));
                            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()), 16);
                            myMap.animateCamera(cu);
                        }
                    }
                }
            }, null);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                takeLocation();

            } else {

                Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
            return;

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    private static final int PERMISSIONS_REQUEST = 1;
    final int REQUEST_CHECK_SETTINGS=1;
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
                            status.startResolutionForResult(PickLocationActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        break;
                }
            }
        });
    }
    GoogleMap myMap;
    Marker marker;
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        myMap = googleMap;
        myMap.setMaxZoomPreference(17.5f);
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(33.828334,35.645076), 9);
        myMap.moveCamera(cu);
        takeLocation();
        myMap.setOnMapClickListener(
                new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {

                        if(((LocationManager)getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER))
                        {
                            currentLocation.setLatitude(latLng.latitude);
                            currentLocation.setLongitude(latLng.longitude);
                            marker.setPosition(latLng);
                        }
                        else
                            Toast.makeText(PickLocationActivity.this, "enable location", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void saveLocation(View view)
    {
        if(currentLocation!=null) {
            RegisterActivity.location_taken = true;
            SharedPreferences preferences = getSharedPreferences("authenticated", MODE_PRIVATE);
            preferences.edit().putString("latitude", "" + currentLocation.getLatitude())
                    .putString("longitude", "" + currentLocation.getLongitude())
                    .apply();
            finish();
        }
        else
        {
            Snackbar snackbar=Snackbar.make(findViewById(R.id.map_change_location),"Please take a location", BaseTransientBottomBar.LENGTH_LONG);
            View snack_view=snackbar.getView();
            snack_view.setBackgroundColor(Color.RED);
            snackbar.show();
        }
    }
}
