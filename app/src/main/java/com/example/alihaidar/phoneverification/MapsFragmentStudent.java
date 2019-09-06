package com.example.alihaidar.phoneverification;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapsFragmentStudent extends Fragment implements OnMapReadyCallback, RoutingListener {
    private GoogleMap mMap;
    MapsFragmentStudent fragment_main;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor preferenceEditor;
    boolean firstSetMarker = true;
    LatLng driverlocation;
    Marker driverMarker;

    ArrayList<Student> orderedStudents;
    ArrayList<LatLng> orderedTrackList;

    Student student;
    public MapsFragmentStudent() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragment_main=this;
        return inflater.inflate(R.layout.fragment_map_student, container, false);
    }
    SharedPreferences defaultPreferences;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isMorning() throws ParseException {
        SimpleDateFormat parser = new SimpleDateFormat("HH:mm");
        Date six = parser.parse("6:00");
        Date nine = parser.parse("9:00");
        Date date=new Date();
        String now=parser.format(date);
        Date nowDate=parser.parse(now);
        if(nowDate.after(six) && nowDate.before(nine))
            return true;
        else
            return false;
    }
    public boolean isAfternoon() throws ParseException {
        SimpleDateFormat parser = new SimpleDateFormat("HH:mm");
        Date twelve = parser.parse("12:00");
        Date sixteen = parser.parse("16:00");
        Date date=new Date();
        String now=parser.format(date);
        Date nowDate=parser.parse(now);

        if(nowDate.after(twelve) && nowDate.before(sixteen))
            return true;
        else
            return false;
    }
    int selected_driver=0;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedPreferences = this.getActivity().getPreferences(getActivity().MODE_PRIVATE);
        preferenceEditor = sharedPreferences.edit();
        preferenceEditor.putBoolean("firstLaunch",false).apply();
        defaultPreferences= PreferenceManager.getDefaultSharedPreferences(getContext());
        final MapFragment fragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);

        student=MainActivity.student_user;
        final SharedPreferences pref_student=getActivity().getSharedPreferences("pref_student",Context.MODE_PRIVATE);
        selected_driver=pref_student.getInt("selected_driver",0);

        com.github.clans.fab.FloatingActionButton fab_select_driver=getActivity().findViewById(R.id.select_Driver_Button);
        fab_select_driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert=new AlertDialog.Builder(getActivity());
                alert.setTitle("Select a Driver");
                CharSequence driversNames[]=new CharSequence[student.myDrivers.size()];
                for(int i=0;i<student.myDrivers.size();i++)
                {
                    driversNames[i]=student.myDrivers.get(i).getName();
                }
                alert.setSingleChoiceItems(driversNames, selected_driver, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selected_driver=i;
                    }
                });
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        pref_student.edit().putInt("selected_driver",selected_driver).apply();

                        getFragmentManager().beginTransaction().replace(R.id.content_navigation,new MapsFragmentStudent()).commit();
                    }
                });
                alert.create().show();
            }
        });
        if (student.myDrivers.get(selected_driver).getTrackType().equals("sortedStudents")) {
            orderedStudents = new ArrayList<>();
            GetStudentsOrderedTask task=new GetStudentsOrderedTask();
            task.execute(student.myDrivers.get(selected_driver).getPhoneNb());
            try {
                orderedStudents=task.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            orderedTrackList=new ArrayList<>();
            for (Student s : orderedStudents) {
                try {
                    if(isMorning())
                    {
                        if(s.morning==1)
                            orderedTrackList.add(s.getLatLng());
                    }
                    else
                    {
                        if(s.afternoon==1)
                            orderedTrackList.add(0,s.getLatLng());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            try {
                if(isMorning())
                {
                    orderedTrackList.add(student.myDrivers.get(selected_driver).getLatLng());
                }
                else
                    orderedTrackList.add(0,student.myDrivers.get(selected_driver).getLatLng());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (!isNetworkAvailable())
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
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    Snackbar snackbar_routes;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(student.myDrivers.size()==0)
        {
            AlertDialog.Builder alert=new AlertDialog.Builder(getActivity());
            alert.setMessage("You are not registered with any driver yet go find your drivers");
            alert.setPositiveButton("Find", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //replace with find drivers fragment...
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                         getActivity().finish();
                         MainActivity.finish.finish();
                }
            });
            alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                    if (i == KeyEvent.KEYCODE_BACK) {
                        dialogInterface.dismiss();
                        getActivity().finish();
                    }
                    return false;
                }
            });
            alert.setCancelable(false);
            alert.create().show();
        }
        else {
            mMap = googleMap;
            viewChosenMapType();
            mMap.setMaxZoomPreference(17);

            mMap.addMarker(new MarkerOptions().position(student.myDrivers.get(selected_driver).getLatLng()).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_house)));
            mMap.addMarker(new MarkerOptions().position(MainActivity.student_user.getLatLng()));
            try {
                int zoomValue;
                if (isMorning() || isAfternoon()) {
                    zoomValue = 10;
                    subscribeToUpdates();
                } else {
                    zoomValue = 16;
                    snackbar_routes = Snackbar.make(getActivity().findViewById(R.id.map), "There is no routes right now", BaseTransientBottomBar.LENGTH_INDEFINITE);
                    View snack_view = snackbar_routes.getView();
                    snack_view.setBackgroundColor(Color.RED);
                    snackbar_routes.show();
                }
                CircleOptions circleOptions = new CircleOptions()
                        .center(student.getLatLng())
                        .radius(student.getFenceRaduis())
                        .fillColor(0x40ff0000)
                        .strokeColor(Color.TRANSPARENT)
                        .strokeWidth(2);
                mMap.addCircle(circleOptions);
                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(MainActivity.student_user.getLatLng(), zoomValue);
                mMap.moveCamera(cu);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
    private void subscribeToUpdates() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Locations/bus" + student.myDrivers.get(selected_driver).getPhoneNb());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                    setMarker(dataSnapshot);
                else
                {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setMessage("Your driver isn't registered yet");
                    alert.setOnKeyListener(new DialogInterface.OnKeyListener()
                    {
                        @Override
                        public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent)
                        {
                            if (i == KeyEvent.KEYCODE_BACK)
                            {
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
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    AlertDialog alert_load;
    private void setMarker(final DataSnapshot dataSnapshot) {
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onMapLoaded() {
                String key = dataSnapshot.getKey();
                HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();
                double lat = Double.parseDouble(value.get("latitude").toString());
                double lng = Double.parseDouble(value.get("longitude").toString());
                LatLng location = new LatLng(lat, lng);
                driverlocation = location;
                if (firstSetMarker)
                {
                    driverMarker = mMap.addMarker(new MarkerOptions().title(key).position(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_name)));
                    if(defaultPreferences.getBoolean("showTrack",false)) {
                        alert_load = new AlertDialog.Builder(getActivity()).create();
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View alertLayout = inflater.inflate(R.layout.layout_loading, null);
                        TextView text_load=alertLayout.findViewById(R.id.text_loading);
                        text_load.setText("Acquiring path please wait..");
                        alert_load.setView(alertLayout);
                        alert_load.setCancelable(false);
                        alert_load.show();
                        if (student.myDrivers.get(selected_driver).getTrackType().equals("savedTrack"))
                        {
                            ArrayList<LatLng> track=getTrackForStudentFromFireBase();
                            if(track.size()>1)
                                getTrackFromGoogle(track);
                            else
                            {
                                alert_load.dismiss();
                                Snackbar snackbar=Snackbar.make(getActivity().findViewById(R.id.map),"No path can be drawn right now", BaseTransientBottomBar.LENGTH_LONG);
                                View snack_view=snackbar.getView();
                                snack_view.setBackgroundColor(Color.RED);
                                snackbar.show();
                            }
                        }
                        else if (student.myDrivers.get(selected_driver).getTrackType().equals("sortedStudents")) {
                            if(orderedTrackList.size()>1)
                                getTrackFromGoogle(orderedTrackList);
                            else
                            {
                                alert_load.dismiss();
                                Snackbar snackbar=Snackbar.make(getActivity().findViewById(R.id.map),"No path can be drawn right now", BaseTransientBottomBar.LENGTH_LONG);
                                View snack_view=snackbar.getView();
                                snack_view.setBackgroundColor(Color.RED);
                                snackbar.show();
                            }
                        }
                    }
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(location);
                    builder.include(student.myDrivers.get(selected_driver).getLatLng());
                    builder.include(MainActivity.student_user.getLatLng());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));

                    firstSetMarker = false;
                }
                driverMarker.setPosition(location);
           }
        });
    }
    public ArrayList<LatLng> getTrackForStudentFromFireBase() {
        String path = "NewAdjustedLocation/bus" + student.myDrivers.get(selected_driver).getPhoneNb();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        final ArrayList<LatLng> trackPoints = new ArrayList<>();
        ref.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ArrayList<Double> latlng = new ArrayList<>();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            latlng.add((double) s.getValue());
                        }
                        trackPoints.add(new LatLng(latlng.get(0), latlng.get(1)));
                    }
                } else {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return trackPoints;
    }
    int countDraw=0;
    int countRoute=0;
    public void getTrackFromGoogle(ArrayList<LatLng> array) {

        List<LatLng> sub=new ArrayList<>();
        while (true)
        {
            countRoute++;
            if (array.size() <= 5)
            {
                if(array.size()>1)
                {
                    sub.clear();
                    sub.addAll(array.subList(0, array.size()));
                    DrawTrack(sub);
                }
                break;
            }
            else
            {
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
    public void DrawTrack(List<LatLng> array) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .key("AIzaSyCC0wmjngzkdckh85u9HHzgfkyerxXugcc")
                .waypoints(array)
                .alternativeRoutes(true)
                .build();
        routing.execute();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRoutingFailure(RouteException e) {
        Toast.makeText(getContext(), "" + e, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoutingStart() {

    }

    PolylineOptions AllPolyOptions=new PolylineOptions();
    @Override
    public void onRoutingSuccess(ArrayList<Route> routes, int shortestPathIndex) {
        Route minRoute = routes.get(shortestPathIndex);
        AllPolyOptions.addAll(minRoute.getPoints());
        countDraw++;
        if(countRoute==countDraw) {

            AllPolyOptions.color(getResources().getColor(R.color.color_path));
            AllPolyOptions.width(21);
            mMap.addPolyline(AllPolyOptions);
            alert_load.dismiss();
        }
}

    @Override
    public void onRoutingCancelled() {

    }

    final int REQUEST_CHECK_SETTINGS = 1;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        subscribeToUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        getActivity().finish();
                        break;
                    default:
                        break;
                }
                break;
        }
    }
}
