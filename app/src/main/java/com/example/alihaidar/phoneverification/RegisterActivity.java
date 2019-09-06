package com.example.alihaidar.phoneverification;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;


public class RegisterActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }
    public void takeLocation(View view)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        CharSequence[] items=new CharSequence[2];
        items[0]="Auto Detect";
        items[1]="Pick From Map";
        alert.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i==0)
                    autoDetect();
                else
                {
                    Intent intent=new Intent(getApplicationContext(),PickLocationActivity.class);
                    startActivity(intent);
                }
            }
        });
        alert.setNeutralButton("Cancel",null);
        alert.create().show();
    }
    String user_type="D";
    ArrayList<NameValuePair> values = new ArrayList<>();
    public void register(View view) {
        EditText text_fname = findViewById(R.id.register_fname);
        EditText text_lname = findViewById(R.id.register_lname);
        if (text_fname.getText().toString().equals("") || text_lname.getText().toString().equals("")) {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.register_fname), "Please fill all fields", BaseTransientBottomBar.LENGTH_LONG);
            View snack_view = snackbar.getView();
            snack_view.setBackgroundColor(Color.RED);
            snackbar.show();
        } else if (!location_taken) {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.register_fname), "Please take a location", BaseTransientBottomBar.LENGTH_LONG);
            View snack_view = snackbar.getView();
            snack_view.setBackgroundColor(Color.RED);
            snackbar.show();
        } else {
            SharedPreferences pref=getSharedPreferences("register",MODE_PRIVATE);
            String latitude = pref.getString("latitude", "");
            String longitude = pref.getString("longitude", "");


            values.add(new BasicNameValuePair("fName", text_fname.getText().toString()));
            values.add(new BasicNameValuePair("lName", text_lname.getText().toString()));
            values.add(new BasicNameValuePair("phoneNb", getPhoneNumber()));
            values.add(new BasicNameValuePair("longtitude", "" + longitude));
            values.add(new BasicNameValuePair("latitude", "" + latitude));
            AlertDialog.Builder alert=new AlertDialog.Builder(this);
            alert.setTitle("Registration Type");
            CharSequence items[]={"Driver","User"};
            Toast.makeText(this, text_fname.getText().toString(), Toast.LENGTH_SHORT).show();
            alert.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                       if(i==1)
                           user_type="U";
                       else
                           user_type="D";
                }
            });
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    values.add(new BasicNameValuePair("user_type",user_type));
                    addUserTask task = new addUserTask();
                    task.execute(values);

                    getSharedPreferences("authenticated", Context.MODE_PRIVATE).edit().putBoolean("profile_registered",true).apply();
                    Intent intent=new Intent(getApplication(),MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            alert.setNegativeButton("Cancel",null);
            alert.create().show();
        }
    }
    public String getPhoneNumber()
    {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String phone = auth.getCurrentUser().getPhoneNumber();
        String ph="";
        char[] elements = phone.toCharArray();
        for (int i = 4; i < elements.length; i++)
            ph += elements[i];
        return ph;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1)
            autoDetect();
    }

    final int REQUEST_CHECK_SETTINGS = 1;
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
                            status.startResolutionForResult(RegisterActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        break;
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                autoDetect();
            } else {
                finish();
            }
            return;

        }
    }

    private static final int PERMISSIONS_REQUEST = 1;
    boolean loc_first_time=false;
    static boolean location_taken=false;
    public void autoDetect() {
        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationRequest request = new LocationRequest();
        request.setInterval(1000);
        request.setFastestInterval(1000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {

            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                displayLocationSettingsRequest(RegisterActivity.this);
            }
            else {
                final AlertDialog alert = new AlertDialog.Builder(this).create();
                LayoutInflater inflater = getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.layout_loading, null);
                alert.setView(alertLayout);
                alert.setCancelable(false);
                alert.show();
                client.requestLocationUpdates(request, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (!loc_first_time) {
                            loc_first_time = true;
                            Location location = locationResult.getLastLocation();
                            if (location != null) {
                                alert.dismiss();
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                SharedPreferences preferences=getSharedPreferences("register",MODE_PRIVATE);
                                SharedPreferences.Editor editor=preferences.edit();
                                editor.putString("latitude", "" + latitude);
                                editor.putString("longitude", "" + longitude);
                                editor.apply();
                                location_taken = true;
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.register_fname), "Saved !", BaseTransientBottomBar.LENGTH_LONG);
                                View snack_view = snackbar.getView();
                                snack_view.setBackgroundColor(Color.GREEN);
                                snackbar.show();
                            }
                        }
                    }
                }, null);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST);
        }
    }
}
