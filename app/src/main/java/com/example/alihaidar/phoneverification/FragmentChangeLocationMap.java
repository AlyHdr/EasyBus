package com.example.alihaidar.phoneverification;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.concurrent.ExecutionException;

public class FragmentChangeLocationMap extends android.app.Fragment implements OnMapReadyCallback {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fragment_change_location_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MapFragment fragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map_change_location);
        fragment.getMapAsync(this);

    }

    GoogleMap myMap;
    Marker marker;
    LatLng userLocation;
    SharedPreferences preferences;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {

        myMap = googleMap;
        myMap.setMaxZoomPreference(17.5f);
        preferences = getActivity().getSharedPreferences("authenticated", getActivity().MODE_PRIVATE);
        if(MainActivity.student_user!=null) {
            marker = myMap.addMarker(new MarkerOptions().position(MainActivity.student_user.getLatLng()));
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(MainActivity.student_user.getLatLng());
            myMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
        }
        myMap.setOnMapClickListener(
                new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                userLocation=latLng;
                Toast.makeText(getContext(), "Clickedd", Toast.LENGTH_SHORT).show();
               marker.setPosition(latLng);
            }
        });
        Button btn_save=getActivity().findViewById(R.id.btn_save_change_location);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert=new AlertDialog.Builder(getContext());
                alert.setMessage("Are you sure this is your location ?");
                alert.setNegativeButton("No",null);
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(MainActivity.student_user!=null) {
                            MainActivity.student_user.setLatLng(userLocation);
                            UpdateLocationTask task = new UpdateLocationTask(getContext());
                            task.execute(String.valueOf(MainActivity.student_user.id), String.valueOf(userLocation.latitude), String.valueOf(userLocation.longitude));
                        }
                        else
                        {
                            SharedPreferences pref=getActivity().getSharedPreferences("authenticated", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor=pref.edit();
                            editor.putString("latitude", "" + userLocation.latitude);
                            editor.putString("longitude", "" + userLocation.longitude);
                            editor.apply();
                        }
                        FragmentManager manager=getFragmentManager();
                        manager.beginTransaction().replace(R.id.content_navigation,new StudentSettings()).commit();
                    }
                });
                alert.create().show();
            }
        });
    }
}
