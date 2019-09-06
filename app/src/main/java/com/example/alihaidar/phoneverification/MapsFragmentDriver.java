package com.example.alihaidar.phoneverification;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsFragmentDriver extends Fragment implements OnMapReadyCallback, RoutingListener {
    private GoogleMap mMap;
    String phoneNb;
    public static boolean isSavingTrack = false;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor preferenceEditor;
    public static boolean draw;
    private GeofencingClient mGeofencingClient;
    Circle circle;
    FloatingActionButton button_save;
    static FloatingActionButton fabStartService;
    ArrayList<Geofence> geofenceList;
    Marker driverMarker;
    Polyline mainPolyLine;
    public static HashMap<Integer,Marker> markers = new HashMap<>();
    boolean driverfirstloc = true;
    public Fragment fragment = this;
    String trackType;
    boolean firstLaunch;
    SharedPreferences defaultPreferences;
    public static boolean morning;
    public School school;
    public LatLng houseLatLng;

    public MapsFragmentDriver() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //get lat and lng of school from intent
        double schoolLat=getActivity().getIntent().getExtras().getDouble("schoolLat",0);
        double schoolLong=getActivity().getIntent().getExtras().getDouble("schoolLng",0);
        String schoolName=getActivity().getIntent().getExtras().getString("schoolName","School");
        school =new School(schoolName,new LatLng(schoolLat,schoolLong));

        double houseLat=getActivity().getIntent().getExtras().getDouble("houseLat",0);
        double houseLng=getActivity().getIntent().getExtras().getDouble("houseLng",0);
        houseLatLng=new LatLng(houseLat,houseLng);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Calendar calendar=Calendar.getInstance();
        int hour=calendar.get(Calendar.HOUR_OF_DAY);
        if(hour>=6 && hour<=9)
            morning=true;
        else
            morning=false;
        return inflater.inflate(R.layout.fragment_maps_driver, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        sharedPreferences = this.getActivity().getPreferences(getActivity().MODE_PRIVATE);
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferenceEditor = sharedPreferences.edit();
        mGeofencingClient = LocationServices.getGeofencingClient(getActivity());

        firstLaunch = sharedPreferences.getBoolean("firstLaunch", true);
        if (firstLaunch)
        {
            DrawerLayout dr = getActivity().findViewById(R.id.drawer_layout);
            dr.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            DataBaseHandler handler=new DataBaseHandler(getActivity());
            handler.putVisitedStudents(MainActivity.allOrderedStudents);
            preferenceEditor.putBoolean("firstLaunch", false);
            preferenceEditor.apply();
        }

        fabStartService=getActivity().findViewById(R.id.start_service_button);

        if(sharedPreferences.getBoolean("tracking",false))
            fabStartService.setImageResource(R.drawable.icon_pause);


        fabStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (!sharedPreferences.getBoolean("tracking", false))
                {
                    LocationManager lm = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("Track");
                        alert.setMessage("Start Tracking?");
                        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(getActivity(), MapsService.class);
                                intent.putExtra("phone", phoneNb);
                                getActivity().startService(intent);
                                Toast.makeText(getActivity(), "Service Started", Toast.LENGTH_SHORT).show();
                                preferenceEditor.putBoolean("tracking", true);
                                preferenceEditor.apply();
                                fabStartService.setImageResource(R.drawable.icon_pause);
                                boolean automaticNotification = defaultPreferences.getBoolean("preference_switch_automaticAlert", false);
                                if (automaticNotification)
                                {
                                    String driverId = String.valueOf(getActivity().getIntent().getExtras().getInt("id", 2));
                                    sendBroadcast("Service Started", driverId);
                                }
                            }
                        });
                        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        alert.show();
                    }
                    else
                    {
                        displayLocationSettingsRequest(getActivity(),REQUEST_CHECK_SETTINGS);
                    }
                }
                else if(sharedPreferences.getBoolean("tracking",false))
                {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle("Track");
                    alert.setMessage("Stop Tracking?");
                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            MapsService.service.unregisterReceiver(MapsService.stopServiceReceiver);
                            MapsService.service.stopSelf();
                            Toast.makeText(getActivity(), "Service Stopped", Toast.LENGTH_SHORT).show();
                            preferenceEditor.putBoolean("tracking", false);
                            preferenceEditor.commit();
                            fabStartService.setImageResource(R.drawable.icon_start);
                        }
                    });
                    alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    alert.show();
                }
            }
        });
        button_save = getActivity().findViewById(R.id.save_Location_Button);
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startSaving(view);
                startSaving();
            }
        });


        draw = sharedPreferences.getBoolean("draw", true);


        if(sharedPreferences.getBoolean("driverFirstLaunch",true))
        {
            SharedPreferences.Editor editor = getActivity().getPreferences(getActivity().MODE_PRIVATE).edit();
            editor.putBoolean("driverFirstLaunch", false);
            editor.apply();

            //make map item the selected item
            NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
            navigationView.getMenu().getItem(0).setChecked(true);
        }
        MapFragment fragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);
        phoneNb = getActivity().getIntent().getExtras().getString("phone");
        trackType = defaultPreferences.getString("preference_list_trackType", "savedTrack");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (isNetworkAvailable())
        {
            if (!sharedPreferences.getBoolean("greenSaving", false)) {
                isSavingTrack = false;
                button_save.setColorNormal(getResources().getColor(R.color.color_red));
            }
            else
            {
                isSavingTrack = true;
                button_save.setColorNormal(getResources().getColor(R.color.color_green));
            }
        }
        else
        {
            AlertDialog.Builder alert = new AlertDialog.Builder(this.getActivity());
            alert.setMessage("No internet connection try again !");
            alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                    if (i == KeyEvent.KEYCODE_BACK) {
                        dialogInterface.dismiss();
                        getActivity().finish();
                        MainActivity.finish.finish();
                    }
                    return false;
                }
            });
            alert.setCancelable(false);
            alert.create().show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        viewChosenMapType(); //function to view map type choosed from list preference

        double zoomLevel = sharedPreferences.getFloat("zoom", 9);
        LatLng lastMapViewedLocation =
                new LatLng(sharedPreferences.getFloat("lastMapViewedLat", (float) 33.8804492),
                        sharedPreferences.getFloat("lastMapViewedLongt", (float) 35.5288292));
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(lastMapViewedLocation);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastMapViewedLocation, (int) zoomLevel));

        mMap.setMaxZoomPreference(20);
        addMarkersAndFences();
    }

    @SuppressLint("MissingPermission")
    public void addMarkersAndFences() {
        ArrayList<Student> orderedStudents;
        DataBaseHandler db=new DataBaseHandler(getActivity());
        if(morning)
            orderedStudents=MainActivity.orderedStudentsMorning;
        else
            orderedStudents=MainActivity.orderedStudentsAfternoon;
        db.printVisited();

        for (Student student : orderedStudents)
        {

            Marker marker = mMap.addMarker(new MarkerOptions().position(student.getLatLng()).title(student.getName()));
            marker.showInfoWindow();
            if(db.getVisited(student.id))
            {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }
            markers.put(student.id,marker);
        }
        //add school
        mMap.addMarker(new MarkerOptions().position(school.getLatLng()).title(school.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_school)));
        //add drivers house
        mMap.addMarker(new MarkerOptions().position(houseLatLng).title("Driver's house").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_house)));

        requestLocationUpdates();
        geofenceList = new ArrayList<>();
        for (int i = 0; i < orderedStudents.size(); i++)
        {
            Student student = orderedStudents.get(i);
            geofenceList.add(new Geofence.Builder().
                    setRequestId(student.id + "")
                    .setCircularRegion(student.getLatLng().latitude, student.getLatLng().longitude, student.getFenceRaduis())
                    .setExpirationDuration(Integer.MAX_VALUE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());

            CircleOptions circleOptions = new CircleOptions()
                    .center(student.getLatLng())
                    .radius(student.getFenceRaduis())
                    .fillColor(0x40ff0000)
                    .strokeColor(Color.TRANSPARENT)
                    .strokeWidth(2);
            circle = mMap.addCircle(circleOptions);

        }
        //Add the school
        geofenceList.add(new Geofence.Builder().
                setRequestId("school")
                .setCircularRegion(school.getLatLng().latitude, school.getLatLng().longitude, 300)
                .setExpirationDuration(Integer.MAX_VALUE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
        //add school circle
        CircleOptions circleOptions = new CircleOptions()
                .center(school.getLatLng())
                .radius(300)
                .fillColor(0x4000FF00)
                .strokeColor(Color.TRANSPARENT)
                .strokeWidth(2);
        circle = mMap.addCircle(circleOptions);

        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });
        drawMainTrack();
    }
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    PendingIntent mGeofencePendingIntent;

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null)
            return mGeofencePendingIntent;

        else {
            Intent intent = new Intent(getActivity(),GeofenceReciever.class);
            return PendingIntent.getBroadcast(this.getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    private void displayLocationSettingsRequest(Context context, final int requestCode) {
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
                switch (status.getStatusCode())
                {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try
                        {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(getActivity(), requestCode);
                        }
                        catch (IntentSender.SendIntentException e)
                        {

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void getTrackFromGoogle(ArrayList<LatLng> array) {
        List<LatLng> sub = new ArrayList<>();
        while (true)
        {
            countDraw++;
            if (array.size() <= 5)
            {
                if (array.size() > 1) {
                    sub.clear();
                    sub.addAll(array.subList(0, array.size()));
                    DrawTrack(sub);
                }
                else
                    alert_load.dismiss();
                break;
            } else {
                sub.clear();
                sub.addAll(array.subList(0, 5));
                array.remove(0);
                array.remove(0);
                array.remove(0);
                array.remove(0);
                DrawTrack(sub);
            }
        }
    }
    public void DrawTrack(List<LatLng> array)
    {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(MapsFragmentDriver.this)
                //.optimize(true)
                .key("AIzaSyCC0wmjngzkdckh85u9HHzgfkyerxXugcc")
                .waypoints(array)
                .alternativeRoutes(true)
                .build();
        routing.execute();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        Toast.makeText(getActivity(), "xxxx"+e.getMessage()+"xxx", Toast.LENGTH_SHORT).show();
    }

    int countRoutes=0;

    int countDraw=0;
    @Override
    public void onRoutingStart() {
    }
    public PolylineOptions AllPolyOptions=new PolylineOptions();
    public ArrayList<LatLng> all_route_points=new ArrayList<>();
    @Override
    public void onRoutingSuccess(ArrayList<Route> routes, int shortestPathIndex) {
        Route minRoute = routes.get(shortestPathIndex);
       all_route_points.addAll(minRoute.getPoints());
        AllPolyOptions.addAll(minRoute.getPoints());
        countRoutes++;
        if(countRoutes==countDraw) {

            AllPolyOptions.color(Color.BLUE);
            AllPolyOptions.width(15);

            mainPolyLine=mMap.addPolyline(AllPolyOptions);
            alert_load.dismiss();
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng latLng : all_route_points) {
                builder.include(latLng);
            }
            final LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 100);
            mMap.moveCamera(cu);
        }
    }

    @Override
    public void onRoutingCancelled() {

    }
    public static LatLng driver_location_for_reciever;


    private static int counter = 0;
    private static int count = 0;

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(1000);
        request.setFastestInterval(1000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getActivity());
        int permission = ContextCompat.checkSelfPermission(this.getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location l = locationResult.getLastLocation();
                    LatLng latLng = new LatLng(l.getLatitude(), l.getLongitude());
                    driver_location_for_reciever=latLng;
                    if (driverfirstloc)
                    {
                        driverMarker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_name)));
                        driverfirstloc = false;
                    }
                    Location location = locationResult.getLastLocation();
                    if(isSavingTrack)
                    {
                        System.out.println("counttttttttttttttt "+count);
                        if (count % 5 == 0)
                        {
                            String path2 = "NewAdjustedLocation/bus" + phoneNb + "/point" + counter;
                            DatabaseReference refForAdjustLocation = FirebaseDatabase.getInstance().getReference(path2);
                            if (count == 0)
                            {
                                counter=0;
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("NewAdjustedLocation/bus" + phoneNb);
                                ref.removeValue();
                            }
                            refForAdjustLocation.setValue(new LatLng(location.getLatitude(), location.getLongitude()));
                            counter++;
                        }
                        count++;
                    }
                    driverMarker.setPosition(latLng);

                }
            }, null);
        }
    }

    final int REQUEST_CHECK_SETTINGS = 1;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode)
        {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        fabStartService.performClick();
                        break;
                    case Activity.RESULT_CANCELED:
                        Snackbar snackbar=Snackbar.make(getActivity().findViewById(R.id.app_bar),"You can't start service without providing location", BaseTransientBottomBar.LENGTH_LONG);
                        View snack_view=snackbar.getView();
                        snack_view.setBackgroundColor(Color.RED);
                        snackbar.show();
                        break;
                    default:
                        break;
                }
                break;
            case REQUEST_FOR_SAVING_TRACK:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        button_save.performClick();
                        break;
                    case Activity.RESULT_CANCELED:
                        Snackbar snackbar=Snackbar.make(getActivity().findViewById(R.id.app_bar),"You can't save track without providing location", BaseTransientBottomBar.LENGTH_LONG);
                        View snack_view=snackbar.getView();
                        snack_view.setBackgroundColor(Color.RED);
                        snackbar.show();
                        break;
                    default:
                        break;
                }
        }
    }
    AlertDialog alert_load;
    public void drawMainTrack() {
        alert_load = new AlertDialog.Builder(getActivity()).create();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_loading, null);
        TextView text_load=alertLayout.findViewById(R.id.text_loading);
        text_load.setText("Acquiring path please wait..");
        alert_load.setView(alertLayout);
        alert_load.setCancelable(false);
        alert_load.show();
        if (defaultPreferences.getString("preference_list_trackType", "sortedStudents").equals("savedTrack"))
        {
            String path = "NewAdjustedLocation/bus" + phoneNb;
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    System.out.println("Draw-----------------------" + draw);
                    if (draw)
                    {
                        if (dataSnapshot.exists())
                        {
                            ArrayList<LatLng> trackPoints = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren())
                            {
                                ArrayList<Double> latlng = new ArrayList<>();
                                for (DataSnapshot s : snapshot.getChildren())
                                {
                                    latlng.add((double) s.getValue());
                                }
                                trackPoints.add(new LatLng(latlng.get(0), latlng.get(1)));
                            }
                            if(morning)
                            {
                                trackPoints.add(0,houseLatLng);
                                trackPoints.add(school.getLatLng());
                            }
                            else
                            {
                                trackPoints.add(0,school.getLatLng());
                                trackPoints.add(houseLatLng);
                            }
                            getTrackFromGoogle(trackPoints);
                        }
                        else
                        {
                            Snackbar.make(getActivity().findViewById(R.id.app_bar), "This is your first route you can start saving your best track!", Snackbar.LENGTH_LONG).show();
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else if (defaultPreferences.getString("preference_list_trackType", "sortedStudents").equals("sortedStudents")) {
            ArrayList<LatLng> latlngs = new ArrayList<>();
            System.out.println("We are in "+morning);
            if(morning)
            {
                latlngs.add(houseLatLng);
                for (Student student : MainActivity.orderedStudentsMorning)
                {
                    latlngs.add(new LatLng(student.getLatLng().latitude, student.getLatLng().longitude));
                }
                latlngs.add(school.getLatLng());
            }
            else
            {
                latlngs.add(school.getLatLng());
                for (Student student : MainActivity.orderedStudentsAfternoon)
                {
                    latlngs.add(new LatLng(student.getLatLng().latitude, student.getLatLng().longitude));
                }
                latlngs.add(houseLatLng);
            }
            getTrackFromGoogle(latlngs);
        }
    }

    final int REQUEST_FOR_SAVING_TRACK=12;

    public void startSaving() {
        LocationManager lm = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (isSavingTrack) {
                //disable saving
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setMessage("Are you sure you want to stop saving your track?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isSavingTrack = false;
                        preferenceEditor.putBoolean("greenSaving", false);
                        preferenceEditor.commit();
                        count=0;
                        counter=0;
                        button_save.setColorNormal(getResources().getColor(R.color.color_red));
                        draw = true;
                        preferenceEditor.putBoolean("draw", true);
                        preferenceEditor.commit();
                    }
                });
                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alert.create().show();
            } else {
                AlertDialog.Builder alert = new AlertDialog.Builder(this.getActivity());
                alert.setMessage("Are you sure you want to start saving your track?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isSavingTrack = true;
                        preferenceEditor.putBoolean("greenSaving", true);
                        preferenceEditor.commit();
                        draw = false;
                        count = 0;
                        button_save.setColorNormal(getResources().getColor(R.color.color_green));
                        preferenceEditor.putBoolean("draw", false);
                        preferenceEditor.commit();
                    }
                });
                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alert.create().show();
            }
        }
        else
        {
            displayLocationSettingsRequest(getActivity(),REQUEST_FOR_SAVING_TRACK);
        }
    }

    @Override
    public void onDestroy() {
        if (mMap != null) {
            float zoom = mMap.getCameraPosition().zoom;
            LatLng coordinates = mMap.getCameraPosition().target;
            preferenceEditor.putFloat("lastMapViewedLat", (float) coordinates.latitude);
            preferenceEditor.putFloat("lastMapViewedLongt", (float) coordinates.longitude);
            preferenceEditor.putFloat("zoom", zoom);
            preferenceEditor.commit();
        }
        super.onDestroy();
    }

    protected void viewChosenMapType() {
        String choosenMap = defaultPreferences.getString("preference_list_mapType", "Normal");
        switch (choosenMap) {
            case "Hybrid":
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case "Normal":
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case "Satellite":
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case "Terrain":
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
        }
    }

    String app_server_url2 = "https://easybusmanagment.000webhostapp.com/send_broadcast.php?morning="+morning;

    public void sendBroadcast(final String message, final String id) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, app_server_url2, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Snackbar snackbar=Snackbar.make(getActivity().findViewById(R.id.app_bar),"Sent!", BaseTransientBottomBar.LENGTH_LONG);
                View snack_view=snackbar.getView();
                snack_view.setBackgroundColor(Color.GREEN);
                snackbar.show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Error "+error, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("message", message);
                params.put("title", "EasyBus");
                params.put("driverId", id);
                return params;

            }
        };
        MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }
}


