package com.example.alihaidar.phoneverification;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.SeekBar;
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
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ali Haidar on 5/9/2018.
 */

public class GeofenceReciever extends BroadcastReceiver implements RoutingListener {
    Context con;
    String studentId;

    public boolean isMorning() throws ParseException {
        SimpleDateFormat parser = new SimpleDateFormat("HH:mm");
        Date six = parser.parse("6:00");
        Date nine = parser.parse("9:00");
        Date date = new Date();
        String now = parser.format(date);
        Date nowDate = parser.parse(now);

        if (nowDate.after(six) && nowDate.before(nine))
            return true;
        else
            return false;
    }

    DataBaseHandler db;

    @Override
    public void onReceive(Context context, Intent intent) {
        con = context;
        db = new DataBaseHandler(context);
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        // Get the transition type.
        if (geofencingEvent.getTriggeringGeofences() != null) {
            String id = geofencingEvent.getTriggeringGeofences().get(0).getRequestId();
            studentId = id;
            int geofenceTransition = geofencingEvent.getGeofenceTransition();

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
            {
                if (!studentId.equals("school")) {
                    try {
                        int next = con.getSharedPreferences("next_student", Context.MODE_PRIVATE).getInt("next", 0);
                        if (isMorning())
                        {

                            if (String.valueOf(MainActivity.orderedStudentsMorning.get(next).id).equals(studentId)) {
                                db.updateVisited(Integer.parseInt(studentId));
                                con.getSharedPreferences("fatima", Context.MODE_PRIVATE).edit().putBoolean("sent", false).apply();
                                Routing routing = new Routing.Builder()
                                        .travelMode(AbstractRouting.TravelMode.DRIVING)
                                        .withListener(this)
                                        .key("AIzaSyCC0wmjngzkdckh85u9HHzgfkyerxXugcc")
                                        .waypoints(MapsFragmentDriver.driver_location_for_reciever, MainActivity.orderedStudentsMorning.get(next).getLatLng())
                                        .alternativeRoutes(true)
                                        .build();
                                routing.execute();
                                if (NavigationActivity.finish != null) {
                                    MapsFragmentDriver.markers.get(Integer.parseInt(studentId)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                }
                                SharedPreferences pref = context.getSharedPreferences("next_student", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                next++;
                                editor.putInt("next", next);
                                editor.apply();
                            }
                        }
                        else
                        {
                            if (String.valueOf(MainActivity.orderedStudentsAfternoon.get(next).id).equals(studentId))
                            {
                                con.getSharedPreferences("fatima", Context.MODE_PRIVATE).edit().putBoolean("sent", false).apply();
                                db.updateVisited(Integer.parseInt(studentId));
                                Routing routing = new Routing.Builder()
                                        .travelMode(AbstractRouting.TravelMode.DRIVING)
                                        .withListener(this)
                                        .key("AIzaSyCC0wmjngzkdckh85u9HHzgfkyerxXugcc")
                                        .waypoints(MapsFragmentDriver.driver_location_for_reciever, MainActivity.orderedStudentsAfternoon.get(next).getLatLng())
                                        .alternativeRoutes(true)
                                        .build();
                                routing.execute();
                                if (NavigationActivity.finish != null) {
                                    MapsFragmentDriver.markers.get(Integer.parseInt(studentId)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                }
                                SharedPreferences pref = context.getSharedPreferences("next_student", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                next++;
                                editor.putInt("next", next);
                                editor.apply();
                            }
                        }
                    }
                    catch (ParseException e)
                    {
                        e.printStackTrace();
                    }
                    SharedPreferences preferences = con.getSharedPreferences("next_student", Context.MODE_PRIVATE);
                    if (preferences.getInt("next", 0) == MainActivity.allOrderedStudents.size()) {
                        Toast.makeText(context, "finished", Toast.LENGTH_SHORT).show();
                        //makeRed();
                        DataBaseHandler db=new DataBaseHandler(context);
                        db.putVisitedStudents(MainActivity.allOrderedStudents);
                        db.close();
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("next", 0);
                        editor.apply();
                    }
                }
                else
                {
                    //Toast.makeText(context, "in fence don't send", Toast.LENGTH_SHORT).show();
                    if (!con.getSharedPreferences("fatima", Context.MODE_PRIVATE).getBoolean("sent", false)) {
                        //Toast.makeText(context, "in fence", Toast.LENGTH_SHORT).show();
                        int driverId = con.getSharedPreferences("fatima", Context.MODE_PRIVATE).getInt("driverId", 0);
                        SendBroadCastTask task = new SendBroadCastTask(con);
                        task.execute("Bus reached school safely", String.valueOf(driverId));
                        con.getSharedPreferences("fatima", Context.MODE_PRIVATE).edit().putBoolean("sent", true).apply();
                    }
                }
            }
            else
            {

            }
        }
    }

    public void makeRed()
    {
        for(int i=0;i<MapsFragmentDriver.markers.size();i++)
        {
            if(MapsFragmentDriver.morning)
            {
                MapsFragmentDriver.markers.get(MainActivity.orderedStudentsMorning.get(i).getId()).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }
            else
                MapsFragmentDriver.markers.get(MainActivity.orderedStudentsAfternoon.get(i).getId()).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        }
    }
    String app_server_url2 = "https://easybusmanagment.000webhostapp.com/send_notif.php";

    public void sendMessage(final String message, final String id) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, app_server_url2, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("Ressss: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Errrrr " + error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("message", message);
                params.put("title", "EasyBus");
                params.put("id", id);
                return params;

            }
        };
        MySingleton.getInstance(con).addToRequestQueue(stringRequest);
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        Toast.makeText(con, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> routes, int i) {
        Route bestRoute = routes.get(i);
        sendMessage("Your bus is coming after " + bestRoute.getDurationText(), studentId);
    }

    @Override
    public void onRoutingCancelled() {

    }
}
